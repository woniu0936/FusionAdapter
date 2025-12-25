package com.fusion.adapter.paging

import androidx.lifecycle.Lifecycle
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.placeholder.FusionPlaceholder

// =================================================================
// Paging Extensions
// =================================================================

/**
 * [Quick Setup] Initialize FusionPagingAdapter and attach to RecyclerView.
 */
inline fun <reified T : Any> RecyclerView.setupFusionPaging(
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context),
    noinline block: (FusionPagingAdapter<T>.() -> Unit)? = null
): FusionPagingAdapter<T> {
    this.layoutManager = layoutManager
    val adapter = FusionPagingAdapter<T>()
    block?.invoke(adapter)
    this.adapter = adapter
    return adapter
}

/**
 * [Paging专用 - 替换模式] 显示占位符（骨架屏）
 *
 * 仅适用于泛型为 Any 的 Adapter (FusionPagingAdapter<Any>)。
 * 如果是强类型 (如 User)，请使用 Paging3 原生的 enablePlaceholders = true 配置。
 *
 * @param lifecycle Paging3 提交数据需要 Lifecycle (用于管理协程作用域)
 * @param count 占位符数量，默认 5
 */
fun FusionPagingAdapter<Any>.showPlaceholders(lifecycle: Lifecycle, count: Int = 5) {
    val placeholders = List<Any>(count) { FusionPlaceholder() }
    val pagingData = PagingData.from(placeholders)
    this.submitData(lifecycle, pagingData)
}

/**
 * [Paging专用 - 替换模式] 显示占位符 (Suspend 版本)
 *
 * 适用于在协程环境中调用
 */
suspend fun FusionPagingAdapter<Any>.showPlaceholders(count: Int = 5) {
    val placeholders = List<Any>(count) { FusionPlaceholder() }
    val pagingData = PagingData.from(placeholders)
    this.submitData(pagingData)
}

/**
 * [Paging专用 - 清空模式] 清空列表
 *
 * 相当于显示空白页
 */
fun FusionPagingAdapter<*>.clear(lifecycle: Lifecycle) {
    this.submitData(lifecycle, PagingData.empty())
}
