package com.fusion.adapter.extensions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.core.R

/**
 * [click]
 * 核心 View 扩展。
 */
internal fun View.click(debounce: Long?, block: (View) -> Unit) {
    val safeDebounce = debounce ?: 500L
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0
        override fun onClick(v: View) {
            val now = System.currentTimeMillis()
            if (now - lastClickTime > safeDebounce) {
                lastClickTime = now
                block(v)
            }
        }
    })
}

/**
 * [getItem]
 * 核心语义：从 View 的 Tag 中直接提取关联的数据项。
 * 遵循 AndroidX O(1) 获取规范。
 */
@Suppress("UNCHECKED_CAST")
fun <T> View.getItem(): T? = getTag(R.id.fusion_item_tag) as? T

fun View.setItem(item: Any?) {
    setTag(R.id.fusion_item_tag, item)
}

fun <T> RecyclerView.ViewHolder.getItem(): T? = itemView.getItem()

fun RecyclerView.ViewHolder.setItem(item: Any?) {
    itemView.setItem(item)
}

