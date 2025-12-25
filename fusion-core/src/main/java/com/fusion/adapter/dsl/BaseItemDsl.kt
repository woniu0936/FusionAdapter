package com.fusion.adapter.dsl

import androidx.annotation.RestrictTo
import kotlin.reflect.KProperty1
import com.fusion.adapter.internal.*

/**
 * [BaseItemDsl]
 * 顶级抽象 DSL 基类。
 * 实现了 Binding 模式和 Layout 模式共有的所有语法特性。
 *
 * @param T 数据类型
 * @param V View载体类型 (ViewBinding 或 LayoutHolder)
 */
@FusionDsl
abstract class BaseItemDsl<T : Any, V : Any> {

    // 内部持有配置，外部不可见
    @PublishedApi
    internal val config = ItemConfiguration<T, V>()

    /**
     * [架构修复] 跨模块访问桥梁
     * 允许 fusion-paging 等子模块获取配置，但对普通用户隐藏。
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun getConfiguration(): ItemConfiguration<T, V> {
        return config
    }

    // ============================================================================================
    // Core Identity & Diff
    // ============================================================================================

    /**
     * 配置非侵入式的 Stable ID。
     * 用于 DiffUtil 识别 Item 身份，极大提升性能。
     */
    fun stableId(block: (item: T) -> Any?) {
        config.stableId = block
    }

    /**
     * 自定义 Diff 内容比对逻辑。
     * return true 表示内容未变，无需刷新。
     */
    fun areContentsTheSame(block: (old: T, new: T) -> Boolean) {
        config.diffCallback = block
    }

    // ============================================================================================
    // Lifecycle
    // ============================================================================================

    /**
     * View 创建时的初始化逻辑 (只执行一次)。
     * 适合设置固定的 Listener、背景、字体等。
     */
    fun onCreate(block: V.() -> Unit) {
        config.onCreate = block
    }

    // ============================================================================================
    // Data Binding (Strict API)
    // ============================================================================================

    /**
     * [全量更新]
     * 仅在 Item 首次绑定或全量刷新时调用。
     */
    fun onBind(block: V.(item: T) -> Unit) {
        config.onBind = { item, _ -> block(item) }
    }

    /**
     * [全量更新 - 带位置]
     */
    fun onBindIndexed(block: V.(item: T, position: Int) -> Unit) {
        config.onBind = block
    }

    /**
     * [增量更新] (已改名，解决重载歧义)
     * 仅在 payloads 不为空时调用。
     * **注意**：配置此方法不会覆盖 [onBind]，两者共存。
     */
    fun onBindWithPayloads(block: V.(item: T, payloads: List<Any>) -> Unit) {
        config.onBindWithPayloads = { item, _, payloads -> block(item, payloads) }
    }

    // ============================================================================================
    // Property Watchers (Smart Payload)
    // ============================================================================================

    // 1 参数
    fun <P> onBindPayload(prop: KProperty1<T, P>, action: V.(P) -> Unit) {
        config.watchers.add(PropertyWatcher1(prop, action))
    }

    // 2 参数
    fun <P1, P2> onBindPayload(
        p1: KProperty1<T, P1>, p2: KProperty1<T, P2>,
        action: V.(P1, P2) -> Unit
    ) {
        config.watchers.add(PropertyWatcher2(p1, p2, action))
    }

    // ... 3, 4, 5, 6 参数的重载逻辑同上，省略以节省篇幅，实际代码中应补全 ...

    // ============================================================================================
    // Interactions
    // ============================================================================================

    fun onClick(debounceMs: Long? = null, block: (item: T) -> Unit) {
        config.clickDebounce = debounceMs
        config.onClick = { _, item, _ -> block(item) }
    }

    fun onClickIndexed(
        debounceMs: Long? = null,
        block: (scope: V, item: T, position: Int) -> Unit
    ) {
        config.clickDebounce = debounceMs
        config.onClick = block
    }

    fun onLongClick(block: (item: T) -> Boolean) {
        config.onLongClick = { _, item, _ -> block(item) }
    }

    // ============================================================================================
    // Layout Control (Grid / Staggered)
    // ============================================================================================

    /**
     * [Grid] 设置跨度。
     */
    fun spanSize(block: SpanScope.(item: T, position: Int) -> Int) {
        config.spanSize = { item, pos, scope -> scope.block(item, pos) }
    }

    /**
     * [Grid] 固定跨度。
     */
    fun spanSize(count: Int) {
        config.spanSize = { _, _, _ -> count }
    }

    /**
     * [Universal] 强制占满全屏。
     */
    fun fullSpan() {
        config.spanSize = { _, _, scope -> scope.totalSpans }
    }

    /**
     * [Universal] 条件性占满全屏。
     */
    fun fullSpanIf(condition: (item: T) -> Boolean) {
        config.spanSize = { item, _, scope ->
            if (condition(item)) scope.totalSpans else 1
        }
    }
}