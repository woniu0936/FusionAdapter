package com.fusion.adapter.dsl

import androidx.viewbinding.ViewBinding

/**
 * [ViewBindingDsl]
 * 专用于 ViewBinding 模式的 DSL 配置入口。
 */
class ViewBindingDsl<T : Any, VB : ViewBinding> : BaseItemDsl<T, VB>()