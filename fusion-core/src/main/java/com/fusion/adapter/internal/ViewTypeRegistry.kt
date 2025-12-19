package com.fusion.adapter.internal

import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.Fusion
import com.fusion.adapter.delegate.FusionDelegate
import java.util.concurrent.ConcurrentHashMap

/**
 * [ViewTypeRegistry]
 * 核心类型注册与分发表。
 *
 * 架构特性:
 * 1. **Thread Safe**: 使用 ConcurrentHashMap，支持 DiffUtil 在子线程并发计算 Diff。
 * 2. **O(1) Hot Path**: 融合了注册表与缓存，命中缓存后仅需一次哈希查找。
 * 3. **Polymorphism**: 支持继承与接口查找，并自动缓存查找结果 ("热点缓存")。
 */
class ViewTypeRegistry {

    companion object {
        // 使用一个与 FALLBACK_VIEW_TYPE (-2048) 不同的负数
        const val TYPE_PLACEHOLDER = -2049
    }

    // [核心路由表]
    // Key: 数据类型 Class
    // Value: 路由连接器 Linker
    // 作用: 存储显式注册的类型，以及通过继承查找后缓存的类型。
    private val classToLinker = ConcurrentHashMap<Class<*>, TypeRouter<Any>>()

    // [UI 查找表]
    // Key: ViewType (Int)
    // Value: Delegate
    // 作用: onCreateViewHolder 时根据 int ID 找回 Delegate。
    // 注意: 仅在主线程使用，SparseArrayCompat 足够高效且节省内存。
    private val viewTypeToDelegate = SparseArrayCompat<FusionDelegate<Any, RecyclerView.ViewHolder>>()

    // 兜底 ViewType ID (固定负数)
    private val FALLBACK_VIEW_TYPE = -2048

    /**
     * 注册入口
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> register(clazz: Class<T>, linker: TypeRouter<T>) {
        // 1. 存入路由表 (支持覆盖)
        classToLinker[clazz] = linker as TypeRouter<Any>

        // 2. 提取 Delegate 生成全局唯一 ID，并注册到 UI 查找表
        linker.getAllDelegates().forEach { registerDelegateGlobal(it) }
    }

    private fun registerDelegateGlobal(delegate: FusionDelegate<*, *>) {
        @Suppress("UNCHECKED_CAST")
        val castedDelegate = delegate as FusionDelegate<Any, RecyclerView.ViewHolder>

        // A. 获取全局唯一 Key (Class 或 Inflater 引用)
        val uniqueKey = delegate.getUniqueViewType()

        // B. 从全局池获取/生成 ID (核心安全机制)
        val viewType = GlobalViewTypePool.getId(uniqueKey)

        // C. 存入当前 Adapter 的查找表
        viewTypeToDelegate.put(viewType, castedDelegate)
    }

    /**
     * [核心查找算法] - O(1) in Hot Path
     */
    fun getItemViewType(item: Any): Int {
        val clazz = item.javaClass

        // [Step 1] 快速查找 (热路径 O(1))
        // ConcurrentHashMap.get 是无锁读，极快
        var linker = classToLinker[clazz]

        // [Step 2] 如果没找到，尝试继承查找 (冷路径 O(N))
        if (linker == null) {
            linker = findLinkerForInheritance(clazz)
            if (linker != null) {
                // [Cache] 找到后存入缓存，下次直接命中 Step 1
                // putIfAbsent 保证线程安全，防止覆盖（虽然这里结果是一样的）
                classToLinker.putIfAbsent(clazz, linker)
            }
        }

        // [Step 3] 如果还没找到 (linker 依然为 null)，说明未注册且无父类匹配
        if (linker == null) {
            return handleMissingType(item)
        }

        try {
            // [Step 4] 路由解析
            val delegate = linker.resolve(item)
                ?: throw IllegalStateException("Fusion: 路由失败 (Key 未映射) -> ${clazz.simpleName}")

            // [Step 5] 获取 ID
            val uniqueKey = delegate.getUniqueViewType()
            val id = GlobalViewTypePool.getId(uniqueKey)

            // [添加日志] 关键！打印 Key 和 ID
            logD("FusionTracker") {
                """
                ✅ [GetViewType] Resolved:
                   Item: ${clazz.simpleName}
                   Delegate: ${delegate.javaClass.simpleName} @${System.identityHashCode(delegate)}
                   UniqueKey: $uniqueKey
                   GlobalID: $id
            """.trimIndent()
            }

            return id

        } catch (e: Exception) {
            return handleException(item, e)
        }
    }

    fun hasLinker(item: Any): Boolean {
        val clazz = item.javaClass

        // 1. 先查一级缓存 (精确匹配)
        if (classToLinker.containsKey(clazz)) {
            return true
        }

        // 2. 查二级缓存 (继承/接口匹配)
        // 复用你现有的逻辑 findLinkerForInheritance
        val linker = findLinkerForInheritance(clazz)
        if (linker != null) {
            // 查到了就写入一级缓存，提升下次性能
            classToLinker[clazz] = linker as TypeRouter<Any>
            return true
        }

        // 3. 确实没有注册
        return false
    }

    /**
     * [继承查找策略]
     * 仅在 classToLinker 未命中时执行。
     */
    private fun findLinkerForInheritance(clazz: Class<*>): TypeRouter<Any>? {
        // A. 向上查找父类 (优先匹配父类)
        var current = clazz.superclass
        while (current != null && current != Any::class.java) {
            val linker = classToLinker[current]
            if (linker != null) {
                logD("Registry") { "[Inheritance Match] Item=${clazz.simpleName} -> Super=${current.simpleName}" }
                return linker
            }
            current = current.superclass
        }

        // B. 查找接口 (遍历直接实现的接口)
        for (inf in clazz.interfaces) {
            val linker = classToLinker[inf]
            if (linker != null) {
                logD("Registry") { "[Interface Match] Item=${clazz.simpleName} -> Interface=${inf.simpleName}" }
                return linker
            }
        }

        return null
    }

    /**
     * 异常/兜底处理
     */
    private fun handleMissingType(item: Any): Int {
        val e = IllegalStateException("Fusion: 未注册的数据类型 -> ${item.javaClass.simpleName}")
        return handleException(item, e)
    }

    private fun handleException(item: Any, e: Exception): Int {
        val config = Fusion.getConfig()

        // Debug 模式直接崩溃，暴露问题
        if (config.isDebug) {
            throw e
        }

        // 生产环境：上报日志 + 兜底
        config.errorListener?.onError(item, e)

        val fallback = config.globalFallbackDelegate ?: throw e // 如果没配兜底，只能崩

        // 懒加载注册兜底 Delegate
        if (viewTypeToDelegate.indexOfKey(FALLBACK_VIEW_TYPE) < 0) {
            @Suppress("UNCHECKED_CAST")
            viewTypeToDelegate.put(FALLBACK_VIEW_TYPE, fallback as FusionDelegate<Any, RecyclerView.ViewHolder>)
        }

        return FALLBACK_VIEW_TYPE
    }

    fun getDelegate(viewType: Int): FusionDelegate<Any, RecyclerView.ViewHolder> {
        return viewTypeToDelegate[viewType]
            ?: throw IllegalStateException("Fusion: Critical - Unknown ViewType $viewType")
    }

    fun getDelegateOrNull(viewType: Int): FusionDelegate<Any, RecyclerView.ViewHolder>? {
        return viewTypeToDelegate[viewType]
    }
}