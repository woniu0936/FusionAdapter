package com.fusion.adapter.dsl

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.ItemKeyProvider
import com.fusion.adapter.internal.registry.RouterConfiguration
import com.fusion.adapter.internal.registry.DslAdapterFactory
import com.fusion.adapter.router.TypeRouter

/**
 * [RouterScope]
 * Scope for configuring one-to-many routing.
 */
@FusionDsl
class RouterScope<T : Any> @PublishedApi internal constructor(
    @PublishedApi internal val modelClass: Class<T>
) {

    // 2. 保持：这个必须是 @PublishedApi，因为 inline 函数用到了 config
    @PublishedApi internal val config = RouterConfiguration<T>()

    fun stableId(block: (T) -> Any?) {
        config.itemKeyProvider = ItemKeyProvider(block)
    }

    fun match(block: (T) -> Any?) {
        config.viewTypeProvider = ItemKeyProvider(block)
    }

    // 3. 修改：inline + reified，并增加 viewType 参数
    // 原代码中 viewType 变量未定义，通常它应该作为 on 的参数传入
    inline fun <reified VB : ViewBinding> map(
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
            modelClass,          // 现在合法访问
            VB::class.java, // 类型安全
            inflate,
            scope.config
        )

        // 注册 delegate
        config.delegates[viewType] = delegate
    }

    @PublishedApi
    internal fun build(): TypeRouter<T> {
        val builder = TypeRouter.Builder<T>()
        config.itemKeyProvider?.let { builder.stableId(it::getKey) }
        config.viewTypeProvider?.let { builder.match(it::getKey) }
        config.delegates.forEach { (key, delegate) ->
            builder.map(key, delegate)
        }
        return builder.build()
    }
}
