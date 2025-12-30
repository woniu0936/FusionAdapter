package com.fusion.adapter.internal.diff

import com.fusion.adapter.internal.GlobalTypeKey
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
        if (key !is GlobalTypeKey) {
            // Fallback: 如果有旧的自定义 Key 实现，退化为基于实例的映射(不共享)或自行保证
            return key.hashCode()
        }
        return keyToViewType.getOrPut(key) { nextViewType.getAndIncrement() }
    }
}
