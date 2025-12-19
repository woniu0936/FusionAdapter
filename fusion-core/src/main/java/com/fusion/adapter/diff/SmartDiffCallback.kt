package com.fusion.adapter.diff

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

/**
 * [SmartDiffCallback]
 * 通用 Diff 策略，无需为每个页面单独写 DiffCallback。
 */
object SmartDiffCallback : DiffUtil.ItemCallback<Any>() {

    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        // 类型不同，绝对不是同一个 Item
        if (oldItem::class.java != newItem::class.java) return false

        // 兜底：如果没实现接口，降级为 equals 判断 (或引用判断)
        return oldItem == newItem
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        // 内容比对，直接使用 Data Class 的 equals
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Any, newItem: Any): Any? {
        // 默认不分发 Payload，如果需要局部刷新，建议在 Delegate 中实现 getChangePayload
        return null
    }
}