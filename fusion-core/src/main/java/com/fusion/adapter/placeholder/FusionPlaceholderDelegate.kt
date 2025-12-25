package com.fusion.adapter.placeholder

import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.internal.ViewTypeKey
import com.fusion.adapter.internal.ClassTypeKey

abstract class FusionPlaceholderDelegate<VH : RecyclerView.ViewHolder> : FusionDelegate<Any, VH>() {
    override val viewTypeKey: ViewTypeKey = ClassTypeKey(this::class.java)

    final override fun onBindViewHolder(holder: VH, item: Any, position: Int, payloads: MutableList<Any>) {
        onBindPlaceholder(holder)
    }

    abstract fun onBindPlaceholder(holder: VH)
}