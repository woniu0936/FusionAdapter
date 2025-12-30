package com.fusion.adapter.internal.registry

import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.internal.diff.ViewTypeStorage
import com.fusion.adapter.placeholder.FusionPlaceholder
import java.util.concurrent.ConcurrentHashMap

/**
 * [ViewTypeRegistry]
 */
class ViewTypeRegistry {

    companion object {
        const val TYPE_PLACEHOLDER = -2049
    }

    private val classToRouter = ConcurrentHashMap<Class<*>, TypeRouter<Any>>()
    private val viewTypeToDelegate = ConcurrentHashMap<Int, FusionDelegate<Any, RecyclerView.ViewHolder>>()

    private val supportedCache = ConcurrentHashMap<Class<*>, Boolean>()
    private val inheritanceCache = ConcurrentHashMap<Class<*>, TypeRouter<Any>?>()

    @Volatile
    private var hasPlaceholderDelegate = false

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> register(clazz: Class<T>, router: TypeRouter<T>) {
        classToRouter[clazz] = router as TypeRouter<Any>
        router.getAllDelegates().forEach { registerDelegateGlobal(it) }
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
        if (classToRouter.containsKey(clazz)) return true
        if (findRouterForInheritanceCached(clazz) != null) return true
        return false
    }

    fun getItemViewType(item: Any): Int {
        if (item is FusionPlaceholder) {
            if (hasPlaceholderDelegate) return TYPE_PLACEHOLDER
            throw IllegalStateException("Fusion: FusionPlaceholder used but no PlaceholderDelegate registered.")
        }
        val clazz = item.javaClass
        var router = classToRouter[clazz]

        if (router == null) {
            router = findRouterForInheritanceCached(clazz)
            if (router != null) {
                classToRouter.putIfAbsent(clazz, router)
            }
        }

        if (router == null) {
            throw IllegalStateException("Fusion: Critical - Item ${clazz.simpleName} has no registered Router.")
        }

        val delegate = router.select(item)
            ?: throw IllegalStateException("Fusion: 分发失败 (Key 未映射) -> ${clazz.simpleName}")

        return ViewTypeStorage.getViewType(delegate.viewTypeKey)
    }

    private fun findRouterForInheritanceCached(clazz: Class<*>): TypeRouter<Any>? {
        if (inheritanceCache.containsKey(clazz)) {
            return inheritanceCache[clazz]
        }
        val router = findRouterForInheritanceInternal(clazz)
        inheritanceCache[clazz] = router
        return router
    }

    private fun findRouterForInheritanceInternal(clazz: Class<*>): TypeRouter<Any>? {
        var current: Class<*>? = clazz.superclass
        while (current != null && current != Any::class.java) {
            val router = classToRouter[current]
            if (router != null) return router
            current = current.superclass
        }
        for (inf in clazz.interfaces) {
            val router = classToRouter[inf]
            if (router != null) return router
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