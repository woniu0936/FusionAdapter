package com.fusion.adapter.internal.registry

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.delegate.DslBindingDelegate
import com.fusion.adapter.delegate.DslLayoutDelegate
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.delegate.LayoutHolder
import com.fusion.adapter.dsl.ItemConfiguration
import com.fusion.adapter.internal.DslTypeKey

/**
 * [DslAdapterFactory]
 */
@PublishedApi
internal object DslAdapterFactory {

    @PublishedApi
    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any, VB : ViewBinding> createDelegate(
        clazz: Class<T>,
        vbClazz: Class<VB>,
        inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        config: ItemConfiguration<T, VB>
    ): FusionDelegate<T, *> {
        val key = DslTypeKey(clazz, config)
        return DslBindingDelegate(key, inflate, config)
    }

    @PublishedApi
    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> createLayoutDelegate(
        clazz: Class<T>,
        layoutResId: Int,
        config: ItemConfiguration<T, LayoutHolder>
    ): FusionDelegate<T, *> {
        val key = DslTypeKey(clazz, config)
        return DslLayoutDelegate(key, layoutResId, config)
    }
}
