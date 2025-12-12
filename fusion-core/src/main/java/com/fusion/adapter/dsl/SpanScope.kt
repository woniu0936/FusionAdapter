package com.fusion.adapter.dsl

/**
 * [SpanScope] 为 DSL 提供上下文感知的辅助能力。
 * 使用 Value Class 避免内存分配，实现 Zero-Cost Abstraction。
 */
@JvmInline
value class SpanScope(val totalSpans: Int) {
    // 语义化属性，避免用户手动计算 totalSpans / 2
    inline val full get() = totalSpans
    inline val half get() = kotlin.math.max(1, totalSpans / 2)
    inline val third get() = kotlin.math.max(1, totalSpans / 3)
    inline val quarter get() = kotlin.math.max(1, totalSpans / 4)
}