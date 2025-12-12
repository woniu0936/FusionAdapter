package com.fusion.adapter.delegate

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.internal.ViewSignature

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
     * [身份签名]
     * 必须由子类提供唯一的身份标识。
     */
    abstract val signature: ViewSignature

    /**
     * [唯一标识生成器]
     * 用于生成全局唯一的 ViewType Key。
     *
     */
    final fun getUniqueViewType(): Any = signature

    // ============================================================================================
    // Layout Strategy (核心布局策略)
    // ============================================================================================

    /**
     * [Java/Kotlin Override]
     * 子类重写此方法以定义 Grid 布局中占用的列数。
     * 默认返回 1。
     */
    open fun onSpanSize(item: T, position: Int, totalSpans: Int): Int {
        return 1
    }

    /**
     * [Java/Kotlin Override]
     * 子类重写此方法以定义是否在 Staggered 布局中强制占满全屏。
     * 默认返回 false。
     */
    open fun isFullSpan(item: T): Boolean {
        return false
    }

    // ============================================================================================
    // Internal Configuration (DSL 注入点)
    // ============================================================================================

    // 内部持有的 DSL 配置策略 (优先级高于 Override)
    internal var configSpanSize: ((item: T, position: Int, totalSpans: Int) -> Int)? = null
    internal var configFullSpan: ((item: T) -> Boolean)? = null

    /**
     * [Internal Dispatch]
     * 核心调度逻辑：DSL 配置 > 子类重写 > 默认值
     */
    internal fun resolveSpanSize(item: T, position: Int, totalSpans: Int): Int {
        return configSpanSize?.invoke(item, position, totalSpans)
            ?: onSpanSize(item, position, totalSpans)
    }

    /**
     * [Internal Dispatch]
     */
    @PublishedApi
    internal fun resolveFullSpan(item: T): Boolean {
        return configFullSpan?.invoke(item)
            ?: isFullSpan(item)
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