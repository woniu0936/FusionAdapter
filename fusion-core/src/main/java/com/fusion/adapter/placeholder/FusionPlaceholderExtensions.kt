package com.fusion.adapter.placeholder

import com.fusion.adapter.FusionAdapter
import com.fusion.adapter.FusionListAdapter
import java.util.Collections

// ============================================================================================
// FusionListAdapter Extensions
// ============================================================================================

fun FusionListAdapter.submitPlaceholders(count: Int = 5, commitCallback: Runnable? = null) {
    val placeholders = List(count) { FusionPlaceholder() }
    this.submitList(placeholders, commitCallback)
}

fun FusionListAdapter.clear(commitCallback: Runnable? = null) {
    this.submitList(emptyList(), commitCallback)
}

// ============================================================================================
// FusionAdapter Extensions
// ============================================================================================

fun FusionAdapter.setPlaceholders(count: Int = 5) {
    val placeholders = List(count) { FusionPlaceholder() }
    this.setItems(placeholders)
}

fun FusionAdapter.clear() {
    this.setItems(emptyList())
}