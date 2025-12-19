package com.fusion.adapter.paging

import FusionPagingAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.dsl.DelegateDsl
import com.fusion.adapter.dsl.RegistrationBuilder
import com.fusion.adapter.dsl.RouteScope

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

// ============================================================================================
// [High-Level API] - 专为 Kotlin 用户设计的 DSL 门面
// ============================================================================================

/**
 * [DSL] 统一注册入口 - 简单模式 (1对1)
 * 用户感知: adapter.register(ItemBinding::inflate) { ... }
 */
inline fun <reified T : Any, reified VB : ViewBinding> FusionPagingAdapter<*>.register(
    noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
    crossinline block: DelegateDsl<T, VB>.() -> Unit
) {
    val builder = RegistrationBuilder(T::class.java)
    builder.bind(inflate, block)

    // 👇 调用底层 API
    this.attachLinker(T::class.java, builder.linker)
}

/**
 * [DSL] 统一注册入口 - 路由模式 (1对多)
 * 用户感知: adapter.register<Message> { match ... map ... }
 */
inline fun <reified T : Any> FusionPagingAdapter<*>.register(
    block: RouteScope<T>.() -> Unit
) {
    val scope = RouteScope(T::class.java)
    scope.block()

    this.attachLinker(T::class.java, scope.builder.linker)
}

/**
 * 清空 Paging 数据。通常用于退出登录或重置搜索。
 */
fun FusionPagingAdapter<*>.clear(lifecycle: Lifecycle) {
    this.submitData(lifecycle, PagingData.empty())
}
