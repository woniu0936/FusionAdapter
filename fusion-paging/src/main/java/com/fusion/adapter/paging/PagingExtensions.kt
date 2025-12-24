package com.fusion.adapter.paging

import FusionPagingAdapter
import androidx.lifecycle.Lifecycle
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// =================================================================
// Paging Extensions (Cleaned)
//
// No register() methods here! They are inherited from RegistryOwner.
// =================================================================

/**
 * [Quick Setup] Initialize FusionPagingAdapter and attach to RecyclerView.
 */
inline fun <reified T : Any> RecyclerView.setupFusionPaging(
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context),
    noinline block: (FusionPagingAdapter<T>.() -> Unit)? = null
): FusionPagingAdapter<T> {
    this.layoutManager = layoutManager
    val adapter = FusionPagingAdapter<T>()
    block?.invoke(adapter)
    this.adapter = adapter
    return adapter
}

/**
 * Clear Paging Data. Usually used when logging out or resetting search.
 */
fun FusionPagingAdapter<*>.clear(lifecycle: Lifecycle) {
    this.submitData(lifecycle, PagingData.empty())
}