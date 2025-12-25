package com.fusion.adapter.dsl

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.ItemKeyProvider
import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.adapter.delegate.LayoutDelegate
import com.fusion.adapter.internal.DispatcherConfiguration
import com.fusion.adapter.internal.DslAdapterFactory
import com.fusion.adapter.internal.TypeDispatcher

/**
 * [DispatcherScope]
 */
@FusionDsl
class DispatcherScope<T : Any>(@PublishedApi internal val itemClass: Class<T>) {
    @PublishedApi internal val config = DispatcherConfiguration<T>()

    fun viewTypeKey(extractor: (item: T) -> Any?) {
        config.viewTypeExtractor = ItemKeyProvider(extractor)
    }

    fun uniqueKey(extractor: (item: T) -> Any?) {
        config.uniqueItemKeyProvider = ItemKeyProvider(extractor)
    }

    inline fun <reified VB : ViewBinding> dispatch(key: Any?, delegate: BindingDelegate<T, VB>) {
        config.mappings[key] = delegate
    }

    fun dispatch(key: Any?, delegate: LayoutDelegate<T>) {
        config.mappings[key] = delegate
    }

    inline fun <reified VB : ViewBinding> dispatch(
        key: Any?,
        noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        crossinline block: BindingDefinitionScope<T, VB>.() -> Unit
    ) {
        val scope = BindingDefinitionScope<T, VB>().apply(block)
        config.mappings[key] = DslAdapterFactory.createDelegate(itemClass, VB::class.java, inflate, scope.config)
    }

    @PublishedApi internal fun build(): TypeDispatcher<T> = TypeDispatcher(config)
}