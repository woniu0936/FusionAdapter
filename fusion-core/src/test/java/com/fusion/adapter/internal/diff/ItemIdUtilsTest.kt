package com.fusion.adapter.internal.diff

import androidx.recyclerview.widget.RecyclerView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ItemIdUtilsTest {

    @Test
    fun testIdStability() {
        val viewType = 10001
        val key = "user_123456"

        val id1 = ItemIdUtils.getItemId(viewType, key)
        val id2 = ItemIdUtils.getItemId(viewType, key)

        // 1. 验证确定性
        assertEquals(id1, id2)

        // 2. 验证 ViewType 隔离（即使 Key 相同，ViewType 不同 ID 也必须不同，防止 ConcatAdapter 冲突）
        val id3 = ItemIdUtils.getItemId(10002, key)
        assertNotEquals(id1, id3)
    }

    @Test
    fun testPrimitiveKeySupport() {
        val vt = 100
        // 测试各种基础类型的 Key
        assertNotEquals(RecyclerView.NO_ID, ItemIdUtils.getItemId(vt, 123L))
        assertNotEquals(RecyclerView.NO_ID, ItemIdUtils.getItemId(vt, 123))
        assertNotEquals(RecyclerView.NO_ID, ItemIdUtils.getItemId(vt, "id"))
        
        // 验证 null 必须返回 NO_ID
        assertEquals(RecyclerView.NO_ID, ItemIdUtils.getItemId(vt, null))
    }
}
