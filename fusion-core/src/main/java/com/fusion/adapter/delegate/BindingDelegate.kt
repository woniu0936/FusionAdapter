package com.fusion.adapter.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.core.R
import com.fusion.adapter.internal.ClassSignature
import com.fusion.adapter.internal.ViewSignature
import com.fusion.adapter.internal.Watcher
import com.fusion.adapter.internal.click
import kotlin.reflect.KProperty1

/**
 * [BindingDelegate]
 * 专为 ViewBinding 设计的高效委托，消灭 ViewHolder 样板代码。
 *
 * @sample
 * class TextDelegate : BindingDelegate<String, ItemTextBinding>(ItemTextBinding::inflate) {
 *     override fun onBind(binding: ItemTextBinding, item: String, position: Int) {
 *         binding.textView.text = item
 *     }
 * }
 */
abstract class BindingDelegate<T : Any, VB : ViewBinding>(
    private val inflater: BindingInflater<VB>
) : FusionDelegate<T, BindingHolder<VB>>() {

    /**
     * [默认签名策略]
     * 对于手动创建的 class UserDelegate : BindingDelegate...
     * 它的签名就是 UserDelegate::class。
     *
     * 性能：this::class.java 是原生操作，极快。
     */
    override val signature: ViewSignature = ClassSignature(this::class.java)

    // 点击事件回调 (View, Item, Position)
    var onItemClick: ((view: VB, item: T, position: Int) -> Unit)? = null
    var onItemLongClick: ((view: VB, item: T, position: Int) -> Boolean)? = null

    /**
     * [新增属性]
     * 用于存储防抖时间配置。
     * internal 修饰符允许 FunctionalBindingDelegate (子类) 访问并赋值。
     * null = 使用全局配置。
     */
    internal var itemClickDebounceInterval: Long? = null

    /**
     * 自定义防抖时间
     */
    fun setOnItemClick(debounceMs: Long, listener: (view: VB, item: T, position: Int) -> Unit) {
        this.onItemClick = listener
        this.itemClickDebounceInterval = debounceMs
    }

    final override fun onCreateViewHolder(parent: ViewGroup): BindingHolder<VB> {
        val binding = inflater.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = BindingHolder(binding)
        if (onItemClick != null) {
            holder.itemView.click(itemClickDebounceInterval) {
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

    /** 提供钩子函数供子类初始化点击事件等 */
    protected open fun onViewHolderCreated(binding: VB) {}

    final override fun onBindViewHolder(holder: BindingHolder<VB>, item: T, position: Int, payloads: MutableList<Any>) {
        holder.itemView.setTag(R.id.fusion_item_tag, item)
        if (payloads.isNotEmpty()) {
            // 1. 尝试自动分发 (智能属性监听)
            val handled = dispatchHandledPayloads(holder, item, payloads)
            // 2. 调用新版回调 (带状态)
            onBindPayload(holder.binding, item, position, payloads, handled)
        } else {
            onBind(holder.binding, item, position)
        }
    }

    /** 子类实现：数据绑定 */
    abstract fun onBind(binding: VB, item: T, position: Int)

    open fun onBindPayload(
        binding: VB,
        item: T,
        position: Int,
        payloads: MutableList<Any>,
        handled: Boolean // 新增参数
    ) {
        // 默认行为：如果自动分发没处理，且有 payload，回退到全量 bind
        // 或者你可以选择什么都不做。这里为了安全，如果没处理则刷新。
        if (!handled) {
            onBind(binding, item, position)
        }
    }

    /** 子类实现：局部刷新 (可选) */
    open fun onBindPayload(binding: VB, item: T, position: Int, payloads: MutableList<Any>) {
        onBind(binding, item, position)
    }

    /**
     * 供 DSL 使用，把 VB 注册进去
     */
    internal fun registerWatcherFromDsl(watcher: Watcher<T>) {
        // 直接存入，但在 execute 时，Watcher 需要处理 Holder -> Binding 的转换
        // 为了简单，我们这里做一个代理 Watcher，把 execute 的 receiver 替换为 binding
        // 或者，我们在 FunctionalBindingDelegate 里做这件事更合适。
        // *最佳修正*: 让 BindingDelegate 的 Watcher 直接操作 Binding。

        // 由于 Watcher 定义是 Watcher<T>，execute(receiver: Any, item: T)
        // 我们在 DSL 里传入的 Action 是 VB.() -> Unit
        // 而 dispatchHandledPayloads 传入的是 Holder。
        // 所以我们需要一个中间层。

        // 为了不把逻辑搞太复杂，建议在这里强制注册：
        super.registerWatcher(object : Watcher<T> {
            override fun checkChange(oldItem: T, newItem: T) = watcher.checkChange(oldItem, newItem)
            override fun execute(receiver: Any, item: T) {
                // [关键]：这里把 holder 剥离，拿出 binding 传给原始 watcher
                @Suppress("UNCHECKED_CAST")
                val holder = receiver as BindingHolder<VB>
                watcher.execute(holder.binding, item)
            }
        })
    }

    /** 1 参数 */
    protected fun <P> bindPayload(prop: KProperty1<T, P>, action: VB.(P) -> Unit) {
        registerDataWatcher(prop) { value -> this.binding.action(value) }
    }

    /** 2 参数 */
    protected fun <P1, P2> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>,
        action: VB.(P1, P2) -> Unit
    ) {
        registerDataWatcher(p1, p2) { v1, v2 -> this.binding.action(v1, v2) }
    }

    /** 3 参数 */
    protected fun <P1, P2, P3> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>,
        action: VB.(P1, P2, P3) -> Unit
    ) {
        registerDataWatcher(p1, p2, p3) { v1, v2, v3 -> this.binding.action(v1, v2, v3) }
    }

    /** 4 参数 */
    protected fun <P1, P2, P3, P4> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, p4: KProperty1<T, P4>,
        action: VB.(P1, P2, P3, P4) -> Unit
    ) {
        registerDataWatcher(p1, p2, p3, p4) { v1, v2, v3, v4 -> this.binding.action(v1, v2, v3, v4) }
    }

    /** 5 参数 */
    protected fun <P1, P2, P3, P4, P5> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, p4: KProperty1<T, P4>, p5: KProperty1<T, P5>,
        action: VB.(P1, P2, P3, P4, P5) -> Unit
    ) {
        registerDataWatcher(p1, p2, p3, p4, p5) { v1, v2, v3, v4, v5 -> this.binding.action(v1, v2, v3, v4, v5) }
    }

    /** 6 参数 */
    protected fun <P1, P2, P3, P4, P5, P6> bindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>, p3: KProperty1<T, P3>, p4: KProperty1<T, P4>, p5: KProperty1<T, P5>, p6: KProperty1<T, P6>,
        action: VB.(P1, P2, P3, P4, P5, P6) -> Unit
    ) {
        registerDataWatcher(p1, p2, p3, p4, p5, p6) { v1, v2, v3, v4, v5, v6 -> this.binding.action(v1, v2, v3, v4, v5, v6) }
    }

}