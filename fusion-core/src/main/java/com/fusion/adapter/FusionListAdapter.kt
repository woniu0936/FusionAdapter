package com.fusion.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.internal.AdapterController
import com.fusion.adapter.internal.TypeRouter
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.RegistryOwner
import com.fusion.adapter.extensions.attachFusionGridSupport
import com.fusion.adapter.extensions.attachFusionStaggeredSupport
import com.fusion.adapter.intercept.FusionDataInterceptor

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
open class FusionListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() , RegistryOwner {

    // 核心引擎
    private val core = AdapterController()

    // ========================================================================================
    // DiffUtil 策略配置
    // ========================================================================================

    private val diffCallback = object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return core.areItemsTheSame(oldItem, newItem)
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
    override fun <T : Any> attachLinker(clazz: Class<T>, linker: TypeRouter<T>) {
        core.register(clazz, linker)
    }

    /**
     * [Java/普通接口] 注册单类型委托 (一对一)
     * 内部会自动创建一个默认的 Linker，简化非 DSL 场景的使用。
     */
    fun <T : Any> attachDelegate(clazz: Class<T>, delegate: FusionDelegate<T, *>) {
        val linker = TypeRouter<T>()
        linker.map(Unit, delegate) // 默认 Key 为 Unit
        core.register(clazz, linker)
    }

    // ========================================================================================
    // 数据操作
    // ========================================================================================

    fun addInterceptor(interceptor: FusionDataInterceptor) {
        core.addInterceptor(interceptor)
    }

    /** 提交数据列表 (异步计算 Diff) */
    fun submitList(list: List<Any>?, commitCallback: Runnable? = null) {
        val rawList = list ?: emptyList()

        // 🔥 核武器启动点：进入数据管道
        // 得益于 Controller 的优化，如果没配置拦截器，这里开销为 0
        val processedList = core.processData(rawList)

        differ.submitList(processedList, commitCallback)
    }

    /** 获取当前数据列表 (只读) */
    val currentList: List<Any>
        get() = differ.currentList

    // ========================================================================================
    // RecyclerView.Adapter 实现委托
    // ========================================================================================

    override fun getItemCount(): Int = differ.currentList.size

    override fun getItemViewType(position: Int): Int {
        return core.getItemViewType(differ.currentList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return core.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.attachFusionStaggeredSupport(item) { core.getDelegate(it) }
        core.onBindViewHolder(holder, item, position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val item = differ.currentList[position]
            holder.attachFusionStaggeredSupport(item) { core.getDelegate(it) }
            core.onBindViewHolder(holder, item, position, payloads)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.attachFusionGridSupport(
            adapter = this,
            getItem = { pos -> if (pos in differ.currentList.indices) differ.currentList[pos] else null },
            getDelegate = { item -> core.getDelegate(item) }
        )
    }

    // --- 生命周期分发 (防止内存泄漏) ---
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) = core.onViewRecycled(holder)
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) = core.onViewAttachedToWindow(holder)
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) = core.onViewDetachedFromWindow(holder)
}