package com.fusion.example.model

import com.fusion.adapter.diff.StableId

// 用于展示 Payload 局部刷新的模型
data class SocialPost(
    val id: String,
    val username: String,
    val content: String,
    val likeCount: Int,
    val isLiked: Boolean
) : StableId {
    override val stableId: Any = id
}