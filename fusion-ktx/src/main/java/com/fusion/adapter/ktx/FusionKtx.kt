package com.fusion.adapter.ktx


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.FusionAdapter
import com.fusion.adapter.FusionListAdapter
import com.fusion.adapter.core.FusionLinker
import com.fusion.adapter.delegate.BindingDelegate

// ============================================================================================
// 1. DSL 配置容器 (Delegate Configuration)
// ============================================================================================

/**
 * [DelegateDsl]
 * 用于配置匿名 Delegate 的 DSL 接收者。
 * 用户在 register { ... } 闭包中接触到的就是这个类。
 */
class DelegateDsl<T : Any, VB : ViewBinding> {

    // 使用 @PublishedApi internal 隐藏实现细节，同时允许 inline 函数访问
    @PublishedApi
    internal var bindBlock: (VB.(item: T, position: Int) -> Unit)? = null
    @PublishedApi
    internal var bindPayloadBlock: (VB.(item: T, position: Int, payloads: List<Any>) -> Unit)? = null
    @PublishedApi
    internal var clickAction: ((view: VB, item: T, position: Int) -> Unit)? = null
    @PublishedApi
    internal var longClickAction: ((view: VB, item: T, position: Int) -> Boolean)? = null
    @PublishedApi
    internal var contentSameBlock: ((old: T, new: T) -> Boolean)? = null

    /** 定义数据绑定逻辑 (简易版，不带 position) */
    fun onBind(block: VB.(item: T) -> Unit) {
        bindBlock = { item, _ -> block(item) }
    }

    /** 定义数据绑定逻辑 (带 position) */
    fun onBindIndexed(block: VB.(item: T, position: Int) -> Unit) {
        bindBlock = block
    }

    /** 定义局部刷新逻辑 (Payload) */
    fun onBindPayload(block: VB.(item: T, payloads: List<Any>) -> Unit) {
        bindPayloadBlock = { item, _, payloads -> block(item, payloads) }
    }

    /** 定义点击事件 */
    fun onClick(block: (item: T) -> Unit) {
        clickAction = { _, item, _ -> block(item) }
    }

    /** 定义长按事件 */
    fun onLongClick(block: (item: T) -> Boolean) {
        longClickAction = { _, item, _ -> block(item) }
    }

    /** 定义高性能 Diff 内容比对 (return true 表示内容未变) */
    fun areContentsTheSame(block: (old: T, new: T) -> Boolean) {
        contentSameBlock = block
    }
}

/**
 * [FusionBuilder]
 * 统一注册 DSL 容器。处理 "一对一" 绑定和 "一对多" 路由配置。
 */
class FusionBuilder<T : Any> {

    // 内部持有 Core 层的 Linker
    @PublishedApi
    internal val linker = FusionLinker<T>()

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

// ============================================================================================
// 2. 作用域隔离 (Scope Isolation) - 关键设计
// ============================================================================================

/**
 * [RouteScope]
 * 仅用于 registerRoute。只暴露 match 和 map，隐藏 bind，防止歧义。
 */
class RouteScope<T : Any> {
    // 【关键修复】加上 @PublishedApi，允许 inline 函数访问 internal 属性
    @PublishedApi
    internal val builder = FusionBuilder<T>()

    fun match(mapper: (item: T) -> Any?) = builder.match(mapper)

    inline fun <reified VB : ViewBinding> map(
        key: Any?,
        noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        crossinline block: DelegateDsl<T, VB>.() -> Unit
    ) = builder.map(key, inflate, block)

    // 重载 map 支持直接传 Delegate 实例
    fun map(key: Any?, delegate: BindingDelegate<T, *>) = builder.map(key, delegate)
}

// ============================================================================================
// 3. Adapter 扩展入口 (API Surface)
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
// 4. 快速启动 (Setup)
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
// 5. 实用工具 (Utilities)
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