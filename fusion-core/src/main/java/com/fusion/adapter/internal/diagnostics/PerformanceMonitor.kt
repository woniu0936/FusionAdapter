package com.fusion.adapter.internal.diagnostics

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.LongAdder

/**
 * [PerformanceMonitor]
 * Internal tracker for Adapter performance metrics.
 * Designed to be lock-free and thread-safe.
 */
internal class PerformanceMonitor {

    private val createStats = ConcurrentHashMap<Int, TimeAccumulator>()
    private val bindCounts = ConcurrentHashMap<Int, LongAdder>()

    fun recordCreate(viewType: Int, durationNanos: Long) {
        createStats.computeIfAbsent(viewType) { TimeAccumulator() }.add(durationNanos)
    }

    fun recordBind(viewType: Int) {
        bindCounts.computeIfAbsent(viewType) { LongAdder() }.increment()
    }

    fun getStats(viewType: Int): StatSnapshot {
        val create = createStats[viewType]
        val bind = bindCounts[viewType]
        return StatSnapshot(
            createCount = create?.count?.sum() ?: 0,
            totalCreateTimeNs = create?.totalNanos?.sum() ?: 0,
            bindCount = bind?.sum() ?: 0
        )
    }

    fun reset() {
        createStats.clear()
        bindCounts.clear()
    }

    private class TimeAccumulator {
        val count = LongAdder()
        val totalNanos = LongAdder()

        fun add(nanos: Long) {
            count.increment()
            totalNanos.add(nanos)
        }
    }

    data class StatSnapshot(
        val createCount: Long,
        val totalCreateTimeNs: Long,
        val bindCount: Long
    ) {
        val avgCreateTimeNs: Double
            get() = if (createCount > 0) totalCreateTimeNs.toDouble() / createCount else 0.0
    }
}
