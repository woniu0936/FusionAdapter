package com.fusion.adapter.dsl

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.delegate.BindingDelegate

/**
 * [RouteScope]
 * 仅用于 register。只暴露 match 和 map，隐藏 bind，防止歧义。
 */
class RouteScope<T : Any>(itemClass: Class<T>) {
    // 【关键修复】加上 @PublishedApi，允许 inline 函数访问 internal 属性
    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    val builder = RegistrationBuilder(itemClass)

    /**
     * [API] 配置当前类型的 Stable ID。
     * 这是 Level 2：针对 T 类型的一对多匹配，设置共享 ID
     *
     * 在一对多场景下，这相当于默认配置。
     * 所有通过此 Scope 注册的 Delegate，如果未定义自己的 ID，将使用此规则。
     */
    fun stableId(block: (T) -> Any?) = builder.stableId(block)

    fun match(mapper: (item: T) -> Any?) = builder.match(mapper)

    inline fun <reified VB : ViewBinding> map(
        key: Any?,
        noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        crossinline block: BindingDsl<T, VB>.() -> Unit
    ) = builder.map(key, inflate, block)

    // 重载 map 支持直接传 Delegate 实例
    fun map(key: Any?, delegate: BindingDelegate<T, *>) = builder.map(key, delegate)
}