package com.fusion.adapter.dsl

import androidx.viewbinding.ViewBinding

/**
 * [DelegateDsl]
 * 用于配置匿名 Delegate 的 DSL 接收者。
 * 用户在 register { ... } 闭包中接触到的就是这个类。
 */
class DelegateDsl<T : Any, VB : ViewBinding> {

    // 使用 @PublishedApi internal 隐藏实现细节，同时允许 inline 函数访问
    @PublishedApi
    internal var bindBlock: (VB.(item: T, position: Int) -> Unit)? = null

    @PublishedApi
    internal var bindPayloadBlock: (VB.(item: T, position: Int, payloads: List<Any>) -> Unit)? = null

    @PublishedApi
    internal var clickAction: ((view: VB, item: T, position: Int) -> Unit)? = null

    @PublishedApi
    internal var clickDebounce: Long? = null

    @PublishedApi
    internal var longClickAction: ((view: VB, item: T, position: Int) -> Boolean)? = null

    @PublishedApi
    internal var contentSameBlock: ((old: T, new: T) -> Boolean)? = null

    @PublishedApi
    internal var createBlock: (VB.() -> Unit)? = null

    // 临时存储 DSL 配置
    @PublishedApi
    internal var spanSizeBlock: ((item: T, position: Int, scope: SpanScope) -> Int)? = null

    @PublishedApi
    internal var fullSpanBlock: ((item: T) -> Boolean)? = null

    /** 定义数据绑定逻辑 (简易版，不带 position) */
    fun onBind(block: VB.(item: T) -> Unit) {
        bindBlock = { item, _ -> block(item) }
    }

    /** 定义数据绑定逻辑 (带 position) */
    fun onBindIndexed(block: VB.(item: T, position: Int) -> Unit) {
        bindBlock = block
    }

    /** 定义局部刷新逻辑 (Payload) */
    fun onBindPayload(block: VB.(item: T, payloads: List<Any>) -> Unit) {
        bindPayloadBlock = { item, _, payloads -> block(item, payloads) }
    }

    /** 定义点击事件
     * @param debounceMs 自定义防抖时间。不传则使用全局配置。
     */
    fun onItemClick(debounceMs: Long? = null, block: (item: T) -> Unit) {
        this.clickDebounce = debounceMs
        this.clickAction = { _, item, _ -> block(item) }
    }

    /**
     * [Item 点击事件 - 带 View 和 Position]
     */
    fun onItemClickIndexed(debounceMs: Long? = null, block: (view: VB, item: T, position: Int) -> Unit) {
        this.clickDebounce = debounceMs
        this.clickAction = block
    }

    /** 定义长按事件 */
    fun onItemLongClick(block: (item: T) -> Boolean) {
        longClickAction = { _, item, _ -> block(item) }
    }

    /** 定义高性能 Diff 内容比对 (return true 表示内容未变) */
    fun areContentsTheSame(block: (old: T, new: T) -> Boolean) {
        contentSameBlock = block
    }

    /**
     * 定义 View 创建时的初始化逻辑 (只执行一次)
     * 适合设置固定样式、监听器等
     */
    fun onCreate(block: VB.() -> Unit) {
        createBlock = block
    }

    // ============================================================================================
    // Layout API - Flat & Context Aware (扁平化 & 上下文感知)
    // ============================================================================================

    /**
     * [Grid] 设置跨度。
     * 使用 SpanScope 上下文，可直接调用 totalSpans, half, third。
     *
     * 示例: spanSize { _, _ -> totalSpans }
     */
    fun spanSize(block: SpanScope.(item: T, position: Int) -> Int) {
        // 将 DSL 的 Scope 转换为 Lambda 存储
        spanSizeBlock = { item, pos, scope -> scope.block(item, pos) }
    }

    /**
     * [Grid] 固定跨度 (便捷重载)
     */
    fun spanSize(count: Int) {
        spanSizeBlock = { _, _, _ -> count }
    }

    /**
     * [Universal] 强制占满全屏 (自动适配 Grid 和 Staggered)
     */
    fun fullSpan() {
        fullSpanBlock = { true }
        // 自动联动: Grid 模式下返回 totalSpans
        spanSizeBlock = { _, _, scope -> scope.totalSpans }
    }

    /**
     * [Universal] 条件性占满全屏
     */
    fun fullSpanIf(condition: (item: T) -> Boolean) {
        fullSpanBlock = condition
        spanSizeBlock = { item, _, scope ->
            if (condition(item)) scope.totalSpans else 1
        }
    }
}