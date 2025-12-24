package com.fusion.adapter.dsl

import com.fusion.adapter.delegate.LayoutHolder

/**
 * [LayoutIdDsl]
 * 专用于 Layout Resource ID 模式的 DSL 配置入口。
 */
class LayoutIdDsl<T : Any> : BaseItemDsl<T, LayoutHolder>()