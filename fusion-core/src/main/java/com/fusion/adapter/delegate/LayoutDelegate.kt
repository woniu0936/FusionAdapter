package com.fusion.adapter.delegate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.core.R
import com.fusion.adapter.internal.ClassSignature
import kotlin.reflect.KProperty1

/**
 * [LayoutDelegate]
 * Base class for Layout ID based delegates.
 */
abstract class LayoutDelegate<T : Any>(
    @LayoutRes private val layoutResId: Int
) : FusionDelegate<T, LayoutHolder>() {

    override val viewTypeKey: Any = ClassSignature(this::class.java)

    // Click listeners using LayoutHolder instead of raw View
    private var onItemClickListener: ((holder: LayoutHolder, item: T, position: Int) -> Unit)? = null
    private var onItemLongClickListener: ((holder: LayoutHolder, item: T, position: Int) -> Boolean)? = null
    private var clickDebounceMs: Long? = null

    fun setOnItemClick(debounceMs: Long? = null, listener: (holder: LayoutHolder, item: T, position: Int) -> Unit) {
        this.clickDebounceMs = debounceMs
        this.onItemClickListener = listener
    }

    fun setOnItemLongClick(listener: (holder: LayoutHolder, item: T, position: Int) -> Boolean) {
        this.onItemLongClickListener = listener
    }

    final override fun onCreateViewHolder(parent: ViewGroup): LayoutHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        val holder = LayoutHolder(view)

        // Setup Tag for O(1) Retrieval
        view.setTag(R.id.fusion_holder_tag, holder)

        if (onItemClickListener != null) {
            holder.itemView.click(clickDebounceMs) { v ->
                val position = holder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    @Suppress("UNCHECKED_CAST")
                    val item = v.getTag(R.id.fusion_item_tag) as? T
                    if (item != null) {
                        onItemClickListener?.invoke(holder, item, position)
                    }
                }
            }
        }

        if (onItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener { v ->
                val position = holder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    @Suppress("UNCHECKED_CAST")
                    val item = v.getTag(R.id.fusion_item_tag) as? T
                    if (item != null) {
                        return@setOnLongClickListener onItemLongClickListener?.invoke(holder, item, position) == true
                    }
                }
                false
            }
        }

        onViewHolderCreated(holder)
        return holder
    }

    open fun onViewHolderCreated(holder: LayoutHolder) {}

    abstract fun LayoutHolder.onBind(item: T)

    final override fun onBindViewHolder(holder: LayoutHolder, item: T, position: Int, payloads: MutableList<Any>) {
        holder.itemView.setTag(R.id.fusion_item_tag, item)

        if (payloads.isNotEmpty()) {
            val handled = dispatchHandledPayloads(holder, item, payloads)
            onBindPayload(holder, item, position, payloads, handled)
        } else {
            holder.onBind(item)
        }
    }

    open fun onBindPayload(
        holder: LayoutHolder,
        item: T,
        position: Int,
        payloads: MutableList<Any>,
        handled: Boolean
    ) {
        if (!handled) {
            // Fallback to full bind
            holder.onBind(item)
        }
    }

    // --- Payload Binding Overloads (1-6 Params) ---

    protected fun <P> bindPayload(prop: KProperty1<T, P>, action: LayoutHolder.(P) -> Unit) {
        registerDataWatcher(prop, action)
    }

    protected fun <P1, P2> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>,
        action: LayoutHolder.(P1, P2) -> Unit
    ) {
        registerDataWatcher(p1, p2, action)
    }

    protected fun <P1, P2, P3> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>,
        action: LayoutHolder.(P1, P2, P3) -> Unit
    ) {
        registerDataWatcher(p1, p2, p3, action)
    }

    protected fun <P1, P2, P3, P4> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, p4: KProperty1<T, P4>,
        action: LayoutHolder.(P1, P2, P3, P4) -> Unit
    ) {
        registerDataWatcher(p1, p2, p3, p4, action)
    }

    protected fun <P1, P2, P3, P4, P5> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, p4: KProperty1<T, P4>, p5: KProperty1<T, P5>,
        action: LayoutHolder.(P1, P2, P3, P4, P5) -> Unit
    ) {
        registerDataWatcher(p1, p2, p3, p4, p5, action)
    }

    protected fun <P1, P2, P3, P4, P5, P6> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, p4: KProperty1<T, P4>, p5: KProperty1<T, P5>, p6: KProperty1<T, P6>,
        action: LayoutHolder.(P1, P2, P3, P4, P5, P6) -> Unit
    ) {
        registerDataWatcher(p1, p2, p3, p4, p5, p6, action)
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
}