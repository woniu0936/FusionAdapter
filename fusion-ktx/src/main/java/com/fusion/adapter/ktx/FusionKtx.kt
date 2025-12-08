package com.fusion.adapter.ktx

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.Fusion
import com.fusion.adapter.FusionAdapter
import com.fusion.adapter.FusionConfig
import com.fusion.adapter.FusionListAdapter
import com.fusion.adapter.dsl.DelegateDsl
import com.fusion.adapter.dsl.FusionBuilder
import com.fusion.adapter.dsl.RouteScope

// ============================================================================================
// Adapter 扩展入口 (API Surface)
// ============================================================================================

/**
 * [路由注册] - 适用于复杂场景 (一对多)
 * 需要在 block 中配置 match 规则和 map 映射。
 *
 * @sample
 * adapter.registerRoute<Message> {
 *     match { it.type }
 *     map(TYPE_TEXT, ItemTextBinding::inflate) { ... }
 *     map(TYPE_IMAGE, ItemImageBinding::inflate) { ... }
 * }
 */
inline fun <reified T : Any> FusionListAdapter.registerRoute(
    block: RouteScope<T>.() -> Unit
) {
    val scope = RouteScope<T>()
    scope.block()
    this.registerLinker(T::class.java, scope.builder.linker)
}

inline fun <reified T : Any> FusionAdapter.registerRoute(
    block: RouteScope<T>.() -> Unit
) {
    val scope = RouteScope<T>()
    scope.block()
    this.registerLinker(T::class.java, scope.builder.linker)
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
inline fun <reified T : Any, reified VB : ViewBinding> FusionListAdapter.register(
    noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
    crossinline block: DelegateDsl<T, VB>.() -> Unit
) {
    val builder = FusionBuilder<T>()
    builder.bind(inflate, block)
    this.registerLinker(T::class.java, builder.linker)
}

/** [极简注册] 手动挡 Adapter 版本 */
inline fun <reified T : Any, reified VB : ViewBinding> FusionAdapter.register(
    noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
    crossinline block: DelegateDsl<T, VB>.() -> Unit
) {
    val builder = FusionBuilder<T>()
    builder.bind(inflate, block)
    this.registerLinker(T::class.java, builder.linker)
}

// ============================================================================================
// 快速启动 (Setup)
// ============================================================================================

/**
 * [快速启动] 初始化 FusionListAdapter (自动挡) 并绑定到 RecyclerView。
 * 推荐用于 MVVM + DiffUtil 场景。
 */
fun RecyclerView.setupFusion(
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context),
    block: FusionListAdapter.() -> Unit
): FusionListAdapter {
    this.layoutManager = layoutManager
    val adapter = FusionListAdapter()
    adapter.block()
    this.adapter = adapter
    return adapter
}

/**
 * [快速启动] 初始化 FusionAdapter (手动挡) 并绑定到 RecyclerView。
 * 推荐用于静态列表或需要绝对控制刷新的场景。
 */
fun RecyclerView.setupFusionManual(
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context),
    block: FusionAdapter.() -> Unit
): FusionAdapter {
    this.layoutManager = layoutManager
    val adapter = FusionAdapter()
    adapter.block()
    this.adapter = adapter
    return adapter
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