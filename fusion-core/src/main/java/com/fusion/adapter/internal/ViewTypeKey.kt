package com.fusion.adapter.internal

/**
 * 标识 ViewType 唯一性的接口。
 * 实现类必须正确实现 equals 和 hashCode。
 */
interface ViewTypeKey

/**
 * [V1.0 架构核心] 全局类型 Key。
 *
 * 通过组合 "数据类型" + "视图特征" 来自动生成唯一的 ViewType ID。
 * 只要这两个特征一致，无论在哪个 Adapter 实例中，都会映射到同一个 ID，从而支持 RecycledViewPool 共享。
 *
 * @param primary 核心分类，通常是数据模型的 Class 对象 (如 User::class.java)
 * @param secondary 视图特征，可以是 ViewBinding 的 Class，布局 ID (Int)，或者是 Delegate 自身的 Class。
 */
data class GlobalTypeKey(
    val primary: Class<*>,
    val secondary: Any
) : ViewTypeKey
