package com.fusion.adapter.internal

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
    // 使用 ConcurrentHashMap 保证多线程/多 Adapter 初始化时的安全性
    private val keyToId = ConcurrentHashMap<Any, Int>()

    // 起始值设置大一点 (10000)，避免与 Header/Footer 库常用的固定 ID (0, 1, 2...) 冲突
    private val atomicId = AtomicInteger(10000)

    /**
     * 获取或生成全局唯一的 ViewType ID
     */
    fun getId(key: Any): Int {
        // computeIfAbsent 是原子的，保证同一 Key 永远拿到同一个 ID
        return keyToId.computeIfAbsent(key) {
            val newId = atomicId.getAndIncrement()
            // [添加日志] 只有生成新ID时打印
            logW("FusionTracker") { "🆕 [Pool] New ID Generated: ID=$newId for Key=$key" }
            newId
        }
    }
}