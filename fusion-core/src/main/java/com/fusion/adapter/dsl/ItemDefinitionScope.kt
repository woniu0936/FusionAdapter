package com.fusion.adapter.dsl

import androidx.annotation.RestrictTo
import com.fusion.adapter.internal.diff.*
import kotlin.reflect.KProperty1

/**
 * [ItemDefinitionScope]
 */
@FusionDsl
abstract class ItemDefinitionScope<T : Any, V : Any> {
    @PublishedApi internal val config = ItemConfiguration<T, V>()

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun getConfiguration(): ItemConfiguration<T, V> = config

    fun uniqueKey(block: (item: T) -> Any?) { config.itemKey = block }
    fun onBind(block: V.(item: T) -> Unit) { config.onBind = { item, _ -> block(item) } }
    fun onBindIndexed(block: V.(item: T, position: Int) -> Unit) { config.onBind = block }
    fun onBindPartial(block: V.(item: T, payloads: List<Any>) -> Unit) { config.onBindPartial = { item, _, payloads -> block(item, payloads) } }
    
    fun <P> onPayload(p1: KProperty1<T, P>, action: V.(P) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        config.observers.add(PropertyObserver1({ p1.get(it) }, action as Any.(P) -> Unit))
    }
    fun <P1, P2> onPayload(p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, action: V.(P1, P2) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        config.observers.add(PropertyObserver2({ p1.get(it) }, { p2.get(it) }, action as Any.(P1, P2) -> Unit))
    }
    fun <P1, P2, P3> onPayload(p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, action: V.(P1, P2, P3) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        config.observers.add(PropertyObserver3({ p1.get(it) }, { p2.get(it) }, { p3.get(it) }, action as Any.(P1, P2, P3) -> Unit))
    }
    fun <P1, P2, P3, P4> onPayload(p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, p4: KProperty1<T, P4>, action: V.(P1, P2, P3, P4) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        config.observers.add(PropertyObserver4({ p1.get(it) }, { p2.get(it) }, { p3.get(it) }, { p4.get(it) }, action as Any.(P1, P2, P3, P4) -> Unit))
    }
    fun <P1, P2, P3, P4, P5> onPayload(p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, p4: KProperty1<T, P4>, p5: KProperty1<T, P5>, action: V.(P1, P2, P3, P4, P5) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        config.observers.add(PropertyObserver5({ p1.get(it) }, { p2.get(it) }, { p3.get(it) }, { p4.get(it) }, { p5.get(it) }, action as Any.(P1, P2, P3, P4, P5) -> Unit))
    }
    fun <P1, P2, P3, P4, P5, P6> onPayload(p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, p4: KProperty1<T, P4>, p5: KProperty1<T, P5>, p6: KProperty1<T, P6>, action: V.(P1, P2, P3, P4, P5, P6) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        config.observers.add(PropertyObserver6({ p1.get(it) }, { p2.get(it) }, { p3.get(it) }, { p4.get(it) }, { p5.get(it) }, { p6.get(it) }, action as Any.(P1, P2, P3, P4, P5, P6) -> Unit))
    }

    fun onClick(debounceMs: Long? = null, block: V.(item: T) -> Unit) { config.clickDebounce = debounceMs; config.onClick = { item, _ -> block(item) } }
    fun onLongClick(block: V.(item: T) -> Boolean) { config.onLongClick = { item, _ -> block(item) } }
    fun spanSize(block: SpanSizeScope.(item: T, position: Int) -> Int) { config.spanSize = { item, pos, scope -> scope.block(item, pos) } }
    fun fullSpanIf(condition: (item: T) -> Boolean) { config.spanSize = { item, _, scope -> if (condition(item)) scope.totalSpans else 1 } }
}