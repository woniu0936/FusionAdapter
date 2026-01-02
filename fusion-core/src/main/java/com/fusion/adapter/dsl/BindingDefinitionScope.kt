package com.fusion.adapter.dsl

import androidx.viewbinding.ViewBinding

/**
 * [BindingDefinitionScope]
 */
@FusionDsl
class BindingDefinitionScope<T : Any, VB : ViewBinding> @PublishedApi internal constructor() : ItemDefinitionScope<T, VB>()
