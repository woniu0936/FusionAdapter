package com.fusion.adapter.core

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.delegate.FusionItemDelegate
import com.fusion.adapter.diff.FusionDiffCallback

/**
 * [FusionCore]
 * 核心引擎门面。负责连接 Adapter 与 Registry，并处理生命周期分发。
 *
 * 特性：
 * 1. 管理 Registry (注册表)
 * 2. 注入 Adapter 引用到 Delegate
 * 3. 代理 DiffUtil 的内容比对逻辑
 */
class FusionCore(private val adapter: RecyclerView.Adapter<*>) {

    private val registry = DelegateRegistry()

    /**
     * 注册路由连接器 (核心入口)
     * @param clazz 数据类型 Class
     * @param linker 包含路由规则和 Delegate 集合的连接器
     */
    fun <T : Any> register(clazz: Class<T>, linker: FusionLinker<T>) {
        // 1. 依赖注入：将 adapter 引用注入到 Linker 包含的所有 Delegate 中
        // 这样 Delegate 内部才能通过 adapter.context 获取上下文
        linker.getAllDelegates().forEach { delegate ->
            @Suppress("UNCHECKED_CAST")
            val casted = delegate as FusionItemDelegate<Any, RecyclerView.ViewHolder>
            casted.adapter = adapter
        }

        // 2. 注册到注册表
        registry.register(clazz, linker)
    }

    // ========================================================================================
    // RecyclerView 核心代理
    // ========================================================================================

    fun getItemViewType(item: Any): Int {
        // 路由不再依赖 position，只依赖 item 内容 (O(1) Key 映射)
        return registry.getItemViewType(item)
    }

    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return registry.getDelegate(viewType).onCreateViewHolder(parent)
    }

    fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Any, position: Int, payloads: MutableList<Any>) {
        val delegate = registry.getDelegate(holder.itemViewType)
        delegate.onBindViewHolder(holder, item, position, payloads)
    }

    // ========================================================================================
    // DiffUtil 代理 (Smart Diff 支持)
    // ========================================================================================

    /**
     * [关键修复] 代理 DiffUtil.areItemsTheSame
     * 必须确保 ViewType 相同，否则不能复用 ViewHolder
     */
    fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        // 1. 先获取两个 Item 的 ViewType
        val oldType = registry.getItemViewType(oldItem)
        val newType = registry.getItemViewType(newItem)

        // 2. 如果类型变了（比如从 Text 变 Image），绝对不是同一个 Item
        // 即使 ID 一样，也必须销毁重建
        if (oldType != newType) {
            return false
        }

        // 3. 类型一样，再交给静态策略去比对 ID
        return FusionDiffCallback.areItemsTheSame(oldItem, newItem)
    }

    /**
     * 代理 DiffUtil.Callback.areContentsTheSame
     */
    fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        // 1. 获取 ViewType (O(1) 查找)
        val oldType = registry.getItemViewType(oldItem)
        val newType = registry.getItemViewType(newItem)

        // 2. 如果类型变了（比如从 Text 变成了 Image），肯定不是同一个内容
        if (oldType != newType) return false

        // 3. 找到 Delegate，让 Delegate 自己去比对内容
        val delegate = registry.getDelegate(oldType)
        return delegate.areContentsTheSame(oldItem, newItem)
    }

    /**
     * 代理 DiffUtil.Callback.getChangePayload
     */
    fun getChangePayload(oldItem: Any, newItem: Any): Any? {
        val oldType = registry.getItemViewType(oldItem)
        val newType = registry.getItemViewType(newItem)

        if (oldType != newType) return null

        val delegate = registry.getDelegate(oldType)
        return delegate.getChangePayload(oldItem, newItem)
    }

    // ========================================================================================
    // 生命周期分发 (防止内存泄漏)
    // ========================================================================================

    fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        registry.getDelegate(holder.itemViewType).onViewRecycled(holder)
    }

    fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        registry.getDelegate(holder.itemViewType).onViewAttachedToWindow(holder)
    }

    fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        registry.getDelegate(holder.itemViewType).onViewDetachedFromWindow(holder)
    }
}