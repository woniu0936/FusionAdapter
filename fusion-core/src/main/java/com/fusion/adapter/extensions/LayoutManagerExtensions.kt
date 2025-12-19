package com.fusion.adapter.extensions

import androidx.annotation.RestrictTo
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.placeholder.FusionPlaceholder

/**
 * [性能关键] 自动为 ViewHolder 注入 Staggered 全屏支持。
 *
 * 优化点：
 * 1. 使用 inline 避免在 onBindViewHolder 高频调用时创建 Function 对象。
 * 2. 只有当 LayoutParams 类型匹配且状态确实需要变更时，才执行赋值，减少 requestLayout 开销。
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun RecyclerView.ViewHolder.attachFusionStaggeredSupport(
    item: Any?,
    getDelegate: (Any) -> FusionDelegate<Any, *>?
) {
    // 1. 快速检查：如果 item 为空，直接返回，避免后续类型转换开销
    if (item == null) return

    val lp = itemView.layoutParams
    // 2. 类型检查
    if (lp is StaggeredGridLayoutManager.LayoutParams) {
        // 3. 只有在确定是 Staggered 布局时，才调用 getDelegate Lambda
        // 由于是 inline，这里的 Lambda 执行是零开销的
        val delegate = getDelegate(item) ?: return

        val shouldFullSpan = delegate.resolveFullSpan(item)

        // 4. 脏检查：防止重复赋值导致不必要的 UI 重绘
        if (lp.isFullSpan != shouldFullSpan) {
            lp.isFullSpan = shouldFullSpan
        }
    }
}

/**
 * 自动为 RecyclerView 注入 Grid 跨度支持。
 *
 * 注意：此函数不使用 inline。
 * 原因：getDelegate Lambda 需要被 SpanSizeLookup 匿名内部类持有（Capture & Store），
 * 因此它必须是一个堆对象，无法被内联。且此函数仅调用一次，无性能瓶颈。
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun RecyclerView.attachFusionGridSupport(
    adapter: RecyclerView.Adapter<*>,
    getItem: (Int) -> Any?,
    getDelegate: (Any) -> FusionDelegate<Any, *>?
) {
    val layoutManager = this.layoutManager
    if (layoutManager is GridLayoutManager) {
        // 使用 Adapter 的 HashCode 或 ID 作为缓存 Key 可能更好，但这里直接覆盖是安全的
        // 因为这是 onAttach 阶段

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (position == RecyclerView.NO_POSITION || position >= adapter.itemCount) return 1

                val item = getItem(position) ?: FusionPlaceholder
                // 这里的 getDelegate 是一个闭包对象调用
                val delegate = getDelegate(item) ?: return 1

                return delegate.resolveSpanSize(item, position, layoutManager.spanCount)
            }
        }
    }
}