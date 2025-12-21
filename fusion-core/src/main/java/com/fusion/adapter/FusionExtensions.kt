package com.fusion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.delegate.FunctionalLayoutDelegate
import com.fusion.adapter.dsl.BindingDsl
import com.fusion.adapter.dsl.LayoutDsl
import com.fusion.adapter.dsl.RegistrationBuilder
import com.fusion.adapter.dsl.RouteScope
import com.fusion.adapter.internal.DslSignature
import com.fusion.adapter.internal.TypeRouter

// ============================================================================================
// Adapter 扩展入口 (API Surface)
// ============================================================================================

/**
 * [路由注册] - 适用于复杂场景 (一对多)
 * 需要在 block 中配置 match 规则和 map 映射。
 *
 * @sample
 * adapter.register<Message> {
 *     match { it.type }
 *     map(TYPE_TEXT, ItemTextBinding::inflate) { ... }
 *     map(TYPE_IMAGE, ItemImageBinding::inflate) { ... }
 * }
 */
inline fun <reified T : Any> RegistryOwner.register(
    block: RouteScope<T>.() -> Unit
) {
    val scope = RouteScope(T::class.java)
    scope.block()
    this.attachLinker(T::class.java, scope.builder.linker)
}

/**
 * [极简注册] - 适用于简单场景 (一对一)
 * 直接绑定布局和逻辑，无需配置路由。
 *
 * @sample
 * adapter.register(ItemUserBinding::inflate) {
 *     onBind { user -> ... }
 * }
 */
inline fun <reified T : Any, reified VB : ViewBinding> RegistryOwner.register(
    noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
    crossinline block: BindingDsl<T, VB>.() -> Unit
) {
    val builder = RegistrationBuilder(T::class.java)
    builder.bind(inflate, block)
    this.attachLinker(T::class.java, builder.linker)
}

/**
 * [API 扩展] 支持直接使用 Layout ID 进行注册，无需 ViewBinding。
 */
inline fun <reified T : Any> RegistryOwner.register(
    @LayoutRes layoutResId: Int,
    noinline block: LayoutDsl<T>.() -> Unit
) {
    // 1. 创建 DSL 配置对象并执行用户的 block
    val dsl = LayoutDsl<T>().apply(block)

    // 2. 创建内部 Delegate 实现
    // 使用 DslSignature 确保 ViewType 唯一性 (Class + LayoutRes 维度)
    // 这里我们用 LayoutHolder::class.java 作为占位符，或者自定义一个标记类
    val signature = DslSignature(T::class.java, Int::class.java) // Int 代表 layoutId 维度

    val delegate = FunctionalLayoutDelegate<T>(signature, layoutResId)
    delegate.applyDsl(dsl)

    // 3. 安全检查 (复用之前写的 checkStableIdRequirement)
    // 此时 delegate 已经是 FunctionalLayoutDelegate，检查逻辑能正确识别 keyProvider
    // (注意：需要确保 attachLinker 内部调用了 checkStableIdRequirement)
    val linker = TypeRouter<T>()
    linker.map(Unit, delegate)

    this.attachLinker(T::class.java, linker)
}

// ============================================================================================
// 快速启动 (Setup)
// ============================================================================================

/**
 * [快速启动 - 推荐]
 * 初始化自动挡 FusionListAdapter (基于 AsyncListDiffer)。
 * 适用于 MVVM、DiffUtil、自动计算差异的场景。
 */
inline fun RecyclerView.setupFusion(
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context),
    block: FusionListAdapter.() -> Unit
): FusionListAdapter {
    this.layoutManager = layoutManager
    // 使用 apply 链式调用，代码更紧凑，不产生临时变量
    return FusionListAdapter().apply {
        block()
        this@setupFusion.adapter = this
    }
}

/**
 * [快速启动 - 手动挡]
 * 初始化手动挡 FusionAdapter。
 * 适用于静态列表、需要绝对控制刷新动画、或不需要 Diff 的简单场景。
 */
inline fun RecyclerView.setupFusionManual(
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context),
    block: FusionAdapter.() -> Unit
): FusionAdapter {
    this.layoutManager = layoutManager
    return FusionAdapter().apply {
        block()
        this@setupFusionManual.adapter = this
    }
}

// ============================================================================================
// 实用工具 (Utilities)
// ============================================================================================

/**
 * [操作符重载] 支持使用 `+=` 追加列表数据。
 * 注意：对于 FusionListAdapter，这会创建新集合并提交 Diff。
 */
operator fun FusionListAdapter.plusAssign(items: List<Any>) {
    val newList = ArrayList(this.currentList)
    newList.addAll(items)
    this.submitList(newList)
}

/**
 * [操作符重载] 支持使用 `+=` 追加单个 Item。
 */
operator fun FusionListAdapter.plusAssign(item: Any) {
    val newList = ArrayList(this.currentList)
    newList.add(item)
    this.submitList(newList)
}

/**
 * [自动滚动] 监听数据插入并自动滚动到底部。
 * 适用于聊天、日志、控制台等场景。
 */
fun RecyclerView.autoScrollToBottom(adapter: RecyclerView.Adapter<*>) {
    adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            if (itemCount > 0) {
                // post 确保 LayoutManager 布局完成
                this@autoScrollToBottom.post {
                    if (adapter.itemCount > 0) {
                        this@autoScrollToBottom.smoothScrollToPosition(adapter.itemCount - 1)
                    }
                }
            }
        }
    })
}

inline fun Fusion.initialize(block: FusionConfig.Builder.() -> Unit) {
    initialize(FusionConfig.Builder().apply(block).build())
}
