package com.fusion.adapter.internal.diff

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * [ItemIdStorage]
 */
internal object ItemIdStorage {
    private val viewTypeToMap = ConcurrentHashMap<Int, ConcurrentHashMap<Any, Long>>()
    private val nextId = AtomicLong(1)

    fun getItemId(viewType: Int, uniqueKey: Any): Long {
        val keyMap = viewTypeToMap.getOrPut(viewType) { ConcurrentHashMap() }
        return keyMap.getOrPut(uniqueKey) { nextId.getAndIncrement() }
    }
}
