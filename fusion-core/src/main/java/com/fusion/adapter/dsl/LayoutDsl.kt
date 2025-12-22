package com.fusion.adapter.dsl

import android.view.View
import com.fusion.adapter.delegate.LayoutHolder
import com.fusion.adapter.internal.*
import kotlin.reflect.KProperty1

/**
 * [LayoutDsl]
 * 专门用于 register(R.layout.xxx) 的 DSL 配置类。
 * 对应 LayoutRes 模式。
 *
 * API 设计与 [BindingDsl] 保持完全一致。
 */
class LayoutDsl<T : Any> {

    internal var bindBlock: (LayoutHolder.(item: T, position: Int) -> Unit)? = null
    internal var rawPayloadBlock: (LayoutHolder.(item: T, position: Int, payloads: List<Any>) -> Unit)? = null
    internal var clickAction: ((view: View, item: T, position: Int) -> Unit)? = null
    internal var clickDebounce: Long? = null
    internal var longClickAction: ((view: View, item: T, position: Int) -> Boolean)? = null
    internal var contentSameBlock: ((old: T, new: T) -> Boolean)? = null
    internal var createBlock: (LayoutHolder.() -> Unit)? = null // 对应 BindingDsl 的 onCreate
    internal var spanSizeBlock: ((item: T, position: Int, scope: SpanScope) -> Int)? = null
    internal var fullSpanBlock: ((item: T) -> Boolean)? = null
    @PublishedApi
    internal var idProviderBlock: ((T) -> Any?)? = null

    internal val pendingWatchers = ArrayList<Watcher<T>>()

    // --- 公开 API ---

    fun stableId(block: (item: T) -> Any?) {
        this.idProviderBlock = block
    }

    // --- Data Binding ---

    fun onBind(block: LayoutHolder.(item: T) -> Unit) {
        bindBlock = { item, _ -> block(item) }
    }

    fun onBindIndexed(block: LayoutHolder.(item: T, position: Int) -> Unit) {
        bindBlock = block
    }

    // --- Payload (Incremental Update) ---

    fun onBindPayload(block: LayoutHolder.(item: T, payloads: List<Any>) -> Unit) {
        rawPayloadBlock = { item, _, payloads -> block(item, payloads) }
    }

    // Property Watchers (完全对标 BindingDsl，只是 Receiver 变成了 LayoutHolder)
    fun <P> onBindPayload(prop: KProperty1<T, P>, action: LayoutHolder.(P) -> Unit) {
        pendingWatchers.add(PropertyWatcher1(prop, action))
    }

    fun <P1, P2> onBindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>,
        action: LayoutHolder.(P1, P2) -> Unit
    ) {
        pendingWatchers.add(PropertyWatcher2(p1, p2, action))
    }

    fun <P1, P2, P3> onBindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>,
        action: LayoutHolder.(P1, P2, P3) -> Unit
    ) {
        pendingWatchers.add(PropertyWatcher3(p1, p2, p3, action))
    }

    // 4 参数
    fun <P1, P2, P3, P4> onBindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, p4: KProperty1<T, P4>,
        action: LayoutHolder.(P1, P2, P3, P4) -> Unit
    ) {
        pendingWatchers.add(PropertyWatcher4(p1, p2, p3, p4, action))
    }

    // 5 参数
    fun <P1, P2, P3, P4, P5> onBindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, p4: KProperty1<T, P4>, p5: KProperty1<T, P5>,
        action: LayoutHolder.(P1, P2, P3, P4, P5) -> Unit
    ) {
        pendingWatchers.add(PropertyWatcher5(p1, p2, p3, p4, p5, action))
    }

    // 6 参数
    fun <P1, P2, P3, P4, P5, P6> onBindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, p4: KProperty1<T, P4>, p5: KProperty1<T, P5>, p6: KProperty1<T, P6>,
        action: LayoutHolder.(P1, P2, P3, P4, P5, P6) -> Unit
    ) {
        pendingWatchers.add(PropertyWatcher6(p1, p2, p3, p4, p5, p6, action))
    }

    // --- Interactions ---

    fun onItemClick(debounceMs: Long? = null, block: (item: T) -> Unit) {
        this.clickDebounce = debounceMs
        this.clickAction = { _, item, _ -> block(item) }
    }

    fun onItemClickIndexed(debounceMs: Long? = null, block: (view: View, item: T, position: Int) -> Unit) {
        this.clickDebounce = debounceMs
        this.clickAction = block
    }

    fun onItemLongClick(block: (item: T) -> Boolean) {
        longClickAction = { _, item, _ -> block(item) }
    }

    // --- Lifecycle & Utils ---

    /**
     * 在 ViewHolder 创建时回调。
     * 可用于 View 的一次性初始化（如设置固定的 Listener、背景等）。
     */
    fun onCreate(block: LayoutHolder.() -> Unit) {
        createBlock = block
    }

    fun areContentsTheSame(block: (old: T, new: T) -> Boolean) {
        contentSameBlock = block
    }

    // --- Grid Span ---

    fun spanSize(block: SpanScope.(item: T, position: Int) -> Int) {
        spanSizeBlock = { item, pos, scope -> scope.block(item, pos) }
    }

    fun spanSize(count: Int) {
        spanSizeBlock = { _, _, _ -> count }
    }

    fun fullSpan() {
        fullSpanBlock = { true }
        spanSizeBlock = { _, _, scope -> scope.totalSpans }
    }

    fun fullSpanIf(condition: (item: T) -> Boolean) {
        fullSpanBlock = condition
        spanSizeBlock = { item, _, scope ->
            if (condition(item)) scope.totalSpans else 1
        }
    }
}