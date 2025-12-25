package com.fusion.adapter.dsl

import androidx.viewbinding.ViewBinding

/**
 * [BindingDefinitionScope]
 * 专用于 ViewBinding 模式的配置域。
 */
open class BindingDefinitionScope<T : Any, VB : ViewBinding> : ItemDefinitionScope<T, VB>()