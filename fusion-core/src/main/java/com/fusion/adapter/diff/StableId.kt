package com.fusion.adapter.diff

/**
 * [StableId]
 * 唯一 ID 接口。建议所有用于 FusionAdapter 的数据模型实现此接口。
 *
 * 作用：
 * 1. 帮助 DiffUtil 精确判断 `areItemsTheSame`（是否是同一个数据的不同状态）。
 * 2. 解决列表刷新时的闪烁问题。
 *
 * @sample
 * data class User(val uid: String, val name: String) : FusionStableId {
 *     override val stableId: Any = uid
 * }
 */
interface StableId {
    val stableId: Any
}