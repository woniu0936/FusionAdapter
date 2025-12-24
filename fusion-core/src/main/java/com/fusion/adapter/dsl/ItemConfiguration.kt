package com.fusion.adapter.dsl

import android.view.View
import com.fusion.adapter.internal.Watcher

/**
 * [ItemConfiguration]
 * 纯数据载体。
 * 将 DSL 的语法定义与底层的数据存储彻底分离。
 *
 * @param T 数据类型
 * @param V View载体类型 (ViewBinding 或 LayoutHolder)
 */
class ItemConfiguration<T : Any, V : Any> {

    // --- ID & Diff ---
    var stableId: ((T) -> Any?)? = null
    var diffCallback: ((old: T, new: T) -> Boolean)? = null

    // --- LifeCycle ---
    var onCreate: (V.() -> Unit)? = null

    // --- Binding (Strict Separation) ---
    // 极致设计：全量与增量分开存储，互不覆盖
    var onBind: (V.(item: T, position: Int) -> Unit)? = null
    var onBindWithPayloads: (V.(item: T, position: Int, payloads: List<Any>) -> Unit)? = null

    // --- Interaction ---
    var onClick: ((scope: V, item: T, position: Int) -> Unit)? = null
    var onLongClick: ((scope: V, item: T, position: Int) -> Boolean)? = null
    var clickDebounce: Long? = null

    // --- Layout Control ---
    var spanSize: ((item: T, position: Int, scope: SpanScope) -> Int)? = null

    // --- Watchers ---
    val watchers = ArrayList<Watcher<T>>()
}