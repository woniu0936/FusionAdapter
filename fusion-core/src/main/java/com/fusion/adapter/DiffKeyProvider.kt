package com.fusion.adapter

/**
 * [路由 Key 提取器]
 * SAM 接口，支持 Java Lambda 和 Kotlin Lambda。
 */
fun interface DiffKeyProvider<T> {
    fun map(item: T): Any?
}