package com.fusion.adapter.internal

import androidx.annotation.RestrictTo
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.ItemKeyProvider
import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.adapter.delegate.FusionDelegate
import java.util.Collections

/**
 * [TypeDispatcher]
 */
class TypeDispatcher<T : Any> internal constructor(
    config: DispatcherConfiguration<T>
) {
    private val viewTypeProvider: ItemKeyProvider<T> = config.viewTypeExtractor
    private val delegatesMap: Map<Any?, FusionDelegate<T, *>>

    init {
        val mapCopy = HashMap(config.mappings)
        val uniqueKeyPrivider = config.uniqueItemKeyProvider
        if (uniqueKeyPrivider != null) {
            val provider: (T) -> Any? = { item -> uniqueKeyPrivider.getKey(item) }
            mapCopy.values.forEach { it.attachDefaultUniqueKeyProvider(provider) }
        }
        this.delegatesMap = Collections.unmodifiableMap(mapCopy)
    }

    internal fun select(item: T): FusionDelegate<T, *>? {
        val key = viewTypeProvider.getKey(item)
        return delegatesMap[key]
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun getAllDelegates(): Collection<FusionDelegate<T, *>> = delegatesMap.values

    companion object {
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        fun <T : Any> create(delegate: FusionDelegate<T, *>): TypeDispatcher<T> {
            val config = DispatcherConfiguration<T>()
            config.mappings[Unit] = delegate
            return TypeDispatcher(config)
        }
    }

    /**
     * [Builder] Java & Manual API
     */
    class Builder<T : Any> {
        private val config = DispatcherConfiguration<T>()

        // --- Java 友好方法 (set前缀) ---
        fun setViewType(extractor: ItemKeyProvider<T>): Builder<T> = apply { config.viewTypeExtractor = extractor }
        fun setUniqueKey(extractor: ItemKeyProvider<T>): Builder<T> = apply { config.uniqueItemKeyProvider = extractor }
        fun addDelegate(key: Any?, delegate: FusionDelegate<T, *>): Builder<T> = apply { config.mappings[key] = delegate }

        // --- Kotlin 简洁方法 ---
        fun viewType(extractor: ItemKeyProvider<T>): Builder<T> = setViewType(extractor)
        fun uniqueKey(extractor: ItemKeyProvider<T>): Builder<T> = setUniqueKey(extractor)
        fun delegate(key: Any?, delegate: FusionDelegate<T, *>): Builder<T> = addDelegate(key, delegate)

        fun <VB : ViewBinding> delegate(key: Any?, delegate: BindingDelegate<T, VB>): Builder<T> = addDelegate(key, delegate)

        fun build(): TypeDispatcher<T> = TypeDispatcher(config)
    }
}
