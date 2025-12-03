package com.fusion.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.core.FusionCore
import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.adapter.delegate.FusionItemDelegate
import com.fusion.adapter.diff.FusionDiffCallback

/**
 * [FusionListAdapter] - 自动挡
 *
 * 基于 AsyncListDiffer 实现，内置 FusionDiffCallback。
 * 适合配合 ViewModel 和 LiveData/Flow 使用，自动计算 Diff 并刷新列表。
 *
 * @sample
 * val adapter = FusionListAdapter()
 * adapter.register(UserDelegate())
 * adapter.submitList(users)
 */
open class FusionListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val core = FusionCore()

    // 自定义 DiffCallback 逻辑，路由给 Delegate 处理 Payload
    private val diffCallback = object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any) =
            FusionDiffCallback.areItemsTheSame(oldItem, newItem)

        override fun areContentsTheSame(oldItem: Any, newItem: Any) =
            FusionDiffCallback.areContentsTheSame(oldItem, newItem)

        override fun getChangePayload(oldItem: Any, newItem: Any): Any? {
            // 将 Payload 判断代理给 Core -> Registry -> Delegate
            return core.getChangePayload(oldItem, newItem)
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

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

    /** 提交数据 */
    fun submitList(list: List<Any>?, commitCallback: Runnable? = null) {
        differ.submitList(list, commitCallback)
    }

    /** 获取当前列表 (只读) */
    val currentList: List<Any>
        get() = differ.currentList

    // --- Adapter 实现 ---

    override fun getItemCount() = differ.currentList.size

    override fun getItemViewType(position: Int): Int {
        return core.getItemViewType(differ.currentList[position], position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return core.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        core.onBindViewHolder(holder, differ.currentList[position], position, emptyList())
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        core.onBindViewHolder(holder, differ.currentList[position], position, payloads)
    }

    // 生命周期分发
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) = core.onViewRecycled(holder)
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) = core.onViewAttachedToWindow(holder)
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) = core.onViewDetachedFromWindow(holder)
}