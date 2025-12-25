package com.fusion.adapter.internal

import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.placeholder.FusionPlaceholder
import java.util.concurrent.ConcurrentHashMap

/**
 * [ViewTypeRegistry]
 */
class ViewTypeRegistry {

    companion object {
        const val TYPE_PLACEHOLDER = -2049
    }

    private val classToDispatcher = ConcurrentHashMap<Class<*>, TypeDispatcher<Any>>()
    private val viewTypeToDelegate = ConcurrentHashMap<Int, FusionDelegate<Any, RecyclerView.ViewHolder>>()

    // 缓存：记录 "Item是否被支持"，包含继承查找的结果 (并发安全)
    private val supportedCache = ConcurrentHashMap<Class<*>, Boolean>()

    // 缓存：记录 "子类 -> 父类Dispatcher" 的查找结果，避免重复反射 (并发安全)
    private val inheritanceCache = ConcurrentHashMap<Class<*>, TypeDispatcher<Any>?>()

    @Volatile
    private var hasPlaceholderDelegate = false

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> register(clazz: Class<T>, dispatcher: TypeDispatcher<T>) {
        classToDispatcher[clazz] = dispatcher as TypeDispatcher<Any>
        dispatcher.getAllDelegates().forEach { registerDelegateGlobal(it) }
        // 注册新类型时，清除缓存
        supportedCache.clear()
        inheritanceCache.clear()
    }

    private fun registerDelegateGlobal(delegate: FusionDelegate<*, *>) {
        @Suppress("UNCHECKED_CAST")
        val castedDelegate = delegate as FusionDelegate<Any, RecyclerView.ViewHolder>
        val uniqueKey = delegate.viewTypeKey
        val viewType = ViewTypeStorage.getViewType(uniqueKey)
        viewTypeToDelegate.put(viewType, castedDelegate)
    }

    fun registerPlaceholder(delegate: FusionDelegate<*, *>) {
        @Suppress("UNCHECKED_CAST")
        val castedDelegate = delegate as FusionDelegate<Any, RecyclerView.ViewHolder>
        viewTypeToDelegate.put(TYPE_PLACEHOLDER, castedDelegate)
        hasPlaceholderDelegate = true
        supportedCache.clear()
    }

    fun getPlaceholderDelegate(): FusionDelegate<Any, RecyclerView.ViewHolder>? = viewTypeToDelegate[TYPE_PLACEHOLDER]

    fun isSupported(item: Any): Boolean {
        if (item is FusionPlaceholder) return hasPlaceholderDelegate
        val clazz = item.javaClass
        supportedCache[clazz]?.let { return it }
        val isSupported = checkIsSupportedInternal(clazz)
        supportedCache[clazz] = isSupported
        return isSupported
    }

    private fun checkIsSupportedInternal(clazz: Class<*>): Boolean {
        if (classToDispatcher.containsKey(clazz)) return true
        if (findDispatcherForInheritanceCached(clazz) != null) return true
        return false
    }

    fun getItemViewType(item: Any): Int {
        if (item is FusionPlaceholder) {
            if (hasPlaceholderDelegate) return TYPE_PLACEHOLDER
            throw IllegalStateException("Fusion: FusionPlaceholder used but no PlaceholderDelegate registered.")
        }
        val clazz = item.javaClass
        var dispatcher = classToDispatcher[clazz]

        if (dispatcher == null) {
            dispatcher = findDispatcherForInheritanceCached(clazz)
            if (dispatcher != null) {
                classToDispatcher.putIfAbsent(clazz, dispatcher)
            }
        }

        if (dispatcher == null) {
            throw IllegalStateException("Fusion: Critical - Item ${clazz.simpleName} has no registered Dispatcher.")
        }

        val delegate = dispatcher.select(item)
            ?: throw IllegalStateException("Fusion: 分发失败 (Key 未映射) -> ${clazz.simpleName}")

        return ViewTypeStorage.getViewType(delegate.viewTypeKey)
    }

    //带缓存的继承查找
    private fun findDispatcherForInheritanceCached(clazz: Class<*>): TypeDispatcher<Any>? {
        if (inheritanceCache.containsKey(clazz)) {
            return inheritanceCache[clazz]
        }
        val dispatcher = findDispatcherForInheritanceInternal(clazz)
        inheritanceCache[clazz] = dispatcher // 即使是 null 也要缓存，表示"查过了，确实没有"
        return dispatcher
    }

    private fun findDispatcherForInheritanceInternal(clazz: Class<*>): TypeDispatcher<Any>? {
        var current: Class<*>? = clazz.superclass
        while (current != null && current != Any::class.java) {
            val dispatcher = classToDispatcher[current]
            if (dispatcher != null) return dispatcher
            current = current.superclass
        }
        for (inf in clazz.interfaces) {
            val dispatcher = classToDispatcher[inf]
            if (dispatcher != null) return dispatcher
        }
        return null
    }

    fun getDelegate(viewType: Int): FusionDelegate<Any, RecyclerView.ViewHolder> {
        return viewTypeToDelegate[viewType]
            ?: throw IllegalStateException("Fusion: Critical - Unknown ViewType $viewType")
    }

    fun getDelegateOrNull(viewType: Int): FusionDelegate<Any, RecyclerView.ViewHolder>? {
        return viewTypeToDelegate[viewType]
    }
}
