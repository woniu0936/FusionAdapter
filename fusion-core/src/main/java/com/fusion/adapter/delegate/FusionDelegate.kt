package com.fusion.adapter.delegate

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * [FusionDelegate]
 * 纯粹的 UI 渲染器。移除了 isFor，不再关心数据匹配逻辑。
 */
abstract class FusionDelegate<T : Any, VH : RecyclerView.ViewHolder> {

    lateinit var adapter: RecyclerView.Adapter<*>

    // Diff 相关
    open fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
    open fun getChangePayload(oldItem: T, newItem: T): Any? = null

    // UI 相关
    abstract fun onCreateViewHolder(parent: ViewGroup): VH
    abstract fun onBindViewHolder(holder: VH, item: T, position: Int, payloads: MutableList<Any>)

    // 生命周期
    open fun onViewRecycled(holder: VH) {}
    open fun onViewAttachedToWindow(holder: VH) {}
    open fun onViewDetachedFromWindow(holder: VH) {}
}
