package com.fusion.adapter.paging

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView

/**
 * 默认的 Paging 占位符 ViewHolder。
 * 当 Paging 开启 placeholders 但 Core 中未注册对应的 Linker 时使用。
 *
 * 默认行为：展示一个不可见的 View，占据布局位置 (如果 Paging 提供了数据)
 * 注意：通常建议用户显式注册 Placeholder 类型的 Linker 以展示骨架屏。
 */
class FusionPlaceholderViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    FrameLayout(parent.context).apply {
        // 设置一个默认参数，防止完全塌陷，但实际上 Paging 的 placeholder
        // 应该由用户自定义布局来决定高度
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // 默认设置为不可见，或者根据需求设置为骨架屏背景
        // visibility = View.INVISIBLE
    }
)
