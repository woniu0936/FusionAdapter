package com.fusion.adapter.dsl

import androidx.viewbinding.ViewBinding
import com.fusion.adapter.DiffKeyProvider
import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.adapter.delegate.LayoutDelegate
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
class RouterDsl<T : Any> {

    // 内部持有纯数据配置，外部不可见
    @PublishedApi
    internal val config = RouterConfiguration<T>()

    /**
     * [必需] 配置路由分发规则 (Matcher)。
     * 从数据项中提取特征 Key (例如: item.type, item.isHeader)。
     *
     * 示例: match { it.viewType }
     */
    fun match(matcher: (item: T) -> Any?) {
        config.matcher = DiffKeyProvider(matcher)
    }

    /**
     * [可选] 配置该类型数据的全局 Stable ID 获取规则。
     * 如果子 Delegate 没有单独配置 stableId，将默认使用此规则。
     */
    fun stableId(block: (item: T) -> Any?) {
        config.defaultIdProvider = block
    }

    /**
     * [映射] 注册 ViewBinding 类型的委托。
     *
     * @param key 路由 Key (需与 match 返回值匹配)
     * @param delegate 对应的 BindingDelegate 实例
     */
    inline fun <reified VB : ViewBinding> map(key: Any?, delegate: BindingDelegate<T, VB>) {
        // 由于 reified VB 的存在，这里不仅是语法糖，更保证了泛型 VB 的类型安全
        config.mappings[key] = delegate
    }

    /**
     * [映射] 注册 Layout ID 类型的委托。
     *
     * @param key 路由 Key
     * @param delegate 对应的 LayoutDelegate 实例
     */
    fun map(key: Any?, delegate: LayoutDelegate<T>) {
        config.mappings[key] = delegate
    }

    /**
     * [Internal Factory]
     * 构建不可变的运行时 Router。仅供 FusionExtensions 调用。
     */
    @PublishedApi
    internal fun build(): TypeRouter<T> {
        return TypeRouter(config)
    }
}