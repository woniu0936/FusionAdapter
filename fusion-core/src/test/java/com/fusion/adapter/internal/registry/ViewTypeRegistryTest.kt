package com.fusion.adapter.internal.registry

import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.internal.FusionInternalTags
import com.fusion.adapter.internal.GlobalTypeKey
import com.fusion.adapter.internal.diff.ViewTypeStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ViewTypeRegistryTest {

    private val registry = ViewTypeRegistry()

    @Test
    fun testViewTypeConsistency() {
        // 模拟两个不同的数据模型
        val userClass = String::class.java
        val adClass = Int::class.java

        // 模拟同一个数据模型使用不同的视图标签（例如 JavaDelegate 和 BindingDelegate）
        val key1 = GlobalTypeKey(userClass, FusionInternalTags.TAG_JAVA_DELEGATE)
        val key2 = GlobalTypeKey(userClass, FusionInternalTags.TAG_BINDING_DELEGATE)
        val key3 = GlobalTypeKey(adClass, FusionInternalTags.TAG_JAVA_DELEGATE)

        val vt1 = ViewTypeStorage.getViewType(key1)
        val vt2 = ViewTypeStorage.getViewType(key2)
        val vt3 = ViewTypeStorage.getViewType(key3)

        // 验证：不同组合必须产生不同的 ViewType
        assertNotEquals(vt1, vt2)
        assertNotEquals(vt1, vt3)
        assertNotEquals(vt2, vt3)

        // 验证：相同组合在任何时候获取都必须一致（保证共享）
        assertEquals(vt1, ViewTypeStorage.getViewType(GlobalTypeKey(userClass, FusionInternalTags.TAG_JAVA_DELEGATE)))
    }

    @Test
    fun testInheritanceSupport() {
        // 测试类继承：Base -> Sub
        open class Base
        class Sub : Base()

        val mockDelegate = mock<FusionDelegate<Base, *>>()
        whenever(mockDelegate.viewTypeKey) doReturn GlobalTypeKey(Base::class.java, "base")
        
        val router = TypeRouter.Builder<Base>()
            .map("key", mockDelegate)
            .match { "key" }
            .build()
            
        registry.register(Base::class.java, router)

        // 虽然只注册了 Base，但 Sub 应该能通过继承链查找到对应的 Router
        assertTrue(registry.isSupported(Sub()))
        assertEquals(registry.getItemViewType(Sub()), registry.getItemViewType(Base()))
    }
}
