package com.fusion.adapter.placeholder

import androidx.viewbinding.ViewBinding
import com.fusion.adapter.dsl.ViewBindingDsl

/**
 * [架构升级] 骨架屏专用 DSL
 * 屏蔽了 item 参数，让 onBind 只关注 View 本身。
 */
class SkeletonDsl<VB : ViewBinding> : ViewBindingDsl<Any, VB>() {

    /**
     * 覆盖父类方法，提供无参的 onBind。
     * 在骨架屏场景下，没有任何数据需要绑定，只有 UI 状态（如启动动画）。
     */
    fun onBind(block: VB.() -> Unit) {
        // 桥接到父类的通用配置，忽略 item
        config.onBind = { _, _ -> block() }
    }

    // 屏蔽点击事件（骨架屏通常不可点击）
    // 如果确实需要，用户依然可以调用父类的 onClick，但不推荐
}