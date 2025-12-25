package com.fusion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.dsl.LayoutIdDsl
import com.fusion.adapter.dsl.RouterDsl
import com.fusion.adapter.dsl.ViewBindingDsl
import com.fusion.adapter.internal.DslAdapterFactory
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// ============================================================================================
// Adapter 扩展入口 (API Surface)
// ============================================================================================

/**
 * [Entry 1] 注册单类型 Item (ViewBinding 模式)
 *
 * @param inflate ViewBinding 的 inflate 函数引用，例如 ItemUserBinding::inflate
 * @param block DSL 配置块
 */
inline fun <reified T : Any, reified VB : ViewBinding> RegistryOwner.register(
    noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
    crossinline block: ViewBindingDsl<T, VB>.() -> Unit
) {
    // 1. 执行 DSL，收集纯数据配置
    val dsl = ViewBindingDsl<T, VB>().apply(block)

    // 2. 通过工厂创建不可变的 Delegate (注入 Config)
    val delegate = DslAdapterFactory.createDelegate(
        itemClass = T::class.java,
        viewBindingClass = VB::class.java,
        inflate = inflate,
        config = dsl.config // ✅ 核心：将 DSL 产生的 Config 注入 Runtime
    )

    // 3. 注册到 Adapter (使用新接口)
    this.registerDelegate(T::class.java, delegate)
}

/**
 * [Entry 2] 注册单类型 Item (Layout Res ID 模式)
 *
 * @param layoutRes 布局资源 ID
 * @param block DSL 配置块
 */
inline fun <reified T : Any> RegistryOwner.register(
    @LayoutRes layoutRes: Int,
    noinline block: LayoutIdDsl<T>.() -> Unit
) {
    // 1. 执行 DSL
    val dsl = LayoutIdDsl<T>().apply(block)

    // 2. 通过工厂创建不可变的 Delegate
    val delegate = DslAdapterFactory.createLayoutDelegate(
        itemClass = T::class.java,
        layoutRes = layoutRes,
        config = dsl.config
    )

    // 3. 注册到 Adapter
    this.registerDelegate(T::class.java, delegate)
}

/**
 * [Entry 3] 注册多类型路由 (Router 模式)
 *
 * 适用于一个数据类型 T 对应多种视图的场景 (如: 消息列表中的 文本消息/图片消息)。
 */
inline fun <reified T : Any> RegistryOwner.register(
    block: RouterDsl<T>.() -> Unit
) {
    // 🔥 修改点：传入 T::class.java
    val dsl = RouterDsl(T::class.java).apply(block)
    val router = dsl.build()
    this.registerRouter(T::class.java, router)
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

/**
 * Kotlin 协程扩展支持
 * 允许在协程中以挂起的方式调用异步更新，自动处理取消。
 */
suspend fun FusionAdapter.setItemsSuspending(newItems: List<Any>) {
    return suspendCancellableCoroutine { continuation ->
        // 调用 Java 风格的异步方法
        this.setItemsAsync(newItems) {
            // 任务完成，恢复协程
            if (continuation.isActive) {
                continuation.resume(Unit)
            }
        }

        // 如果协程被取消（例如页面关闭），尝试取消 Adapter 内部的任务
        // 注意：Adapter 内部的 pendingTask 会在 onDetached 时自动取消，
        // 但这里显式响应协程取消会更灵敏。
        // (注：由于 setItemsAsync 没有暴露 Cancellable 返回值给外部，
        // 这里主要依赖 Adapter 自身的生命周期管理，或者你可以修改 setItemsAsync 返回 Cancellable)
    }
}

/**
 * FusionListAdapter 的协程支持
 * 挂起直到 Diff 计算完成且 UI 更新完毕。
 */
suspend fun FusionListAdapter.submitListSuspending(list: List<Any>) {
    return suspendCancellableCoroutine { continuation ->
        this.submitList(list) {
            if (continuation.isActive) {
                continuation.resume(Unit)
            }
        }
    }
}
