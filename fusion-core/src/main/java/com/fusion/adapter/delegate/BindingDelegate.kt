package com.fusion.adapter.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.extensions.getItem
import com.fusion.adapter.extensions.setItem
import com.fusion.adapter.internal.FusionInternalTags
import com.fusion.adapter.internal.GlobalTypeKey
import com.fusion.adapter.internal.ViewTypeKey
import com.fusion.adapter.internal.diff.*
import kotlin.reflect.KProperty1

/**
 * [BindingDelegate]
 */
abstract class BindingDelegate<T : Any, VB : ViewBinding>(
    private val inflater: BindingInflater<VB>? = null
) : FusionDelegate<T, BindingHolder<VB>>() {

    override val viewTypeKey: ViewTypeKey = GlobalTypeKey(this::class.java, FusionInternalTags.TAG_BINDING_DELEGATE)

    private var onItemClick: ((binding: VB, item: T, position: Int) -> Unit)? = null
    private var onItemLongClick: ((binding: VB, item: T, position: Int) -> Boolean)? = null
    private var clickDebounceMs: Long? = null

    fun interface OnItemClickListener<T, VB : ViewBinding> {
        fun onItemClick(binding: VB, item: T, position: Int)
    }

    fun setOnItemClick(listener: OnItemClickListener<T, VB>) {
        this.onItemClick = { b, i, p -> listener.onItemClick(b, i, p) }; this.clickDebounceMs = null
    }

    fun setOnItemClick(debounceMs: Long?, listener: OnItemClickListener<T, VB>) {
        this.onItemClick = { b, i, p -> listener.onItemClick(b, i, p) }; this.clickDebounceMs = debounceMs
    }

    fun setOnItemLongClick(listener: (binding: VB, item: T, position: Int) -> Boolean) {
        this.onItemLongClick = listener
    }

    protected open fun onInflateBinding(inflater: LayoutInflater, parent: ViewGroup): VB {
        return this.inflater?.inflate(inflater, parent, false)
            ?: throw IllegalStateException("Fusion: Either provide an inflater in constructor or override onInflateBinding()")
    }

    final override fun onCreateViewHolder(parent: ViewGroup): BindingHolder<VB> {
        val binding = onInflateBinding(LayoutInflater.from(parent.context), parent)
        val holder = BindingHolder(binding)
        if (onItemClick != null) {
            holder.itemView.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = holder.getItem<T>()
                    if (item != null) onItemClick?.invoke(holder.binding, item, pos)
                }
            }
        }
        if (onItemLongClick != null) {
            holder.itemView.setOnLongClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = holder.getItem<T>()
                    if (item != null) return@setOnLongClickListener onItemLongClick?.invoke(holder.binding, item, pos) == true
                }
                false
            }
        }
        onCreate(holder.binding); return holder
    }

    open fun onCreate(binding: VB) {}

    final override fun onBindViewHolder(holder: BindingHolder<VB>, item: T, position: Int, payloads: MutableList<Any>) {
        holder.setItem(item)
        if (payloads.isNotEmpty()) {
            val handled = dispatchHandledPayloads(holder.binding, item, payloads)
            onPayload(holder.binding, item, position, payloads, handled)
        } else {
            onBind(holder.binding, item, position)
        }
    }

    abstract fun onBind(binding: VB, item: T, position: Int)

    open fun onPayload(binding: VB, item: T, position: Int, payloads: List<Any>, handled: Boolean) {
        if (!handled) onBind(binding, item, position)
    }

    override fun addObserver(observer: PropertyObserver<T>) {
        super.addObserver(object : PropertyObserver<T> {
            override fun checkChange(oldItem: T, newItem: T) = observer.checkChange(oldItem, newItem)
            override fun execute(receiver: Any, item: T) {
                val target = if (receiver is BindingHolder<*>) receiver.binding else receiver
                observer.execute(target, item)
            }
        })
    }

    @Suppress("UNCHECKED_CAST")
    override fun <P> registerPropertyObserver(g1: (T) -> P, action: BindingHolder<VB>.(P) -> Unit) {
        super.addObserver(PropertyObserver1(g1) { p -> (this as BindingHolder<VB>).action(p) })
    }

    @Suppress("UNCHECKED_CAST")
    override fun <P1, P2> registerPropertyObserver(g1: (T) -> P1, g2: (T) -> P2, action: BindingHolder<VB>.(P1, P2) -> Unit) {
        super.addObserver(PropertyObserver2(g1, g2) { v1, v2 -> (this as BindingHolder<VB>).action(v1, v2) })
    }

    @Suppress("UNCHECKED_CAST")
    override fun <P1, P2, P3> registerPropertyObserver(g1: (T) -> P1, g2: (T) -> P2, g3: (T) -> P3, action: BindingHolder<VB>.(P1, P2, P3) -> Unit) {
        super.addObserver(PropertyObserver3(g1, g2, g3) { v1, v2, v3 -> (this as BindingHolder<VB>).action(v1, v2, v3) })
    }

    @Suppress("UNCHECKED_CAST")
    override fun <P1, P2, P3, P4> registerPropertyObserver(
        g1: (T) -> P1,
        g2: (T) -> P2,
        g3: (T) -> P3,
        g4: (T) -> P4,
        action: BindingHolder<VB>.(P1, P2, P3, P4) -> Unit
    ) {
        super.addObserver(PropertyObserver4(g1, g2, g3, g4) { v1, v2, v3, v4 -> (this as BindingHolder<VB>).action(v1, v2, v3, v4) })
    }

    @Suppress("UNCHECKED_CAST")
    override fun <P1, P2, P3, P4, P5> registerPropertyObserver(
        g1: (T) -> P1,
        g2: (T) -> P2,
        g3: (T) -> P3,
        g4: (T) -> P4,
        g5: (T) -> P5,
        action: BindingHolder<VB>.(P1, P2, P3, P4, P5) -> Unit
    ) {
        super.addObserver(PropertyObserver5(g1, g2, g3, g4, g5) { v1, v2, v3, v4, v5 -> (this as BindingHolder<VB>).action(v1, v2, v3, v4, v5) })
    }

    @Suppress("UNCHECKED_CAST")
    override fun <P1, P2, P3, P4, P5, P6> registerPropertyObserver(
        g1: (T) -> P1,
        g2: (T) -> P2,
        g3: (T) -> P3,
        g4: (T) -> P4,
        g5: (T) -> P5,
        g6: (T) -> P6,
        action: BindingHolder<VB>.(P1, P2, P3, P4, P5, P6) -> Unit
    ) {
        super.addObserver(PropertyObserver6(g1, g2, g3, g4, g5, g6) { v1, v2, v3, v4, v5, v6 -> (this as BindingHolder<VB>).action(v1, v2, v3, v4, v5, v6) })
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <P> onPayload(p1: KProperty1<T, P>, action: VB.(P) -> Unit) {
        addObserver(PropertyObserver1({ p1.get(it) }) { v1 -> (this as VB).action(v1) })
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <P1, P2> onPayload(p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, action: VB.(P1, P2) -> Unit) {
        addObserver(PropertyObserver2({ p1.get(it) }, { p2.get(it) }) { v1, v2 -> (this as VB).action(v1, v2) })
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <P1, P2, P3> onPayload(p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, action: VB.(P1, P2, P3) -> Unit) {
        addObserver(PropertyObserver3({ p1.get(it) }, { p2.get(it) }, { p3.get(it) }) { v1, v2, v3 -> (this as VB).action(v1, v2, v3) })
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <P1, P2, P3, P4> onPayload(
        p1: KProperty1<T, P1>,
        p2: KProperty1<T, P2>,
        p3: KProperty1<T, P3>,
        p4: KProperty1<T, P4>,
        action: VB.(P1, P2, P3, P4) -> Unit
    ) {
        addObserver(PropertyObserver4({ p1.get(it) }, { p2.get(it) }, { p3.get(it) }, { p4.get(it) }) { v1, v2, v3, v4 -> (this as VB).action(v1, v2, v3, v4) })
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <P1, P2, P3, P4, P5> onPayload(
        p1: KProperty1<T, P1>,
        p2: KProperty1<T, P2>,
        p3: KProperty1<T, P3>,
        p4: KProperty1<T, P4>,
        p5: KProperty1<T, P5>,
        action: VB.(P1, P2, P3, P4, P5) -> Unit
    ) {
        addObserver(
            PropertyObserver5(
                { p1.get(it) },
                { p2.get(it) },
                { p3.get(it) },
                { p4.get(it) },
                { p5.get(it) }) { v1, v2, v3, v4, v5 -> (this as VB).action(v1, v2, v3, v4, v5) })
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <P1, P2, P3, P4, P5, P6> onPayload(
        p1: KProperty1<T, P1>,
        p2: KProperty1<T, P2>,
        p3: KProperty1<T, P3>,
        p4: KProperty1<T, P4>,
        p5: KProperty1<T, P5>,
        p6: KProperty1<T, P6>,
        action: VB.(P1, P2, P3, P4, P5, P6) -> Unit
    ) {
        addObserver(
            PropertyObserver6(
                { p1.get(it) },
                { p2.get(it) },
                { p3.get(it) },
                { p4.get(it) },
                { p5.get(it) },
                { p6.get(it) }) { v1, v2, v3, v4, v5, v6 -> (this as VB).action(v1, v2, v3, v4, v5, v6) })
    }
}
