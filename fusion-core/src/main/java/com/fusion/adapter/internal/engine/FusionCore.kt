package com.fusion.adapter.internal.engine

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.Fusion
import com.fusion.adapter.FusionConfig
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.exception.MissingUniqueKeyException
import com.fusion.adapter.exception.UnregisteredTypeException
import com.fusion.adapter.internal.diff.ItemIdStorage
import com.fusion.adapter.internal.diff.ViewTypeStorage
import com.fusion.adapter.internal.registry.TypeDispatcher
import com.fusion.adapter.internal.registry.ViewTypeRegistry
import com.fusion.adapter.log.FusionLogger
import com.fusion.adapter.placeholder.FusionPlaceholderDelegate
import com.fusion.adapter.placeholder.FusionPlaceholderViewHolder
import java.util.Collections

/**
 * [FusionCore]
 */
class FusionCore {

    val viewTypeRegistry = ViewTypeRegistry()

    fun filter(safeList: List<Any>): List<Any> {
        val start = System.currentTimeMillis()
        if (safeList.isEmpty()) {
            FusionLogger.d("Core") { "Filter skipped: Input list is empty." }
            return safeList
        }
        
        val config = Fusion.getConfig()
        val result = ArrayList<Any>(safeList.size)
        var hasRemoved = false

        for (item in safeList) {
            if (Thread.currentThread().isInterrupted) {
                FusionLogger.w("Core") { "Filter interrupted." }
                return emptyList()
            }

            if (viewTypeRegistry.isSupported(item)) {
                result.add(item)
            } else {
                handleUnregisteredItem(item, config)
                hasRemoved = true
            }
        }
        
        val duration = System.currentTimeMillis() - start
        if (hasRemoved) {
            FusionLogger.w("Core") { "Filter finished in ${duration}ms. Removed ${safeList.size - result.size} unregistered items." }
        } else {
            FusionLogger.d("Core") { "Filter finished in ${duration}ms. No items removed. Total: ${result.size}" }
        }
        
        return if (hasRemoved) result else safeList
    }

    private fun handleUnregisteredItem(item: Any, config: FusionConfig) {
        val exception = UnregisteredTypeException(item)
        FusionLogger.e("Core", exception) { "Unregistered type detected: ${item.javaClass.name}" }
        
        if (config.isDebug) {
            throw exception
        } else {
            config.errorListener?.onError(item, exception)
        }
    }

    fun registerPlaceholder(delegate: FusionPlaceholderDelegate<*>) {
        FusionLogger.i("Core") { "Registering PlaceholderDelegate: ${delegate.javaClass.simpleName}" }
        viewTypeRegistry.registerPlaceholder(delegate)
    }

    fun getPlaceholderDelegate(): FusionPlaceholderDelegate<*>? {
        return viewTypeRegistry.getPlaceholderDelegate() as? FusionPlaceholderDelegate<*>
    }

    fun <T : Any> registerDispatcher(clazz: Class<T>, dispatcher: TypeDispatcher<T>) {
        val count = dispatcher.getAllDelegates().size
        FusionLogger.i("Registry") { "Registering Dispatcher for ${clazz.simpleName}. Delegates count: $count" }
        enforceUniqueKeys(clazz, dispatcher.getAllDelegates())
        viewTypeRegistry.register(clazz, dispatcher)
    }

    fun getItemViewType(item: Any): Int = viewTypeRegistry.getItemViewType(item)

    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ViewTypeRegistry.TYPE_PLACEHOLDER) {
            FusionLogger.d("Core") { "Creating Placeholder ViewHolder" }
            return viewTypeRegistry.getPlaceholderDelegate()?.onCreateViewHolder(parent)
                ?: FusionPlaceholderViewHolder(parent)
        }
        // Performance log for creation (potentially heavy)
        val start = System.nanoTime()
        val delegate = viewTypeRegistry.getDelegate(viewType)
        val holder = delegate.onCreateViewHolder(parent)
        val duration = (System.nanoTime() - start) / 1000 // micros
        
        // Log if creation takes > 2ms (frame drop risk)
        if (duration > 2000) {
             FusionLogger.w("Perf") { "onCreateViewHolder took ${duration}us for ViewType $viewType" }
        }
        return holder
    }

    fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Any, position: Int, payloads: MutableList<Any> = Collections.emptyList()) {
        val viewType = viewTypeRegistry.getItemViewType(item)
        val delegate = viewTypeRegistry.getDelegateOrNull(viewType)
        
        if (delegate != null) {
            delegate.onBindViewHolder(holder, item, position, payloads)
        } else {
            FusionLogger.e("Core") { "Bind failed: No delegate found for item at position $position" }
        }
    }

    fun getItemId(item: Any): Long {
        val viewType = viewTypeRegistry.getItemViewType(item)
        val delegate = viewTypeRegistry.getDelegate(viewType)
        val uniqueKey = delegate.getUniqueKey(item) ?: return System.identityHashCode(item).toLong()
        return ItemIdStorage.getItemId(viewType, uniqueKey)
    }

    fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        if (oldItem === newItem) return true
        if (oldItem.javaClass != newItem.javaClass) return false

        val oldType = viewTypeRegistry.getItemViewType(oldItem)
        val newType = viewTypeRegistry.getItemViewType(newItem)
        if (oldType != newType) return false

        val delegate = viewTypeRegistry.getDelegate(oldType)
        val oldKey = delegate.getUniqueKey(oldItem)
        val newKey = delegate.getUniqueKey(newItem)

        if (oldKey != null && newKey != null) {
            return oldKey == newKey
        }
        return oldItem == newItem
    }

    fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        val type = viewTypeRegistry.getItemViewType(oldItem)
        if (type != viewTypeRegistry.getItemViewType(newItem)) return false
        
        val same = viewTypeRegistry.getDelegate(type).areContentsTheSame(oldItem, newItem)
        if (!same) {
            FusionLogger.d("Diff") { "Content changed for ${oldItem.javaClass.simpleName}" }
        }
        return same
    }

    fun getChangePayload(oldItem: Any, newItem: Any): Any? {
        val type = viewTypeRegistry.getItemViewType(oldItem)
        if (type != viewTypeRegistry.getItemViewType(newItem)) return null
        
        val payload = viewTypeRegistry.getDelegate(type).getChangePayload(oldItem, newItem)
        if (payload != null) {
            FusionLogger.d("Diff") { "Payload generated for ${oldItem.javaClass.simpleName}" }
        }
        return payload
    }

    fun getDelegate(item: Any): FusionDelegate<Any, RecyclerView.ViewHolder>? {
        val viewType = viewTypeRegistry.getItemViewType(item)
        return viewTypeRegistry.getDelegateOrNull(viewType)
    }

    fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        viewTypeRegistry.getDelegateOrNull(holder.itemViewType)?.onViewRecycled(holder)
    }

    fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        viewTypeRegistry.getDelegateOrNull(holder.itemViewType)?.onViewAttachedToWindow(holder)
    }

    fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        viewTypeRegistry.getDelegateOrNull(holder.itemViewType)?.onViewDetachedFromWindow(holder)
    }

    fun enforceUniqueKeys(clazz: Class<*>, delegates: Collection<FusionDelegate<*, *>>) {
        if (!Fusion.getConfig().defaultItemIdEnabled) return
        for (delegate in delegates) {
            if (!delegate.isUniqueKeyDefined) {
                val ex = MissingUniqueKeyException(clazz, delegate.javaClass)
                FusionLogger.e("Core", ex) { "UniqueKey missing for delegate: ${delegate.javaClass.simpleName}" }
                throw ex
            }
        }
    }
}
