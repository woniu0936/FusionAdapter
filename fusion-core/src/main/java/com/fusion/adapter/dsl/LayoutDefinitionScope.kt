package com.fusion.adapter.dsl

import com.fusion.adapter.delegate.LayoutHolder

/**
 * [LayoutDefinitionScope]
 * 专用于 Layout Resource ID 模式的配置域。
 */
class LayoutDefinitionScope<T : Any> : ItemDefinitionScope<T, LayoutHolder>()
