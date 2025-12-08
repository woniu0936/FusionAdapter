package com.fusion.adapter.core

import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.Fusion
import com.fusion.adapter.delegate.FusionItemDelegate

/**
 * [DelegateRegistry]
 * 负责管理所有类型的映射关系。
 *
 */
class DelegateRegistry {

    // 全局 ViewType 池
    // 索引 (int) -> Delegate 实例
    // 这里的索引就是 RecyclerView 需要的 viewType
    private val viewTypeToDelegate = SparseArrayCompat<FusionItemDelegate<Any, RecyclerView.ViewHolder>>()

    // 反向查找表: Delegate -> viewType (int)
    // 用于快速获取 Delegate 对应的 ID
    private val delegateToViewType = HashMap<FusionItemDelegate<*, *>, Int>()

    // 核心路由表: 数据类型 Class -> 路由连接器 Linker
    private val classToLinker = HashMap<Class<*>, FusionLinker<Any>>()

    // ViewType 计数器 (自增)
    private var nextViewType = 0

    // [新增] 定义一个固定的 ViewType 给兜底使用
    // 使用负数是为了避免与 nextViewType (从0开始自增) 冲突
    private val FALLBACK_VIEW_TYPE = -2048

    /**
     * 注册 Linker
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> register(clazz: Class<T>, linker: FusionLinker<T>) {
        classToLinker[clazz] = linker as FusionLinker<Any>

        // 扁平化注册：将 Linker 内部持有的所有 Delegate 注册到全局 ViewType 池中
        linker.getAllDelegates().forEach { delegate ->
            registerDelegateGlobal(delegate)
        }
    }

    /**
     * 将 Delegate 注册到全局池，分配 ViewType
     */
    private fun registerDelegateGlobal(delegate: FusionItemDelegate<*, *>) {
        @Suppress("UNCHECKED_CAST")
        val castedDelegate = delegate as FusionItemDelegate<Any, RecyclerView.ViewHolder>

        // 如果这个 Delegate 还没注册过，分配一个新的 ViewType
        if (!delegateToViewType.containsKey(castedDelegate)) {
            val viewType = nextViewType++
            viewTypeToDelegate.put(viewType, castedDelegate)
            delegateToViewType[castedDelegate] = viewType
        }
    }

    /**
     * [O(1) 核心查找] 根据 Item 获取 ViewType
     * 包含完整的异常拦截与兜底机制。
     */
    fun getItemViewType(item: Any): Int {
        val clazz = item.javaClass

        try {
            // 1. 查找 Linker (Map 查找)
            val linker = classToLinker[clazz]
                ?: throw IllegalStateException("Fusion: 未注册的数据类型 -> ${clazz.simpleName}")

            // 2. 路由解析 (Item -> Key -> Delegate)
            val delegate = linker.resolve(item)
                ?: throw IllegalStateException("Fusion: 路由失败 (未配置 Key 映射) -> ${clazz.simpleName}, item=$item")

            // 3. 获取 ViewType (Map 查找)
            return delegateToViewType[delegate]
                ?: throw IllegalStateException("Fusion: Delegate 未注册到全局池 (System Error)")

        } catch (e: Exception) {
            // [修改] 获取单例配置
            val config = Fusion.getConfig()

            // 场景 A: Debug 模式直接抛出，暴露问题给开发者
            if (config.isDebug) {
                throw e
            }

            // 场景 B: 生产环境 -> 拦截异常，启用兜底

            // B1. 上报日志 (回调 Java 接口)
            config.errorListener?.onError(item, e)

            // B2. 获取兜底 Delegate
            val fallback = config.globalFallbackDelegate
                ?: throw e // 如果用户强行把兜底设为 null，说明他想崩，那就崩吧

            // B3. [关键修复] 懒加载注册 Fallback Delegate
            // 只有第一次发生错误时才会执行注册，后续直接复用
            if (viewTypeToDelegate.indexOfKey(FALLBACK_VIEW_TYPE) < 0) {
                @Suppress("UNCHECKED_CAST")
                val castedFallback = fallback as FusionItemDelegate<Any, RecyclerView.ViewHolder>

                // 将兜底 Delegate 放入池中，使用固定的 ID
                viewTypeToDelegate.put(FALLBACK_VIEW_TYPE, castedFallback)
                // 不需要放入 delegateToViewType，因为我们不会反向查它
            }

            // B4. [关键修复] 返回兜底 ViewType
            // 这样 RecyclerView 就会拿到这个 ID，去调用 getDelegate(FALLBACK_VIEW_TYPE)
            return FALLBACK_VIEW_TYPE
        }
    }

    /**
     * 根据 ViewType 获取 Delegate (用于 onCreate/onBind)
     */
    fun getDelegate(viewType: Int): FusionItemDelegate<Any, RecyclerView.ViewHolder> {
        return viewTypeToDelegate[viewType]
            ?: throw IllegalStateException("Fusion: 未知的 ViewType -> $viewType")
    }
}

