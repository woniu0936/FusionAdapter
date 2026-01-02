package com.fusion.adapter.delegate

import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.core.ViewTypeKey
import com.fusion.adapter.internal.diff.PropertyObserver
import com.fusion.adapter.internal.diff.PropertyObserver1
import com.fusion.adapter.internal.diff.PropertyObserver2
import com.fusion.adapter.internal.diff.PropertyObserver3
import com.fusion.adapter.internal.diff.PropertyObserver4
import com.fusion.adapter.internal.diff.PropertyObserver5
import com.fusion.adapter.internal.diff.PropertyObserver6
import com.fusion.adapter.log.FusionLogger

/**
 * [FusionDelegate]
 */
abstract class FusionDelegate<T : Any, VH : RecyclerView.ViewHolder> {
    
    abstract val viewTypeKey: ViewTypeKey

    // Fast-path slots for the first 3 observers (covers 99% of use cases)
    private var observer1: PropertyObserver<T>? = null
    private var observer2: PropertyObserver<T>? = null
    private var observer3: PropertyObserver<T>? = null
    private var extraObservers: MutableList<PropertyObserver<T>>? = null

    internal var internalRouterKeyProvider: ((T) -> Any?)? = null

    abstract fun getStableId(item: T): Any

    internal fun internalInjectRouterKey(provider: (T) -> Any?) {
        this.internalRouterKeyProvider = provider
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

    /**
     * [Optimization] Highly optimized for performance and memory allocation.
     * Uses a lazy-list approach and fast-path slots.
     */
    open fun getChangePayload(oldItem: T, newItem: T): Any? {
        val obs1 = observer1 ?: return null
        
        val p1 = obs1.checkChange(oldItem, newItem)
        val p2 = observer2?.checkChange(oldItem, newItem)
        val p3 = observer3?.checkChange(oldItem, newItem)
        
        // Fast-path: Only 1-3 observers and no extras
        if (extraObservers == null) {
            return when {
                p1 != null && p2 != null && p3 != null -> listOf(p1, p2, p3)
                p1 != null && p2 != null -> listOf(p1, p2)
                p1 != null && p3 != null -> listOf(p1, p3)
                p2 != null && p3 != null -> listOf(p2, p3)
                p1 != null -> p1
                p2 != null -> p2
                p3 != null -> p3
                else -> null
            }
        }

        // Slow-path: Strictly lazy allocation. 
        // We only create the ArrayList if at least one property has actually changed.
        var result: MutableList<Any>? = null
        
        if (p1 != null) {
            result = ArrayList(4)
            result.add(p1)
        }
        
        if (p2 != null) {
            if (result == null) result = ArrayList(4)
            result.add(p2)
        }
        
        if (p3 != null) {
            if (result == null) result = ArrayList(4)
            result.add(p3)
        }
        
        extraObservers?.let { list ->
            for (i in list.indices) {
                val p = list[i].checkChange(oldItem, newItem)
                if (p != null) {
                    if (result == null) result = ArrayList()
                    result!!.add(p)
                }
            }
        }
        
        return result
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
        when {
            observer1 == null -> observer1 = observer
            observer2 == null -> observer2 = observer
            observer3 == null -> observer3 = observer
            else -> {
                if (extraObservers == null) extraObservers = ArrayList(4)
                extraObservers!!.add(observer)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    open fun <P> registerPropertyObserver(getter1: (T) -> P, action: VH.(P) -> Unit) {
        addObserver(PropertyObserver1(getter1) { value -> (this as VH).action(value) })
    }

    @Suppress("UNCHECKED_CAST")
    open fun <P1, P2> registerPropertyObserver(getter1: (T) -> P1, getter2: (T) -> P2, action: VH.(P1, P2) -> Unit) {
        addObserver(PropertyObserver2(getter1, getter2) { v1, v2 -> (this as VH).action(v1, v2) })
    }

    @Suppress("UNCHECKED_CAST")
    open fun <P1, P2, P3> registerPropertyObserver(getter1: (T) -> P1, getter2: (T) -> P2, getter3: (T) -> P3, action: VH.(P1, P2, P3) -> Unit) {
        addObserver(PropertyObserver3(getter1, getter2, getter3) { v1, v2, v3 -> (this as VH).action(v1, v2, v3) })
    }

    @Suppress("UNCHECKED_CAST")
    open fun <P1, P2, P3, P4> registerPropertyObserver(getter1: (T) -> P1, getter2: (T) -> P2, getter3: (T) -> P3, getter4: (T) -> P4, action: VH.(P1, P2, P3, P4) -> Unit) {
        addObserver(PropertyObserver4(getter1, getter2, getter3, getter4) { v1, v2, v3, v4 -> (this as VH).action(v1, v2, v3, v4) })
    }

    @Suppress("UNCHECKED_CAST")
    open fun <P1, P2, P3, P4, P5> registerPropertyObserver(
        getter1: (T) -> P1,
        getter2: (T) -> P2,
        getter3: (T) -> P3,
        getter4: (T) -> P4,
        getter5: (T) -> P5,
        action: VH.(P1, P2, P3, P4, P5) -> Unit
    ) {
        addObserver(PropertyObserver5(getter1, getter2, getter3, getter4, getter5) { v1, v2, v3, v4, v5 -> (this as VH).action(v1, v2, v3, v4, v5) })
    }

    @Suppress("UNCHECKED_CAST")
    open fun <P1, P2, P3, P4, P5, P6> registerPropertyObserver(
        getter1: (T) -> P1,
        getter2: (T) -> P2,
        getter3: (T) -> P3,
        getter4: (T) -> P4,
        getter5: (T) -> P5,
        getter6: (T) -> P6,
        action: VH.(P1, P2, P3, P4, P5, P6) -> Unit
    ) {
        addObserver(PropertyObserver6(getter1, getter2, getter3, getter4, getter5, getter6) { v1, v2, v3, v4, v5, v6 -> (this as VH).action(v1, v2, v3, v4, v5, v6) })
    }

    open fun onViewRecycled(holder: VH) {}
    open fun onViewAttachedToWindow(holder: VH) {}
    open fun onViewDetachedFromWindow(holder: VH) {}
}
