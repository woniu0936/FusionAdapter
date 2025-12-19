package com.fusion.example.model


data class GridItem(val id: String, val type: Int) {
    companion object {
        const val TYPE_SMALL = 1 // 占 1 格
        const val TYPE_BIG = 2   // 占满一行
    }
}