package com.fusion.adapter.internal

import androidx.annotation.RestrictTo
import com.fusion.adapter.ItemKeyProvider
import com.fusion.adapter.delegate.FusionDelegate

/**
 * [DispatcherConfiguration]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DispatcherConfiguration<T : Any> {
    var viewTypeExtractor: ItemKeyProvider<T> = ItemKeyProvider { Unit }
    var uniqueItemKeyProvider: ItemKeyProvider<T>? = null
    val mappings = LinkedHashMap<Any?, FusionDelegate<T, *>>()
}