package com.fusion.adapter.paging

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.paging.filter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.Fusion
import com.fusion.adapter.RegistryOwner
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.diff.SmartDiffCallback
import com.fusion.adapter.extensions.attachFusionGridSupport
import com.fusion.adapter.extensions.attachFusionStaggeredSupport
import com.fusion.adapter.intercept.FusionPagingContext
import com.fusion.adapter.intercept.FusionPagingInterceptor
import com.fusion.adapter.internal.AdapterController
import com.fusion.adapter.internal.TypeRouter
import com.fusion.adapter.internal.ViewTypeRegistry
import com.fusion.adapter.internal.logD
import com.fusion.adapter.internal.logW

/**
 * [FusionPagingAdapter]
 * 专为 AndroidX Paging 3 设计的 Fusion 适配器。
 *
 * 架构特性：
 * 1. **全功能复刻**：支持 Fusion v2.2 的所有特性（O(1)路由、Smart Diff、Payload、生命周期托管）。
 * 2. **无缝兼容**：API 设计与 FusionListAdapter 完全一致，迁移零成本。
 * 3. **延迟代理**：解决了 PagingDataAdapter 构造函数需要 DiffCallback 但 Core 尚未初始化的死锁问题。
 *
 * 注意：建议在 PagingConfig 中设置 enablePlaceholders = false，因为 Fusion 强依赖类型系统。
 */
open class FusionPagingAdapter<T : Any> private constructor(
    private val diffProxy: DiffCallbackProxy<T>
) : PagingDataAdapter<T, RecyclerView.ViewHolder>(diffProxy), RegistryOwner {

    constructor() : this(DiffCallbackProxy())

    // 核心引擎
    private val core = AdapterController()

    // Paging 拦截器容器
    private val interceptors = ArrayList<FusionPagingInterceptor<T>>()

    // 缓存 Context，避免重复创建
    private val pagingContext = object : FusionPagingContext {
        override val registry: ViewTypeRegistry get() = core.registry
        override val config get() = Fusion.getConfig()
    }

    init {
        // [关键步骤] 构造完成后，将 Core 注入到 DiffCallbackProxy 中
        // 此时 Core 已初始化完毕，可以安全进行 Diff 计算
        diffProxy.attachCore(core)
    }

    // ------------------------------------------------------
    // 🔥 核心 Hook：拦截器管道 (Interceptor Pipeline)
    // ------------------------------------------------------

    fun addInterceptor(interceptor: FusionPagingInterceptor<T>) {
        interceptors.add(interceptor)
    }

    /**
     * 数据处理管道：应用所有拦截器 + 自动安全过滤
     */
    private fun sanitizePagingData(pagingData: PagingData<T>): PagingData<T> {
        var currentData = pagingData

        // 1. 执行用户自定义拦截器 (Map / Transform)
        // PagingData 是流式定义，这里的循环只是构建操作链，开销极小
        for (interceptor in interceptors) {
            currentData = interceptor.intercept(currentData, pagingContext)
        }

        // 2. 🔥 核心安全机制：自动过滤未注册类型 (Registry-Aware Safety)
        // 这是 Paging 版的 "Zero Side-Effect" 保证。
        // 直接利用 Paging3 的 filter 操作符，在后台线程执行过滤。
        currentData = currentData.filter { item ->
            val hasLinker = core.registry.hasLinker(item)
            if (!hasLinker) {
                // Debug 模式下打印日志，但不崩，因为 Paging 的流是异步的，崩在 Diff 线程很难查
                logW("FusionPaging") { "⚠️ Paging 自动剔除未注册数据: ${item.javaClass.simpleName}" }
            }
            hasLinker
        }

        return currentData
    }

    // ------------------------------------------------------
    // 🔥 submit 入口
    // ------------------------------------------------------

    suspend fun submit(pagingData: PagingData<T>) {
        val safeData = sanitizePagingData(pagingData)
        super.submitData(safeData)
    }

    fun submit(lifecycle: Lifecycle, pagingData: PagingData<T>) {
        val safeData = sanitizePagingData(pagingData)
        super.submitData(lifecycle, safeData)
    }

    // -----------------------------------------------------------------------
    // [底层 API] - 供 Java 用户使用，或供 Kotlin 扩展函数内部调用
    // -----------------------------------------------------------------------

    /**
     * [Low-Level API] 挂载路由连接器
     * 原名: registerLinker
     * 语义: 将构建好的 Linker 挂载到 Core 引擎中
     */
    override fun <T : Any> attachLinker(clazz: Class<T>, linker: TypeRouter<T>) {
        core.register(clazz, linker)
    }

    /**
     * [Low-Level API] 挂载单一委托
     * 原名: register
     * 语义: 将单一 Delegate 挂载到 Core 引擎中
     */
    fun <T : Any> attachDelegate(clazz: Class<T>, delegate: FusionDelegate<T, *>) {
        val linker = TypeRouter<T>()
        linker.map(Unit, delegate)
        core.register(clazz, linker)
    }

    // ========================================================================================
    // 1. 注册 API (与 FusionListAdapter 保持 100% 一致)
    // ========================================================================================

    /**
     * [KTX 底层接口] 注册路由连接器
     */
    fun <T : Any> register(clazz: Class<T>, linker: TypeRouter<T>) {
        core.register(clazz, linker)
    }

    /**
     * [Java 快捷接口] 注册单类型委托
     */
    fun <T : Any> register(clazz: Class<T>, delegate: FusionDelegate<T, *>) {
        val linker = TypeRouter<T>()
        linker.map(Unit, delegate)
        core.register(clazz, linker)
    }

    // ========================================================================================
    // 2. 核心桥接 (Core Bridge)
    // ========================================================================================

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position) ?: return 0
        return core.getItemViewType(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return core.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        holder.attachFusionStaggeredSupport(item) { core.getDelegate(it) }
        if (item != null) {
            // 使用空列表避免对象分配
            core.onBindViewHolder(holder, item, position)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val item = getItem(position)
            // 局部刷新也要处理布局
            holder.attachFusionStaggeredSupport(item) { core.getDelegate(it) }
            if (item != null) {
                core.onBindViewHolder(holder, item, position, payloads)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.attachFusionGridSupport(
            adapter = this,
            getItem = { pos -> this.getItem(pos) }, // Paging 的 getItem
            getDelegate = { item -> core.getDelegate(item) }
        )
    }

    // --- 生命周期分发 ---

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) =
        core.onViewRecycled(holder)

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) =
        core.onViewAttachedToWindow(holder)

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) =
        core.onViewDetachedFromWindow(holder)

    // ========================================================================================
    // 3. 内部代理类 (解决构造函数依赖循环)
    // ========================================================================================

    /**
     * [DiffCallbackProxy]
     * 作为一个中间层传给 super()，并在 init 中连接 Core。
     */
    private class DiffCallbackProxy<T : Any> : DiffUtil.ItemCallback<T>() {

        private var core: AdapterController? = null

        fun attachCore(core: AdapterController) {
            this.core = core
        }

        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            // ID 判断是无状态的，直接调用静态策略
            logD("Paging") { "🔥🔥🔥 [Diff Fatal] Core is NULL! Fallback to legacy check." }
            return core?.areItemsTheSame(oldItem, newItem)
                ?: SmartDiffCallback.areItemsTheSame(oldItem, newItem)
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            // 内容判断依赖 Delegate，需要 Core
            return core?.areContentsTheSame(oldItem, newItem)
                ?: (oldItem == newItem) // 兜底：如果 Core 还没注入(理论上不会)，降级为 equals
        }

        override fun getChangePayload(oldItem: T, newItem: T): Any? {
            return core?.getChangePayload(oldItem, newItem)
        }
    }
}
