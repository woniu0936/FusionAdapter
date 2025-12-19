import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.paging.*
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.Fusion
import com.fusion.adapter.FusionConfig
import com.fusion.adapter.RegistryOwner
import com.fusion.adapter.diff.StableId
import com.fusion.adapter.extensions.attachFusionStaggeredSupport
import com.fusion.adapter.intercept.FusionContext
import com.fusion.adapter.intercept.FusionPagingContext
import com.fusion.adapter.intercept.FusionPagingInterceptor
import com.fusion.adapter.internal.AdapterController
import com.fusion.adapter.internal.TypeRouter
import com.fusion.adapter.internal.ViewTypeRegistry
import com.fusion.adapter.internal.logW
import com.fusion.adapter.paging.FusionPlaceholderViewHolder
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.CopyOnWriteArrayList

/**
 * FusionPagingAdapter: 顶级分页适配器 (Final Version)
 *
 * 核心特性：
 * 1. [Safety] 100% 复用 AdapterController 的 Diff 安全逻辑，防止 "ID相同类型不同" 导致的 Crash。
 * 2. [Performance] 支持 StableId 接口，大幅提升 RecyclerView 更新性能。
 * 3. [Architecture] 严格遵循 Paging3 代理模式，补全了 LoadState/ConcatAdapter 支持。
 */
open class FusionPagingAdapter<T : Any> : RecyclerView.Adapter<RecyclerView.ViewHolder>(), RegistryOwner {

    // 复用已有的核心引擎
    private val core = AdapterController()

    // Paging 数据流拦截器 (注意：这是针对 PagingData 流的拦截，区别于 Core 的 List 拦截)
    private val interceptors = CopyOnWriteArrayList<FusionPagingInterceptor<T>>()

    // 上下文环境
    private val pagingContext = object : FusionPagingContext {
        override val registry: ViewTypeRegistry get() = core.registry
        override val config: FusionConfig get() = Fusion.getConfig()
    }

    // 内部代理适配器
    private val helperAdapter = PagingHelperAdapter()

    init {

        // [数据观察者桥接]
        // 将 Paging 的刷新通知转发给 RecyclerView
        helperAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() = notifyDataSetChanged()
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = notifyItemRangeChanged(positionStart, itemCount)
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) = notifyItemRangeChanged(positionStart, itemCount, payload)
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = notifyItemRangeInserted(positionStart, itemCount)
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = notifyItemRangeRemoved(positionStart, itemCount)
            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) = notifyItemMoved(fromPosition, toPosition)

            // [关键] 同步状态恢复策略 (避免旋转屏幕位置丢失)
            override fun onStateRestorationPolicyChanged() {
                this@FusionPagingAdapter.stateRestorationPolicy = helperAdapter.stateRestorationPolicy
            }
        })
    }

    // ------------------------------------------------------
    // 🔥 核心 API (Paging3 标准)
    // ------------------------------------------------------

    suspend fun submitData(pagingData: PagingData<T>) {
        helperAdapter.submitData(applyInterceptors(pagingData))
    }

    fun submitData(lifecycle: Lifecycle, pagingData: PagingData<T>) {
        helperAdapter.submitData(lifecycle, applyInterceptors(pagingData))
    }

    fun retry() = helperAdapter.retry()
    fun refresh() = helperAdapter.refresh()

    // 获取快照 (List)
    fun snapshot(): ItemSnapshotList<T> = helperAdapter.snapshot()

    /**
     * 安全获取 Item (不触发加载)
     * 适用于 ClickListener 或 Analytics
     */
    fun peek(index: Int): T? = helperAdapter.peek(index)

    // ------------------------------------------------------
    // 🛠 Adapter 实现细节 (委托给 Core)
    // ------------------------------------------------------

    override fun getItemCount(): Int = helperAdapter.itemCount

    override fun getItemViewType(position: Int): Int {
        // 必须调用 getItemInternal 以触发 Paging 加载 (如果需要)
        // 注意：Paging3 使用 null 表示 Placeholder
        val item = helperAdapter.getItemInternal(position) ?: return ViewTypeRegistry.TYPE_PLACEHOLDER

        // 直接调用 Core，Registry 内部有缓存 (O(1)) 和兜底逻辑
        return core.getItemViewType(item)
    }

    override fun getItemId(position: Int): Long {
        // 仅在 setHasStableIds(true) 时有效
        if (!hasStableIds()) return RecyclerView.NO_ID

        // 使用 peek 避免为了获取 ID 而触发网络请求
        val item = helperAdapter.peek(position) ?: return RecyclerView.NO_ID

        // [对接你的 StableId 接口]
        return if (item is StableId) {
            // 假设 stableId 是 Long 或 Int。如果是 String 的 hashcode 可能会碰撞，需注意
            (item.stableId as? Long) ?: item.stableId.hashCode().toLong()
        } else {
            RecyclerView.NO_ID
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // 专门处理 Placeholder，Core 可能没有注册这个负数 ID
        if (viewType == ViewTypeRegistry.TYPE_PLACEHOLDER) {
            // 如果你有专门的 Placeholder 布局，可以在这里 create。
            // 否则需要一个空的 ViewHolder 防止 Crash
            return FusionPlaceholderViewHolder(parent)
        }
        return core.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = helperAdapter.getItemInternal(position)
        if (item != null) {
            // 绑定 StaggeredGrid 逻辑 (如果有)
            holder.attachFusionStaggeredSupport(item) { core.getDelegate(it) }
            // Core 的 onBind 已经包含了 "delegate == null" 的检查，这里直接传进去很安全
            core.onBindViewHolder(holder, item, position)
        } else {
            // 处理 Placeholder 的绑定 (可选)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val item = helperAdapter.getItemInternal(position)
            if (item != null) {
                holder.attachFusionStaggeredSupport(item) { core.getDelegate(it) }
                core.onBindViewHolder(holder, item, position, payloads)
            }
        }
    }

    fun addInterceptor(interceptor: FusionPagingInterceptor<T>) {
        interceptors.add(interceptor)
    }

    // ------------------------------------------------------
    // ⚙️ 内部逻辑
    // ------------------------------------------------------

    private fun applyInterceptors(pagingData: PagingData<T>): PagingData<T> {
        var data = pagingData
        // 执行 PagingData 流拦截器
        interceptors.forEach { interceptor ->
            data = interceptor.intercept(data, pagingContext)
        }

        // [Thread Safe] Registry 使用 ConcurrentHashMap，这里在 Diff 线程运行是安全的
        // 自动过滤未注册的数据，防止渲染层 Crash
        return data.filter { item ->
            val supported = core.registry.hasLinker(item)
            if (!supported && pagingContext.isDebug) {
                // 复用 Core 的日志风格
                logW("Fusion") { "⚠️ [Paging Filter] 剔除未注册类型: ${item.javaClass.simpleName}" }
            }
            supported
        }
    }

    override fun <T : Any> attachLinker(clazz: Class<T>, linker: TypeRouter<T>) {
        core.register(clazz, linker)
    }

    // ------------------------------------------------------
    // 🧩 LoadState / Header / Footer 支持
    // ------------------------------------------------------

    val loadStateFlow: Flow<CombinedLoadStates> get() = helperAdapter.loadStateFlow

    fun addLoadStateListener(listener: (CombinedLoadStates) -> Unit) = helperAdapter.addLoadStateListener(listener)

    fun removeLoadStateListener(listener: (CombinedLoadStates) -> Unit) = helperAdapter.removeLoadStateListener(listener)

    fun addOnPagesUpdatedListener(listener: () -> Unit) = helperAdapter.addOnPagesUpdatedListener(listener)

    fun removeOnPagesUpdatedListener(listener: () -> Unit) = helperAdapter.removeOnPagesUpdatedListener(listener)

    /**
     * 正确实现 ConcatAdapter 组装
     * 必须把 `this` (FusionPagingAdapter) 放进去，而不是 helperAdapter
     */
    fun withLoadStateHeaderAndFooter(
        header: LoadStateAdapter<*>,
        footer: LoadStateAdapter<*>
    ): ConcatAdapter {
        addLoadStateListener { loadStates ->
            header.loadState = loadStates.prepend
            footer.loadState = loadStates.append
        }
        return ConcatAdapter(header, this, footer)
    }

    fun withLoadStateFooter(footer: LoadStateAdapter<*>): ConcatAdapter {
        addLoadStateListener { loadStates ->
            footer.loadState = loadStates.append
        }
        return ConcatAdapter(this, footer)
    }

    // ------------------------------------------------------
    // 🔒 内部代理类
    // ------------------------------------------------------

    private inner class PagingHelperAdapter : PagingDataAdapter<T, RecyclerView.ViewHolder>(
        // [Best Practice] 直接复用 Core 的 Diff 逻辑
        object : DiffUtil.ItemCallback<T>() {
            /**
             * 必须使用 core.areItemsTheSame!
             * 因为 AdapterController 会先检查 ViewType。
             * 如果 T 的 ID 没变，但 Class 变了，core 会返回 false（正确）。
             * 如果只比较 ID，RecyclerView 可能会尝试用旧的 ViewHolder 渲染新类型的数据，导致 Crash。
             */
            override fun areItemsTheSame(old: T, new: T) = core.areItemsTheSame(old, new)

            override fun areContentsTheSame(old: T, new: T) = core.areContentsTheSame(old, new)

            override fun getChangePayload(old: T, new: T) = core.getChangePayload(old, new)
        }
    ) {
        // 暴露受保护的方法
        fun getItemInternal(position: Int): T? = super.getItem(position)

        // 屏蔽 UI 构建能力，防止误用
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            throw IllegalStateException("Proxy Error: Helper adapter should never create views.")
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            throw IllegalStateException("Proxy Error: Helper adapter should never bind views.")
        }
    }
}