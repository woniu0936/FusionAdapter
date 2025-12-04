package com.fusion.adapter.ktx

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.FusionAdapter
import com.fusion.adapter.FusionListAdapter
import com.fusion.adapter.delegate.BindingDelegate

/**
 * [Fusion KTX] - 极致开发效率套件
 */

// ============================================================================================
// 1. RecyclerView 快速启动 DSL
// ============================================================================================

/**
 * 快速配置 FusionListAdapter 并绑定到 RecyclerView。
 * 默认使用 LinearLayoutManager (垂直)，也可手动指定。
 *
 * @param layoutManager 可选，默认垂直线性布局
 * @param block Adapter 配置代码块
 * @return 配置好的 FusionListAdapter
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
 * [手动挡] 快速配置 FusionAdapter (无 DiffUtil)。
 * 适用于数据量极小、静态列表、或者需要绝对控制刷新逻辑（notifyItemChanged）的场景。
 *
 * @param layoutManager 可选，默认垂直线性布局
 * @param block Adapter 配置代码块
 * @return 配置好的 FusionAdapter
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

/**
 * [功能增强] 让 RecyclerView 自动跟随 Adapter 数据变化滚动到底部。
 * 适合聊天页面或日志页面。
 */
fun RecyclerView.autoScrollToBottom(adapter: RecyclerView.Adapter<*>) {
    adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            // 只有当插入位置在列表末尾时才滚动 (可选逻辑)
            // 或者简单粗暴：只要有插入就滚
            if (itemCount > 0) {
                // post 一下确保 LayoutManager 已经完成布局
                this@autoScrollToBottom.post {
                    // 检查 adapter.itemCount 是否有效
                    if (adapter.itemCount > 0) {
                        this@autoScrollToBottom.smoothScrollToPosition(adapter.itemCount - 1)
                    }
                }
            }
        }
    })
}

// ============================================================================================
// 2. 匿名委托 DSL (Boilerplate Killer)
// ============================================================================================

/**
 * [神器] 快速创建 Delegate，无需定义 class 文件。
 * 适合简单的 Item 或者原型开发。
 *
 * @param inflate ViewBinding 的 inflate 方法引用
 * @param builder DSL 配置块
 */
inline fun <reified T : Any, VB : ViewBinding> fusionDelegate(
    noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
    crossinline builder: DelegateDsl<T, VB>.() -> Unit
): BindingDelegate<T, VB> {

    val dsl = DelegateDsl<T, VB>().apply(builder)

    return object : BindingDelegate<T, VB>(inflate) {

        init {
            // 绑定点击事件
            dsl.clickAction?.let { action ->
                onItemClick = { view, item, position -> action(view, item, position) }
            }
            dsl.longClickAction?.let { action ->
                onItemLongClick = { view, item, position -> action(view, item, position) }
            }
        }

        // 绑定 isFor
        override fun isFor(item: T, position: Int): Boolean {
            return dsl.isForBlock?.invoke(item, position) ?: super.isFor(item, position)
        }

        // 绑定 onBind
        override fun onBind(binding: VB, item: T, position: Int) {
            dsl.bindBlock?.invoke(binding, item, position)
        }

        // 绑定 Payload
        override fun onBindPayload(binding: VB, item: T, position: Int, payloads: List<Any>) {
            if (dsl.bindPayloadBlock != null) {
                dsl.bindPayloadBlock?.invoke(binding, item, position, payloads)
            } else {
                super.onBindPayload(binding, item, position, payloads)
            }
        }

        // 绑定 Diff
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return dsl.contentSameBlock?.invoke(oldItem, newItem) ?: super.areContentsTheSame(oldItem, newItem)
        }
    }
}

/** DSL 配置容器 */
class DelegateDsl<T : Any, VB : ViewBinding> {

    // 加上 @PublishedApi
    @PublishedApi
    internal var bindBlock: (VB.(item: T, position: Int) -> Unit)? = null

    @PublishedApi
    internal var bindPayloadBlock: (VB.(item: T, position: Int, payloads: List<Any>) -> Unit)? = null

    @PublishedApi
    internal var isForBlock: ((item: T, position: Int) -> Boolean)? = null

    @PublishedApi
    internal var clickAction: ((view: VB, item: T, position: Int) -> Unit)? = null

    @PublishedApi
    internal var longClickAction: ((view: VB, item: T, position: Int) -> Boolean)? = null

    @PublishedApi
    internal var contentSameBlock: ((old: T, new: T) -> Boolean)? = null

    /** 定义数据绑定逻辑 */
    fun onBind(block: VB.(item: T) -> Unit) {
        // 简化版 API，不带 position
        bindBlock = { item, _ -> block(item) }
    }

    /** 定义数据绑定逻辑 (带 position) */
    fun onBindIndexed(block: VB.(item: T, position: Int) -> Unit) {
        bindBlock = block
    }

    /** 定义局部刷新逻辑 */
    fun onBindPayload(block: VB.(item: T, payloads: List<Any>) -> Unit) {
        bindPayloadBlock = { item, _, payloads -> block(item, payloads) }
    }

    /** 定义多类型匹配逻辑 */
    fun isFor(block: (item: T) -> Boolean) {
        isForBlock = { item, _ -> block(item) }
    }

    /** 定义点击事件 */
    fun onClick(block: (item: T) -> Unit) {
        clickAction = { _, item, _ -> block(item) }
    }

    /** 定义内容比对逻辑 (Diff) */
    fun areContentsTheSame(block: (old: T, new: T) -> Boolean) {
        contentSameBlock = block
    }
}

// ============================================================================================
// 3. 操作符重载 (Syntactic Sugar)
// ============================================================================================

/**
 * 支持使用 `+=` 添加数据。
 * 注意：由于 AsyncListDiffer 是不可变的，这会创建一个新列表，适合小量数据追加。
 */
operator fun FusionListAdapter.plusAssign(items: List<Any>) {
    val newList = ArrayList(this.currentList)
    newList.addAll(items)
    this.submitList(newList)
}

operator fun FusionListAdapter.plusAssign(item: Any) {
    val newList = ArrayList(this.currentList)
    newList.add(item)
    this.submitList(newList)
}

// ============================================================================================
// 4. 极致 DSL 注册入口 (Syntactic Sugar)
// ============================================================================================

/**
 * [DSL 注册] 自动挡 Adapter
 * 直接在 register 中编写逻辑，无需显式调用 fusionDelegate。
 *
 * @sample
 * adapter.register<User, ItemUserBinding>(ItemUserBinding::inflate) {
 *     onBind { user -> ... }
 * }
 */
inline fun <reified T : Any, VB : ViewBinding> FusionListAdapter.register(
    noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
    crossinline block: DelegateDsl<T, VB>.() -> Unit
) {
    // 复用 fusionDelegate 的逻辑创建对象
    val delegate = fusionDelegate(inflate, block)
    // 调用核心注册方法
    this.register(delegate)
}

/**
 * [DSL 注册] 手动挡 Adapter
 * 直接在 register 中编写逻辑，无需显式调用 fusionDelegate。
 */
inline fun <reified T : Any, VB : ViewBinding> FusionAdapter.register(
    noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
    crossinline block: DelegateDsl<T, VB>.() -> Unit
) {
    val delegate = fusionDelegate(inflate, block)
    this.register(delegate)
}