package com.fusion.adapter

import android.util.Log
import androidx.annotation.RestrictTo

/**
 * [Fusion 核心入口]
 * 负责全局初始化的门面类。
 */
object Fusion {

    @Volatile
    private var config: FusionConfig? = null

    /**
     * [初始化]
     * 建议在 Application.onCreate 中调用。
     * 只能初始化一次，重复调用将被忽略或抛出警告。
     */
    @JvmStatic
    fun initialize(config: FusionConfig) {
        if (this.config != null) {
            Log.w("Fusion", "Fusion is already initialized.")
            return
        }
        synchronized(this) {
            if (this.config == null) {
                this.config = config
            }
        }
    }

    /**
     * [内部获取配置]
     * 如果未初始化，提供默认配置。
     */
    @JvmStatic
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun getConfig(): FusionConfig {
        return config ?: synchronized(this) {
            config ?: FusionConfig.Builder().build().also { config = it }
        }
    }
}