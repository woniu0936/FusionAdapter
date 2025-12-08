package com.fusion.adapter.dsl

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.core.FusionLinker
import com.fusion.adapter.delegate.BindingDelegate

/**
 * [FusionBuilder]
 * 统一注册 DSL 容器。处理 "一对一" 绑定和 "一对多" 路由配置。
 */
class FusionBuilder<T : Any> {

    // 内部持有 Core 层的 Linker
    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    val linker = FusionLinker<T>()

    // -----------------------------------------------------------------------
    // 模式 A: 一对一 (Bind)
    // -----------------------------------------------------------------------

    /** 绑定现有的 Delegate 实例 */
    fun bind(delegate: BindingDelegate<T, *>) {
        linker.map(Unit, delegate)
    }

    /** [DSL] 快速创建匿名 Delegate 并绑定 */
    inline fun <reified VB : ViewBinding> bind(
        noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        crossinline block: DelegateDsl<T, VB>.() -> Unit
    ) {
        val delegate = createAnonymousDelegate(inflate, block)
        bind(delegate)
    }

    // -----------------------------------------------------------------------
    // 模式 B: 一对多 (Match & Map)
    // -----------------------------------------------------------------------

    /** 定义路由规则：从 Item 中提取 Key (O(1) 查找的关键) */
    fun match(mapper: (item: T) -> Any?) {
        linker.match(mapper)
    }

    /** 映射 Key -> 现有的 Delegate 实例 */
    fun map(key: Any?, delegate: BindingDelegate<T, *>) {
        linker.map(key, delegate)
    }

    /** [DSL] 映射 Key -> 匿名 Delegate */
    inline fun <reified VB : ViewBinding> map(
        key: Any?,
        noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        crossinline block: DelegateDsl<T, VB>.() -> Unit
    ) {
        val delegate = createAnonymousDelegate(inflate, block)
        map(key, delegate)
    }

    // -----------------------------------------------------------------------
    // 内部工厂方法 (Factory)
    // -----------------------------------------------------------------------

    @PublishedApi
    internal inline fun <reified VB : ViewBinding> createAnonymousDelegate(
        noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        crossinline block: DelegateDsl<T, VB>.() -> Unit
    ): BindingDelegate<T, VB> {
        val dsl = DelegateDsl<T, VB>().apply(block)
        return object : BindingDelegate<T, VB>(inflate) {
            init {
                onItemClick = dsl.clickAction
                onItemLongClick = dsl.longClickAction
            }

            override fun onBind(binding: VB, item: T, position: Int) {
                dsl.bindBlock?.invoke(binding, item, position)
            }

            override fun onBindPayload(binding: VB, item: T, position: Int, payloads: List<Any>) {
                dsl.bindPayloadBlock?.invoke(binding, item, position, payloads)
                    ?: super.onBindPayload(binding, item, position, payloads)
            }

            override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
                return dsl.contentSameBlock?.invoke(oldItem, newItem)
                    ?: super.areContentsTheSame(oldItem, newItem)
            }
        }
    }
}