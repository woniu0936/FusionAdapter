package com.fusion.adapter.delegate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.core.R
import com.fusion.adapter.internal.ClassSignature
import com.fusion.adapter.internal.Watcher
import kotlin.reflect.KProperty1

/**
 * [BindingDelegate]
 * Base class for ViewBinding based delegates.
 * Optimized for type safety and performance.
 */
abstract class BindingDelegate<T : Any, VB : ViewBinding>(
    private val inflater: BindingInflater<VB>
) : FusionDelegate<T, BindingHolder<VB>>() {

    /** Default signature: UserDelegate::class */
    override val viewTypeKey: Any = ClassSignature(this::class.java)

    // Click listeners using VB instead of raw View
    private var onItemClick: ((binding: VB, item: T, position: Int) -> Unit)? = null
    private var onItemLongClick: ((binding: VB, item: T, position: Int) -> Boolean)? = null

    // Internal state, kept private
    private var clickDebounceMs: Long? = null

    /**
     * Set click listener with optional debounce.
     * @param debounceMs Custom debounce time, null uses global default.
     */
    fun setOnItemClick(debounceMs: Long? = null, listener: (binding: VB, item: T, position: Int) -> Unit) {
        this.onItemClick = listener
        this.clickDebounceMs = debounceMs
    }

    fun setOnItemLongClick(listener: (binding: VB, item: T, position: Int) -> Boolean) {
        this.onItemLongClick = listener
    }

    final override fun onCreateViewHolder(parent: ViewGroup): BindingHolder<VB> {
        val binding = inflater.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = BindingHolder(binding)

        // Setup Tag for O(1) Retrieval
        holder.itemView.setTag(R.id.fusion_binding_tag, binding)

        // Click Listener Setup
        if (onItemClick != null) {
            // Use extension function `click` from Fusion library
            holder.itemView.click(clickDebounceMs) {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    @Suppress("UNCHECKED_CAST")
                    val item = holder.itemView.getTag(R.id.fusion_item_tag) as? T
                    if (item != null) {
                        onItemClick?.invoke(holder.binding, item, pos)
                    }
                }
            }
        }

        // Long Click Listener Setup
        if (onItemLongClick != null) {
            holder.itemView.setOnLongClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    @Suppress("UNCHECKED_CAST")
                    val item = holder.itemView.getTag(R.id.fusion_item_tag) as? T
                    if (item != null) {
                        return@setOnLongClickListener onItemLongClick?.invoke(holder.binding, item, pos) == true
                    }
                }
                false
            }
        }

        onViewHolderCreated(holder.binding)
        return holder
    }

    /** Hook for initialization, e.g. setting static listeners */
    protected open fun onViewHolderCreated(binding: VB) {}

    final override fun onBindViewHolder(holder: BindingHolder<VB>, item: T, position: Int, payloads: MutableList<Any>) {
        holder.itemView.setTag(R.id.fusion_item_tag, item)

        if (payloads.isNotEmpty()) {
            // 1. Automatic Dispatch (Property Watchers)
            val handled = dispatchHandledPayloads(holder, item, payloads)

            // 2. Manual Dispatch (Unified Callback)
            // Even if handled=true, we still call this to allow "hybrid" logic if needed.
            onBindPayload(holder.binding, item, position, payloads, handled)
        } else {
            onBind(holder.binding, item, position)
        }
    }

    /** Subclass Implementation: Full Bind */
    abstract fun onBind(binding: VB, item: T, position: Int)

    /**
     * Subclass Implementation: Partial Bind.
     * Default behavior: If not handled by watchers, fallback to full bind.
     */
    open fun onBindPayload(
        binding: VB,
        item: T,
        position: Int,
        payloads: MutableList<Any>,
        handled: Boolean
    ) {
        if (!handled) {
            onBind(binding, item, position)
        }
    }

    // --- Property Watcher Helpers ---

    // Override register to proxy holder->binding
    override fun registerWatcher(watcher: Watcher<T>) {
        super.registerWatcher(object : Watcher<T> {
            override fun checkChange(oldItem: T, newItem: T) = watcher.checkChange(oldItem, newItem)
            override fun execute(receiver: Any, item: T) {
                @Suppress("UNCHECKED_CAST")
                val holder = receiver as BindingHolder<VB>
                watcher.execute(holder.binding, item)
            }
        })
    }

    // --- Payload Binding Overloads (1-6 Params) ---

    protected fun <P> bindPayload(prop: KProperty1<T, P>, action: VB.(P) -> Unit) {
        registerDataWatcher(prop) { value -> this.binding.action(value) }
    }

    protected fun <P1, P2> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>,
        action: VB.(P1, P2) -> Unit
    ) {
        registerDataWatcher(p1, p2) { v1, v2 -> this.binding.action(v1, v2) }
    }

    protected fun <P1, P2, P3> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>,
        action: VB.(P1, P2, P3) -> Unit
    ) {
        registerDataWatcher(p1, p2, p3) { v1, v2, v3 -> this.binding.action(v1, v2, v3) }
    }

    protected fun <P1, P2, P3, P4> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, p4: KProperty1<T, P4>,
        action: VB.(P1, P2, P3, P4) -> Unit
    ) {
        registerDataWatcher(p1, p2, p3, p4) { v1, v2, v3, v4 -> this.binding.action(v1, v2, v3, v4) }
    }

    protected fun <P1, P2, P3, P4, P5> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, p4: KProperty1<T, P4>, p5: KProperty1<T, P5>,
        action: VB.(P1, P2, P3, P4, P5) -> Unit
    ) {
        registerDataWatcher(p1, p2, p3, p4, p5) { v1, v2, v3, v4, v5 -> this.binding.action(v1, v2, v3, v4, v5) }
    }

    protected fun <P1, P2, P3, P4, P5, P6> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, p4: KProperty1<T, P4>, p5: KProperty1<T, P5>, p6: KProperty1<T, P6>,
        action: VB.(P1, P2, P3, P4, P5, P6) -> Unit
    ) {
        registerDataWatcher(p1, p2, p3, p4, p5, p6) { v1, v2, v3, v4, v5, v6 -> this.binding.action(v1, v2, v3, v4, v5, v6) }
    }
}

private inline fun View.click(
    debounce: Long?,
    crossinline block: (View) -> Unit
) {
    // 获取全局默认值，这里假设是 500ms，你可以换成 FusionConfig.globalDebounce
    val safeDebounce = debounce ?: 500L

    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0
        override fun onClick(v: View) {
            val now = System.currentTimeMillis()
            if (now - lastClickTime > safeDebounce) {
                lastClickTime = now
                block(v)
            }
        }
    })
}