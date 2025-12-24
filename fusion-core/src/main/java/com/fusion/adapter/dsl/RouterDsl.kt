package com.fusion.adapter.dsl

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.DiffKeyProvider
import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.adapter.delegate.LayoutDelegate
import com.fusion.adapter.internal.DslAdapterFactory
import com.fusion.adapter.internal.RouterConfiguration
import com.fusion.adapter.internal.TypeRouter

/**
 * [RouterDsl]
 * 用于构建多类型路由的 DSL 构建器。
 * 采用 Builder 模式：收集配置 -> build() -> 不可变 TypeRouter。
 *
 * @param T 数据类型
 */
@FusionDsl
class RouterDsl<T : Any>(
    @PublishedApi
    internal val itemClass: Class<T>
) {

    @PublishedApi
    internal val config = RouterConfiguration<T>()

    fun match(matcher: (item: T) -> Any?) {
        config.matcher = DiffKeyProvider(matcher)
    }

    fun stableId(block: (item: T) -> Any?) {
        config.defaultIdProvider = block
    }

    // ========================================================================================
    // 标准映射 (Mapping existing delegates)
    // ========================================================================================

    inline fun <reified VB : ViewBinding> map(key: Any?, delegate: BindingDelegate<T, VB>) {
        config.mappings[key] = delegate
    }

    fun map(key: Any?, delegate: LayoutDelegate<T>) {
        config.mappings[key] = delegate
    }

    // ========================================================================================
    // 内联映射 (Inline Mapping)
    // ========================================================================================

    /**
     * [Inline] 注册 ViewBinding 逻辑
     */
    inline fun <reified VB : ViewBinding> map(
        key: Any?,
        noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        crossinline block: ViewBindingDsl<T, VB>.() -> Unit
    ) {
        // 1. 复用 ViewBindingDsl 解析配置
        val dsl = ViewBindingDsl<T, VB>().apply(block)

        // 2. 复用 Factory 创建 Delegate (利用 itemClass)
        // ✅ 这里的 itemClass 现在可以被访问了
        val delegate = DslAdapterFactory.createDelegate(
            itemClass = this.itemClass,
            viewBindingClass = VB::class.java,
            inflate = inflate,
            config = dsl.config
        )

        // 3. 注册
        config.mappings[key] = delegate
    }

    /**
     * [Inline] 注册 LayoutRes 逻辑
     */
    inline fun map(
        key: Any?,
        @LayoutRes layoutRes: Int,
        crossinline block: LayoutIdDsl<T>.() -> Unit
    ) {
        // 1. 复用 LayoutIdDsl
        val dsl = LayoutIdDsl<T>().apply(block)

        // 2. 复用 Factory
        // ✅ 这里的 itemClass 现在可以被访问了
        val delegate = DslAdapterFactory.createLayoutDelegate(
            itemClass = this.itemClass,
            layoutRes = layoutRes,
            config = dsl.config
        )

        // 3. 注册
        config.mappings[key] = delegate
    }

    // ========================================================================================
    // Build
    // ========================================================================================

    @PublishedApi
    internal fun build(): TypeRouter<T> {
        return TypeRouter(config)
    }
}