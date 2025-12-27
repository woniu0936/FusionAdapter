package com.fusion.adapter.dsl

import com.fusion.adapter.internal.diff.PropertyObserver

/**
 * [ItemConfiguration]
 */
class ItemConfiguration<T : Any, V : Any> {
    var itemKey: ((T) -> Any?)? = null
    var onBind: (V.(item: T, position: Int) -> Unit)? = null
    var onBindPartial: (V.(item: T, position: Int, payloads: List<Any>) -> Unit)? = null
    var onClick: (V.(item: T, position: Int) -> Unit)? = null
    var onLongClick: (V.(item: T, position: Int) -> Boolean)? = null
    var clickDebounce: Long? = null
    var spanSize: (item: T, position: Int, scope: SpanSizeScope) -> Int = { _, _, _ -> 1 }

    val observers = ArrayList<PropertyObserver<T>>()
}