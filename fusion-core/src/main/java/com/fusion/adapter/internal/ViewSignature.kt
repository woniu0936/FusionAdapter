package com.fusion.adapter.internal

/**
 * [ViewSignature]
 * 视图签名接口。
 * 用于定义 FusionDelegate 的唯一性身份。
 */
interface ViewSignature {
    // 接口仅作为标记，具体实现负责重写 equals/hashCode
}

/**
 * [DslSignature]
 * 专用于 DSL 模式。
 * 唯一性由 <数据类型, 布局类型> 决定。
 */
data class DslSignature(
    val itemClass: Class<*>,
    val identifier: Any
) : ViewSignature

/**
 * [ClassSignature]
 * 专用于手动继承模式。
 * 唯一性由 Delegate 自身的 Class 对象决定。
 */
data class ClassSignature(
    val delegateClass: Class<*>
) : ViewSignature