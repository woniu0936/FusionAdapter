package com.fusion.adapter.internal

/**
 * [ViewTypeKey]
 * 视图类型标识键。
 */
interface ViewTypeKey

/**
 * [DslTypeKey]
 */
data class DslTypeKey(
    val itemClass: Class<*>,
    val identifier: Any
) : ViewTypeKey

/**
 * [ClassTypeKey]
 */
data class ClassTypeKey(
    val delegateClass: Class<*>
) : ViewTypeKey
