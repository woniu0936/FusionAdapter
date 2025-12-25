package com.fusion.adapter.placeholder

import androidx.viewbinding.ViewBinding

/**
 * [Java Compatible] 用于在 Java 中配置占位符 Scope
 */
fun interface PlaceholderConfigurator<VB : ViewBinding> {
    fun configure(scope: PlaceholderDefinitionScope<VB>)
}

/**
 * [Java Compatible] 用于在 Java 中处理占位符的绑定逻辑
 */
fun interface PlaceholderBinder<VB : ViewBinding> {
    fun bind(binding: VB)
}