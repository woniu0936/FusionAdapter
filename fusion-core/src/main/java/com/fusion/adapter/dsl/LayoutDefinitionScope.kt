package com.fusion.adapter.dsl

import com.fusion.adapter.delegate.LayoutHolder

/**
 * [LayoutDefinitionScope]
 * 专用于 Layout Resource ID 模式的配置域。
 */
@FusionDsl
class LayoutDefinitionScope<T : Any> @PublishedApi internal constructor() : ItemDefinitionScope<T, LayoutHolder>()
