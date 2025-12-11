package com.fusion.adapter.delegate

import android.util.SparseArray
import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView

/**
 * [LayoutHolder]
 * A specialized ViewHolder optimized for LayoutRes-based delegates.
 *
 * Implements a lazy-caching mechanism (SparseArray) to ensure O(1) view lookup performance.
 */
open class LayoutHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val cachedViews = SparseArray<View>()

    /**
     * Finds a view by ID with caching.
     *
     * @throws IllegalStateException if the view ID is invalid.
     */
    @Suppress("UNCHECKED_CAST")
    fun <V : View> findView(@IdRes id: Int): V {
        var view = cachedViews.get(id)
        if (view == null) {
            view = itemView.findViewById(id)
            checkNotNull(view) { "Fusion: View with ID #$id not found in layout." }
            cachedViews.put(id, view)
        }
        return view as V
    }

    // Expose context for convenience usage in DSL
    val context = itemView.context
}