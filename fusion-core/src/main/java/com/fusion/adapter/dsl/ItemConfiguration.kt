package com.fusion.adapter.dsl

import androidx.annotation.RestrictTo
import com.fusion.adapter.internal.PropertyObserver

/**
 * [ItemConfiguration]
 * 存储 DSL 配置结果的静态容器。
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ItemConfiguration<T : Any, V : Any> {

    var itemKey: ((T) -> Any?)? = null
    var diffCallback: ((old: T, new: T) -> Boolean)? = null
    var onCreate: (V.() -> Unit)? = null
    var onBind: (V.(item: T, position: Int) -> Unit)? = null
    var onBindPayloads: (V.(item: T, position: Int, payloads: List<Any>) -> Unit)? = null
    var onClick: ((scope: V, item: T, position: Int) -> Unit)? = null
    var onLongClick: ((scope: V, item: T, position: Int) -> Boolean)? = null
    var clickDebounce: Long? = null
    var spanSize: ((item: T, position: Int, scope: SpanSizeScope) -> Int)? = null

    // 彻底重命名为 observers
    val observers = ArrayList<PropertyObserver<T>>()
}
