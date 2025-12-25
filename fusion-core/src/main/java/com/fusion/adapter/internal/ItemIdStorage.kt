package com.fusion.adapter.internal

import androidx.collection.LruCache
import java.util.concurrent.atomic.AtomicLong

/**
 * [ItemIdStorage]
 * 核心职能：业务 UniqueKey -> 系统 Long Id 的映射与管理。
 */
internal object ItemIdStorage {
    private val itemIdCounter = AtomicLong(1)
    private val itemIdCache = LruCache<Pair<Int, Any>, Long>(3000)

    fun getItemId(viewType: Int, uniqueKey: Any): Long {
        val compositeKey = viewType to uniqueKey
        synchronized(itemIdCache) {
            itemIdCache.get(compositeKey)?.let { return it }
            val newId = itemIdCounter.getAndIncrement()
            itemIdCache.put(compositeKey, newId)
            return newId
        }
    }
}