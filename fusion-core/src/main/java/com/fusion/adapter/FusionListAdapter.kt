package com.fusion.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.core.FusionCore
import com.fusion.adapter.core.FusionLinker
import com.fusion.adapter.delegate.FusionItemDelegate
import com.fusion.adapter.diff.FusionDiffCallback

/**
 * [FusionListAdapter] - 自动挡
 *
 * 基于 AsyncListDiffer 实现，内置 Smart Diff 策略。
 * 适合 MVVM 架构，配合 ViewModel 和 LiveData/Flow 使用。
 *
 * 特性：
 * 1. O(1) 路由分发
 * 2. 自动计算 Diff (支持 FusionStableId)
 * 3. 自动分发 Payload 局部刷新
 * 4. 生命周期全托管
 *
 * @sample
 * val adapter = FusionListAdapter()
 * adapter.register(UserDelegate())
 * adapter.submitList(users)
 */
open class FusionListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 核心引擎
    private val core = FusionCore(this)

    // ========================================================================================
    // DiffUtil 策略配置
    // ========================================================================================

    private val diffCallback = object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return FusionDiffCallback.areItemsTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            // 路由到 Delegate 内部判断内容是否变化
            return core.areContentsTheSame(oldItem, newItem)
        }

        override fun getChangePayload(oldItem: Any, newItem: Any): Any? {
            // 路由到 Delegate 获取局部刷新 Payload
            return core.getChangePayload(oldItem, newItem)
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    // ========================================================================================
    // 注册接口 (API)
    // ========================================================================================

    /**
     * [KTX 专用接口] 注册路由连接器
     * KTX DSL 通过此方法注入配置好的 FusionLinker。
     */
    fun <T : Any> registerLinker(clazz: Class<T>, linker: FusionLinker<T>) {
        core.register(clazz, linker)
    }

    /**
     * [Java/普通接口] 注册单类型委托 (一对一)
     * 内部会自动创建一个默认的 Linker，简化非 DSL 场景的使用。
     */
    fun <T : Any> register(clazz: Class<T>, delegate: FusionItemDelegate<T, *>) {
        val linker = FusionLinker<T>()
        linker.map(Unit, delegate) // 默认 Key 为 Unit
        core.register(clazz, linker)
    }

    // ========================================================================================
    // 数据操作
    // ========================================================================================

    /** 提交数据列表 (异步计算 Diff) */
    fun submitList(list: List<Any>?, commitCallback: Runnable? = null) {
        differ.submitList(list, commitCallback)
    }

    /** 获取当前数据列表 (只读) */
    val currentList: List<Any>
        get() = differ.currentList

    // ========================================================================================
    // RecyclerView.Adapter 实现委托
    // ========================================================================================

    override fun getItemCount(): Int = differ.currentList.size

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

    // --- 生命周期分发 (防止内存泄漏) ---
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) = core.onViewRecycled(holder)
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) = core.onViewAttachedToWindow(holder)
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) = core.onViewDetachedFromWindow(holder)
}