package com.fusion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
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
import com.fusion.adapter.placeholder.FusionPlaceholderDelegate
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger

/**
 * [FusionAdapter] - 手动挡
 *
 * 不使用 DiffUtil，完全由开发者控制数据的刷新。
 * 适用于：
 * 1. 静态列表 (Settings 页面)
 * 2. 追求极致性能的简单列表
 * 3. 需要精确控制 notifyItemMoved 等动画的场景 (拖拽排序)
 */
open class FusionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), RegistryOwner {

    // 核心引擎
    @PublishedApi
    internal val core = AdapterController()

    // 内部数据持有
    private var items: List<Any> = Collections.emptyList()

    /**
     * 获取当前实际渲染的数据列表 (Read-Only)
     * 这是经过所有拦截器处理、安全检查后的最终列表。
     * 即 RecyclerView 真正看到的列表。
     */
    val currentItems: List<Any>
        get() = Collections.unmodifiableList(items)

    private val maxScheduledGeneration = AtomicInteger(0)

    private var pendingTask: FusionExecutors.Cancellable? = null

    // Java 回调接口
    fun interface OnItemsChangedListener {
        fun onItemsChanged()
    }

    init {
        if (Fusion.getConfig().defaultStableId) {
            setHasStableIds(true)
        }
    }

    // ========================================================================================
    // 注册接口 (API)
    // ========================================================================================

    override fun <T : Any> registerRouter(clazz: Class<T>, router: TypeRouter<T>) {
        checkStableIdRequirement(this, clazz, router.getAllDelegates(), core)
        core.register(clazz, router)
    }

    override fun <T : Any> registerDelegate(clazz: Class<T>, delegate: FusionDelegate<T, *>) {
        checkStableIdRequirement(this, clazz, listOf(delegate), core)
        val router = TypeRouter.create(delegate)
        core.register(clazz, router)
    }

    // ========================================================================================
    // 数据操作 (Manual)
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
     * ✅ Java 兼容：注册占位符实例
     * Java 用户可以通过 new FusionPlaceholderDelegate<Binding>() { ... } 来调用
     */
    fun registerPlaceholder(delegate: FusionPlaceholderDelegate<*>) {
        core.registerPlaceholder(delegate)
    }

    /**
     * [同步] 设置数据
     * 适用于小数据量或已经在外部处理好线程的场景。
     */
    @MainThread
    fun setItems(newItems: List<Any>) {
        // 版本号自增，立即使之前的异步任务失效
        maxScheduledGeneration.incrementAndGet()
        pendingTask?.cancel()

        val safeItems = core.sanitize(newItems)
        updateInternal(safeItems)
    }

    /**
     * [异步] 设置数据 (推荐)
     * 自动处理线程切换、数据清洗、并发乱序和内存泄漏问题。
     */
    fun setItemsAsync(newItems: List<Any>, listener: OnItemsChangedListener? = null) {
        // 1. 取消上一个未完成的任务
        pendingTask?.cancel()

        // 2. 获取当前代次
        val generation = maxScheduledGeneration.incrementAndGet()

        // 3. 后台清洗
        pendingTask = FusionExecutors.runInBackground {
            // 如果任务被 cancel(true)，sanitize 内部会感知并提前退出
            val safeItems = core.sanitize(newItems)

            // 4. 主线程回调
            FusionExecutors.runOnMain {
                // 只有代次匹配，才应用结果
                if (maxScheduledGeneration.get() == generation) {
                    updateInternal(safeItems)
                    listener?.onItemsChanged()
                    pendingTask = null
                }
            }
        }
    }

    private fun updateInternal(safeItems: List<Any>) {
        this.items = safeItems
        notifyDataSetChanged()
    }

    fun addItems(newItems: List<Any>) {
        val safeItems = core.sanitize(newItems)
        if (safeItems.isNotEmpty()) {
            val newList = ArrayList(this.items)
            val startPosition = newList.size
            newList.addAll(safeItems)
            this.items = Collections.unmodifiableList(newList)
            notifyItemRangeInserted(startPosition, safeItems.size)
        }
    }

    fun insertItem(position: Int, item: Any) {
        val safeList = core.sanitize(listOf(item))
        if (safeList.isNotEmpty()) {
            if (position < 0 || position > items.size) return
            val newList = ArrayList(this.items)
            newList.addAll(position, safeList)
            this.items = Collections.unmodifiableList(newList)
            notifyItemRangeInserted(position, safeList.size)
        }
    }

    fun removeItem(position: Int) {
        if (position in items.indices) {
            val newList = ArrayList(this.items)
            newList.removeAt(position)
            this.items = Collections.unmodifiableList(newList)
            notifyItemRemoved(position)
        }
    }

    fun getItem(position: Int): Any = items[position]

    // ========================================================================================
    // RecyclerView.Adapter 实现委托
    // ========================================================================================

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return core.getItemViewType(items[position])
    }

    override fun getItemId(position: Int): Long {
        // 1. 性能开关
        if (!hasStableIds()) return RecyclerView.NO_ID

        // 2. 边界检查
        if (position !in items.indices) return RecyclerView.NO_ID

        return core.getItemId(items[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return core.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        holder.attachFusionStaggeredSupport(item) { core.getDelegate(it) }
        core.onBindViewHolder(holder, item, position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val item = items[position]
            // ★ 即使是局部刷新，也要确保布局参数正确 (以防 ViewHolder 复用或布局变动)
            holder.attachFusionStaggeredSupport(item) { core.getDelegate(it) }
            // 局部刷新
            core.onBindViewHolder(holder, item, position, payloads)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.attachFusionGridSupport(
            adapter = this,
            getItem = { pos -> if (pos in items.indices) items[pos] else null },
            getDelegate = { item -> core.getDelegate(item) }
        )
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        // 页面销毁时，立即取消 pending 任务，断开 Adapter 引用链
        pendingTask?.cancel()
        pendingTask = null
    }

    // --- 生命周期分发 ---
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) = core.onViewRecycled(holder)
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) = core.onViewAttachedToWindow(holder)
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) = core.onViewDetachedFromWindow(holder)

}