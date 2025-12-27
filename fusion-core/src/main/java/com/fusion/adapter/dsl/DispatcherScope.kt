package com.fusion.adapter.dsl

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.ItemKeyProvider
import com.fusion.adapter.internal.registry.DispatcherConfiguration
import com.fusion.adapter.internal.registry.DslAdapterFactory
import com.fusion.adapter.internal.registry.TypeDispatcher

/**
 * [DispatcherScope]
 */
@FusionDsl
class DispatcherScope<T : Any>(private val clazz: Class<T>) {

    @PublishedApi internal val config = DispatcherConfiguration<T>()

    fun uniqueKey(block: (T) -> Any?) {
        config.itemKeyProvider = ItemKeyProvider(block)
    }

    fun viewTypeKey(block: (T) -> Any?) {
        config.viewTypeProvider = ItemKeyProvider(block)
    }

    fun <VB : ViewBinding> dispatch(
        viewType: Any,
        inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        block: BindingDefinitionScope<T, VB>.() -> Unit
    ) {
        val scope = BindingDefinitionScope<T, VB>().apply(block)
        val delegate = DslAdapterFactory.createDelegate(clazz, ViewBinding::class.java as Class<VB>, inflate, scope.config)
        config.delegates[viewType] = delegate
    }

    @PublishedApi
    internal fun build(): TypeDispatcher<T> {
        val builder = TypeDispatcher.Builder<T>()
        config.itemKeyProvider?.let { builder.uniqueKey(it::getKey) }
        config.viewTypeProvider?.let { builder.viewType(it::getKey) }
        config.delegates.forEach { (key, delegate) ->
            builder.delegate(key, delegate)
        }
        return builder.build()
    }
}