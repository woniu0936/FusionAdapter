package com.fusion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.delegate.BindingHolder
import com.fusion.adapter.delegate.BindingInflater
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.delegate.LayoutHolder
import com.fusion.adapter.dsl.ItemConfiguration
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
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger

/**
 * [FusionAdapter]
 */
open class FusionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), FusionRegistry, PlaceholderRegistry {

    @PublishedApi
    internal val core = FusionCore()
    private var items: List<Any> = Collections.emptyList()
    val currentItems: List<Any> get() = Collections.unmodifiableList(items)
    private val maxScheduledGeneration = AtomicInteger(0)
    private var pendingTask: FusionDispatcher.Cancellable? = null

    fun interface OnItemsChangedListener {
        fun onItemsChanged()
    }

    init {
        if (Fusion.getConfig().defaultItemIdEnabled) {
            setHasStableIds(true)
        }
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
                val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
                return LayoutHolder(view)
            }

            override fun onBindPlaceholder(holder: LayoutHolder) {}
        }
        core.registerPlaceholder(delegate)
    }

    override fun <VB : ViewBinding> registerPlaceholder(
        inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        block: (PlaceholderDefinitionScope<VB>.() -> Unit)?
    ) {
        val scope = PlaceholderDefinitionScope<VB>()
        block?.invoke(scope)

        val delegate = object : FusionPlaceholderDelegate<BindingHolder<VB>>() {
            override fun onCreateViewHolder(parent: ViewGroup): BindingHolder<VB> {
                return BindingHolder(inflate(LayoutInflater.from(parent.context), parent, false))
            }

            override fun onBindPlaceholder(holder: BindingHolder<VB>) {
                @Suppress("UNCHECKED_CAST")
                val settings = scope.getConfiguration() as ItemConfiguration<Any, VB>
                settings.onBind?.invoke(holder.binding, FusionPlaceholder(), 0)
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

    @MainThread
    fun setItems(newItems: List<Any>) {
        maxScheduledGeneration.incrementAndGet()
        pendingTask?.cancel()
        val safeItems = core.filter(newItems)
        updateInternal(safeItems)
    }

    fun setItemsAsync(newItems: List<Any>, listener: OnItemsChangedListener? = null) {
        pendingTask?.cancel()
        val generation = maxScheduledGeneration.incrementAndGet()
        pendingTask = FusionDispatcher.dispatch {
            val safeItems = core.filter(newItems)
            FusionDispatcher.runOnMain {
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

    fun insertItem(position: Int, item: Any) {
        val safeList = core.filter(listOf(item))
        if (safeList.isNotEmpty()) {
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

    override fun getItemCount(): Int = items.size
    override fun getItemViewType(position: Int): Int = core.getItemViewType(items[position])
    override fun getItemId(position: Int): Long {
        if (!hasStableIds() || position !in items.indices) return RecyclerView.NO_ID
        return core.getItemId(items[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = core.onCreateViewHolder(parent, viewType)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        holder.setupStaggeredSupport(item) { core.getDelegate(it) }
        core.onBindViewHolder(holder, item, position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) onBindViewHolder(holder, position)
        else {
            val item = items[position]
            holder.setupStaggeredSupport(item) { core.getDelegate(it) }
            core.onBindViewHolder(holder, item, position, payloads)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.setupGridSupport(this, { pos -> if (pos in items.indices) items[pos] else null }, { core.getDelegate(it) })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        pendingTask?.cancel()
        pendingTask = null
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) = core.onViewRecycled(holder)
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) = core.onViewAttachedToWindow(holder)
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) = core.onViewDetachedFromWindow(holder)
}