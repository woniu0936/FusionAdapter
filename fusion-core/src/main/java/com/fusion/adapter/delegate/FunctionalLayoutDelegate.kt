package com.fusion.adapter.delegate

import androidx.annotation.LayoutRes
import com.fusion.adapter.dsl.ItemConfiguration
import com.fusion.adapter.dsl.SpanScope
import com.fusion.adapter.internal.DslSignature

@PublishedApi
internal class FunctionalLayoutDelegate<T : Any>(
    private val signature: DslSignature,
    @LayoutRes layoutResId: Int,
    config: ItemConfiguration<T, LayoutHolder>
) : LayoutDelegate<T>(layoutResId) {

    private val onBindLogic = config.onBind
    private val onPayloadLogic = config.onBindWithPayloads
    private val onCreateLogic = config.onCreate
    private val diffLogic = config.diffCallback

    override val viewTypeKey: Any = signature

    init {
        // ID Provider
        config.stableId?.let { setStableId(it) }

        // Click Listeners
        // 修正点：Base class 提供了 Holder
        config.onClick?.let { action ->
            setOnItemClick(config.clickDebounce) { holder, item, pos ->
                action(holder, item, pos)
            }
        }

        config.onLongClick?.let { action ->
            setOnItemLongClick { holder, item, pos ->
                action(holder, item, pos)
            }
        }

        // Span Size
        config.spanSize?.let { spanAction ->
            this.configSpanSize = { item, pos, total ->
                spanAction(item, pos, SpanScope(total))
            }
        }

        // Watchers
        config.watchers.forEach { super.registerWatcher(it) }
    }

    override fun onViewHolderCreated(holder: LayoutHolder) {
        onCreateLogic?.invoke(holder)
    }

    override fun LayoutHolder.onBind(item: T) {
        onBindLogic?.invoke(this, item, bindingAdapterPosition)
    }

    override fun onBindPayload(
        holder: LayoutHolder,
        item: T,
        position: Int,
        payloads: MutableList<Any>,
        handled: Boolean
    ) {
        if (onPayloadLogic != null) {
            onPayloadLogic.invoke(holder, item, position, payloads)
        } else if (!handled) {
            holder.onBind(item)
        }
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return diffLogic?.invoke(oldItem, newItem)
            ?: super.areContentsTheSame(oldItem, newItem)
    }
}