package com.fusion.adapter

import com.fusion.adapter.delegate.FusionItemDelegate
import com.fusion.adapter.delegate.FusionFallbackDelegate // 假设这是默认兜底
import com.fusion.adapter.interfaces.FusionErrorListener

/**
 * [全局配置]
 * 不可变对象，确保线程安全。只能通过 Builder 构建。
 */
class FusionConfig private constructor(builder: Builder) {

    @JvmField val isDebug: Boolean = builder.isDebug
    @JvmField val errorListener: FusionErrorListener? = builder.errorListener
    @JvmField val globalFallbackDelegate: FusionItemDelegate<Any, *>? = builder.globalFallbackDelegate

    /**
     * [Builder 模式]
     * 提供流式 API，Java/Kotlin 双向友好。
     */
    class Builder {
        internal var isDebug: Boolean = false
        internal var errorListener: FusionErrorListener? = null
        internal var globalFallbackDelegate: FusionItemDelegate<Any, *>? = FusionFallbackDelegate()

        /**
         * 设置调试模式
         * true: 遇到未注册类型抛出异常
         * false: 遇到未注册类型使用兜底 Delegate
         */
        fun setDebug(debug: Boolean): Builder {
            this.isDebug = debug
            return this
        }

        /**
         * 设置异常监听器 (线上监控)
         */
        fun setErrorListener(listener: FusionErrorListener): Builder {
            this.errorListener = listener
            return this
        }

        /**
         * 设置自定义全局兜底 Delegate
         * 默认为隐藏 Item (高度为0)
         */
        fun setGlobalFallback(delegate: FusionItemDelegate<*, *>): Builder {
            @Suppress("UNCHECKED_CAST")
            this.globalFallbackDelegate = delegate as? FusionItemDelegate<Any, *>
            return this
        }

        fun build(): FusionConfig {
            return FusionConfig(this)
        }
    }
}