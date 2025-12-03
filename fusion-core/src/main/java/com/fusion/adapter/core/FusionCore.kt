package com.fusion.adapter.core

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.delegate.FusionItemDelegate

/**
 * [FusionCore]
 * 连接 Adapter 和 Registry 的桥梁。
 */
class FusionCore {
    private val registry = DelegateRegistry()

    fun register(clazz: Class<*>, delegate: FusionItemDelegate<*, *>) {
        registry.register(clazz, delegate)
    }

    fun getItemViewType(item: Any, position: Int): Int {
        return registry.getItemViewType(item, position)
    }

    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return registry.getDelegate(viewType).onCreateViewHolder(parent)
    }

    fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Any, position: Int, payloads: List<Any>) {
        val delegate = registry.getDelegate(holder.itemViewType)
        delegate.onBindViewHolder(holder, item, position, payloads)
    }

    // 生命周期分发
    fun onViewRecycled(holder: RecyclerView.ViewHolder) = registry.getDelegate(holder.itemViewType).onViewRecycled(holder)
    fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) = registry.getDelegate(holder.itemViewType).onViewAttachedToWindow(holder)
    fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) = registry.getDelegate(holder.itemViewType).onViewDetachedFromWindow(holder)

    // Payload 路由
    fun getChangePayload(oldItem: Any, newItem: Any): Any? {
        val oldType = registry.getItemViewType(oldItem, 0)
        val newType = registry.getItemViewType(newItem, 0)
        if (oldType != newType) return null
        return registry.getDelegatePayload(oldType, oldItem, newItem)
    }
}