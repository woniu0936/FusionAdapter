package com.fusion.adapter.internal

import androidx.collection.LruCache
import java.util.concurrent.atomic.AtomicLong

/**
 * 全局唯一 ID 生成器
 * 解决问题：将 (ViewType + BusinessKey) 映射为 RecyclerView 要求的全局唯一 Long ID
 * 避免问题：不同 ViewType 使用相同的 BusinessKey (如 User id=1 和 Order id=1) 导致的哈希冲突
 */
internal object GlobalIdGenerator {
    // ID 计数器，从 1 开始（0 有时被视为无效 ID）
    private val idCounter = AtomicLong(1)

    // 使用 LruCache 而非 WeakHashMap，防止 String 等临时对象类型的 Key 被 GC 后导致 ID 变化
    // 容量设为 3000，足以覆盖绝大多数 Android 列表的一屏+缓冲区的数量，甚至几页分页数据
    private val idCache = LruCache<Pair<Int, Any>, Long>(3000)

    fun getUniqueId(viewType: Int, rawKey: Any): Long {
        // 组合键：确保即使 rawKey 相同，只要 viewType 不同，生成的 ID 也不同
        val compositeKey = viewType to rawKey

        synchronized(idCache) {
            // 1. 命中缓存：返回已存在的 Stable ID
            idCache.get(compositeKey)?.let { return it }

            // 2. 未命中：生成新 ID 并存入
            val newId = idCounter.getAndIncrement()
            idCache.put(compositeKey, newId)
            return newId
        }
    }
}