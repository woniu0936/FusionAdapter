package com.fusion.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.core.FusionCore
import com.fusion.adapter.core.FusionLinker
import com.fusion.adapter.delegate.FusionItemDelegate

/**
 * [FusionAdapter] - 手动挡
 *
 * 不使用 DiffUtil，完全由开发者控制数据的刷新。
 * 适用于：
 * 1. 静态列表 (Settings 页面)
 * 2. 追求极致性能的简单列表
 * 3. 需要精确控制 notifyItemMoved 等动画的场景 (拖拽排序)
 */
open class FusionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 核心引擎
    private val core = FusionCore(this)

    // 内部数据持有
    private val items = ArrayList<Any>()

    // ========================================================================================
    // 注册接口 (API)
    // ========================================================================================

    /** [KTX 专用接口] 注册路由连接器 */
    fun <T : Any> registerLinker(clazz: Class<T>, linker: FusionLinker<T>) {
        core.register(clazz, linker)
    }

    /** [Java/普通接口] 注册单类型委托 (一对一) */
    fun <T : Any> register(clazz: Class<T>, delegate: FusionItemDelegate<T, *>) {
        val linker = FusionLinker<T>()
        linker.map(Unit, delegate)
        core.register(clazz, linker)
    }

    // ========================================================================================
    // 数据操作 (Manual)
    // ========================================================================================

    fun setItems(newItems: List<Any>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItems(newItems: List<Any>) {
        val start = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(start, newItems.size)
    }

    fun insertItem(position: Int, item: Any) {
        items.add(position, item)
        notifyItemInserted(position)
    }

    fun removeItem(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getItem(position: Int): Any = items[position]

    // ========================================================================================
    // RecyclerView.Adapter 实现委托
    // ========================================================================================

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return core.getItemViewType(items[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return core.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        core.onBindViewHolder(holder, items[position], position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            core.onBindViewHolder(holder, items[position], position, payloads)
        }
    }

    // --- 生命周期分发 ---
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) = core.onViewRecycled(holder)
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) = core.onViewAttachedToWindow(holder)
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) = core.onViewDetachedFromWindow(holder)
}