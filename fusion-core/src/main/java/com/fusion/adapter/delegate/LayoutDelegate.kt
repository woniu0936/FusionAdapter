package com.fusion.adapter.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.extensions.getItem
import com.fusion.adapter.extensions.setItem
import com.fusion.adapter.internal.ClassTypeKey
import com.fusion.adapter.internal.ViewTypeKey
import kotlin.reflect.KProperty1

/**
 * [LayoutDelegate]
 */
abstract class LayoutDelegate<T : Any>(
    @LayoutRes private val layoutResId: Int
) : FusionDelegate<T, LayoutHolder>() {

    override val viewTypeKey: ViewTypeKey = ClassTypeKey(this::class.java)
    private var onItemClickListener: ((holder: LayoutHolder, item: T, position: Int) -> Unit)? = null
    private var onItemLongClickListener: ((holder: LayoutHolder, item: T, position: Int) -> Boolean)? = null
    private var clickDebounceMs: Long? = null

    fun interface OnItemClickListener<T> {
        fun onItemClick(holder: LayoutHolder, item: T, position: Int)
    }

    fun setOnItemClick(listener: OnItemClickListener<T>) {
        this.onItemClickListener = { h, i, p -> listener.onItemClick(h, i, p) }; this.clickDebounceMs = null
    }

    fun setOnItemClick(debounceMs: Long?, listener: OnItemClickListener<T>) {
        this.onItemClickListener = { h, i, p -> listener.onItemClick(h, i, p) }; this.clickDebounceMs = debounceMs
    }

    fun setOnItemLongClick(listener: (holder: LayoutHolder, item: T, position: Int) -> Boolean) {
        this.onItemLongClickListener = listener
    }

    final override fun onCreateViewHolder(parent: ViewGroup): LayoutHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        val holder = LayoutHolder(view)
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = it.getItem<T>()
                    if (item != null) onItemClickListener?.invoke(holder, item, pos)
                }
            }
        }
        if (onItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = it.getItem<T>()
                    if (item != null) return@setOnLongClickListener onItemLongClickListener?.invoke(holder, item, pos) == true
                }
                false
            }
        }
        onCreate(holder); return holder
    }

    open fun onCreate(holder: LayoutHolder) {}
    abstract fun LayoutHolder.onBind(item: T)

    final override fun onBindViewHolder(holder: LayoutHolder, item: T, position: Int, payloads: MutableList<Any>) {
        holder.setItem(item)
        if (payloads.isNotEmpty()) {
            val handled = dispatchHandledPayloads(holder, item, payloads)
            onBindPartial(holder, item, position, payloads, handled)
        } else holder.onBind(item)
    }

    open fun onBindPartial(holder: LayoutHolder, item: T, position: Int, payloads: List<Any>, handled: Boolean) {
        if (!handled) holder.onBind(item)
    }

    // --- onPayload 重载 (1-6 参数) ---
    // [极致修复] 移除所有手动强转，利用基类的类型安全包装

    protected fun <P> onPayload(p1: KProperty1<T, P>, action: LayoutHolder.(P) -> Unit) {
        registerPropertyObserver({ p1.get(it) }, action)
    }

    protected fun <P1, P2> onPayload(p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, action: LayoutHolder.(P1, P2) -> Unit) {
        registerPropertyObserver({ p1.get(it) }, { p2.get(it) }, action)
    }

    protected fun <P1, P2, P3> onPayload(p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, action: LayoutHolder.(P1, P2, P3) -> Unit) {
        registerPropertyObserver({ p1.get(it) }, { p2.get(it) }, { p3.get(it) }, action)
    }

    protected fun <P1, P2, P3, P4> onPayload(
        p1: KProperty1<T, P1>,
        p2: KProperty1<T, P2>,
        p3: KProperty1<T, P3>,
        p4: KProperty1<T, P4>,
        action: LayoutHolder.(P1, P2, P3, P4) -> Unit
    ) {
        registerPropertyObserver({ p1.get(it) }, { p2.get(it) }, { p3.get(it) }, { p4.get(it) }, action)
    }

    protected fun <P1, P2, P3, P4, P5> onPayload(
        p1: KProperty1<T, P1>,
        p2: KProperty1<T, P2>,
        p3: KProperty1<T, P3>,
        p4: KProperty1<T, P4>,
        p5: KProperty1<T, P5>,
        action: LayoutHolder.(P1, P2, P3, P4, P5) -> Unit
    ) {
        registerPropertyObserver({ p1.get(it) }, { p2.get(it) }, { p3.get(it) }, { p4.get(it) }, { p5.get(it) }, action)
    }

    protected fun <P1, P2, P3, P4, P5, P6> onPayload(
        p1: KProperty1<T, P1>,
        p2: KProperty1<T, P2>,
        p3: KProperty1<T, P3>,
        p4: KProperty1<T, P4>,
        p5: KProperty1<T, P5>,
        p6: KProperty1<T, P6>,
        action: LayoutHolder.(P1, P2, P3, P4, P5, P6) -> Unit
    ) {
        registerPropertyObserver({ p1.get(it) }, { p2.get(it) }, { p3.get(it) }, { p4.get(it) }, { p5.get(it) }, { p6.get(it) }, action)
    }
}