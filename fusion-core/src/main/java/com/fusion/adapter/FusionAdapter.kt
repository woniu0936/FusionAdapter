package com.fusion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.delegate.BindingInflater
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.extensions.setupGridSupport
import com.fusion.adapter.extensions.setupStaggeredSupport
import com.fusion.adapter.internal.engine.FusionCore
import com.fusion.adapter.internal.engine.FusionDispatcher
import com.fusion.adapter.internal.registry.TypeDispatcher
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

    override fun <T : Any> registerDispatcher(clazz: Class<T>, dispatcher: TypeDispatcher<T>) = core.registerDispatcher(clazz, dispatcher)
    override fun <T : Any> register(clazz: Class<T>, delegate: FusionDelegate<T, *>) = core.register(clazz, delegate)
    override fun registerPlaceholder(delegate: FusionPlaceholderDelegate<*>) = core.registerPlaceholder(delegate)
    override fun registerPlaceholder(@LayoutRes layoutResId: Int) = core.registerPlaceholder(layoutResId)
    override fun <VB : ViewBinding> registerPlaceholder(
        inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        block: (PlaceholderDefinitionScope<VB>.() -> Unit)?
    ) = core.registerPlaceholder(inflate, block)

    override fun <VB : ViewBinding> registerPlaceholder(
        inflater: BindingInflater<VB>,
        configurator: PlaceholderConfigurator<VB>?
    ) = core.registerPlaceholder(inflater, configurator)

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
        val item = items[position]
        return core.getItemId(item,position)
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