package com.fusion.adapter.internal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.delegate.DslBindingDelegate
import com.fusion.adapter.delegate.DslLayoutDelegate
import com.fusion.adapter.delegate.LayoutHolder
import com.fusion.adapter.dsl.ItemConfiguration

@PublishedApi
internal object DslAdapterFactory {

    fun <T : Any, VB : ViewBinding> createDelegate(
        itemClass: Class<T>,
        viewBindingClass: Class<VB>,
        inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        config: ItemConfiguration<T, VB>
    ): DslBindingDelegate<T, VB> {
        return DslBindingDelegate(
            signature = DslTypeKey(itemClass, viewBindingClass),
            inflater = inflate,
            config = config
        )
    }

    fun <T : Any> createLayoutDelegate(
        itemClass: Class<T>,
        layoutRes: Int,
        config: ItemConfiguration<T, LayoutHolder>
    ): DslLayoutDelegate<T> {
        return DslLayoutDelegate(
            signature = DslTypeKey(itemClass, layoutRes),
            layoutResId = layoutRes,
            config = config
        )
    }
}