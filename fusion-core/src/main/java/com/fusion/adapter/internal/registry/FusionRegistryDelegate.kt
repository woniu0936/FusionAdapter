package com.fusion.adapter.internal.registry

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.RestrictTo
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.delegate.BindingHolder
import com.fusion.adapter.delegate.BindingInflater
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.delegate.LayoutHolder
import com.fusion.adapter.internal.engine.FusionCore
import com.fusion.adapter.internal.registry.TypeDispatcher
import com.fusion.adapter.placeholder.*

/**
 * [FusionRegistryDelegate]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class FusionRegistryDelegate(private val core: FusionCore) : PlaceholderRegistry {

    fun <T : Any> registerDispatcher(clazz: Class<T>, dispatcher: TypeDispatcher<T>) {
        core.registerDispatcher(clazz, dispatcher)
    }

    fun <T : Any> register(clazz: Class<T>, delegate: FusionDelegate<T, *>) {
        val dispatcher = TypeDispatcher.create(delegate)
        core.registerDispatcher(clazz, dispatcher)
    }

    override fun registerPlaceholder(delegate: FusionPlaceholderDelegate<*>) {
        core.registerPlaceholder(delegate)
    }

    override fun registerPlaceholder(@LayoutRes layoutResId: Int) {
        val delegate = object : FusionPlaceholderDelegate<LayoutHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup): LayoutHolder {
                return LayoutHolder(LayoutInflater.from(parent.context).inflate(layoutResId, parent, false))
            }
            override fun onBindPlaceholder(holder: LayoutHolder) {}
        }
        core.registerPlaceholder(delegate)
    }

    override fun <VB : ViewBinding> registerPlaceholder(
        inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        block: (PlaceholderDefinitionScope<VB>.() -> Unit)?
    ) {
        val scope = PlaceholderDefinitionScope<VB>()
        block?.invoke(scope)
        val delegate = object : FusionPlaceholderDelegate<BindingHolder<VB>>() {
            override fun onCreateViewHolder(parent: ViewGroup): BindingHolder<VB> {
                return BindingHolder(inflate(LayoutInflater.from(parent.context), parent, false))
            }
            override fun onBindPlaceholder(holder: BindingHolder<VB>) {
                val itemConfiguration = scope.getConfiguration()
                itemConfiguration.onBind?.invoke(holder.binding, FusionPlaceholder(), 0)
            }
        }
        core.registerPlaceholder(delegate)
    }

    override fun <VB : ViewBinding> registerPlaceholder(
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
        core.registerPlaceholder(delegate)
    }
}
