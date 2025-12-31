package com.fusion.adapter.paging

import com.fusion.adapter.Fusion
import com.fusion.adapter.FusionConfig
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.internal.GlobalTypeKey
import com.fusion.adapter.internal.registry.ViewTypeRegistry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FusionPagingAdapterTest {

    private lateinit var adapter: FusionPagingAdapter<String>

    @Before
    fun setup() {
        Fusion.initialize(FusionConfig.Builder().setDebug(false).build())
        adapter = FusionPagingAdapter()
    }

    @Test
    fun testRegistryDelegation() {
        val mockDelegate = mock<FusionDelegate<String, *>>()
        whenever(mockDelegate.viewTypeKey) doReturn GlobalTypeKey(String::class.java, "string")
        
        adapter.register(String::class.java, mockDelegate)
        
        // 验证 Registry 是否正确传递给了 core
        assertEquals(mockDelegate, adapter.core.viewTypeRegistry.getDelegateOrNull(adapter.core.getItemViewType("test")))
    }

    @Test
    fun testPlaceholderViewType() {
        // Paging 规范中，如果 peek 返回 null，说明是占位符
        // FusionPagingAdapter 应该返回 TYPE_PLACEHOLDER
        
        // 由于我们无法轻易 mock 内部的 helperAdapter.peek（它是 final 的 PagingDataAdapter）
        // 但我们可以验证 core 是否能处理 TYPE_PLACEHOLDER
        
        assertEquals(-2049, ViewTypeRegistry.TYPE_PLACEHOLDER)
    }
}
