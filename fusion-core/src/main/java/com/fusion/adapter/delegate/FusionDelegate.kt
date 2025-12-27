package com.fusion.adapter.delegate

import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.internal.ViewTypeKey
import com.fusion.adapter.internal.diff.*
import com.fusion.adapter.log.FusionLogger

/**
 * [FusionDelegate]
 */
abstract class FusionDelegate<T : Any, VH : RecyclerView.ViewHolder> {
    abstract val viewTypeKey: ViewTypeKey
    private val propertyObservers = ArrayList<PropertyObserver<T>>()

    internal var specificUniqueKeyExtractor: ((T) -> Any?)? = null
    internal var defaultUniqueKeyExtractor: ((T) -> Any?)? = null

    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    internal val isUniqueKeyDefined: Boolean
        get() = specificUniqueKeyExtractor != null ||
                defaultUniqueKeyExtractor != null ||
                isManualUniqueKeyDefined

    protected open val isManualUniqueKeyDefined: Boolean = false

    open fun getUniqueKey(item: T): Any? {
        return specificUniqueKeyExtractor?.invoke(item) ?: defaultUniqueKeyExtractor?.invoke(item)
    }

    fun setUniqueKey(extractor: (T) -> Any?) {
        this.specificUniqueKeyExtractor = extractor
    }

    internal fun attachDefaultUniqueKeyProvider(provider: (T) -> Any?) {
        this.defaultUniqueKeyExtractor = provider
    }

    // --- Layout Strategy ---
    open fun onSpanSize(item: T, position: Int, totalSpans: Int): Int = 1
    open fun isFullSpan(item: T): Boolean = false
    internal var configSpanSize: ((item: T, position: Int, totalSpans: Int) -> Int)? = null
    internal var configFullSpan: ((item: T) -> Boolean)? = null

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun resolveSpanSize(item: T, position: Int, totalSpans: Int): Int =
        configSpanSize?.invoke(item, position, totalSpans) ?: onSpanSize(item, position, totalSpans)

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun resolveFullSpan(item: T): Boolean = configFullSpan?.invoke(item) ?: isFullSpan(item)

    // --- LifeCycle ---
    abstract fun onCreateViewHolder(parent: ViewGroup): VH
    abstract fun onBindViewHolder(holder: VH, item: T, position: Int, payloads: MutableList<Any>)

    open fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        val same = oldItem == newItem
        if (!same) {
            FusionLogger.d("Diff") { "Content changed: $oldItem vs $newItem" }
        }
        return same
    }

    open fun getChangePayload(oldItem: T, newItem: T): Any? {
        if (propertyObservers.isEmpty()) return null
        val payloads = propertyObservers.mapNotNull { it.checkChange(oldItem, newItem) }
        
        if (payloads.isNotEmpty()) {
            FusionLogger.d("Diff") { "Payloads generated: ${payloads.size}" }
        }
        return if (payloads.isNotEmpty()) payloads else null
    }

    protected fun dispatchHandledPayloads(receiver: Any, item: T, payloads: List<Any>): Boolean {
        var handled = false
        for (rawPayload in payloads) {
            val items = if (rawPayload is List<*>) rawPayload else listOf(rawPayload)
            for (p in items) {
                if (p is PropertyObserver<*>) {
                    @Suppress("UNCHECKED_CAST")
                    (p as PropertyObserver<T>).execute(receiver, item)
                    handled = true
                }
            }
        }
        return handled
    }

    open fun addObserver(observer: PropertyObserver<T>) {
        propertyObservers.add(observer)
    }

    @Suppress("UNCHECKED_CAST")
    open fun <P> registerPropertyObserver(g1: (T) -> P, action: VH.(P) -> Unit) {
        addObserver(PropertyObserver1(g1) { value -> (this as VH).action(value) })
    }

    @Suppress("UNCHECKED_CAST")
    open fun <P1, P2> registerPropertyObserver(g1: (T) -> P1, g2: (T) -> P2, action: VH.(P1, P2) -> Unit) {
        addObserver(PropertyObserver2(g1, g2) { v1, v2 -> (this as VH).action(v1, v2) })
    }

    @Suppress("UNCHECKED_CAST")
    open fun <P1, P2, P3> registerPropertyObserver(g1: (T) -> P1, g2: (T) -> P2, g3: (T) -> P3, action: VH.(P1, P2, P3) -> Unit) {
        addObserver(PropertyObserver3(g1, g2, g3) { v1, v2, v3 -> (this as VH).action(v1, v2, v3) })
    }

    @Suppress("UNCHECKED_CAST")
    open fun <P1, P2, P3, P4> registerPropertyObserver(g1: (T) -> P1, g2: (T) -> P2, g3: (T) -> P3, g4: (T) -> P4, action: VH.(P1, P2, P3, P4) -> Unit) {
        addObserver(PropertyObserver4(g1, g2, g3, g4) { v1, v2, v3, v4 -> (this as VH).action(v1, v2, v3, v4) })
    }

    @Suppress("UNCHECKED_CAST")
    open fun <P1, P2, P3, P4, P5> registerPropertyObserver(
        g1: (T) -> P1,
        g2: (T) -> P2,
        g3: (T) -> P3,
        g4: (T) -> P4,
        g5: (T) -> P5,
        action: VH.(P1, P2, P3, P4, P5) -> Unit
    ) {
        addObserver(PropertyObserver5(g1, g2, g3, g4, g5) { v1, v2, v3, v4, v5 -> (this as VH).action(v1, v2, v3, v4, v5) })
    }

    @Suppress("UNCHECKED_CAST")
    open fun <P1, P2, P3, P4, P5, P6> registerPropertyObserver(
        g1: (T) -> P1,
        g2: (T) -> P2,
        g3: (T) -> P3,
        g4: (T) -> P4,
        g5: (T) -> P5,
        g6: (T) -> P6,
        action: VH.(P1, P2, P3, P4, P5, P6) -> Unit
    ) {
        addObserver(PropertyObserver6(g1, g2, g3, g4, g5, g6) { v1, v2, v3, v4, v5, v6 -> (this as VH).action(v1, v2, v3, v4, v5, v6) })
    }

    open fun onViewRecycled(holder: VH) {}
    open fun onViewAttachedToWindow(holder: VH) {}
    open fun onViewDetachedFromWindow(holder: VH) {}
}
