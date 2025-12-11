package com.fusion.adapter.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.core.R
import com.fusion.adapter.internal.ClassSignature
import com.fusion.adapter.internal.ViewSignature
import com.fusion.adapter.internal.click

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
) : FusionDelegate<T, BindingDelegate.BindingHolder<VB>>() {

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

    /**
     * [默认签名策略]
     * 对于手动创建的 class UserDelegate : BindingDelegate...
     * 它的签名就是 UserDelegate::class。
     *
     * 性能：this::class.java 是原生操作，极快。
     */
    override val signature: ViewSignature = ClassSignature(this::class.java)

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
        onViewHolderCreated(holder)
        return holder
    }

    /** 提供钩子函数供子类初始化点击事件等 */
    protected open fun onViewHolderCreated(holder: BindingHolder<VB>) {}

    final override fun onBindViewHolder(holder: BindingHolder<VB>, item: T, position: Int, payloads: MutableList<Any>) {
        holder.itemView.setTag(R.id.fusion_item_tag, item)

        if (payloads.isNotEmpty()) {
            onBindPayload(holder.binding, item, position, payloads)
        } else {
            onBind(holder.binding, item, position)
        }
    }

    /** 子类实现：数据绑定 */
    abstract fun onBind(binding: VB, item: T, position: Int)

    /** 子类实现：局部刷新 (可选) */
    open fun onBindPayload(binding: VB, item: T, position: Int, payloads: MutableList<Any>) {
        onBind(binding, item, position)
    }

    class BindingHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)
}