package com.fusion.adapter.internal

import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * [GlobalViewTypePool]
 * 全局 ViewType 分发池。
 *
 * 作用：
 * 确保即使在不同的 Adapter 实例中，只要是相同的 Delegate 逻辑（Key 相同），
 * 就能获取到全局一致的 ViewType ID。
 *
 * 核心价值：
 * 1. 支持 RecyclerView.setRecycledViewPool() 共享缓存池。
 * 2. 避免不同 Adapter 间复用 ViewHolder 时因 ID 冲突导致的 ClassCastException。
 */
internal object GlobalViewTypePool {

    // Key (Delegate特征) -> ViewType (Int)
    // 核心修改：使用 Synchronized WeakHashMap 防止 Class 引用泄漏
    // 当 Key (通常是 ClassSignature 或 DslSignature) 不再被 Delegate 持有时，
    // 它可以被 GC 回收，从而防止静态强引用导致的 Memory Leak。
    private val keyToId = Collections.synchronizedMap(WeakHashMap<Any, Int>())

    // 起始值设置大一点 (10000)，避免与 Header/Footer 库常用的固定 ID (0, 1, 2...) 冲突
    private val atomicId = AtomicInteger(10000)

    /**
     * 获取或生成全局唯一的 ViewType ID
     */
    fun getId(key: Any): Int {
        // computeIfAbsent 在 synchronizedMap 中不是原子操作，
        // 但对于 viewType 生成，重复生成一次造成的代价极小，
        // 且 synchronizedMap 保证了读写可见性。
        // 为了绝对的线程安全，使用 synchronized 块 double-check
        var id = keyToId[key]
        if (id == null) {
            synchronized(keyToId) {
                id = keyToId[key]
                if (id == null) {
                    id = atomicId.getAndIncrement()
                    keyToId[key] = id
                    logW("FusionTracker") { "🆕 [Pool] New ID Generated: ID=$id for Key=$key" }
                }
            }
        }
        return id!!
    }
}