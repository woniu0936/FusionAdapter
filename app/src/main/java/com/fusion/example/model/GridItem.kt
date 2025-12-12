package com.fusion.example.model

import com.fusion.adapter.diff.StableId

data class GridItem(val id: String, val type: Int) : StableId {
    override val stableId: Any = id
    companion object {
        const val TYPE_SMALL = 1 // 占 1 格
        const val TYPE_BIG = 2   // 占满一行
    }
}