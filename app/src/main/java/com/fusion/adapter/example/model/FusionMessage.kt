package com.fusion.adapter.example.model

import com.fusion.adapter.interfaces.FusionStableId

/**
 * [通用消息实体]
 * 同一个类，既可以是文本，也可以是图片，通过 msgType 区分。
 */
data class FusionMessage(
    val id: String,
    val msgType: Int, // 1: 文本, 2: 图片, 3: 系统提示
    val content: String
) : FusionStableId {

    override val stableId: Any = id

    companion object {
        const val TYPE_TEXT = 1
        const val TYPE_IMAGE = 2
        const val TYPE_SYSTEM = 3
    }
}