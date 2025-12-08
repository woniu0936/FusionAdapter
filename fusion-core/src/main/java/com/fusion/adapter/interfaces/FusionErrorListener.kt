package com.fusion.adapter.interfaces


/**
 * [异常监听接口]
 * SAM 接口，支持 Java Lambda 和 Kotlin Lambda。
 */
fun interface FusionErrorListener {
    fun onError(item: Any, e: Exception)
}