package com.fusion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion.adapter.delegate.BindingHolder
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.placeholder.FusionPlaceholderDelegate
import com.fusion.adapter.delegate.LayoutHolder
import com.fusion.adapter.extensions.attachFusionGridSupport
import com.fusion.adapter.extensions.attachFusionStaggeredSupport
import com.fusion.adapter.internal.AdapterController
import com.fusion.adapter.internal.TypeRouter
import com.fusion.adapter.internal.checkStableIdRequirement
import com.fusion.adapter.internal.mapToRecyclerViewId
import java.util.Collections

/**
 * [FusionAdapter] - 手动挡
 *
 * 不使用 DiffUtil，完全由开发者控制数据的刷新。
 * 适用于：
 * 1. 静态列表 (Settings 页面)
 * 2. 追求极致性能的简单列表
 * 3. 需要精确控制 notifyItemMoved 等动画的场景 (拖拽排序)
 */
open class FusionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), RegistryOwner {

    // 核心引擎
    @PublishedApi
    internal val core = AdapterController()

    // 内部数据持有
    private val items = ArrayList<Any>()

    /**
     * 获取当前实际渲染的数据列表 (Read-Only)
     * 这是经过所有拦截器处理、安全检查后的最终列表。
     * 即 RecyclerView 真正看到的列表。
     */
    val currentItems: List<Any>
        get() = Collections.unmodifiableList(items)

    init {
        if (Fusion.getConfig().defaultStableId) {
            setHasStableIds(true)
        }
    }

    // ========================================================================================
    // 注册接口 (API)
    // ========================================================================================

    /** [KTX 专用接口] 注册路由连接器 */
    override fun <T : Any> attachLinker(clazz: Class<T>, linker: TypeRouter<T>) {
        checkStableIdRequirement(this, clazz, linker.getAllDelegates(), core)
        core.register(clazz, linker)
    }

    /** [Java/普通接口] 注册单类型委托 (一对一) */
    fun <T : Any> attachDelegate(clazz: Class<T>, delegate: FusionDelegate<T, *>) {
        checkStableIdRequirement(this, clazz, listOf(delegate), core)
        val linker = TypeRouter<T>()
        linker.map(Unit, delegate)
        core.register(clazz, linker)
    }

    // ========================================================================================
    // 数据操作 (Manual)
    // ========================================================================================

    /**
     * 注册占位符 (ViewBinding 模式)
     */
    inline fun <reified VB : ViewBinding> registerPlaceholder(
        noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        crossinline onBind: (VB) -> Unit = {}
    ) {
        val delegate = object : FusionPlaceholderDelegate<BindingHolder<VB>>() {
            override fun onCreatePlaceholderViewHolder(parent: ViewGroup): BindingHolder<VB> {
                return BindingHolder(inflate(LayoutInflater.from(parent.context), parent, false))
            }

            override fun onBindPlaceholder(holder: BindingHolder<VB>) {
                onBind(holder.binding)
            }
        }
        core.registerPlaceholder(delegate)
    }

    /**
     * 注册占位符 (LayoutRes 模式)
     * 使用 LayoutHolder，与库中的 LayoutDelegate 保持一致。
     *
     * @param layoutResId 布局资源 ID
     * @param onBind 可选的绑定回调（用于初始化 View，如开始动画）
     */
    fun registerPlaceholder(
        @LayoutRes layoutResId: Int,
        onBind: (LayoutHolder.() -> Unit)? = null
    ) {
        val delegate = object : FusionPlaceholderDelegate<LayoutHolder>() {
            override fun onCreatePlaceholderViewHolder(parent: ViewGroup): LayoutHolder {
                val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
                return LayoutHolder(view)
            }

            override fun onBindPlaceholder(holder: LayoutHolder) {
                onBind?.invoke(holder)
            }
        }
        core.registerPlaceholder(delegate)
    }

    /**
     * ✅ Java 兼容：注册占位符实例
     * Java 用户可以通过 new FusionPlaceholderDelegate<Binding>() { ... } 来调用
     */
    fun registerPlaceholder(delegate: FusionPlaceholderDelegate<*>) {
        core.registerPlaceholder(delegate)
    }

    fun setItems(newItems: List<Any>) {
        // ✅ 清洗
        val safeItems = core.sanitize(newItems)
        items.clear()
        items.addAll(safeItems)
        notifyDataSetChanged()
    }

    fun addItems(newItems: List<Any>) {
        // ✅ 清洗
        val safeItems = core.sanitize(newItems)
        if (safeItems.isNotEmpty()) {
            val start = items.size
            items.addAll(safeItems)
            notifyItemRangeInserted(start, safeItems.size)
        }
    }

    fun insertItem(position: Int, item: Any) {
        // 单个 Item 也要清洗（检查是否支持）
        val safeList = core.sanitize(listOf(item))
        if (safeList.isNotEmpty()) {
            items.addAll(position, safeList)
            notifyItemRangeInserted(position, safeList.size)
        }
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

    override fun getItemId(position: Int): Long {
        // 1. 性能开关
        if (!hasStableIds()) return RecyclerView.NO_ID

        // 2. 边界检查
        if (position !in items.indices) return RecyclerView.NO_ID

        // 3. 获取数据与 Delegate
        val item = items[position]
        val delegate = core.getDelegate(item) ?: return RecyclerView.NO_ID

        // 4. [核心修改] 调用 Controller 的裁决方法
        // 它会依次查找：Delegate.getUniqueKey -> IdentityRegistry.getUniqueKey
        @Suppress("UNCHECKED_CAST")
        val rawKey = core.getStableId(item, delegate as FusionDelegate<Any, *>)

        // 5. 运行时安全兜底 (Safety Net)
        // 处理 Release 模式下配置缺失且无法关闭开关的极端情况
        if (rawKey == null) {
            return System.identityHashCode(item).toLong()
        }

        // 6. 极致数值转换
        return mapToRecyclerViewId(rawKey)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return core.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        holder.attachFusionStaggeredSupport(item) { core.getDelegate(it) }
        core.onBindViewHolder(holder, item, position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val item = items[position]
            // ★ 即使是局部刷新，也要确保布局参数正确 (以防 ViewHolder 复用或布局变动)
            holder.attachFusionStaggeredSupport(item) { core.getDelegate(it) }
            // 局部刷新
            core.onBindViewHolder(holder, item, position, payloads)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.attachFusionGridSupport(
            adapter = this,
            getItem = { pos -> if (pos in items.indices) items[pos] else null },
            getDelegate = { item -> core.getDelegate(item) }
        )
    }

    // --- 生命周期分发 ---
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) = core.onViewRecycled(holder)
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) = core.onViewAttachedToWindow(holder)
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) = core.onViewDetachedFromWindow(holder)
}