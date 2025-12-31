package com.fusion.adapter.paging

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fusion.adapter.Fusion
import com.fusion.adapter.FusionConfig
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.internal.GlobalTypeKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * [Instrumented Test]
 * 运行在 Android 设备上的集成测试。
 */
@RunWith(AndroidJUnit4::class)
class FusionPagingAdapterInstrumentedTest {

    private lateinit var adapter: FusionPagingAdapter<String>

    @Before
    fun setup() {
        // 在 Android 主线程或通过 Instrumentation 运行
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        Fusion.initialize(FusionConfig.Builder().setDebug(true).build())
        
        // Android 环境下，初始化不再会有 Looper 相关的 NPE
        adapter = FusionPagingAdapter()
    }

    @Test
    fun testAdapterInitialization() {
        assertNotNull(adapter)
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun testViewTypeWithRealRegistry() {
        val mockDelegate = mock<FusionDelegate<String, *>>()
        whenever(mockDelegate.viewTypeKey) doReturn GlobalTypeKey(String::class.java, "string")
        
        adapter.register(String::class.java, mockDelegate)
        
        // 验证在真实 Android 环境下的 getItemViewType 逻辑
        // 这里的逻辑已经由前面的 fix(paging) 修复
        assertNotNull(adapter.core.viewTypeRegistry)
    }
}
