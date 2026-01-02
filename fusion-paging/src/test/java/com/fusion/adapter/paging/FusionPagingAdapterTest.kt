package com.fusion.adapter.paging

import com.fusion.adapter.Fusion
import com.fusion.adapter.FusionConfig
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.internal.GlobalTypeKey
import com.fusion.adapter.internal.registry.ViewTypeRegistry
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FusionPagingAdapterTest {

    private lateinit var adapter: FusionPagingAdapter<String>

    @Before
    fun setup() {
        Fusion.initialize(FusionConfig.Builder().setDebug(false).build())
        adapter = FusionPagingAdapter()
    }

    @Test
    fun givenDelegateRegistered_whenViewTypeQueried_thenItShouldResolveViaRegistry() {
        // Arrange
        val mockDelegate = mockk<FusionDelegate<String, *>>()
        every { mockDelegate.viewTypeKey } returns GlobalTypeKey(String::class.java, "string")
        
        adapter.register(String::class.java, mockDelegate)
        
        // Act
        val viewType = adapter.core.getItemViewType("test")
        val resolvedDelegate = adapter.core.viewTypeRegistry.getDelegateOrNull(viewType)

        // Assert: Verify Registry correctly passed to core
        assertThat(resolvedDelegate).isEqualTo(mockDelegate)
    }

    @Test
    fun givenPlaceholderViewTypeConstant_whenChecked_thenItShouldMatchExpectedValue() {
        // Act & Assert
        // Paging spec implies placeholder handling, verifying the constant is correct
        assertThat(ViewTypeRegistry.TYPE_PLACEHOLDER).isEqualTo(-2049)
    }
}
