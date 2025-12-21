package com.fusion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.delegate.BindingHolder
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.delegate.FusionPlaceholderDelegate
import com.fusion.adapter.delegate.LayoutHolder
import com.fusion.adapter.extensions.attachFusionGridSupport
import com.fusion.adapter.extensions.attachFusionStaggeredSupport
import com.fusion.adapter.internal.AdapterController
import com.fusion.adapter.internal.TypeRouter
import com.fusion.adapter.internal.checkStableIdRequirement
import com.fusion.adapter.internal.mapToRecyclerViewId

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
open class FusionListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), RegistryOwner {

    // 核心引擎
    @PublishedApi
    internal val core = AdapterController()

    init {
        if (Fusion.getConfig().defaultStableId) {
            setHasStableIds(true)
        }
    }

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
        checkStableIdRequirement(this, clazz, linker.getAllDelegates())
        core.register(clazz, linker)
    }

    /**
     * [Java/普通接口] 注册单类型委托 (一对一)
     * 内部会自动创建一个默认的 Linker，简化非 DSL 场景的使用。
     */
    fun <T : Any> attachDelegate(clazz: Class<T>, delegate: FusionDelegate<T, *>) {
        checkStableIdRequirement(this, clazz, listOf(delegate))
        val linker = TypeRouter<T>()
        linker.map(Unit, delegate) // 默认 Key 为 Unit
        core.register(clazz, linker)
    }

    // ========================================================================================
    // 数据操作
    // ========================================================================================

    /**
     * 注册占位符 (ViewBinding 模式)
     */
    inline fun <reified VB : ViewBinding> registerPlaceholder(
        noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        crossinline onBind: (VB) -> Unit = {}
    ) {
        val delegate = object : FusionPlaceholderDelegate<BindingHolder<VB>>() {
            override fun onCreatePlaceholderViewHolder(parent: ViewGroup): BindingHolder<VB> {
                return BindingHolder(inflate(LayoutInflater.from(parent.context), parent, false))
            }

            override fun onBindPlaceholder(holder: BindingHolder<VB>) {
                onBind(holder.binding)
            }
        }
        core.registerPlaceholder(delegate)
    }

    /**
     * 注册占位符 (LayoutRes 模式)
     * 使用 LayoutHolder，与库中的 LayoutDelegate 保持一致。
     *
     * @param layoutResId 布局资源 ID
     * @param onBind 可选的绑定回调（用于初始化 View，如开始动画）
     */
    fun registerPlaceholder(
        @LayoutRes layoutResId: Int,
        onBind: (LayoutHolder.() -> Unit)? = null
    ) {
        val delegate = object : FusionPlaceholderDelegate<LayoutHolder>() {
            override fun onCreatePlaceholderViewHolder(parent: ViewGroup): LayoutHolder {
                val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
                return LayoutHolder(view)
            }

            override fun onBindPlaceholder(holder: LayoutHolder) {
                onBind?.invoke(holder)
            }
        }
        core.registerPlaceholder(delegate)
    }

    /**
     * ✅ Java 兼容
     */
    fun registerPlaceholder(delegate: FusionPlaceholderDelegate<*>) {
        core.registerPlaceholder(delegate)
    }

    fun submitList(list: List<Any>?, commitCallback: Runnable? = null) {
        val rawList = list ?: emptyList()
        // ✅ 核心：在提交给 Diff 之前清洗数据
        val safeList = core.sanitize(rawList)
        differ.submitList(safeList, commitCallback)
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
        // 1. 性能守卫：未开启功能直接返回，不消耗任何性能
        if (!hasStableIds()) return RecyclerView.NO_ID

        // 2. 边界检查 (虽少见，但在并发更新或 Diff 计算间隙可能发生)
        val list = currentList
        if (position !in list.indices) return RecyclerView.NO_ID

        // 3. 获取数据与 Delegate
        val item = list[position]
        val delegate = core.getDelegate(item) ?: return RecyclerView.NO_ID

        // 4. 获取原始 ID (DSL 配置)
        @Suppress("UNCHECKED_CAST")
        val rawId = delegate.getStableId(item)

        // --- 运行时安全兜底 (Safety Net) ---
        if (rawId == null) {
            // 这种情况理论上在 Debug 模式下会被注册检查拦截。
            // 但在 Release 模式下，如果 setHasStableIds(false) 失败，
            // 代码会走到这里。此时绝对不能返回 NO_ID 或重复 ID。

            // 策略：使用 System.identityHashCode。
            // 它在对象生命周期内是不变的，且冲突概率极低。
            // 虽然这会让动画失效（因为每次 new 对象 ID 都变），但它能 100% 保证不 Crash。
            return System.identityHashCode(item).toLong()
        }

        // 5. 极致转换
        return mapToRecyclerViewId(rawId)
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