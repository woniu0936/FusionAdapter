package com.fusion.adapter.delegate

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.internal.PropertyWatcher1
import com.fusion.adapter.internal.PropertyWatcher2
import com.fusion.adapter.internal.PropertyWatcher3
import com.fusion.adapter.internal.PropertyWatcher4
import com.fusion.adapter.internal.PropertyWatcher5
import com.fusion.adapter.internal.PropertyWatcher6
import com.fusion.adapter.internal.ViewSignature
import com.fusion.adapter.internal.Watcher

/**
 * [FusionDelegate]
 * 纯粹的 UI 渲染器。
 *
 * 设计原则 (SOLID):
 * 1. SRP (单一职责): 只负责创建和绑定 View，不持有 Adapter 引用，不处理数据 Diff。
 * 2. OCP (开闭原则): 通过 getUniqueViewType 支持扩展 ID 生成策略。
 */
abstract class FusionDelegate<T : Any, VH : RecyclerView.ViewHolder> {

    /**
     * [身份签名]
     * 必须由子类提供唯一的身份标识。
     */
    abstract val signature: ViewSignature

    // 使用通用接口 Watcher 存储
    private val propertyWatchers = ArrayList<Watcher<T>>()

    /**
     * [唯一标识生成器]
     * 用于生成全局唯一的 ViewType Key。
     *
     */
    final fun getUniqueViewType(): Any = signature

    /**
     * [核心 API] 获取 Item 的唯一标识 (Stable ID)。
     *
     * 作用：用于 DiffUtil 计算和 RecyclerView 动画复用。
     *
     * @return 返回任意具有唯一性的对象 (String, Long, Int, UUID...)。
     *         如果返回 null，表示该 Item 没有 Stable ID。
     */
    open fun getStableId(item: T): Any? {
        return null
    }

    // ============================================================================================
    // Layout Strategy (核心布局策略)
    // ============================================================================================

    /**
     * [Java/Kotlin Override]
     * 子类重写此方法以定义 Grid 布局中占用的列数。
     * 默认返回 1。
     */
    open fun onSpanSize(item: T, position: Int, totalSpans: Int): Int {
        return 1
    }

    /**
     * [Java/Kotlin Override]
     * 子类重写此方法以定义是否在 Staggered 布局中强制占满全屏。
     * 默认返回 false。
     */
    open fun isFullSpan(item: T): Boolean {
        return false
    }

    // ============================================================================================
    // Internal Configuration (DSL 注入点)
    // ============================================================================================

    // 内部持有的 DSL 配置策略 (优先级高于 Override)
    internal var configSpanSize: ((item: T, position: Int, totalSpans: Int) -> Int)? = null
    internal var configFullSpan: ((item: T) -> Boolean)? = null

    /**
     * [Internal Dispatch]
     * 核心调度逻辑：DSL 配置 > 子类重写 > 默认值
     */
    internal fun resolveSpanSize(item: T, position: Int, totalSpans: Int): Int {
        return configSpanSize?.invoke(item, position, totalSpans)
            ?: onSpanSize(item, position, totalSpans)
    }

    /**
     * [Internal Dispatch]
     */
    @PublishedApi
    internal fun resolveFullSpan(item: T): Boolean {
        return configFullSpan?.invoke(item)
            ?: isFullSpan(item)
    }

    /**
     * [修复报错] 这里的 override 去掉，或者确认父类有定义。
     * 通常 FusionDelegate 是顶层类，所以应该是 open fun，而不是 override。
     * 如果你之前定义了父接口，请确保签名一致。这里假设它是基类。
     */
    open fun getChangePayload(oldItem: T, newItem: T): Any? {
        // 1. 如果没有注册观察者，直接返回 null (触发全量刷新)
        // [修复报错] 不要调用 super.getChangePayload，因为 Any 没有这个方法
        if (propertyWatchers.isEmpty()) {
            return null
        }

        // 2. 遍历检测属性变化
        val payloads = ArrayList<Any>()
        // 使用 indices 避免 iterator 创建，极致性能
        for (i in propertyWatchers.indices) {
            val watcher = propertyWatchers[i]
            val result = watcher.checkChange(oldItem, newItem)
            if (result != null) {
                payloads.add(result)
            }
        }

        // 3. 如果有属性变化，返回 List<Watcher>；否则返回 null
        return if (payloads.isNotEmpty()) payloads else null
    }

    /**
     * [新增] 供 DSL 和子类使用的通用注册方法 (解决 JavaDelegate 报错)
     */
    protected fun registerWatcher(watcher: Watcher<T>) {
        propertyWatchers.add(watcher)
    }

    /** 1 参数 */
    protected fun <P> registerDataWatcher(getter: (T) -> P, action: VH.(P) -> Unit) {
        registerWatcher(PropertyWatcher1(getter, action))
    }

    /** 2 参数 */
    protected fun <P1, P2> registerDataWatcher(
        g1: (T) -> P1, g2: (T) -> P2,
        action: VH.(P1, P2) -> Unit
    ) {
        registerWatcher(PropertyWatcher2(g1, g2, action))
    }

    /** 3 参数 */
    protected fun <P1, P2, P3> registerDataWatcher(
        g1: (T) -> P1, g2: (T) -> P2, g3: (T) -> P3,
        action: VH.(P1, P2, P3) -> Unit
    ) {
        registerWatcher(PropertyWatcher3(g1, g2, g3, action))
    }

    /** 4 参数 */
    protected fun <P1, P2, P3, P4> registerDataWatcher(
        g1: (T) -> P1, g2: (T) -> P2, g3: (T) -> P3, g4: (T) -> P4,
        action: VH.(P1, P2, P3, P4) -> Unit
    ) {
        registerWatcher(PropertyWatcher4(g1, g2, g3, g4, action))
    }

    /** 5 参数 */
    protected fun <P1, P2, P3, P4, P5> registerDataWatcher(
        g1: (T) -> P1, g2: (T) -> P2, g3: (T) -> P3, g4: (T) -> P4, g5: (T) -> P5,
        action: VH.(P1, P2, P3, P4, P5) -> Unit
    ) {
        registerWatcher(PropertyWatcher5(g1, g2, g3, g4, g5, action))
    }

    /** 6 参数 */
    protected fun <P1, P2, P3, P4, P5, P6> registerDataWatcher(
        g1: (T) -> P1, g2: (T) -> P2, g3: (T) -> P3, g4: (T) -> P4, g5: (T) -> P5, g6: (T) -> P6,
        action: VH.(P1, P2, P3, P4, P5, P6) -> Unit
    ) {
        registerWatcher(PropertyWatcher6(g1, g2, g3, g4, g5, g6, action))
    }

    /**
     * [修复报错] 统一分发逻辑
     */
    protected fun dispatchHandledPayloads(holder: VH, item: T, payloads: List<Any>): Boolean {
        if (propertyWatchers.isEmpty()) return false

        var handled = false
        for (rawPayload in payloads) {
            val items = if (rawPayload is List<*>) rawPayload else listOf(rawPayload)
            for (p in items) {
                // 判断是否是我们的 Watcher
                if (p is Watcher<*>) {
                    @Suppress("UNCHECKED_CAST")
                    (p as Watcher<T>).execute(holder, item)
                    handled = true
                }
            }
        }
        return handled
    }

    // Diff 相关 (默认实现)
    open fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem

    // UI 相关 (必须实现)
    abstract fun onCreateViewHolder(parent: ViewGroup): VH
    abstract fun onBindViewHolder(holder: VH, item: T, position: Int, payloads: MutableList<Any>)

    // 生命周期 (可选)
    open fun onViewRecycled(holder: VH) {}
    open fun onViewAttachedToWindow(holder: VH) {}
    open fun onViewDetachedFromWindow(holder: VH) {}
}