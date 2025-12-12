package com.fusion.example.model

import com.fusion.adapter.diff.StableId

data class SimpleItem(val id: String, val title: String) : StableId {
    override val stableId: Any = id
}