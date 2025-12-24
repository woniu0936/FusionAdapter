package com.fusion.adapter.delegate

import androidx.viewbinding.ViewBinding
import com.fusion.adapter.dsl.ItemConfiguration
import com.fusion.adapter.dsl.SpanScope
import com.fusion.adapter.internal.DslSignature

@PublishedApi
internal class FunctionalBindingDelegate<T : Any, VB : ViewBinding>(
    private val signature: DslSignature,
    inflater: BindingInflater<VB>,
    config: ItemConfiguration<T, VB>
) : BindingDelegate<T, VB>(inflater) {

    private val onBindLogic = config.onBind
    private val onPayloadLogic = config.onBindWithPayloads
    private val onCreateLogic = config.onCreate
    private val diffLogic = config.diffCallback

    override val viewTypeKey: Any = signature

    init {
        // ID Provider
        config.stableId?.let { setStableId(it) }

        // Click Listeners
        // 修正点：Base class 已经处理了 View->VB 的转换
        // lambda 参数现在直接是 binding, item, pos
        config.onClick?.let { action ->
            setOnItemClick(config.clickDebounce) { binding, item, pos ->
                action(binding, item, pos)
            }
        }

        config.onLongClick?.let { action ->
            setOnItemLongClick { binding, item, pos ->
                action(binding, item, pos)
            }
        }

        // Span Size
        config.spanSize?.let { spanAction ->
            this.configSpanSize = { item, pos, total ->
                spanAction(item, pos, SpanScope(total))
            }
        }

        // Watchers
        // Base class 的 registerWatcher 已经处理了代理逻辑
        config.watchers.forEach { registerWatcher(it) }
    }

    override fun onViewHolderCreated(binding: VB) {
        onCreateLogic?.invoke(binding)
    }

    override fun onBind(binding: VB, item: T, position: Int) {
        onBindLogic?.invoke(binding, item, position)
    }

    override fun onBindPayload(
        binding: VB,
        item: T,
        position: Int,
        payloads: MutableList<Any>,
        handled: Boolean
    ) {
        if (onPayloadLogic != null) {
            onPayloadLogic.invoke(binding, item, position, payloads)
        } else if (!handled) {
            onBindLogic?.invoke(binding, item, position)
        }
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return diffLogic?.invoke(oldItem, newItem)
            ?: super.areContentsTheSame(oldItem, newItem)
    }
}