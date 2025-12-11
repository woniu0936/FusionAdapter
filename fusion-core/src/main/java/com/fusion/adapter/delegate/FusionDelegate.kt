package com.fusion.adapter.delegate

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * [FusionDelegate]
 * 纯粹的 UI 渲染器。
 *
 * 设计原则 (SOLID):
 * 1. SRP (单一职责): 只负责创建和绑定 View，不持有 Adapter 引用，不处理数据 Diff。
 * 2. OCP (开闭原则): 通过 getUniqueViewType 支持扩展 ID 生成策略。
 */
abstract class FusionDelegate<T : Any, VH : RecyclerView.ViewHolder> {

    /**
     * [唯一标识生成器]
     * 用于生成全局唯一的 ViewType Key。
     *
     * 默认策略: 使用 Class 对象。
     * 适用于: 标准的 Class 继承写法 (class MyDelegate : FusionDelegate...)
     *
     * 注意: 如果是匿名内部类或 DSL 生成的 Delegate，必须重写此方法。
     */
    open fun getUniqueViewType(): Any {
        return this::class.java
    }

    // Diff 相关 (默认实现)
    open fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
    open fun getChangePayload(oldItem: T, newItem: T): Any? = null

    // UI 相关 (必须实现)
    abstract fun onCreateViewHolder(parent: ViewGroup): VH
    abstract fun onBindViewHolder(holder: VH, item: T, position: Int, payloads: MutableList<Any>)

    // 生命周期 (可选)
    open fun onViewRecycled(holder: VH) {}
    open fun onViewAttachedToWindow(holder: VH) {}
    open fun onViewDetachedFromWindow(holder: VH) {}
}