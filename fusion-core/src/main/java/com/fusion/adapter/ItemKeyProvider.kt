package com.fusion.adapter

/**
 * [ItemKeyProvider]
 * 负责从对象中提取 Key 或 ID。
 * SAM 接口，支持 Java Lambda 和 Kotlin Lambda。
 */
fun interface ItemKeyProvider<T> {
    fun getKey(item: T): Any?
}
