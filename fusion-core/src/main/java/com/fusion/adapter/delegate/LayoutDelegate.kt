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

abstract class LayoutDelegate<T : Any>(
    @LayoutRes private val layoutResId: Int
) : FusionDelegate<T, LayoutHolder>() {

    override val signature: ViewSignature = ClassSignature(this::class.java)

    // 保存点击事件监听器
    private var onItemClickListener: ((View, T, Int) -> Unit)? = null
    private var onItemLongClickListener: ((View, T, Int) -> Boolean)? = null

    // 特定的防抖时间，为 null 则使用全局配置
    private var specificDebounceInterval: Long? = null

    final override fun onCreateViewHolder(parent: ViewGroup): LayoutHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        val holder = LayoutHolder(view)

        // 统一处理点击事件，只需设置一次，避免在 onBind 中重复创建对象
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

        // 统一处理长按事件
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

    /**
     * 子类重写此方法进行 View 的初始化设置（如设置 LayoutManager 等）
     */
    open fun onViewHolderCreated(holder: LayoutHolder) {}

    /**
     * 定义抽象绑定方法
     */
    abstract fun LayoutHolder.onBind(item: T)

    final override fun onBindViewHolder(holder: LayoutHolder, item: T, position: Int, payloads: MutableList<Any>) {
        // [关键]：将数据锚定到 View 上，这是实现通用点击监听的基础
        holder.itemView.setTag(R.id.fusion_item_tag, item)

        holder.onBind(item)
    }

    // --- 公共 API，对齐 BindingDelegate 的使用体验 ---

    fun setOnItemClick(debounceMs: Long? = null, listener: (view: View, item: T, position: Int) -> Unit) {
        this.specificDebounceInterval = debounceMs
        this.onItemClickListener = listener
    }

    fun setOnItemLongClick(listener: (view: View, item: T, position: Int) -> Boolean) {
        this.onItemLongClickListener = listener
    }
}