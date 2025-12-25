package com.fusion.adapter.extensions

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fusion.adapter.delegate.FusionDelegate

fun RecyclerView.setupGridSupport(
    adapter: RecyclerView.Adapter<*>,
    getItem: (Int) -> Any?,
    getDelegate: (Any) -> FusionDelegate<Any, *>?
) {
    val lm = layoutManager as? GridLayoutManager ?: return
    lm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            val item = getItem(position) ?: return 1
            val delegate = getDelegate(item) ?: return 1
            return delegate.resolveSpanSize(item, position, lm.spanCount)
        }
    }
}

fun RecyclerView.ViewHolder.setupStaggeredSupport(
    item: Any,
    getDelegate: (Any) -> FusionDelegate<Any, *>?
) {
    val lp = itemView.layoutParams as? StaggeredGridLayoutManager.LayoutParams ?: return
    val delegate = getDelegate(item) ?: return
    lp.isFullSpan = delegate.resolveFullSpan(item)
}