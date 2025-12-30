package com.fusion.adapter.internal.registry

import com.fusion.adapter.ItemKeyProvider
import com.fusion.adapter.delegate.FusionDelegate

/**
 * [RouterConfiguration]
 */
@PublishedApi
internal class RouterConfiguration<T : Any> {
    var itemKeyProvider: ItemKeyProvider<T>? = null
    var viewTypeProvider: ItemKeyProvider<T>? = null
    val delegates = HashMap<Any, FusionDelegate<T, *>>()
}
