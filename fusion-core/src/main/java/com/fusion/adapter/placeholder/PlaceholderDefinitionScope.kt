package com.fusion.adapter.placeholder

import androidx.viewbinding.ViewBinding
import com.fusion.adapter.dsl.BindingDefinitionScope

/**
 * [PlaceholderDefinitionScope]
 * 占位符专用配置域。
 * 屏蔽了业务 item 参数，让 onBind 只关注视图本身（如骨架屏动画）。
 */
class PlaceholderDefinitionScope<VB : ViewBinding> : BindingDefinitionScope<Any, VB>() {

    // Kotlin DSL
    fun onBind(block: VB.() -> Unit) {
        config.onBind = { _, _ -> block() }
    }

    // Java API
    fun onBind(binder: PlaceholderBinder<VB>) {
        config.onBind = { _, _ -> binder.bind(this) }
    }
}