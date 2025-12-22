package com.fusion.adapter.placeholder

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.internal.ClassSignature
import com.fusion.adapter.internal.ViewSignature

/**
 * 专用于处理 Placeholder 的 Delegate 基类。
 * 泛型改为 VH，使其既支持 ViewBinding 也支持 LayoutRes。
 */
abstract class FusionPlaceholderDelegate<VH : RecyclerView.ViewHolder> : FusionDelegate<Any, VH>() {

    // Placeholder 依然通过 ViewType ID 匹配，Signature 保持自身 Class 即可
    override val viewTypeKey: ViewSignature = ClassSignature(this::class.java)

    abstract fun onCreatePlaceholderViewHolder(parent: ViewGroup): VH

    final override fun onCreateViewHolder(parent: ViewGroup): VH {
        return onCreatePlaceholderViewHolder(parent)
    }

    final override fun onBindViewHolder(holder: VH, item: Any, position: Int, payloads: MutableList<Any>) {
        onBindPlaceholder(holder)
    }

    open fun onBindPlaceholder(holder: VH) {}

    /**
     * [Stable ID 支持]
     * 直接返回 Placeholder 实例自带的唯一 ID。
     */
    override fun getStableId(item: Any): Any? {
        if (item is FusionPlaceholder) {
            return item.id
        }
        return super.getStableId(item)
    }

}