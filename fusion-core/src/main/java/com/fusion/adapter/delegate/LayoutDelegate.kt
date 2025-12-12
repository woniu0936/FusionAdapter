package com.fusion.adapter.delegate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.core.R
import com.fusion.adapter.internal.ClassSignature
import com.fusion.adapter.internal.ViewSignature
import com.fusion.adapter.internal.click
import kotlin.reflect.KProperty1

abstract class LayoutDelegate<T : Any>(
    @LayoutRes private val layoutResId: Int
) : FusionDelegate<T, LayoutHolder>() {

    override val signature: ViewSignature = ClassSignature(this::class.java)

    private var onItemClickListener: ((View, T, Int) -> Unit)? = null
    private var onItemLongClickListener: ((View, T, Int) -> Boolean)? = null
    private var specificDebounceInterval: Long? = null

    // =================================================================================
    //  [核心升级] 1-6 参数的 Payload 注册方法 (供子类在 init {} 中调用)
    // =================================================================================

    /** 1 参数 */
    protected fun <P> bindPayload(prop: KProperty1<T, P>, action: LayoutHolder.(P) -> Unit) {
        registerDataWatcher(prop, action)
    }

    /** 2 参数 */
    protected fun <P1, P2> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>,
        action: LayoutHolder.(P1, P2) -> Unit
    ) {
        registerDataWatcher(p1, p2, action)
    }

    /** 3 参数 */
    protected fun <P1, P2, P3> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>,
        action: LayoutHolder.(P1, P2, P3) -> Unit
    ) {
        registerDataWatcher(p1, p2, p3, action)
    }

    /** 4 参数 */
    protected fun <P1, P2, P3, P4> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, p4: KProperty1<T, P4>,
        action: LayoutHolder.(P1, P2, P3, P4) -> Unit
    ) {
        registerDataWatcher(p1, p2, p3, p4, action)
    }

    /** 5 参数 */
    protected fun <P1, P2, P3, P4, P5> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, p4: KProperty1<T, P4>, p5: KProperty1<T, P5>,
        action: LayoutHolder.(P1, P2, P3, P4, P5) -> Unit
    ) {
        registerDataWatcher(p1, p2, p3, p4, p5, action)
    }

    /** 6 参数 */
    protected fun <P1, P2, P3, P4, P5, P6> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, p4: KProperty1<T, P4>, p5: KProperty1<T, P5>, p6: KProperty1<T, P6>,
        action: LayoutHolder.(P1, P2, P3, P4, P5, P6) -> Unit
    ) {
        registerDataWatcher(p1, p2, p3, p4, p5, p6, action)
    }

    // =================================================================================
    //  生命周期与绑定逻辑
    // =================================================================================

    final override fun onCreateViewHolder(parent: ViewGroup): LayoutHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        val holder = LayoutHolder(view)

        // 点击事件绑定
        if (onItemClickListener != null) {
            holder.itemView.click(specificDebounceInterval) { v ->
                val position = holder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    @Suppress("UNCHECKED_CAST")
                    val item = v.getTag(R.id.fusion_item_tag) as? T
                    if (item != null) {
                        onItemClickListener?.invoke(v, item, position)
                    }
                }
            }
        }
        // 长按事件绑定
        if (onItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener { v ->
                val position = holder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    @Suppress("UNCHECKED_CAST")
                    val item = v.getTag(R.id.fusion_item_tag) as? T
                    if (item != null) {
                        return@setOnLongClickListener onItemLongClickListener?.invoke(v, item, position) == true
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
            // 1. 尝试自动分发
            val handled = dispatchHandledPayloads(holder, item, payloads)

            // 2. 调用带状态的 onBindPayload
            onBindPayload(holder, item, position, payloads, handled)
        } else {
            // 全量绑定
            holder.onBind(item)
        }
    }

    /**
     * [新版回调] 子类可覆盖，用于处理无法自动分发的复杂 Payload
     */
    open fun onBindPayload(
        holder: LayoutHolder,
        item: T,
        position: Int,
        payloads: MutableList<Any>,
        handled: Boolean
    ) {
        // 兜底逻辑：如果没处理，调用旧版方法
        if (!handled) {
            onBindPayload(holder, item, position, payloads)
        }
    }

    /**
     * [旧版回调] 兼容性保留
     * 默认实现：如果没处理，回退到全量更新 (LayoutHolder.onBind)
     */
    open fun onBindPayload(holder: LayoutHolder, item: T, position: Int, payloads: MutableList<Any>) {
        holder.onBind(item)
    }

    // =================================================================================
    //  事件监听设置
    // =================================================================================

    fun setOnItemClick(debounceMs: Long? = null, listener: (view: View, item: T, position: Int) -> Unit) {
        this.specificDebounceInterval = debounceMs
        this.onItemClickListener = listener
    }

    fun setOnItemLongClick(listener: (view: View, item: T, position: Int) -> Boolean) {
        this.onItemLongClickListener = listener
    }
}