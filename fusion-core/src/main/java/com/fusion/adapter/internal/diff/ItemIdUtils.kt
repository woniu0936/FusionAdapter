package com.fusion.adapter.internal.diff

import androidx.recyclerview.widget.RecyclerView

/**
 * [ItemIdUtils]
 */
internal object ItemIdUtils {
    // FNV-1a 64-bit 常量
    private const val FNV_OFFSET_BASIS_64 = 0xcbf29ce484222325UL
    private const val FNV_PRIME_64 = 0x100000001b3UL

    fun getItemId(viewType: Int, key: Any?): Long {
        if (key == null) return RecyclerView.NO_ID

        // 1. 精确匹配 Long (最常见)
        if (key is Long) return hashLong(viewType, key)

        // 2. 精确匹配 String (次常见)
        if (key is CharSequence) return hashString64Bit(viewType, key)

        // 3. 精确匹配 Int/Short/Byte (无损转 Long)
        // 显式列出这三种，坚决不处理 Float/Double
        if (key is Int) return hashLong(viewType, key.toLong())
        if (key is Short) return hashLong(viewType, key.toLong())
        if (key is Byte) return hashLong(viewType, key.toLong())

        // 4. 兜底策略：其他对象 (Float, Double, Data Class, List...)
        return hashLong(viewType, key.hashCode().toLong())
    }

    /**
     * 对 String 进行 64位 Hash，同时混入 ViewType
     */
    private fun hashString64Bit(viewType: Int, str: CharSequence): Long {
        var result = FNV_OFFSET_BASIS_64

        // 先 Hash ViewType
        result = result xor viewType.toULong()
        result *= FNV_PRIME_64

        // 再 Hash 字符串内容
        val len = str.length
        for (i in 0 until len) {
            result = result xor str[i].code.toULong()
            result *= FNV_PRIME_64
        }
        return result.toLong()
    }

    /**
     * 对 Long 进行 Hash，混入 ViewType
     */
    private fun hashLong(viewType: Int, value: Long): Long {
        var result = FNV_OFFSET_BASIS_64

        // Hash ViewType
        result = result xor viewType.toULong()
        result *= FNV_PRIME_64

        // Hash Long Value (按字节处理)
        // 简单处理：将 Long 拆分为高低位参与 Hash，或者直接异或
        // 这里使用简单的混合策略，足以应付 UI 场景
        result = result xor value.toULong()
        result *= FNV_PRIME_64

        return result.toLong()
    }
}
