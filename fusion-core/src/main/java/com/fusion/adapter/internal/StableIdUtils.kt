package com.fusion.adapter.internal

import androidx.annotation.RestrictTo

/**
 * 将任意类型的 ID 转换为 RecyclerView 要求的 Long 类型 ID。
 *
 * 极致的转换策略：
 * 1. 保持精度：对于 Double/Float，使用 Bit 转换防止截断碰撞 (1.1 vs 1.9)。
 * 2. 兼容性：支持 Kotlin Unsigned 类型 (ULong, UInt)。
 * 3. 性能：高频类型优先匹配。
 * 4. 兜底：使用 hashCode 降级。
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun mapToRecyclerViewId(rawId: Any): Long {
    return when (rawId) {
        // --- Tier 1: 高频原生类型 ---
        is Long -> rawId
        is Int -> rawId.toLong()
        is String -> rawId.hashCode().toLong()

        // --- Tier 2: 浮点数 (关键：使用 RawBits 避免精度截断导致的碰撞) ---
        // 例如：1.1.toLong() == 1, 1.9.toLong() == 1 -> 导致 Crash
        // 使用 doubleToRawLongBits 则完全不同，保证唯一性。
        is Double -> java.lang.Double.doubleToRawLongBits(rawId)
        is Float -> java.lang.Float.floatToRawIntBits(rawId).toLong()

        // --- Tier 3: 其他数字类型 ---
        is Short -> rawId.toLong()
        is Byte -> rawId.toLong()

        // --- Tier 4: Kotlin Unsigned 类型 (不继承 java.lang.Number) ---
        is ULong -> rawId.toLong()
        is UInt -> rawId.toLong()
        is UShort -> rawId.toLong()
        is UByte -> rawId.toLong()

        // --- Tier 5: 特殊基础类型 ---
        is Char -> rawId.code.toLong()
        is Boolean -> if (rawId) 1L else 0L

        // --- Tier 6: 兜底逻辑 ---
        // 处理 BigInteger, BigDecimal 等其他 Number
        is Number -> rawId.toLong()

        // 最终兜底：对象 Hash (如 UUID 对象、自定义 ID 对象)
        else -> rawId.hashCode().toLong()
    }
}