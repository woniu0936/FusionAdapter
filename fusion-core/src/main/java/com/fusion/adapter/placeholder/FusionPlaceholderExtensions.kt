package com.fusion.adapter.placeholder

import com.fusion.adapter.FusionAdapter
import com.fusion.adapter.FusionListAdapter

// ============================================================================================
// FusionListAdapter Extensions
// ============================================================================================

/**
 * [DX 极致] 一键展示骨架屏
 * 适用于：普通列表网络请求开始前
 */
fun FusionAdapter.showSkeleton(count: Int = 10) {
    // 直接塞入 FusionPlaceholder 对象，底层会自动路由到 SkeletonDelegate
    this.setItems(List(count) { FusionPlaceholder() })
}

/**
 * [DX 极致] 隐藏骨架屏
 * 其实就是清空列表，或者直接 setItems(realData) 覆盖即可，这个方法为了语义完整性
 */
fun FusionAdapter.hideSkeleton() {
    this.setItems(emptyList())
}

// FusionListAdapter (DiffUtil) 版本
fun FusionListAdapter.showSkeleton(count: Int = 10) {
    this.submitList(List(count) { FusionPlaceholder() })
}