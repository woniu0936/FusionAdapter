package com.fusion.adapter.dsl

import androidx.annotation.RestrictTo
import com.fusion.adapter.internal.diff.*
import kotlin.reflect.KProperty1

/**
 * [ItemDefinitionScope]
 */
@FusionDsl
abstract class ItemDefinitionScope<T : Any, V : Any> @PublishedApi internal constructor() {
    @PublishedApi internal val config = ItemConfiguration<T, V>()

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun getConfiguration(): ItemConfiguration<T, V> = config

    fun stableId(block: (item: T) -> Any?) { config.itemKey = block }
    fun onCreate(block: V.() -> Unit) { config.onCreate = block }
    fun onBind(block: V.(item: T) -> Unit) { config.onBind = { item, _ -> block(item) } }
    fun onBindIndexed(block: V.(item: T, position: Int) -> Unit) { config.onBind = block }
    fun onPayload(block: V.(item: T, payloads: List<Any>) -> Unit) { config.onPayload = { item, _, payloads -> block(item, payloads) } }
    
    fun <P> onPayload(prop1: KProperty1<T, P>, action: V.(P) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        config.observers.add(PropertyObserver1({ prop1.get(it) }, action as Any.(P) -> Unit))
    }
    fun <P1, P2> onPayload(prop1: KProperty1<T, P1>, prop2: KProperty1<T, P2>, action: V.(P1, P2) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        config.observers.add(PropertyObserver2({ prop1.get(it) }, { prop2.get(it) }, action as Any.(P1, P2) -> Unit))
    }
    fun <P1, P2, P3> onPayload(prop1: KProperty1<T, P1>, prop2: KProperty1<T, P2>, prop3: KProperty1<T, P3>, action: V.(P1, P2, P3) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        config.observers.add(PropertyObserver3({ prop1.get(it) }, { prop2.get(it) }, { prop3.get(it) }, action as Any.(P1, P2, P3) -> Unit))
    }
    fun <P1, P2, P3, P4> onPayload(prop1: KProperty1<T, P1>, prop2: KProperty1<T, P2>, prop3: KProperty1<T, P3>, prop4: KProperty1<T, P4>, action: V.(P1, P2, P3, P4) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        config.observers.add(PropertyObserver4({ prop1.get(it) }, { prop2.get(it) }, { prop3.get(it) }, { prop4.get(it) }, action as Any.(P1, P2, P3, P4) -> Unit))
    }
    fun <P1, P2, P3, P4, P5> onPayload(prop1: KProperty1<T, P1>, prop2: KProperty1<T, P2>, prop3: KProperty1<T, P3>, prop4: KProperty1<T, P4>, prop5: KProperty1<T, P5>, action: V.(P1, P2, P3, P4, P5) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        config.observers.add(PropertyObserver5({ prop1.get(it) }, { prop2.get(it) }, { prop3.get(it) }, { prop4.get(it) }, { prop5.get(it) }, action as Any.(P1, P2, P3, P4, P5) -> Unit))
    }
    fun <P1, P2, P3, P4, P5, P6> onPayload(prop1: KProperty1<T, P1>, prop2: KProperty1<T, P2>, prop3: KProperty1<T, P3>, prop4: KProperty1<T, P4>, prop5: KProperty1<T, P5>, prop6: KProperty1<T, P6>, action: V.(P1, P2, P3, P4, P5, P6) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        config.observers.add(PropertyObserver6({ prop1.get(it) }, { prop2.get(it) }, { prop3.get(it) }, { prop4.get(it) }, { prop5.get(it) }, { prop6.get(it) }, action as Any.(P1, P2, P3, P4, P5, P6) -> Unit))
    }

    fun onItemClick(debounceMs: Long? = null, block: V.(item: T) -> Unit) { config.clickDebounce = debounceMs; config.onClick = { item, _ -> block(item) } }
    fun onLongClick(block: V.(item: T) -> Boolean) { config.onLongClick = { item, _ -> block(item) } }
    fun spanSize(block: SpanSizeScope.(item: T, position: Int) -> Int) { config.spanSize = { item, pos, scope -> scope.block(item, pos) } }
    fun fullSpan(condition: (item: T) -> Boolean) { config.spanSize = { item, _, scope -> if (condition(item)) scope.totalSpans else 1 } }
}