package com.fusion.adapter.delegate

import androidx.annotation.LayoutRes
import com.fusion.adapter.dsl.LayoutDsl
import com.fusion.adapter.dsl.SpanScope
import com.fusion.adapter.internal.DslSignature
import com.fusion.adapter.internal.ViewSignature
import com.fusion.adapter.internal.Watcher

@PublishedApi
internal class FunctionalLayoutDelegate<T : Any>(
    private val dslSignature: DslSignature,
    @LayoutRes layoutResId: Int
) : LayoutDelegate<T>(layoutResId) {

    override val viewTypeKey: ViewSignature = dslSignature

    // 内部状态，对应 DSL 的配置
    private var onBindBlock: (LayoutHolder.(item: T, position: Int) -> Unit)? = null
    private var onBindPayloadRaw: (LayoutHolder.(item: T, position: Int, payloads: List<Any>) -> Unit)? = null
    private var onCreateBlock: (LayoutHolder.() -> Unit)? = null
    private var onContentSame: ((old: T, new: T) -> Boolean)? = null

    // 2. 实现 ViewHolder 创建回调
    override fun onViewHolderCreated(holder: LayoutHolder) {
        onCreateBlock?.invoke(holder)
    }

    // 3. 实现普通绑定
    override fun LayoutHolder.onBind(item: T) {
        onBindBlock?.invoke(this, item, bindingAdapterPosition)
    }

    // 4. 实现 Payload 绑定 (混合了 Watcher 和 Raw Payload)
    override fun onBindPayload(
        holder: LayoutHolder,
        item: T,
        position: Int,
        payloads: MutableList<Any>,
        handled: Boolean
    ) {
        if (onBindPayloadRaw != null) {
            // 如果定义了 raw payload 块，优先回调它
            onBindPayloadRaw?.invoke(holder, item, position, payloads)
        } else if (!handled) {
            // 如果 Watcher 没处理完，且没定义 raw block，走默认（即普通 onBind）
            super.onBindPayload(holder, item, position, payloads, handled)
        }
    }

    // 兼容旧签名
    override fun onBindPayload(holder: LayoutHolder, item: T, position: Int, payloads: MutableList<Any>) {
        if (onBindPayloadRaw != null) {
            onBindPayloadRaw?.invoke(holder, item, position, payloads)
        } else {
            super.onBindPayload(holder, item, position, payloads)
        }
    }

    // 5. 实现内容对比
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return onContentSame?.invoke(oldItem, newItem)
            ?: super.areContentsTheSame(oldItem, newItem)
    }

    // 6. 应用 DSL 配置 (核心入口)
    fun applyDsl(dsl: LayoutDsl<T>) {
        this.onBindBlock = dsl.bindBlock
        this.onBindPayloadRaw = dsl.rawPayloadBlock
        this.onCreateBlock = dsl.createBlock
        this.onContentSame = dsl.contentSameBlock

        dsl.idProviderBlock?.let { setStableId(it) }

        // 应用 Watchers
        dsl.pendingWatchers.forEach { watcher ->
            // 这里我们不需要像 BindingDelegate 那样包装一层，
            // 因为 LayoutDelegate 的 Watcher 泛型就是 <T, LayoutHolder>
            // 只需要确保 PropertyWatcher 的接收者类型匹配
            registerWatcherFromDsl(watcher)
        }

        // 应用点击事件
        dsl.clickAction?.let { action ->
            setOnItemClick(dsl.clickDebounce) { view, item, position ->
                action(view, item, position)
            }
        }
        dsl.longClickAction?.let { action ->
            setOnItemLongClick { view, item, position ->
                action(view, item, position)
            }
        }

        // 应用 Span 配置
        dsl.spanSizeBlock?.let { dslBlock ->
            this.configSpanSize = { item, pos, total ->
                dslBlock(item, pos, SpanScope(total))
            }
        }
        dsl.fullSpanBlock?.let {
            this.configFullSpan = it
        }
    }

    // 辅助方法：注册 Watcher
    private fun registerWatcherFromDsl(watcher: Watcher<T>) {
        // LayoutDelegate.registerWatcher 是 protected 的，需要通过内部机制调用
        // 但我们在 FusionDelegate 中通过 registerWatcher 暴露了。
        // 由于 Watcher 接口定义是 execute(receiver: Any, item: T)
        // 这里的 receiver 运行时就是 LayoutHolder，所以可以直接注册。
        super.registerWatcher(watcher)
    }
}