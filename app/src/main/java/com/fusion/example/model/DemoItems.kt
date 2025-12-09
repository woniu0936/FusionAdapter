package com.fusion.example.model

import com.fusion.adapter.diff.StableId

// 纯文本数据
data class TextItem(val id: String, val content: String) : StableId {
    override val stableId: Any = id
}

// 图片数据
data class ImageItem(val id: String, val url: String) : StableId {
    override val stableId: Any = id
}