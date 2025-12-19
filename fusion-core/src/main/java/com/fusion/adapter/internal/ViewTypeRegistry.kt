package com.fusion.adapter.internal

import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.RecyclerView
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

    // ✅ 性能优化核心：缓存 "Class -> 是否支持" 的结果
    // true: 支持; false: 不支持; null: 未计算
    private val supportedCache = ConcurrentHashMap<Class<*>, Boolean>()

    /**
     * 注册入口
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> register(clazz: Class<T>, linker: TypeRouter<T>) {
        // 1. 存入路由表 (支持覆盖)
        classToLinker[clazz] = linker as TypeRouter<Any>

        // 2. 提取 Delegate 生成全局唯一 ID，并注册到 UI 查找表
        linker.getAllDelegates().forEach { registerDelegateGlobal(it) }

        // 注册表变更时，必须清除缓存，否则旧的判定结果可能过期
        supportedCache.clear()
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
     * ✅ 极速判断：当前数据类型是否被支持
     * 用于 Adapter 在 Diff/Render 之前清洗脏数据
     */
    fun isSupported(item: Any): Boolean {
        val clazz = item.javaClass

        // 1. 一级缓存：直接查 Boolean 结果 (最快路径，O(1))
        supportedCache[clazz]?.let { return it }

        // 2. 二级检查：计算逻辑
        val isSupported = checkIsSupportedInternal(clazz)

        // 3. 写入缓存
        supportedCache[clazz] = isSupported
        return isSupported
    }

    private fun checkIsSupportedInternal(clazz: Class<*>): Boolean {
        // A. 直接匹配
        if (classToLinker.containsKey(clazz)) return true

        // B. 继承/接口匹配 (这是一个耗时操作)
        if (findLinkerForInheritance(clazz) != null) return true

        return false
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
            throw IllegalStateException("Fusion: Critical - Item ${clazz.simpleName} has no registered Linker.")
        }

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
    }

    /**
     * [继承查找策略]
     * 仅在 classToLinker 未命中时执行。
     */
    private fun findLinkerForInheritance(clazz: Class<*>): TypeRouter<Any>? {
        // A. 向上查找父类 (优先匹配父类)
        var current: Class<*>? = clazz.superclass
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

    fun getDelegate(viewType: Int): FusionDelegate<Any, RecyclerView.ViewHolder> {
        return viewTypeToDelegate[viewType]
            ?: throw IllegalStateException("Fusion: Critical - Unknown ViewType $viewType")
    }

    fun getDelegateOrNull(viewType: Int): FusionDelegate<Any, RecyclerView.ViewHolder>? {
        return viewTypeToDelegate[viewType]
    }
}