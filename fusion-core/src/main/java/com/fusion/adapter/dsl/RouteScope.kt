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
class RouteScope<T : Any> {
    // 【关键修复】加上 @PublishedApi，允许 inline 函数访问 internal 属性
    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    val builder = RegistrationBuilder<T>()

    fun match(mapper: (item: T) -> Any?) = builder.match(mapper)

    inline fun <reified VB : ViewBinding> map(
        key: Any?,
        noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        crossinline block: DelegateDsl<T, VB>.() -> Unit
    ) = builder.map(key, inflate, block)

    // 重载 map 支持直接传 Delegate 实例
    fun map(key: Any?, delegate: BindingDelegate<T, *>) = builder.map(key, delegate)
}