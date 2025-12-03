package com.fusion.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.core.FusionCore
import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.adapter.delegate.FusionItemDelegate

/**
 * [FusionAdapter] - 手动挡
 *
 * 不使用 DiffUtil，需要手动调用 notifyDataSetChanged 等方法。
 * 适合数据量极小、或者对刷新行为有绝对控制需求的场景。
 */
open class FusionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val core = FusionCore()
    private val items = ArrayList<Any>()

    /**
     * [基础注册方法] - 非 inline
     * 这个方法是 public 的，可以直接访问 private 的 core。
     * 同时它也方便 Java 调用或者动态注册。
     */
    fun register(clazz: Class<*>, delegate: FusionItemDelegate<*, *>) {
        core.register(clazz, delegate)
    }

    /**
     * [语法糖] - Kotlin inline 扩展
     * 这个方法不直接访问 core，而是调用上面的 register 方法。
     */
    inline fun <reified T : Any> register(delegate: BindingDelegate<T, *>) {
        // 修改这里：调用上面那个 public 的 register，而不是 core.register
        register(T::class.java, delegate)
    }

    fun setItems(newItems: List<Any>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItems(newItems: List<Any>) {
        val start = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(start, newItems.size)
    }

    fun getItem(position: Int): Any = items[position]

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = core.getItemViewType(items[position], position)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = core.onCreateViewHolder(parent, viewType)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = core.onBindViewHolder(holder, items[position], position, emptyList())
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) =
        core.onBindViewHolder(holder, items[position], position, payloads)

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) = core.onViewRecycled(holder)
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) = core.onViewAttachedToWindow(holder)
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) = core.onViewDetachedFromWindow(holder)
}