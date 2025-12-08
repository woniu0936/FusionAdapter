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
    internal var longClickAction: ((view: VB, item: T, position: Int) -> Boolean)? = null
    @PublishedApi
    internal var contentSameBlock: ((old: T, new: T) -> Boolean)? = null

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

    /** 定义点击事件 */
    fun onClick(block: (item: T) -> Unit) {
        clickAction = { _, item, _ -> block(item) }
    }

    /** 定义长按事件 */
    fun onLongClick(block: (item: T) -> Boolean) {
        longClickAction = { _, item, _ -> block(item) }
    }

    /** 定义高性能 Diff 内容比对 (return true 表示内容未变) */
    fun areContentsTheSame(block: (old: T, new: T) -> Boolean) {
        contentSameBlock = block
    }
}