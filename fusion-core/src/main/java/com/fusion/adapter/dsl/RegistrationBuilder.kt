package com.fusion.adapter.dsl

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.adapter.delegate.FunctionalBindingDelegate
import com.fusion.adapter.internal.DslSignature
import com.fusion.adapter.internal.TypeRouter

/**
 * [RegistrationBuilder]
 * 统一注册 DSL 容器。处理 "一对一" 绑定和 "一对多" 路由配置。
 */
class RegistrationBuilder<T : Any>(val itemClass: Class<T>) {

    // 内部持有 Core 层的 Linker
    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    val linker = TypeRouter<T>()

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
        crossinline block: BindingDsl<T, VB>.() -> Unit
    ) {
        val delegate = createDelegate(inflate, block)
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
        crossinline block: BindingDsl<T, VB>.() -> Unit
    ) {
        val delegate = createDelegate(inflate, block)
        map(key, delegate)
    }

    // -----------------------------------------------------------------------
    // 内部工厂方法 (Factory)
    // -----------------------------------------------------------------------

    /**
     * [核心工厂]
     * 利用 reified 泛型，实例化 Core 层的 FunctionalBindingDelegate。
     */
    @PublishedApi
    internal inline fun <reified VB : ViewBinding> createDelegate(
        noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        crossinline block: BindingDsl<T, VB>.() -> Unit
    ): BindingDelegate<T, VB> {

        // 1. 构建签名
        val signature = DslSignature(itemClass, VB::class.java)

        // 2. 执行 DSL 收集配置
        val dsl = BindingDsl<T, VB>().apply(block)

        // 3. 创建 Delegate
        val delegate = FunctionalBindingDelegate<T, VB>(signature, inflate)

        // 4. [核心] 一键应用配置 (替代原本的一堆赋值语句)
        delegate.applyDsl(dsl)

        return delegate
    }
}