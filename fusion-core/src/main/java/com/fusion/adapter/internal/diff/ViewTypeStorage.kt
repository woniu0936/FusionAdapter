package com.fusion.adapter.internal.diff

import com.fusion.adapter.internal.ViewTypeKey
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * [ViewTypeStorage]
 */
internal object ViewTypeStorage {
    private val keyToViewType = ConcurrentHashMap<ViewTypeKey, Int>()
    private val nextViewType = AtomicInteger(10000)

    fun getViewType(key: ViewTypeKey): Int {
        return keyToViewType.getOrPut(key) { nextViewType.getAndIncrement() }
    }
}
