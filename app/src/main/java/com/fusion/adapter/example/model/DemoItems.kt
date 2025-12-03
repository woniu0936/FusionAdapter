package com.fusion.adapter.example.model

import com.fusion.adapter.interfaces.FusionStableId

// 纯文本数据
data class TextItem(val id: String, val content: String) : FusionStableId {
    override val stableId: Any = id
}

// 图片数据
data class ImageItem(val id: String, val url: String) : FusionStableId {
    override val stableId: Any = id
}