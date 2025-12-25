package com.fusion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.delegate.BindingHolder
import com.fusion.adapter.delegate.BindingInflater
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.delegate.LayoutHolder
import com.fusion.adapter.extensions.setupGridSupport
import com.fusion.adapter.extensions.setupStaggeredSupport
import com.fusion.adapter.internal.FusionCore
import com.fusion.adapter.internal.FusionDispatcher
import com.fusion.adapter.internal.TypeDispatcher
import com.fusion.adapter.placeholder.FusionPlaceholder
import com.fusion.adapter.placeholder.FusionPlaceholderDelegate
import com.fusion.adapter.placeholder.PlaceholderConfigurator
import com.fusion.adapter.placeholder.PlaceholderDefinitionScope
import com.fusion.adapter.placeholder.PlaceholderRegistry

/**
 * [FusionListAdapter]
 * 自动挡适配器。
 */
open class FusionListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), FusionRegistry, PlaceholderRegistry {

    @PublishedApi
    internal val core = FusionCore()
    private val differ: AsyncListDiffer<Any> = AsyncListDiffer(
        ListUpdateCallbackWrapper(this),
        AsyncDifferConfig.Builder(FusionDiffCallback(core)).setBackgroundThreadExecutor(FusionDispatcher.backgroundExecutorAdapter).build()
    )

    init {
        if (Fusion.getConfig().defaultItemIdEnabled) {
            setHasStableIds(true)
        }
    }

    private class FusionDiffCallback(private val core: FusionCore) : DiffUtil.ItemCallback<Any>() {
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
            override fun onCreateViewHolder(parent: ViewGroup): LayoutHolder {
                return LayoutHolder(LayoutInflater.from(parent.context).inflate(layoutResId, parent, false))
            }

            override fun onBindPlaceholder(holder: LayoutHolder) {}
        }
        core.registerPlaceholder(delegate)
    }

    override fun <VB : ViewBinding> registerPlaceholder(inflate: (LayoutInflater, ViewGroup, Boolean) -> VB, block: (PlaceholderDefinitionScope<VB>.() -> Unit)?) {
        val scope = PlaceholderDefinitionScope<VB>(); block?.invoke(scope)
        val delegate = object : FusionPlaceholderDelegate<BindingHolder<VB>>() {
            override fun onCreateViewHolder(parent: ViewGroup): BindingHolder<VB> {
                return BindingHolder(inflate(LayoutInflater.from(parent.context), parent, false))
            }

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

    /**
     * 异步提交数据 (Default)
     */
    fun submitList(list: List<Any>?, commitCallback: Runnable? = null) {
        val rawList = if (list == null) emptyList() else ArrayList(list)
        FusionDispatcher.dispatch {
            val safeList = core.filter(rawList)
            differ.submitList(safeList, commitCallback)
        }
    }

    /**
     * [setItems] 同步过滤更新 (对应 FusionAdapter.setItems)
     * 明确语义：立即过滤，然后进行异步 Diff。
     */
    @MainThread
    fun setItems(list: List<Any>?, commitCallback: Runnable? = null) {
        val rawList = if (list == null) emptyList() else ArrayList(list)
        val safeList = core.filter(rawList)
        differ.submitList(safeList, commitCallback)
    }

    val currentList: List<Any> get() = differ.currentList
    override fun getItemCount(): Int = differ.currentList.size
    override fun getItemViewType(position: Int): Int = core.getItemViewType(differ.currentList[position])
    override fun getItemId(position: Int): Long {
        if (!hasStableIds() || position !in currentList.indices) return RecyclerView.NO_ID
        return core.getItemId(currentList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = core.onCreateViewHolder(parent, viewType)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.setupStaggeredSupport(item) { core.getDelegate(it) }
        core.onBindViewHolder(holder, item, position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) onBindViewHolder(holder, position)
        else {
            val item = differ.currentList[position]
            holder.setupStaggeredSupport(item) { core.getDelegate(it) }
            core.onBindViewHolder(holder, item, position, payloads)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.setupGridSupport(this, { pos -> if (pos in currentList.indices) currentList[pos] else null }, { core.getDelegate(it) })
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) = core.onViewRecycled(holder)
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) = core.onViewAttachedToWindow(holder)
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) = core.onViewDetachedFromWindow(holder)
}
