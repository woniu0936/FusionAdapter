package com.fusion.adapter.dsl

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.ItemKeyProvider
import com.fusion.adapter.internal.registry.DispatcherConfiguration
import com.fusion.adapter.internal.registry.DslAdapterFactory
import com.fusion.adapter.internal.registry.TypeDispatcher

/**
 * [DispatcherScope]
 * Scope for configuring one-to-many routing.
 */
@FusionDsl
class DispatcherScope<T : Any>(
    // 1. 修改：加上 @PublishedApi internal
    // 作用：让下面的 inline 函数能读取 clazz，但在 Java/Kotlin 调用方看来它依然不可见
    @PublishedApi internal val clazz: Class<T>
) {

    // 2. 保持：这个必须是 @PublishedApi，因为 inline 函数用到了 config
    @PublishedApi internal val config = DispatcherConfiguration<T>()

    fun uniqueKey(block: (T) -> Any?) {
        config.itemKeyProvider = ItemKeyProvider(block)
    }

    fun viewTypeKey(block: (T) -> Any?) {
        config.viewTypeProvider = ItemKeyProvider(block)
    }

    // 3. 修改：inline + reified，并增加 viewType 参数
    // 原代码中 viewType 变量未定义，通常它应该作为 dispatch 的参数传入
    inline fun <reified VB : ViewBinding> dispatch(
        viewType: Any, // <--- 补充：必须传入 viewType (或是 Int)
        noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        noinline block: BindingDefinitionScope<T, VB>.() -> Unit
    ) {
        val scope = BindingDefinitionScope<T, VB>().apply(block)

        // 逻辑保持不变：
        // 只要 config 是 @PublishedApi，这里就能直接访问，不需要拆分函数
        if (scope.config.itemKey == null && config.itemKeyProvider != null) {
            scope.config.itemKey = config.itemKeyProvider?.let { provider ->
                { item -> provider.getKey(item) }
            }
        }

        // 4. 修复：直接使用 VB::class.java
        // 由于 reified 的存在，这里获取的是真实的 Binding 类，不再有 Unchecked cast
        val delegate = DslAdapterFactory.createDelegate(
            clazz,          // 现在合法访问
            VB::class.java, // 类型安全
            inflate,
            scope.config
        )

        // 注册 delegate
        config.delegates[viewType] = delegate
    }

    @PublishedApi
    internal fun build(): TypeDispatcher<T> {
        val builder = TypeDispatcher.Builder<T>()
        config.itemKeyProvider?.let { builder.uniqueKey(it::getKey) }
        config.viewTypeProvider?.let { builder.viewType(it::getKey) }
        config.delegates.forEach { (key, delegate) ->
            builder.delegate(key, delegate)
        }
        return builder.build()
    }
}
