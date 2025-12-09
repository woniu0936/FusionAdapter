package com.fusion.adapter.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

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

    final override fun onCreateViewHolder(parent: ViewGroup): BindingHolder<VB> {
        val binding = inflater.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = BindingHolder(binding)
        onViewHolderCreated(holder)
        return holder
    }

    /** 提供钩子函数供子类初始化点击事件等 */
    protected open fun onViewHolderCreated(holder: BindingHolder<VB>) {}

    final override fun onBindViewHolder(holder: BindingHolder<VB>, item: T, position: Int, payloads: MutableList<Any>) {
        // 动态绑定监听器，确保获取最新的 AdapterPosition
        // 注意：这种方式在 onBind 中设置监听器虽然会重复 set，但保证了 item 的正确性。
        // 如需极致性能优化，可在 onCreate 中 set，通过 tag 获取 item，但代码复杂度会上升。
        // 对于 99% 的场景，直接 set 没有任何性能问题。

        onItemClick?.let { listener ->
            holder.itemView.setOnClickListener {
                listener(holder.binding, item, holder.bindingAdapterPosition)
            }
        }

        onItemLongClick?.let { listener ->
            holder.itemView.setOnLongClickListener {
                listener(holder.binding, item, holder.bindingAdapterPosition) ?: false
            }
        }

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