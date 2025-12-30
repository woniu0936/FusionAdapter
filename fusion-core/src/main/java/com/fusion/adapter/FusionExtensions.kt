package com.fusion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.dsl.BindingDefinitionScope
import com.fusion.adapter.dsl.RouterScope
import com.fusion.adapter.internal.registry.DslAdapterFactory

/**
 * [register] 核心入口
 */
@JvmName("register")
inline fun <reified T : Any, reified VB : ViewBinding> FusionRegistry.register(
    noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
    crossinline block: BindingDefinitionScope<T, VB>.() -> Unit
) {
    val scope = BindingDefinitionScope<T, VB>().apply(block)
    val delegate = DslAdapterFactory.createDelegate(T::class.java, VB::class.java, inflate, scope.config)
    this.register(T::class.java, delegate)
}

@JvmName("registerRouter")
inline fun <reified T : Any> FusionRegistry.register(
    block: RouterScope<T>.() -> Unit
) {
    val router = RouterScope(T::class.java).apply(block).build()
    this.register(T::class.java, router)
}

/**
 * RecyclerView 快速初始化
 */
@JvmName("setup")
inline fun RecyclerView.setupFusion(
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context),
    block: FusionListAdapter.() -> Unit
): FusionListAdapter {
    this.layoutManager = layoutManager
    val adapter = FusionListAdapter().apply(block)
    this.adapter = adapter
    return adapter
}
