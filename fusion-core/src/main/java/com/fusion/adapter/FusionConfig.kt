package com.fusion.adapter

import com.fusion.adapter.exception.ErrorListener
import com.fusion.adapter.internal.DEFAULT_DEBOUNCE_INTERVAL

/**
 * [FusionConfig]
 */
class FusionConfig private constructor(builder: Builder) {

    @JvmField val isDebug: Boolean = builder.isDebug
    @JvmField val errorListener: ErrorListener? = builder.errorListener
    @JvmField val globalDebounceInterval: Long = builder.globalDebounceInterval
    @JvmField val defaultItemIdEnabled: Boolean = builder.defaultItemIdEnabled
    
    // Log configuration
    @JvmField val logDir: String? = builder.logDir
    @JvmField val logToFile: Boolean = builder.logToFile

    class Builder {
        internal var isDebug: Boolean = false
        internal var errorListener: ErrorListener? = null
        internal var globalDebounceInterval: Long = DEFAULT_DEBOUNCE_INTERVAL
        internal var defaultItemIdEnabled: Boolean = false
        internal var logDir: String? = null
        internal var logToFile: Boolean = false

        fun setDebug(debug: Boolean): Builder { this.isDebug = debug; return this }
        fun setErrorListener(listener: ErrorListener): Builder { this.errorListener = listener; return this }
        fun setGlobalDebounceInterval(interval: Long): Builder { this.globalDebounceInterval = interval; return this }
        fun setDefaultItemIdEnabled(enable: Boolean): Builder { this.defaultItemIdEnabled = enable; return this }
        
        fun setLogOutput(enableFile: Boolean, dirPath: String? = null): Builder {
            this.logToFile = enableFile
            this.logDir = dirPath
            return this
        }
        
        fun build(): FusionConfig = FusionConfig(this)
    }
}