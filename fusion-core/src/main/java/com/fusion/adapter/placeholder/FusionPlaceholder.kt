package com.fusion.adapter.placeholder

import java.util.concurrent.atomic.AtomicLong

/**
 * [FusionPlaceholder]
 * 代表列表中的一个占位符实体。
 * 每个实例都拥有唯一的 [id]，以支持 RecyclerView 的 StableId 机制。
 */
class FusionPlaceholder {
    /**
     * 使用负数序列作为 ID，确保：
     * 1. 自身唯一性 (每次 new 都不一样)
     * 2. 与真实数据隔离 (真实数据 ID 通常为正数)
     */
    val id: Long = nextId()

    companion object {
        private val ID_COUNTER = AtomicLong(-1)

        // 每次调用减 1：-1, -2, -3...
        private fun nextId(): Long = ID_COUNTER.getAndDecrement()
    }

}
