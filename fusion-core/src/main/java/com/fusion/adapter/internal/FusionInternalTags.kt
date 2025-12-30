package com.fusion.adapter.internal

/**
 * [架构层设计]
 * 集中管理 GlobalTypeKey 中使用的 Secondary 标签。
 * 避免硬编码字符串散落在各个 Delegate 实现中，确保系统内部命名空间的统一性。
 */
internal object FusionInternalTags {
    // JavaDelegate 的默认标签
    const val TAG_JAVA_DELEGATE = "Fusion:JavaDelegate"

    // BindingDelegate 的默认标签
    const val TAG_BINDING_DELEGATE = "Fusion:BindingDelegate"

    // LayoutDelegate 的默认标签
    const val TAG_LAYOUT_DELEGATE = "Fusion:LayoutDelegate"

    // 占位符的默认标签
    const val TAG_PLACEHOLDER = "Fusion:Placeholder"

    // TypeDispatcher 自动生成的标签
    const val TAG_DISPATCHER_DEFAULT = "Fusion:DispatcherDefault"
    const val TAG_DISPATCHER_AUTO = "Fusion:DispatcherAuto"
}