package com.fusion.adapter.delegate

import androidx.viewbinding.ViewBinding
import com.fusion.adapter.internal.DslSignature
import com.fusion.adapter.internal.ViewSignature

/**
 * [功能性委托]
 * 这是一个具体的类 (Concrete Class)。
 *
 * 设计目的：
 * 1. 解决泛型擦除 Crash：它是一个泛型明确的类，JVM 不会混淆。
 * 2. 支持 DSL：它开放了 Lambda 属性供外部注入逻辑。
 * 3. 简化使用：不需要 Wrapper，直接传参。
 */
@PublishedApi
internal class FunctionalBindingDelegate<T : Any, VB : ViewBinding>(
    private val dslSignature: DslSignature,
    private val inflater: BindingInflater<VB>
) : BindingDelegate<T, VB>(inflater) {

    // 1. 数据绑定 Lambda
    var onBind: (VB.(item: T, position: Int) -> Unit)? = null

    // 2. Payload Lambda (注意类型 MutableList)
    var onBindPayload: (VB.(item: T, position: Int, payloads: MutableList<Any>) -> Unit)? = null

    // 3. Diff Lambda
    var onContentSame: ((old: T, new: T) -> Boolean)? = null

    var onCreate: (VB.() -> Unit)? = null

    override val signature: ViewSignature = dslSignature

    override fun onBind(binding: VB, item: T, position: Int) {
        onBind?.invoke(binding, item, position)
    }

    override fun onViewHolderCreated(holder: BindingHolder<VB>) {
        // 调用 DSL 传入的初始化逻辑
        onCreate?.invoke(holder.binding)
    }

    override fun onBindPayload(binding: VB, item: T, position: Int, payloads: MutableList<Any>) {
        if (onBindPayload != null) {
            onBindPayload?.invoke(binding, item, position, payloads)
        } else {
            super.onBindPayload(binding, item, position, payloads)
        }
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return onContentSame?.invoke(oldItem, newItem)
            ?: super.areContentsTheSame(oldItem, newItem)
    }

}
