package com.fusion.adapter.delegate

import androidx.viewbinding.ViewBinding
import com.fusion.adapter.dsl.ItemConfiguration
import com.fusion.adapter.dsl.SpanSizeScope
import com.fusion.adapter.internal.DslTypeKey
import com.fusion.adapter.internal.ViewTypeKey

@PublishedApi
internal class DslBindingDelegate<T : Any, VB : ViewBinding>(
    private val signature: DslTypeKey,
    inflater: BindingInflater<VB>,
    config: ItemConfiguration<T, VB>
) : BindingDelegate<T, VB>(inflater) {

    private val onBindLogic = config.onBind
    private val onPayloadLogic = config.onBindPayloads
    private val onCreateLogic = config.onCreate
    private val diffLogic = config.diffCallback

    override val viewTypeKey: ViewTypeKey = signature

    init {
        config.itemKey?.let { setUniqueKey(it) }
        config.onClick?.let { action -> setOnItemClick(config.clickDebounce) { binding, item, pos -> action(binding, item, pos) } }
        config.onLongClick?.let { action -> setOnItemLongClick { binding, item, pos -> action(binding, item, pos) } }
        config.spanSize?.let { spanAction -> this.configSpanSize = { item, pos, total -> spanAction(item, pos, SpanSizeScope(total)) } }
        
        // 使用重命名后的 observers
        config.observers.forEach { addObserver(it) }
    }

    override fun onCreate(binding: VB) { onCreateLogic?.invoke(binding) }
    override fun onBind(binding: VB, item: T, position: Int) { onBindLogic?.invoke(binding, item, position) }
    override fun onBindPartial(binding: VB, item: T, position: Int, payloads: List<Any>, handled: Boolean) {
        if (onPayloadLogic != null) onPayloadLogic.invoke(binding, item, position, payloads)
        else if (!handled) onBindLogic?.invoke(binding, item, position)
    }
}