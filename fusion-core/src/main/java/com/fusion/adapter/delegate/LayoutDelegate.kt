package com.fusion.adapter.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.extensions.getItem
import com.fusion.adapter.extensions.setItem
import com.fusion.adapter.internal.FusionInternalTags
import com.fusion.adapter.internal.GlobalTypeKey
import com.fusion.adapter.internal.ViewTypeKey

/**
 * [LayoutDelegate]
 */
abstract class LayoutDelegate<T : Any>(@LayoutRes private val layoutResId: Int) : FusionDelegate<T, LayoutHolder>() {

    override val viewTypeKey: ViewTypeKey = GlobalTypeKey(this::class.java, FusionInternalTags.TAG_LAYOUT_DELEGATE)

    private var onItemClick: ((holder: LayoutHolder, item: T, position: Int) -> Unit)? = null
    private var onItemLongClick: ((holder: LayoutHolder, item: T, position: Int) -> Boolean)? = null

    fun setOnItemClick(listener: (holder: LayoutHolder, item: T, position: Int) -> Unit) {
        this.onItemClick = listener
    }

    fun setOnItemLongClick(listener: (holder: LayoutHolder, item: T, position: Int) -> Boolean) {
        this.onItemLongClick = listener
    }

    final override fun onCreateViewHolder(parent: ViewGroup): LayoutHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        val holder = LayoutHolder(view)

        if (onItemClick != null) {
            holder.itemView.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = (holder as RecyclerView.ViewHolder).getItem<T>()
                    if (item != null) onItemClick?.invoke(holder, item, pos)
                }
            }
        }

        if (onItemLongClick != null) {
            holder.itemView.setOnLongClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = (holder as RecyclerView.ViewHolder).getItem<T>()
                    if (item != null) return@setOnLongClickListener onItemLongClick?.invoke(holder, item, pos) == true
                }
                false
            }
        }

        onCreate(holder)
        return holder
    }

    open fun onCreate(holder: LayoutHolder) {}

    final override fun onBindViewHolder(holder: LayoutHolder, item: T, position: Int, payloads: MutableList<Any>) {
        (holder as RecyclerView.ViewHolder).setItem(item)
        if (payloads.isNotEmpty()) {
            val handled = dispatchHandledPayloads(holder, item, payloads)
            holder.onPayload(item, payloads, handled)
        } else {
            holder.onBind(item)
        }
    }

    abstract fun LayoutHolder.onBind(item: T)

    open fun LayoutHolder.onPayload(item: T, payloads: List<Any>, handled: Boolean) {
        if (!handled) onBind(item)
    }
}