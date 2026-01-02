package com.fusion.adapter.internal.registry

import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.internal.FusionInternalTags
import com.fusion.adapter.core.GlobalTypeKey
import com.fusion.adapter.router.TypeRouter
import com.fusion.adapter.internal.diff.ViewTypeStorage
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class ViewTypeRegistryTest {

    private val registry = ViewTypeRegistry()

    @Test
    fun `given different delegates, when registered, then they should produce different ViewTypes`() {
        // Arrange
        val userClass = String::class.java
        val adClass = Int::class.java

        val key1 = GlobalTypeKey(userClass, FusionInternalTags.TAG_JAVA_DELEGATE)
        val key2 = GlobalTypeKey(userClass, FusionInternalTags.TAG_BINDING_DELEGATE)
        val key3 = GlobalTypeKey(adClass, FusionInternalTags.TAG_JAVA_DELEGATE)

        // Act
        val vt1 = ViewTypeStorage.getViewType(key1)
        val vt2 = ViewTypeStorage.getViewType(key2)
        val vt3 = ViewTypeStorage.getViewType(key3)

        // Assert
        assertThat(vt1).isNotEqualTo(vt2)
        assertThat(vt1).isNotEqualTo(vt3)
        assertThat(vt2).isNotEqualTo(vt3)
        assertThat(ViewTypeStorage.getViewType(GlobalTypeKey(userClass, FusionInternalTags.TAG_JAVA_DELEGATE))).isEqualTo(vt1)
    }

    @Test
    fun `given registered base class, when subclass is queried, then it should resolve to base delegate`() {
        // Arrange
        open class Base
        class Sub : Base()

        val mockDelegate = mockk<FusionDelegate<Base, *>>()
        every { mockDelegate.viewTypeKey } returns GlobalTypeKey(Base::class.java, "base")
        
        val router = TypeRouter.Builder<Base>()
            .map("key", mockDelegate)
            .match { "key" }
            .build()
            
        registry.register(Base::class.java, router)

        // Act & Assert
        assertThat(registry.isSupported(Sub())).isTrue()
        assertThat(registry.getItemViewType(Sub())).isEqualTo(registry.getItemViewType(Base()))
    }
}