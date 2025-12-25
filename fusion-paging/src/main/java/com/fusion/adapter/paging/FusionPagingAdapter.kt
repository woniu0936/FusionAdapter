package com.fusion.adapter.paging

import android.view.LayoutInflater
import android.view.View
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
import com.fusion.adapter.FusionRegistry
import com.fusion.adapter.delegate.BindingHolder
import com.fusion.adapter.delegate.BindingInflater
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.delegate.LayoutHolder
import com.fusion.adapter.exception.UnregisteredTypeException
import com.fusion.adapter.extensions.setupGridSupport
import com.fusion.adapter.extensions.setupStaggeredSupport
import com.fusion.adapter.internal.FusionCore
import com.fusion.adapter.internal.TypeDispatcher
import com.fusion.adapter.internal.ViewTypeRegistry
import com.fusion.adapter.placeholder.FusionPlaceholder
import com.fusion.adapter.placeholder.FusionPlaceholderDelegate
import com.fusion.adapter.placeholder.PlaceholderConfigurator
import com.fusion.adapter.placeholder.PlaceholderDefinitionScope
import com.fusion.adapter.placeholder.PlaceholderRegistry
import kotlinx.coroutines.flow.Flow

/**
 * [FusionPagingAdapter]
 */
open class FusionPagingAdapter<T : Any> : RecyclerView.Adapter<RecyclerView.ViewHolder>(), FusionRegistry, PlaceholderRegistry {

    @PublishedApi
    internal val core = FusionCore()
    private val helperAdapter = PagingHelperAdapter()

    init {
        if (Fusion.getConfig().defaultItemIdEnabled) {
            setHasStableIds(true)
        }
        helperAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() = notifyDataSetChanged()
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = notifyItemRangeChanged(positionStart, itemCount)
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) = notifyItemRangeChanged(positionStart, itemCount, payload)
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = notifyItemRangeInserted(positionStart, itemCount)
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = notifyItemRangeRemoved(positionStart, itemCount)
            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) = notifyItemMoved(fromPosition, toPosition)
            override fun onStateRestorationPolicyChanged() {
                this@FusionPagingAdapter.stateRestorationPolicy = helperAdapter.stateRestorationPolicy
            }
        })
    }

    override fun <T : Any> registerDispatcher(clazz: Class<T>, dispatcher: TypeDispatcher<T>) {
        core.registerDispatcher(clazz, dispatcher)
    }

    override fun <T : Any> register(clazz: Class<T>, delegate: FusionDelegate<T, *>) {
        val dispatcher = TypeDispatcher.create(delegate)
        core.registerDispatcher(clazz, dispatcher)
    }

    override fun registerPlaceholder(delegate: FusionPlaceholderDelegate<*>) {
        core.registerPlaceholder(delegate)
    }

    override fun registerPlaceholder(@LayoutRes layoutResId: Int) {
        val delegate = object : FusionPlaceholderDelegate<LayoutHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup): LayoutHolder =
                LayoutHolder(LayoutInflater.from(parent.context).inflate(layoutResId, parent, false))

            override fun onBindPlaceholder(holder: LayoutHolder) {}
        }
        core.registerPlaceholder(delegate)
    }

    override fun <VB : ViewBinding> registerPlaceholder(inflate: (LayoutInflater, ViewGroup, Boolean) -> VB, block: (PlaceholderDefinitionScope<VB>.() -> Unit)?) {
        val scope = PlaceholderDefinitionScope<VB>(); block?.invoke(scope)
        val delegate = object : FusionPlaceholderDelegate<BindingHolder<VB>>() {

            override fun onCreateViewHolder(parent: ViewGroup): BindingHolder<VB> = BindingHolder(inflate(LayoutInflater.from(parent.context), parent, false))

            override fun onBindPlaceholder(holder: BindingHolder<VB>) {
                val itemConfiguration = scope.getConfiguration()
                itemConfiguration.onBind?.invoke(holder.binding, FusionPlaceholder(), 0)
            }
        }
        core.registerPlaceholder(delegate)
    }

    /**
     * Java API 实现
     */
    override fun <VB : ViewBinding> registerPlaceholder(
        inflater: BindingInflater<VB>,
        configurator: PlaceholderConfigurator<VB>?
    ) {
        // 1. 创建 Scope
        val scope = PlaceholderDefinitionScope<VB>()
        // 2. 让 Java 用户配置 Scope
        configurator?.configure(scope)

        // 3. 创建 Delegate
        val delegate = object : FusionPlaceholderDelegate<BindingHolder<VB>>() {
            override fun onCreateViewHolder(parent: ViewGroup): BindingHolder<VB> {
                // 使用 Java 传入的 BindingInflater
                val binding = inflater.inflate(LayoutInflater.from(parent.context), parent, false)
                return BindingHolder(binding)
            }

            override fun onBindPlaceholder(holder: BindingHolder<VB>) {
                val itemConfiguration = scope.getConfiguration()
                // 执行绑定
                itemConfiguration.onBind?.invoke(holder.binding, FusionPlaceholder(), 0)
            }
        }

        // 4. 注册到 Core
        core.registerPlaceholder(delegate)
    }

    suspend fun submitData(pagingData: PagingData<T>) {
        helperAdapter.submitData(sanitizePagingData(pagingData))
    }

    fun submitData(lifecycle: Lifecycle, pagingData: PagingData<T>) {
        helperAdapter.submitData(lifecycle, sanitizePagingData(pagingData))
    }

    private fun sanitizePagingData(pagingData: PagingData<T>): PagingData<T> {
        val config = Fusion.getConfig()
        return pagingData.filter { item ->
            if (core.viewTypeRegistry.isSupported(item)) true
            else {
                val exception = UnregisteredTypeException(item)
                if (config.isDebug) throw exception
                else {
                    config.errorListener?.onError(item, exception); false
                }
            }
        }
    }

    fun retry() = helperAdapter.retry()
    fun refresh() = helperAdapter.refresh()
    fun snapshot(): ItemSnapshotList<T> = helperAdapter.snapshot()
    fun peek(index: Int): T? = helperAdapter.peek(index)

    override fun getItemCount(): Int = helperAdapter.itemCount

    override fun getItemViewType(position: Int): Int {
        val item = helperAdapter.peek(position) // 使用 peek 防止触发加载

        if (item == null) {
            return ViewTypeRegistry.TYPE_PLACEHOLDER
        }
        return core.getItemViewType(item)
    }

    override fun getItemId(position: Int): Long {
        if (!hasStableIds()) return RecyclerView.NO_ID
        val item = helperAdapter.peek(position) ?: return RecyclerView.NO_ID
        return core.getItemId(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = core.onCreateViewHolder(parent, viewType)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = helperAdapter.getItemInternal(position)
        val bindItem = item ?: FusionPlaceholder()
        holder.setupStaggeredSupport(bindItem) { queryItem -> if (queryItem is FusionPlaceholder) core.getPlaceholderDelegate() else core.getDelegate(queryItem) }
        if (item == null) {
            val delegate = core.getPlaceholderDelegate()
            if (delegate != null) {
                holder.itemView.visibility = View.VISIBLE
                @Suppress("UNCHECKED_CAST")
                (delegate as FusionDelegate<Any, RecyclerView.ViewHolder>).onBindViewHolder(holder, bindItem, position, mutableListOf())
            } else holder.itemView.visibility = View.INVISIBLE
        } else {
            holder.itemView.visibility = View.VISIBLE
            core.onBindViewHolder(holder, item, position)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) onBindViewHolder(holder, position)
        else {
            val item = helperAdapter.getItemInternal(position)
            if (item != null) {
                holder.setupStaggeredSupport(item) { core.getDelegate(it) }
                core.onBindViewHolder(holder, item, position, payloads)
            } else onBindViewHolder(holder, position)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.setupGridSupport(
            this,
            { pos -> if (pos in helperAdapter.itemCount.let { 0 until it }) helperAdapter.peek(pos) else null },
            { core.getDelegate(it) })
    }

    val loadStateFlow: Flow<CombinedLoadStates> get() = helperAdapter.loadStateFlow
    fun addLoadStateListener(listener: (CombinedLoadStates) -> Unit) = helperAdapter.addLoadStateListener(listener)
    fun removeLoadStateListener(listener: (CombinedLoadStates) -> Unit) = helperAdapter.removeLoadStateListener(listener)
    fun addOnPagesUpdatedListener(listener: () -> Unit) = helperAdapter.addOnPagesUpdatedListener(listener)
    fun removeOnPagesUpdatedListener(listener: () -> Unit) = helperAdapter.removeOnPagesUpdatedListener(listener)

    fun withLoadStateHeaderAndFooter(header: LoadStateAdapter<*>, footer: LoadStateAdapter<*>): ConcatAdapter {
        addLoadStateListener { loadStates -> header.loadState = loadStates.prepend; footer.loadState = loadStates.append }
        return ConcatAdapter(header, this, footer)
    }

    fun withLoadStateFooter(footer: LoadStateAdapter<*>): ConcatAdapter {
        addLoadStateListener { loadStates -> footer.loadState = loadStates.append }
        return ConcatAdapter(this, footer)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        core.onViewRecycled(holder)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        core.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        core.onViewDetachedFromWindow(holder)
    }

    private inner class PagingHelperAdapter : PagingDataAdapter<T, RecyclerView.ViewHolder>(
        object : DiffUtil.ItemCallback<T>() {
            override fun areItemsTheSame(old: T, new: T) = core.areItemsTheSame(old, new)
            override fun areContentsTheSame(old: T, new: T) = core.areContentsTheSame(old, new)
            override fun getChangePayload(old: T, new: T) = core.getChangePayload(old, new)
        }
    ) {
        fun getItemInternal(position: Int): T? = super.getItem(position)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            throw IllegalStateException("Proxy Error")
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            throw IllegalStateException("Proxy Error")
        }
    }
}