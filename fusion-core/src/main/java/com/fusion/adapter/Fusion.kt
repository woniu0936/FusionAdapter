package com.fusion.adapter

/**
 * [Fusion] 门面
 */
object Fusion {
    private var config = FusionConfig.Builder().build()

    @JvmStatic
    fun initialize(config: FusionConfig) {
        this.config = config
    }

    @JvmStatic
    fun getConfig(): FusionConfig = config
}

/**
 * [initialize] 扩展
 */
inline fun Fusion.initialize(block: FusionConfig.Builder.() -> Unit) {
    initialize(FusionConfig.Builder().apply(block).build())
}
