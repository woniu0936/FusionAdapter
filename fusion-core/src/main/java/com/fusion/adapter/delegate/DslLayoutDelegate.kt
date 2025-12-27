package com.fusion.adapter.delegate

import com.fusion.adapter.dsl.ItemConfiguration
import com.fusion.adapter.internal.registry.DslAdapterFactory
import com.fusion.adapter.internal.ViewTypeKey

/**
 * [DslLayoutDelegate]
 */
internal class DslLayoutDelegate<T : Any>(
    override val viewTypeKey: ViewTypeKey,
    layoutResId: Int,
    @PublishedApi internal val config: ItemConfiguration<T, LayoutHolder>
) : LayoutDelegate<T>(layoutResId) {

    init {
        config.itemKey?.let { setUniqueKey(it) }
        config.observers.forEach { addObserver(it) }
    }

    override fun LayoutHolder.onBind(item: T) {
        config.onBind?.invoke(this, item, bindingAdapterPosition)
    }

    override fun LayoutHolder.onBindPartial(item: T, payloads: List<Any>, handled: Boolean) {
        if (config.onBindPartial != null) {
            config.onBindPartial?.invoke(this, item, bindingAdapterPosition, payloads)
        } else {
            // Use the base implementation
            // Since we can't easily call super on member extension, we re-implement the fallback
            if (!handled) onBind(item)
        }
    }
}
