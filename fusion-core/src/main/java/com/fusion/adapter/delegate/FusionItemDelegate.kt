package com.fusion.adapter.delegate

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * [FusionItemDelegate]
 * 所有委托的基类。定义了创建、绑定、匹配和生命周期的标准行为。
 *
 * @param T 数据类型
 * @param VH ViewHolder 类型
 */
abstract class FusionItemDelegate<T : Any, VH : RecyclerView.ViewHolder> {

    /**
     * 核心匹配逻辑 (一对多场景)
     * 当同一个 Class 注册了多个 Delegate 时，会调用此方法。
     * @return true 表示当前 Item 由此 Delegate 处理
     *
     * @sample
     * override fun isFor(item: Message, position: Int): Boolean {
     *     return item.type == Message.TYPE_TEXT
     * }
     */
    open fun isFor(item: T, position: Int): Boolean = true

    /**
     * [必须添加这个方法]
     * 内容比对，用于 DiffUtil。
     * 必须是 open 的，否则子类无法 override。
     */
    open fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem

    /**
     * Payload 局部刷新支持
     * @return 返回非 null 对象将触发 onBindViewHolder(..., payloads)
     */
    open fun getChangePayload(oldItem: T, newItem: T): Any? = null

    abstract fun onCreateViewHolder(parent: ViewGroup): VH

    abstract fun onBindViewHolder(holder: VH, item: T, position: Int, payloads: List<Any>)

    // --- 生命周期托管 (防止内存泄漏，处理视频播放等) ---
    open fun onViewRecycled(holder: VH) {}
    open fun onViewAttachedToWindow(holder: VH) {}
    open fun onViewDetachedFromWindow(holder: VH) {}

}
