package com.fusion.adapter.internal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.delegate.FunctionalBindingDelegate
import com.fusion.adapter.delegate.FunctionalLayoutDelegate
import com.fusion.adapter.delegate.LayoutHolder
import com.fusion.adapter.dsl.ItemConfiguration

/**
 * [DslAdapterFactory]
 * 内部工厂。负责将配置数据注入到不可变的 Delegate 实例中。
 */
@PublishedApi
internal object DslAdapterFactory {

    fun <T : Any, VB : ViewBinding> createDelegate(
        itemClass: Class<T>,
        viewBindingClass: Class<VB>,
        inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        config: ItemConfiguration<T, VB>
    ): FunctionalBindingDelegate<T, VB> {
        return FunctionalBindingDelegate(
            signature = DslSignature(itemClass, viewBindingClass),
            inflater = inflate, // 这里的 inflate 符合 BindingInflater 接口定义
            config = config
        )
    }

    fun <T : Any> createLayoutDelegate(
        itemClass: Class<T>,
        layoutRes: Int,
        config: ItemConfiguration<T, LayoutHolder>
    ): FunctionalLayoutDelegate<T> {
        return FunctionalLayoutDelegate(
            signature = DslSignature(itemClass, layoutRes),
            layoutResId = layoutRes,
            config = config
        )
    }
}