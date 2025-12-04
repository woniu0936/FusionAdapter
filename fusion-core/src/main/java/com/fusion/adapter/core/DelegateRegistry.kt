package com.fusion.adapter.core

import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.RecyclerView
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
     */
    fun getItemViewType(item: Any): Int {
        val clazz = item.javaClass

        // 1. 查找 Linker (Map 查找)
        val linker = classToLinker[clazz]
            ?: throw IllegalStateException("Fusion: 未注册的数据类型 -> ${clazz.simpleName}")

        // 2. 路由解析 (Item -> Key -> Delegate)
        val delegate = linker.resolve(item)
            ?: throw IllegalStateException("Fusion: 路由失败 (未配置 Key 映射) -> ${clazz.simpleName}, item=$item")

        // 3. 获取 ViewType (Map 查找)
        return delegateToViewType[delegate]
            ?: throw IllegalStateException("Fusion: Delegate 未注册到全局池 (System Error)")
    }

    /**
     * 根据 ViewType 获取 Delegate (用于 onCreate/onBind)
     */
    fun getDelegate(viewType: Int): FusionItemDelegate<Any, RecyclerView.ViewHolder> {
        return viewTypeToDelegate[viewType]
            ?: throw IllegalStateException("Fusion: 未知的 ViewType -> $viewType")
    }
}

