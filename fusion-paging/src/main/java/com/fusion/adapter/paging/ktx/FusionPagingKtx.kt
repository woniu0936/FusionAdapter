package com.fusion.adapter.paging.ktx

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.dsl.DelegateDsl
import com.fusion.adapter.dsl.FusionBuilder
import com.fusion.adapter.dsl.RouteScope
import com.fusion.adapter.paging.FusionPagingAdapter

// =================================================================
// 1. 快速启动 (Setup)
// =================================================================

/**
 * [快速启动] 初始化 FusionPagingAdapter 并绑定到 RecyclerView。
 */
inline fun <reified T : Any> RecyclerView.setupFusionPaging(
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context),
    // Block 的接收者也变成带泛型的 Adapter
    noinline block: (FusionPagingAdapter<T>.() -> Unit)? = null
): FusionPagingAdapter<T> {
    this.layoutManager = layoutManager
    val adapter = FusionPagingAdapter<T>()
    block?.invoke(adapter)
    this.adapter = adapter
    return adapter
}

// =================================================================
// 2. 注册扩展 (API Extensions)
// =================================================================

/**
 * [一对多] 路由注册
 * 对应 FusionListAdapter.registerRoute
 */
inline fun <reified T : Any> FusionPagingAdapter<*>.registerRoute(
    block: RouteScope<T>.() -> Unit
) {
    // 复用 fusion-ktx 中的 Scope 逻辑
    val scope = RouteScope<T>()
    scope.block()
    // 通过 Scope 内部的 Builder 获取 Linker (需确保 fusion-ktx 的这些属性对 paging 模块可见)
    // 如果 fusion-ktx 是 api 依赖，且使用了 @PublishedApi internal，这里是可以访问的
    this.registerLinker(T::class.java, scope.builder.linker)
}

/**
 * [一对一] 极简注册
 * 对应 FusionListAdapter.register
 */
inline fun <reified T : Any, reified VB : ViewBinding> FusionPagingAdapter<*>.register(
    noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
    crossinline block: DelegateDsl<T, VB>.() -> Unit
) {
    val builder = FusionBuilder<T>()
    builder.bind(inflate, block)
    this.registerLinker(T::class.java, builder.linker)
}