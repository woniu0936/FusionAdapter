import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.lifecycle.Lifecycle
import androidx.paging.CombinedLoadStates
import androidx.paging.ItemSnapshotList
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.paging.filter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.Fusion
import com.fusion.adapter.RegistryOwner
import com.fusion.adapter.delegate.BindingHolder
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.delegate.LayoutHolder
import com.fusion.adapter.exception.UnregisteredTypeException
import com.fusion.adapter.extensions.attachFusionStaggeredSupport
import com.fusion.adapter.internal.AdapterController
import com.fusion.adapter.internal.TypeRouter
import com.fusion.adapter.internal.ViewTypeRegistry
import com.fusion.adapter.internal.checkStableIdRequirement
import com.fusion.adapter.internal.mapToRecyclerViewId
import com.fusion.adapter.placeholder.FusionPlaceholder
import com.fusion.adapter.placeholder.FusionPlaceholderDelegate
import kotlinx.coroutines.flow.Flow

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
    @PublishedApi
    internal val core = AdapterController()

    // 内部代理适配器
    private val helperAdapter = PagingHelperAdapter()

    init {
        if (Fusion.getConfig().defaultStableId) {
            setHasStableIds(true)
        }
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

    override fun <T : Any> registerRouter(clazz: Class<T>, router: TypeRouter<T>) {
        checkStableIdRequirement(this, clazz, router.getAllDelegates(), core)
        core.register(clazz, router)
    }

    override fun <T : Any> registerDelegate(clazz: Class<T>, delegate: FusionDelegate<T, *>) {
        checkStableIdRequirement(this, clazz, listOf(delegate), core)
        val router = TypeRouter.create(delegate)
        core.register(clazz, router)
    }

    // ------------------------------------------------------
    // 🔥 核心 API (Paging3 标准)
    // ------------------------------------------------------

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

    suspend fun submitData(pagingData: PagingData<T>) {
        helperAdapter.submitData(sanitizePagingData(pagingData))
    }

    fun submitData(lifecycle: Lifecycle, pagingData: PagingData<T>) {
        helperAdapter.submitData(lifecycle, sanitizePagingData(pagingData))
    }

    /**
     * ✅ Paging 数据清洗
     * 利用 Paging 3 的 filter 操作符，在后台线程过滤掉不支持的数据类型。
     * 只有注册过（或有 Fallback）的数据才会进入 Diff 流程。
     */
    private fun sanitizePagingData(pagingData: PagingData<T>): PagingData<T> {
        val config = Fusion.getConfig()
        val isDebug = config.isDebug

        return pagingData.filter { item ->
            val isSupported = core.viewTypeRegistry.isSupported(item)

            if (isSupported) {
                true // 保留
            } else {
                val exception = UnregisteredTypeException(item)
                if (isDebug) {
                    // 🚨 Debug 模式：Paging 中抛出异常会传播到 LoadState.Error
                    // 开发者会在 UI 上看到加载失败，Logcat 会有红字 StackTrace
                    throw exception
                } else {
                    // 🛡️ Release 模式：上报并丢弃
                    config.errorListener?.onError(item, exception)
                    false // 丢弃
                }
            }
        }
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
        if (!hasStableIds()) return RecyclerView.NO_ID

        // Paging 特有：peek 不触发加载
        val item = helperAdapter.peek(position) ?: return RecyclerView.NO_ID
        val delegate = core.getDelegate(item) ?: return RecyclerView.NO_ID

        @Suppress("UNCHECKED_CAST")
        val rawKey = core.getStableId(item, delegate as FusionDelegate<Any, *>)

        if (rawKey == null) {
            return System.identityHashCode(item).toLong()
        }

        return mapToRecyclerViewId(rawKey)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return core.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = helperAdapter.getItemInternal(position)
        // 统一瀑布流支持逻辑
        // 如果 item 是 null，我们用 FusionPlaceholder 单例代替它去查询 Delegate
        // 这样骨架屏也能通过重写 isFullSpan() 来控制布局了
        val layoutItem = item ?: FusionPlaceholder

        holder.attachFusionStaggeredSupport(layoutItem) { queryItem ->
            if (queryItem === FusionPlaceholder) {
                core.viewTypeRegistry.getPlaceholderDelegate()
            } else {
                core.getDelegate(queryItem)
            }
        }
        if (item == null) {
            // 绑定 Placeholder
            val delegate = core.viewTypeRegistry.getPlaceholderDelegate()
            delegate?.onBindViewHolder(holder, Unit, position, mutableListOf())
        } else {
            // 绑定正常数据
            core.onBindViewHolder(holder, item, position)
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
