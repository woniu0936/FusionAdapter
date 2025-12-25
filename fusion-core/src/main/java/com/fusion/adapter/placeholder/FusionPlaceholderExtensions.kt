package com.fusion.adapter.placeholder

import com.fusion.adapter.FusionAdapter
import com.fusion.adapter.FusionListAdapter

/**
 * [替换模式] 显示占位符（骨架屏）
 * 会清空当前列表所有内容，并替换为指定数量的占位符。
 *
 * 场景：页面首次进入，全屏 loading。
 *
 * @param count 占位符数量，默认为 5
 */
fun FusionListAdapter.showPlaceholders(count: Int = 5) {
    val placeholders = ArrayList<Any>(count)
    repeat(count) {
        placeholders.add(FusionPlaceholder())
    }
    this.submitList(placeholders)
}

/**
 * [追加模式] 在列表尾部追加占位符
 * 保留当前已有数据，在末尾添加占位符。
 *
 * 场景：上拉加载更多时，在底部显示 loading 条目。
 *
 * @param count 追加的数量，默认为 1
 */
fun FusionListAdapter.appendPlaceholders(count: Int = 1) {
    val currentList = this.currentList
    val newList = ArrayList<Any>(currentList.size + count)
    newList.addAll(currentList)
    repeat(count) {
        newList.add(FusionPlaceholder())
    }
    this.submitList(newList)
}

/**
 * [移除模式] 移除列表中所有的占位符
 * 只保留真实数据。
 *
 * 场景：加载失败或加载结束，需要去掉 loading 条目。
 */
fun FusionListAdapter.clearPlaceholders() {
    val currentList = this.currentList
    // 如果没有占位符，直接返回，避免触发多余的 Diff 计算
    if (currentList.none { it is FusionPlaceholder }) return

    val newList = currentList.filter { it !is FusionPlaceholder }
    this.submitList(newList)
}

/**
 * [替换模式] 显示占位符
 */
fun FusionAdapter.showPlaceholders(count: Int = 5) {
    val placeholders = ArrayList<Any>(count)
    repeat(count) {
        placeholders.add(FusionPlaceholder())
    }
    this.setItems(placeholders)
}

/**
 * [追加模式] 追加占位符
 */
fun FusionAdapter.appendPlaceholders(count: Int = 1) {
    val currentList = this.currentItems // 假设 FusionAdapter 公开了 currentItems
    val newList = ArrayList<Any>(currentList.size + count)
    newList.addAll(currentList)
    repeat(count) {
        newList.add(FusionPlaceholder())
    }
    this.setItems(newList)
}

/**
 * [移除模式] 移除所有占位符
 */
fun FusionAdapter.clearPlaceholders() {
    val currentList = this.currentItems
    if (currentList.none { it is FusionPlaceholder }) return

    val newList = currentList.filter { it !is FusionPlaceholder }
    this.setItems(newList)
}