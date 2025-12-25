package com.fusion.adapter.placeholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding

/**
 * 赋予 Adapter 骨架屏能力的接口
 */
interface SkeletonOwner {

    // 1. 最底层的注册 (面向高级用户/框架扩展)
    fun registerSkeletonDelegate(delegate: FusionPlaceholderDelegate<*>)

    // 2. 面向 Java 的 Layout ID 注册
    fun registerSkeleton(@LayoutRes layoutResId: Int)

    // 3. 面向 Kotlin 的极致 DSL
    fun <VB : ViewBinding> registerSkeleton(
        inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        block: (SkeletonDsl<VB>.() -> Unit)? = null
    )
}