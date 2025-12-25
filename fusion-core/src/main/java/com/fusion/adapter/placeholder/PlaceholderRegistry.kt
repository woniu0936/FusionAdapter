package com.fusion.adapter.placeholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.delegate.BindingInflater

/**
 * [PlaceholderRegistry]
 * 定义适配器设置占位符的能力。
 */
interface PlaceholderRegistry {

    fun registerPlaceholder(delegate: FusionPlaceholderDelegate<*>)

    fun registerPlaceholder(@LayoutRes layoutResId: Int)

    fun <VB : ViewBinding> registerPlaceholder(
        inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        block: (PlaceholderDefinitionScope<VB>.() -> Unit)? = null
    )

    /**
     * Java 友好的重载方法
     *
     * @param inflater ViewBinding 的 inflate 方法引用 (e.g., ItemBinding::inflate)
     * @param configurator 配置回调 (e.g., scope -> scope.onBind(...))
     */
    fun <VB : ViewBinding> registerPlaceholder(
        inflater: BindingInflater<VB>,
        configurator: PlaceholderConfigurator<VB>?
    )

}
