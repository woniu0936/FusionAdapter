@file:JvmName("FusionViewUtil") // 👈 关键：Java 调用时类名为 FusionViewUtil
package com.fusion.adapter.internal

import android.os.SystemClock
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.Fusion
import com.fusion.adapter.core.R

/**
 * [FusionViewUtil]
 * FusionAdapter 的视图交互基础设施。
 *
 * 设计目标：
 * 1. 统一管理点击防抖逻辑。
 * 2. 提供 Java/Kotlin 双向友好的 API。
 * 3. 预留未来的扩展性 (如长按防抖、双击检测)。
 */

// ============================================================================================
// 1. Core Implementation (核心实现) - 策略模式的落地
// ============================================================================================

/**
 * 默认防抖时间 (ms)。
 * 未来可以从 FusionConfig.globalDebounceInterval 读取。
 */
@PublishedApi
internal const val DEFAULT_DEBOUNCE_INTERVAL = 500L

/**
 * [DebouncedClickListener]
 * 一个轻量级、无状态依赖的防抖包装器。
 *
 * @param intervalMs 防抖阈值
 * @param originListener 原始点击逻辑 (支持 Java 的 OnClickListener 接口)
 */
private class DebouncedClickListener(
    private val intervalMs: Long,
    private val originListener: View.OnClickListener
) : View.OnClickListener {

    private var lastClickTime = 0L

    override fun onClick(v: View) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickTime >= intervalMs) {
            lastClickTime = now
            originListener.onClick(v)
        }
    }
}

// ============================================================================================
// 2. Public API Surface (公共 API) - 兼顾 Java 和 Kotlin
// ============================================================================================

/**
 * 获取当前的生效防抖时间。
 * 如果传入 null 或负数，则使用全局配置。
 */
internal fun resolveDebounce(interval: Long?): Long {
    return if (interval != null && interval >= 0) {
        interval
    } else {
        Fusion.getConfig().globalDebounceInterval
    }
}

/**
 * [Java/Kotlin 通用入口] 设置防抖点击事件。
 *
 * Java Usage:
 * FusionViewUtil.setOnClick(view, v -> { ... });
 * FusionViewUtil.setOnClick(view, 1000L, v -> { ... });
 *
 * @param view 目标 View
 * @param intervalMs 防抖时间 (可选，默认 500ms)
 * @param listener 标准的 OnClickListener
 */
@JvmOverloads
fun setOnClick(
    view: View,
    intervalMs: Long? = null,
    listener: View.OnClickListener?
) {
    if (listener == null) {
        view.setOnClickListener(null)
        return
    }

    val finalInterval = resolveDebounce(intervalMs)
    // 0 或负数视为不需要防抖，直接设置
    if (finalInterval <= 0) {
        view.setOnClickListener(listener)
    } else {
        // 包装原始 Listener
        view.setOnClickListener(DebouncedClickListener(finalInterval, listener))
    }
}

// ============================================================================================
// 3. Kotlin Extensions (Kotlin 扩展) - 语法糖
// ============================================================================================

/**
 * [Kotlin DSL] 点击事件。
 *
 * Usage:
 * view.click { ... }
 * view.click(1000L) { ... }
 */
inline fun View.click(
    intervalMs: Long? = null,
    crossinline block: (View) -> Unit
) {
    // 复用通用入口逻辑，保持行为一致性
    setOnClick(this, intervalMs) { v -> block(v) }
}

/**
 * [Kotlin DSL] 甚至支持直接传入 OnClickListener 对象
 *
 * Usage:
 * view.click(myListener)
 */
fun View.click(
    intervalMs: Long? = null,
    listener: View.OnClickListener
) {
    setOnClick(this, intervalMs, listener)
}

/**
 * 向上遍历视图树，查找绑定在当前 View 或其祖先 View 上的 Fusion Item 数据。
 *
 * 使用 inline + reified 实现类型安全的自动转换。
 *
 * 示例:
 * ```
 * val user = view.findFusionItem<User>() // 自动推断并安全转换
 * ```
 *
 * @return 绑定的数据对象，如果未找到或类型不匹配则返回 null。
 */
inline fun <reified T> View.findItem(): T? {
    // 1. 调用内部非内联函数获取 Any? (避免逻辑代码膨胀)
    val item = findItemInternal()

    // 2. 利用 reified 进行安全的类型转换 (instanceof check)
    return item as? T
}

/**
 * 内部实现：视图树遍历逻辑。
 *
 * 注意：此函数 **不是** inline 的。
 * 这样做是为了防止 `while` 循环逻辑被复制到每个调用处，减少 Bytecode 体积。
 */
@PublishedApi
internal fun View.findItemInternal(): Any? {
    var current: View? = this
    while (current != null) {
        val tag = current.getTag(R.id.fusion_item_tag)
        if (tag != null) {
            return tag
        }

        val parent = current.parent
        current = if (parent is View) parent else null
    }
    return null
}

// ============================================================================================
// ViewHolder Extensions
// ============================================================================================

/**
 * 获取当前 ViewHolder 绑定的 Fusion 数据对象。
 */
inline fun <reified T> RecyclerView.ViewHolder.findItem(): T? {
    val item = itemView.findItemInternal()
    return item as? T
}

// ============================================================================================
// ViewBinding Extensions
// ============================================================================================

/**
 * 获取当前 ViewBinding (Root View) 绑定的 Fusion 数据对象。
 *
 * 极大简化了在 BindingDelegate 中的数据获取代码。
 * 示例: binding.findFusionItem<User>()
 */
inline fun <reified T> ViewBinding.findItem(): T? {
    // Binding 的 root view 承载着数据 (或者其父级)
    val item = root.findItemInternal()
    return item as? T
}
