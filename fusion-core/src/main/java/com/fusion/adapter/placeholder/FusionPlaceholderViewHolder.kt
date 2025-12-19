package com.fusion.adapter.placeholder

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * 默认的 ViewHolder。
 *
 */
class FusionPlaceholderViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    View(parent.context).apply {
        layoutParams = ViewGroup.LayoutParams(0, 0)
        visibility = View.GONE
    }
)
