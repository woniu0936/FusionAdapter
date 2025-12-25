package com.fusion.adapter.delegate

import androidx.annotation.LayoutRes
import com.fusion.adapter.dsl.ItemConfiguration
import com.fusion.adapter.dsl.SpanSizeScope
import com.fusion.adapter.internal.DslTypeKey
import com.fusion.adapter.internal.ViewTypeKey

@PublishedApi
internal class DslLayoutDelegate<T : Any>(
    private val signature: DslTypeKey,
    @LayoutRes layoutResId: Int,
    config: ItemConfiguration<T, LayoutHolder>
) : LayoutDelegate<T>(layoutResId) {

    private val onBindLogic = config.onBind
    private val onPayloadLogic = config.onBindPayloads
    private val onCreateLogic = config.onCreate
    private val diffLogic = config.diffCallback

    override val viewTypeKey: ViewTypeKey = signature

    init {
        config.itemKey?.let { setUniqueKey(it) }
        config.onClick?.let { action -> setOnItemClick(config.clickDebounce) { holder, item, pos -> action(holder, item, pos) } }
        config.onLongClick?.let { action -> setOnItemLongClick { holder, item, pos -> action(holder, item, pos) } }
        config.spanSize?.let { spanAction -> this.configSpanSize = { item, pos, total -> spanAction(item, pos, SpanSizeScope(total)) } }
        
        // 使用重命名后的 observers
        config.observers.forEach { addObserver(it) }
    }

    override fun onCreate(holder: LayoutHolder) { onCreateLogic?.invoke(holder) }
    override fun LayoutHolder.onBind(item: T) { onBindLogic?.invoke(this, item, bindingAdapterPosition) }
    override fun onBindPartial(holder: LayoutHolder, item: T, position: Int, payloads: List<Any>, handled: Boolean) {
        if (onPayloadLogic != null) onPayloadLogic.invoke(holder, item, position, payloads)
        else if (!handled) holder.onBind(item)
    }
}