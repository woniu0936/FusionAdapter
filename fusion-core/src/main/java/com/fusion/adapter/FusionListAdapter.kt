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
import com.fusion.adapter.delegate.BindingInflater
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.extensions.setupGridSupport
import com.fusion.adapter.extensions.setupStaggeredSupport
import com.fusion.adapter.internal.engine.FusionCore
import com.fusion.adapter.internal.engine.FusionExecutor
import com.fusion.adapter.internal.registry.TypeRouter
import com.fusion.adapter.log.FusionLogger
import com.fusion.adapter.placeholder.FusionPlaceholderDelegate
import com.fusion.adapter.placeholder.PlaceholderConfigurator
import com.fusion.adapter.placeholder.PlaceholderDefinitionScope
import com.fusion.adapter.placeholder.PlaceholderRegistry

/**
 * [FusionListAdapter]
 */
open class FusionListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), FusionRegistry, PlaceholderRegistry {

    @PublishedApi
    internal val core = FusionCore()

    private val differ: AsyncListDiffer<Any> = AsyncListDiffer(
        ListUpdateCallbackWrapper(this),
        AsyncDifferConfig.Builder(FusionDiffCallback(core)).setBackgroundThreadExecutor(FusionExecutor.backgroundExecutorAdapter).build()
    )

    init {
        if (Fusion.getConfig().defaultStableIds) {
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

    // --- Registry Delegation ---
    override fun <T : Any> register(clazz: Class<T>, router: TypeRouter<T>) = core.register(clazz, router)
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

    fun submitList(list: List<Any>?, commitCallback: Runnable? = null) {
        val rawList = if (list == null) emptyList() else ArrayList(list)
        FusionLogger.i("Adapter") { "submitList called. Size: ${rawList.size}" }

        FusionExecutor.execute {
            val start = System.currentTimeMillis()
            val safeList = core.filter(rawList)

            if (rawList.isNotEmpty() && safeList.isEmpty()) {
                FusionLogger.w("Adapter") { "submitList: All items were filtered out!" }
            } else {
                FusionLogger.d("Adapter") { "Filter finished in ${System.currentTimeMillis() - start}ms. Safe list size: ${safeList.size}" }
            }

            FusionExecutor.runOnMain {
                differ.submitList(safeList) {
                    FusionLogger.d("Adapter") { "DiffUtil finished. Updating UI." }
                    commitCallback?.run()
                }
            }
        }
    }

    @MainThread
    fun setItems(list: List<Any>?, commitCallback: Runnable? = null) {
        val rawList = if (list == null) emptyList() else ArrayList(list)
        FusionLogger.i("Adapter") { "setItems called (Sync). Size: ${rawList.size}" }
        val safeList = core.filter(rawList)
        differ.submitList(safeList, commitCallback)
    }

    val currentList: List<Any> get() = differ.currentList
    override fun getItemCount(): Int = differ.currentList.size
    override fun getItemViewType(position: Int): Int = core.getItemViewType(differ.currentList[position])
    override fun getItemId(position: Int): Long {
        if (!hasStableIds() || position !in currentList.indices) return RecyclerView.NO_ID
        return core.getItemId(currentList[position], position)
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