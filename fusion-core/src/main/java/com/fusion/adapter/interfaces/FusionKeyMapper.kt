package com.fusion.adapter.interfaces

/**
 * [路由 Key 提取器]
 * SAM 接口，支持 Java Lambda 和 Kotlin Lambda。
 */
fun interface FusionKeyMapper<T> {
    fun map(item: T): Any?
}