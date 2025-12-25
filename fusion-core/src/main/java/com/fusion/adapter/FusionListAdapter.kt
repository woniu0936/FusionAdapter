package com.fusion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.delegate.BindingHolder
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.delegate.LayoutHolder
import com.fusion.adapter.extensions.attachFusionGridSupport
import com.fusion.adapter.extensions.attachFusionStaggeredSupport
import com.fusion.adapter.internal.AdapterController
import com.fusion.adapter.internal.FusionExecutors
import com.fusion.adapter.internal.TypeRouter
import com.fusion.adapter.internal.checkStableIdRequirement
import com.fusion.adapter.placeholder.FusionPlaceholder
import com.fusion.adapter.placeholder.FusionPlaceholderDelegate
import com.fusion.adapter.placeholder.SkeletonOwner
import com.fusion.adapter.placeholder.SkeletonDsl

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
open class FusionListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), RegistryOwner, SkeletonOwner {

    // 核心引擎
    @PublishedApi
    internal val core = AdapterController()

    // 注入 FusionExecutors，使用标准 Java Executor 接口
    private val differ: AsyncListDiffer<Any> = AsyncListDiffer(
        ListUpdateCallbackWrapper(this),
        AsyncDifferConfig.Builder(FusionDiffCallback(core))
            .setBackgroundThreadExecutor(FusionExecutors.backgroundExecutorAdapter)
            .build()
    )

    init {
        if (Fusion.getConfig().defaultStableId) {
            setHasStableIds(true)
        }
    }

    // ========================================================================================
    // 内部类
    // ========================================================================================

    private class FusionDiffCallback(private val core: AdapterController) : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(old: Any, new: Any) = core.areItemsTheSame(old, new)
        override fun areContentsTheSame(old: Any, new: Any) = core.areContentsTheSame(old, new)
        override fun getChangePayload(old: Any, new: Any) = core.getChangePayload(old, new)
    }

    private class ListUpdateCallbackWrapper(private val adapter: RecyclerView.Adapter<*>) : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) = adapter.notifyItemRangeInserted(position, count)
        override fun onRemoved(position: Int, count: Int) = adapter.notifyItemRangeRemoved(position, count)
        override fun onMoved(fromPosition: Int, toPosition: Int) = adapter.notifyItemMoved(fromPosition, toPosition)
        override fun onChanged(position: Int, count: Int, payload: Any?) = adapter.notifyItemRangeChanged(position, count, payload)
    }

    // ========================================================================================
    // 注册接口 (API)
    // ========================================================================================

    /**
     * [KTX Interface] 注册路由连接器
     */
    override fun <T : Any> registerRouter(clazz: Class<T>, router: TypeRouter<T>) {
        checkStableIdRequirement(this, clazz, router.getAllDelegates(), core)
        core.register(clazz, router)
    }

    /**
     * [Java/Common Interface] 注册单类型委托 (一对一)
     * 自动包装为不可变的 TypeRouter。
     */
    override fun <T : Any> registerDelegate(clazz: Class<T>, delegate: FusionDelegate<T, *>) {
        checkStableIdRequirement(this, clazz, listOf(delegate), core)
        val router = TypeRouter.create(delegate)
        core.register(clazz, router)
    }

    override fun registerSkeletonDelegate(delegate: FusionPlaceholderDelegate<*>) {
        core.registerSkeleton(delegate)
    }

    override fun registerSkeleton(@LayoutRes layoutResId: Int) {
        val delegate = object : FusionPlaceholderDelegate<LayoutHolder>() {
            override fun onCreatePlaceholderViewHolder(parent: ViewGroup): LayoutHolder {
                val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
                return LayoutHolder(view)
            }
        }
        core.registerSkeleton(delegate)
    }

    override fun <VB : ViewBinding> registerSkeleton(
        inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        block: (SkeletonDsl<VB>.() -> Unit)?
    ) {
        val dsl = SkeletonDsl<VB>()
        block?.invoke(dsl)

        val delegate = object : FusionPlaceholderDelegate<BindingHolder<VB>>() {
            override fun onCreatePlaceholderViewHolder(parent: ViewGroup): BindingHolder<VB> {
                return BindingHolder(inflate(LayoutInflater.from(parent.context), parent, false))
            }

            override fun onBindPlaceholder(holder: BindingHolder<VB>) {
                dsl.config.onBind?.invoke(holder.binding, FusionPlaceholder(), 0)
            }
        }
        core.registerSkeleton(delegate)
    }

    // ========================================================================================
    // 数据操作
    // ========================================================================================

    /**
     * 提交数据 (Double Async)
     * 利用 FusionExecutors 在后台线程完成 "Sanitize" 和 "Diff" 两步操作。
     */
    fun submitList(list: List<Any>?, commitCallback: Runnable? = null) {
        val rawList = list ?: emptyList()

        // 1. 先投递到后台线程进行清洗
        FusionExecutors.runInBackground {
            val safeList = core.sanitize(rawList)

            // 2. 将清洗后的数据交给 Differ
            // Differ 内部会检测到当前已经在 BackgroundExecutor 上，
            // 因此会直接继续进行 Diff 计算，高效且无额外线程切换。
            differ.submitList(safeList, commitCallback)
        }
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

    override fun getItemId(position: Int): Long {
        if (!hasStableIds()) return RecyclerView.NO_ID

        val list = currentList
        if (position !in list.indices) return RecyclerView.NO_ID

        return core.getItemId(list[position])
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