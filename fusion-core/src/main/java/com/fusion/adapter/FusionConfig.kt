package com.fusion.adapter

import com.fusion.adapter.exception.ErrorListener
import com.fusion.adapter.internal.DEFAULT_DEBOUNCE_INTERVAL

/**
 * [全局配置]
 * 不可变对象，确保线程安全。只能通过 Builder 构建。
 */
class FusionConfig private constructor(builder: Builder) {

    @JvmField
    val isDebug: Boolean = builder.isDebug
    @JvmField
    val errorListener: ErrorListener? = builder.errorListener
    @JvmField
    val globalDebounceInterval: Long = builder.globalDebounceInterval

    /**
     * [Builder 模式]
     * 提供流式 API，Java/Kotlin 双向友好。
     */
    class Builder {
        internal var isDebug: Boolean = false
        internal var errorListener: ErrorListener? = null
        internal var globalDebounceInterval: Long = DEFAULT_DEBOUNCE_INTERVAL

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
        fun setErrorListener(listener: ErrorListener): Builder {
            this.errorListener = listener
            return this
        }

        fun setGlobalDebounceInterval(interval: Long): Builder {
            this.globalDebounceInterval = interval
            return this
        }

        fun build(): FusionConfig {
            return FusionConfig(this)
        }
    }
}