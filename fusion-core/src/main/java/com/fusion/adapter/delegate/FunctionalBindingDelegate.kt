package com.fusion.adapter.delegate

import androidx.viewbinding.ViewBinding
import com.fusion.adapter.dsl.DelegateDsl
import com.fusion.adapter.dsl.SpanScope
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

    // 2. Payload Lambda
    var onBindPayloadRaw: (VB.(item: T, position: Int, payloads: List<Any>) -> Unit)? = null

    // 3. Diff Lambda
    var onContentSame: ((old: T, new: T) -> Boolean)? = null

    var onCreate: (VB.() -> Unit)? = null

    override val signature: ViewSignature = dslSignature

    override fun onBind(binding: VB, item: T, position: Int) {
        onBind?.invoke(binding, item, position)
    }

    override fun onViewHolderCreated(binding: VB) {
        // 调用 DSL 传入的初始化逻辑
        onCreate?.invoke(binding)
    }

    override fun onBindPayload(
        binding: VB,
        item: T,
        position: Int,
        payloads: MutableList<Any>,
        handled: Boolean
    ) {
        // 1. 如果用户定义了 rawPayloadBlock，无条件执行 (给予最高优先级或作为补充)
        if (onBindPayloadRaw != null) {
            onBindPayloadRaw?.invoke(binding, item, position, payloads)
        }
        // 2. 否则，如果自动逻辑也没处理，才回退到 super (通常是全量 bind)
        else if (!handled) {
            super.onBindPayload(binding, item, position, payloads, handled)
        }
    }


    override fun onBindPayload(binding: VB, item: T, position: Int, payloads: MutableList<Any>) {
        if (onBindPayloadRaw != null) {
            onBindPayloadRaw?.invoke(binding, item, position, payloads)
        } else {
            super.onBindPayload(binding, item, position, payloads)
        }
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return onContentSame?.invoke(oldItem, newItem)
            ?: super.areContentsTheSame(oldItem, newItem)
    }

    /**
     * 将 DSL 配置应用到 Delegate 实例。
     * 这里负责将 DSL 中收集的点击事件和防抖参数，同步给父类 BindingDelegate。
     */
    fun applyDsl(dsl: DelegateDsl<T, VB>) {
        this.onBind = dsl.bindBlock
        this.onBindPayloadRaw = dsl.rawPayloadBlock
        this.onCreate = dsl.createBlock
        this.onContentSame = dsl.contentSameBlock
        // 注册所有 Watcher
        dsl.pendingWatchers.forEach { watcher ->
            registerWatcherFromDsl(watcher)
        }

        // 处理点击事件
        this.onItemClick = dsl.clickAction
        this.onItemLongClick = dsl.longClickAction

        // 处理防抖配置 (透传给父类 BindingDelegate)
        // DelegateDsl.clickDebounce 是 Long? (null 代表使用全局配置)
        // BindingDelegate.itemClickDebounceInterval 也是 Long?
        this.itemClickDebounceInterval = dsl.clickDebounce

        // 应用 Layout DSL
        dsl.spanSizeBlock?.let { dslBlock ->
            // 将带有 Scope 的 DSL Lambda 转换为底层需要的 (T, Int, Int) -> Int
            this.configSpanSize = { item, pos, total ->
                dslBlock(item, pos, SpanScope(total))
            }
        }

        dsl.fullSpanBlock?.let {
            this.configFullSpan = it
        }
    }

}
