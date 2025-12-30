package com.fusion.adapter.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.dsl.ItemConfiguration
import com.fusion.adapter.internal.ViewTypeKey

/**
 * [DslBindingDelegate]
 * ViewBinding implementation for DSL mode.
 */
internal class DslBindingDelegate<T : Any, VB : ViewBinding>(
    override val viewTypeKey: ViewTypeKey,
    private val inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
    private val config: ItemConfiguration<T, VB>
) : BindingDelegate<T, VB>() {

    init {
        config.observers.forEach { addObserver(it) }

        // Correctly pass click listeners to base class
        config.onClick?.let { listener ->
            setOnItemClick(config.clickDebounce) { binding: VB, item: T, position: Int ->
                // Invoke the listener from config with correct context
                listener(binding, item, position)
            }
        }

        config.onLongClick?.let { listener ->
            setOnItemLongClick { binding: VB, item: T, position: Int ->
                listener(binding, item, position)
            }
        }
    }

    override fun onInflateBinding(inflater: LayoutInflater, parent: ViewGroup): VB {
        return inflate(inflater, parent, false)
    }

    override fun onBind(binding: VB, item: T, position: Int) {
        config.onBind?.invoke(binding, item, position)
    }

    override fun onPayload(binding: VB, item: T, position: Int, payloads: List<Any>, handled: Boolean) {
        if (config.onPayload != null) {
            config.onPayload?.invoke(binding, item, position, payloads)
        } else {
            super.onPayload(binding, item, position, payloads, handled)
        }
    }

    override fun getUniqueKey(item: T): Any {
        // 1. 优先使用 DSL 中 uniqueKey { ... } 配置的 Lambda
        val dslKey = config.itemKey?.invoke(item)
        if (dslKey != null) return dslKey

        // 2. 其次使用 TypeDispatcher 注入的 Key (internalDispatcherKeyProvider 是我们在上一轮讨论中保留的 internal 字段)
        val dispatchKey = internalDispatcherKeyProvider?.invoke(item)
        if (dispatchKey != null) return dispatchKey

        // 3. 最后回退到 Item 本身 (Identity)
        return item
    }
}
