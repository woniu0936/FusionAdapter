package com.fusion.adapter.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.dsl.ItemConfiguration
import com.fusion.adapter.internal.DslTypeKey
import com.fusion.adapter.internal.ViewTypeKey

/**
 * [DslBindingDelegate]
 */
internal class DslBindingDelegate<T : Any, VB : ViewBinding>(
    override val viewTypeKey: ViewTypeKey,
    private val inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
    private val config: ItemConfiguration<T, VB>
) : BindingDelegate<T, VB>() {

    init {
        config.itemKey?.let { setUniqueKey(it) }
        config.observers.forEach { addObserver(it) }
    }

    override fun onInflateBinding(inflater: LayoutInflater, parent: ViewGroup): VB {
        return inflate(inflater, parent, false)
    }

    override fun onCreate(binding: VB) {
        // Handle initial setup if needed
    }

    override fun onBind(binding: VB, item: T, position: Int) {
        config.onBind?.invoke(binding, item, position)
    }

    override fun onBindPartial(binding: VB, item: T, position: Int, payloads: List<Any>, handled: Boolean) {
        if (config.onBindPartial != null) {
            config.onBindPartial?.invoke(binding, item, position, payloads)
        } else {
            super.onBindPartial(binding, item, position, payloads, handled)
        }
    }
}
