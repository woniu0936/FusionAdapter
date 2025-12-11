package com.fusion.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.internal.ClassSignature
import com.fusion.adapter.internal.ViewSignature

/**
 * [默认兜底委托]
 * 当 Fusion 在生产环境遇到未注册的类型时，使用此委托。
 * 它的作用是创建一个高度为 0 的 View (GONE)，让该 Item 在列表中“消失”，从而避免 Crash。
 */
class FallbackDelegate() : FusionDelegate<Any, RecyclerView.ViewHolder>() {

    override val signature: ViewSignature = ClassSignature(this::class.java)

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = View(parent.context)
        // 设置宽高为 0，确保不占用布局空间
        view.layoutParams = ViewGroup.LayoutParams(0, 0)
        view.visibility = View.GONE
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        item: Any,
        position: Int,
        payloads: MutableList<Any>
    ) {
        // Do nothing intentionally
        // 兜底视图不需要绑定任何数据
    }
}