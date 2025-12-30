package com.fusion.adapter.internal.engine

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.Fusion
import com.fusion.adapter.FusionConfig
import com.fusion.adapter.delegate.BindingHolder
import com.fusion.adapter.delegate.BindingInflater
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.delegate.LayoutHolder
import com.fusion.adapter.exception.MissingUniqueKeyException
import com.fusion.adapter.exception.UnregisteredTypeException
import com.fusion.adapter.internal.diff.ItemIdUtils
import com.fusion.adapter.internal.registry.TypeDispatcher
import com.fusion.adapter.internal.registry.ViewTypeRegistry
import com.fusion.adapter.log.FusionLogger
import com.fusion.adapter.placeholder.FusionPlaceholder
import com.fusion.adapter.placeholder.FusionPlaceholderDelegate
import com.fusion.adapter.placeholder.FusionPlaceholderViewHolder
import com.fusion.adapter.placeholder.PlaceholderConfigurator
import com.fusion.adapter.placeholder.PlaceholderDefinitionScope
import java.util.Collections

/**
 * [FusionCore]
 */
class FusionCore {

    val viewTypeRegistry = ViewTypeRegistry()

    private val scopeId: Long = System.identityHashCode(this).toLong() shl 32

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

    fun registerPlaceholder(@LayoutRes layoutResId: Int) {
        val delegate = object : FusionPlaceholderDelegate<LayoutHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup): LayoutHolder {
                return LayoutHolder(LayoutInflater.from(parent.context).inflate(layoutResId, parent, false))
            }

            override fun onBindPlaceholder(holder: LayoutHolder) {}
        }
        registerPlaceholder(delegate)
    }

    fun <VB : ViewBinding> registerPlaceholder(
        inflater: BindingInflater<VB>,
        configurator: PlaceholderConfigurator<VB>?
    ) {
        val scope = PlaceholderDefinitionScope<VB>()
        configurator?.configure(scope)
        val delegate = object : FusionPlaceholderDelegate<BindingHolder<VB>>() {
            override fun onCreateViewHolder(parent: ViewGroup): BindingHolder<VB> {
                val binding = inflater.inflate(LayoutInflater.from(parent.context), parent, false)
                return BindingHolder(binding)
            }

            override fun onBindPlaceholder(holder: BindingHolder<VB>) {
                val itemConfiguration = scope.getConfiguration()
                itemConfiguration.onBind?.invoke(holder.binding, FusionPlaceholder(), 0)
            }
        }
        registerPlaceholder(delegate)
    }

    fun <VB : ViewBinding> registerPlaceholder(
        inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        block: (PlaceholderDefinitionScope<VB>.() -> Unit)?
    ) {
        // 3. 调用那个基于接口的重载方法 (Java 版)
        // 利用 SAM 转换将 Kotlin 函数包装成接口
        registerPlaceholder(
            BindingInflater(inflate),
            block?.let { lambda ->
                PlaceholderConfigurator { scope -> lambda(scope) }
            }
        )
    }

    fun getPlaceholderDelegate(): FusionPlaceholderDelegate<*>? {
        return viewTypeRegistry.getPlaceholderDelegate() as? FusionPlaceholderDelegate<*>
    }

    /**
     * 生成确定性的占位符 ID。
     * 算法：(AdapterHash << 32) | (Position & 0xFFFFFFFF)
     * 保证：
     * 1. 同一个 Adapter 内，不同 Position ID 不同。
     * 2. 不同 Adapter (ConcatAdapter 场景)，高 32 位不同，ID 空间物理隔离。
     * 3. 只要 Adapter 实例不变，刷新列表时同一个位置的 ID 保持不变 (支持 DiffUtil 动画)。
     */
    fun getPlaceholderId(position: Int, hostHashCode: Int): Long {
        val high = hostHashCode.toLong() shl 32
        val low = position.toLong() and 0xFFFFFFFFL
        return high or low
    }

    fun <T : Any> registerDispatcher(clazz: Class<T>, dispatcher: TypeDispatcher<T>) {
        val count = dispatcher.getAllDelegates().size
        FusionLogger.i("Registry") { "Registering Dispatcher for ${clazz.simpleName}. Delegates count: $count" }
        enforceUniqueKeys(clazz, dispatcher.getAllDelegates())
        viewTypeRegistry.register(clazz, dispatcher)
    }

    fun <T : Any> register(clazz: Class<T>, delegate: FusionDelegate<T, *>) {
        val dispatcher = TypeDispatcher.create(delegate)
        registerDispatcher(clazz, dispatcher)
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

    fun getItemId(item: Any, position: Int): Long {
        if (item is FusionPlaceholder) {
            // 算法：(CoreHash << 32) | (Position & 0xFFFFFFFF)
            return scopeId or (position.toLong() and 0xFFFFFFFFL)
        }
        val viewType = viewTypeRegistry.getItemViewType(item)
        val delegate = viewTypeRegistry.getDelegate(viewType)
        val uniqueKey = delegate.getUniqueKey(item) ?: return System.identityHashCode(item).toLong()
        return ItemIdUtils.getItemId(viewType, uniqueKey)
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
