package com.fusion.adapter.placeholder

import androidx.viewbinding.ViewBinding
import com.fusion.adapter.dsl.FusionDsl
import com.fusion.adapter.dsl.ItemDefinitionScope

/**
 * [PlaceholderDefinitionScope]
 */
@FusionDsl
class PlaceholderDefinitionScope<VB : ViewBinding> @PublishedApi internal constructor() : ItemDefinitionScope<Any, VB>()
