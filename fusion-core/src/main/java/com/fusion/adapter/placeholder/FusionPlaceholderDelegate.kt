package com.fusion.adapter.placeholder

import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.internal.FusionInternalTags.TAG_PLACEHOLDER
import com.fusion.adapter.core.GlobalTypeKey
import com.fusion.adapter.core.ViewTypeKey

abstract class FusionPlaceholderDelegate<VH : RecyclerView.ViewHolder> : FusionDelegate<Any, VH>() {

    override val viewTypeKey: ViewTypeKey = GlobalTypeKey(this::class.java, TAG_PLACEHOLDER)

    final override fun onBindViewHolder(holder: VH, item: Any, position: Int, payloads: MutableList<Any>) {
        onBindPlaceholder(holder)
    }

    abstract fun onBindPlaceholder(holder: VH)
}