package com.fusion.example.model

import com.fusion.adapter.diff.StableId

// 图片数据
data class ImageItem(val id: String, val url: String) : StableId {
    override val stableId: Any = id
}