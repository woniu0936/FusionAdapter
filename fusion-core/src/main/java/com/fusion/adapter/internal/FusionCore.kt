package com.fusion.adapter.internal

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.Fusion
import com.fusion.adapter.FusionConfig
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.exception.MissingUniqueKeyException
import com.fusion.adapter.exception.UnregisteredTypeException
import com.fusion.adapter.placeholder.FusionPlaceholderDelegate
import com.fusion.adapter.placeholder.FusionPlaceholderViewHolder
import java.util.Collections

/**
 * [FusionCore]
 */
class FusionCore {

    val viewTypeRegistry = ViewTypeRegistry()

    fun filter(safeList: List<Any>): List<Any> {
        if (safeList.isEmpty()) return safeList
        val config = Fusion.getConfig()
        val result = ArrayList<Any>(safeList.size)
        var hasRemoved = false

        for (item in safeList) {
            // 响应中断
            if (Thread.currentThread().isInterrupted) return emptyList()

            if (viewTypeRegistry.isSupported(item)) {
                result.add(item)
            } else {
                handleUnregisteredItem(item, config)
                hasRemoved = true
            }
        }
        return if (hasRemoved) result else safeList
    }

    private fun handleUnregisteredItem(item: Any, config: FusionConfig) {
        val exception = UnregisteredTypeException(item)
        if (config.isDebug) {
            throw exception
        } else {
            config.errorListener?.onError(item, exception)
            Log.e("Fusion", "⚠️ [Engine] Dropped unregistered item: ${item.javaClass.simpleName}.")
        }
    }

    fun registerPlaceholder(delegate: FusionPlaceholderDelegate<*>) {
        viewTypeRegistry.registerPlaceholder(delegate)
    }

    fun getPlaceholderDelegate(): FusionPlaceholderDelegate<*>? {
        return viewTypeRegistry.getPlaceholderDelegate() as? FusionPlaceholderDelegate<*>
    }

    fun <T : Any> registerDispatcher(clazz: Class<T>, dispatcher: TypeDispatcher<T>) {
        enforceUniqueKeys(clazz, dispatcher.getAllDelegates())
        viewTypeRegistry.register(clazz, dispatcher)
    }

    fun getItemViewType(item: Any): Int = viewTypeRegistry.getItemViewType(item)

    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ViewTypeRegistry.TYPE_PLACEHOLDER) {
            return viewTypeRegistry.getPlaceholderDelegate()?.onCreateViewHolder(parent)
                ?: FusionPlaceholderViewHolder(parent)
        }
        return viewTypeRegistry.getDelegate(viewType).onCreateViewHolder(parent)
    }

    fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Any, position: Int, payloads: MutableList<Any> = Collections.emptyList()) {
        val viewType = viewTypeRegistry.getItemViewType(item)
        viewTypeRegistry.getDelegateOrNull(viewType)?.onBindViewHolder(holder, item, position, payloads)
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

        // 只有在这里严格比较 equals，才能保证 DiffUtil 在重组/排序时的正确性
        return oldItem == newItem
    }

    fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        val type = viewTypeRegistry.getItemViewType(oldItem)
        if (type != viewTypeRegistry.getItemViewType(newItem)) return false
        return viewTypeRegistry.getDelegate(type).areContentsTheSame(oldItem, newItem)
    }

    fun getChangePayload(oldItem: Any, newItem: Any): Any? {
        val type = viewTypeRegistry.getItemViewType(oldItem)
        if (type != viewTypeRegistry.getItemViewType(newItem)) return null
        return viewTypeRegistry.getDelegate(type).getChangePayload(oldItem, newItem)
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

    fun enforceUniqueKeys(
        clazz: Class<*>,
        delegates: Collection<FusionDelegate<*, *>>,
    ) {
        if (!Fusion.getConfig().defaultItemIdEnabled) return

        for (delegate in delegates) {
            // 通过 isUniqueKeyDefined 直接检查内部是否有提取器引用
            // 避免了传入脏数据导致用户 Lambda 抛出 ClassCastException
            if (!delegate.isUniqueKeyDefined) {
                throw MissingUniqueKeyException(clazz, delegate.javaClass)
            }
        }
    }
}

