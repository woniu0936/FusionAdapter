package com.fusion.adapter.delegate

import com.fusion.adapter.dsl.ItemConfiguration
import com.fusion.adapter.internal.ViewTypeKey

/**
 * [DslLayoutDelegate]
 * Layout-based implementation for DSL mode.
 */
internal class DslLayoutDelegate<T : Any>(
    override val viewTypeKey: ViewTypeKey,
    layoutResId: Int,
    @PublishedApi internal val config: ItemConfiguration<T, LayoutHolder>
) : LayoutDelegate<T>(layoutResId) {

    init {
        config.observers.forEach { addObserver(it) }

        // Correctly pass click listeners to base class
        config.onClick?.let { listener ->
            setOnItemClick { holder: LayoutHolder, item: T, position: Int ->
                listener(holder, item, position)
            }
        }

        config.onLongClick?.let { listener ->
            setOnItemLongClick { holder: LayoutHolder, item: T, position: Int ->
                listener(holder, item, position)
            }
        }
    }

    override fun onCreate(holder: LayoutHolder) {
        config.onCreate?.invoke(holder)
    }

    override fun LayoutHolder.onBind(item: T) {
        config.onBind?.invoke(this, item, bindingAdapterPosition)
    }

    override fun LayoutHolder.onPayload(item: T, payloads: List<Any>, handled: Boolean) {
        if (config.onPayload != null) {
            config.onPayload?.invoke(this, item, bindingAdapterPosition, payloads)
        } else {
            // Since we can't easily call super on member extension, we re-implement the fallback logic
            if (!handled) onBind(item)
        }
    }

    override fun getStableId(item: T): Any {
        // 1. 优先使用 DSL 中 stableId { ... } 配置
        val dslKey = config.itemKey?.invoke(item)
        if (dslKey != null) return dslKey

        // 2. 其次使用 TypeRouter 注入的 Key (来自基类的 internal 字段)
        val dispatchKey = internalRouterKeyProvider?.invoke(item)
        if (dispatchKey != null) return dispatchKey

        // 3. 最后回退到 Item 本身
        return item
    }
}
