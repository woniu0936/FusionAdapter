package com.fusion.adapter.internal

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.Fusion
import com.fusion.adapter.FusionConfig
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.diff.SmartDiffCallback
import com.fusion.adapter.diff.StableId
import com.fusion.adapter.exception.UnregisteredTypeException
import com.fusion.adapter.placeholder.FusionPlaceholderViewHolder
import java.util.Collections

/**
 * [AdapterController]
 * 核心引擎门面。负责连接 Adapter 与 Registry，并处理生命周期分发。
 *
 * 特性：
 * 1. 管理 Registry (注册表)
 * 2. 注入 Adapter 引用到 Delegate
 * 3. 代理 DiffUtil 的内容比对逻辑
 */
class AdapterController {

    val registry = ViewTypeRegistry()

    /**
     * Sanitization
     * 职责：剔除未注册且无 Fallback 的数据，防止 LayoutManager 崩溃或错乱。
     * 性能：基于 Registry 缓存，耗时极低。
     * 策略：
     * 1. 检查 Registry 是否支持。
     * 2. 支持 -> 放行。
     * 3. 不支持：
     *    - Debug 模式 -> 直接抛出异常 Crash (Fail-Fast)。
     *    - Release 模式 -> 丢弃数据，并回调 ErrorListener 进行上报 (Fail-Safe + Observability)。
     */
    fun sanitize(rawList: List<Any>): List<Any> {
        if (rawList.isEmpty()) return rawList
        val config = Fusion.getConfig()
        var hasRemoved = false
        val safeList = ArrayList<Any>(rawList.size)

        for (item in rawList) {
            if (registry.isSupported(item)) {
                safeList.add(item)
            } else {
                handleUnregisteredItem(item, config)
                hasRemoved = true
            }
        }

        return if (hasRemoved) safeList else rawList
    }

    /**
     * 统一处理未注册数据的逻辑
     */
    private fun handleUnregisteredItem(item: Any, config: FusionConfig) {
        val exception = UnregisteredTypeException(item)

        if (config.isDebug) {
            // 🚨 Debug 模式：直接 Crash，强制开发者修复
            throw exception
        } else {
            // 🛡️ Release 模式：静默丢弃，但通过 ErrorListener 上报
            // 开发者可以接入 Firebase/Bugly 等进行追踪
            config.errorListener?.onError(item, exception)
            // 可选：在 Logcat 留个底，方便本地查看 Release 包日志
            Log.e("Fusion", "⚠️ [Sanitizer] Dropped unregistered item: ${item.javaClass.simpleName}. Reported to ErrorListener.")
        }
    }

    fun registerPlaceholder(delegate: FusionDelegate<*, *>) {
        registry.registerPlaceholder(delegate)
    }


    /**
     * 注册路由连接器 (核心入口)
     * @param clazz 数据类型 Class
     * @param linker 包含路由规则和 Delegate 集合的连接器
     */
    fun <T : Any> register(clazz: Class<T>, linker: TypeRouter<T>) {
        // 注册到注册表
        registry.register(clazz, linker)
    }

    // ========================================================================================
    // RecyclerView 核心代理
    // ========================================================================================

    fun getItemViewType(item: Any): Int {
        // 路由不再依赖 position，只依赖 item 内容 (O(1) Key 映射)
        return registry.getItemViewType(item)
    }

    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // 逻辑收敛：统一处理 Placeholder 的创建
        if (viewType == ViewTypeRegistry.TYPE_PLACEHOLDER) {
            val delegate = registry.getPlaceholderDelegate()
            if (delegate != null) {
                return delegate.onCreateViewHolder(parent)
            } else {
                return FusionPlaceholderViewHolder(parent)
            }
        }
        return registry.getDelegate(viewType).onCreateViewHolder(parent)
    }

    fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Any, position: Int, payloads: MutableList<Any> = Collections.emptyList()) {
        val viewType = registry.getItemViewType(item)
        // [核心修复] 兼容 ConcatAdapter / ViewPool 共享场景
        // 如果收到了外部 Adapter 的 ViewType (例如 Paging 的 Footer 0)，直接忽略。
        val delegate = registry.getDelegateOrNull(viewType)
        logD("FusionTracker") {
            """
            ⚡ [OnBind] Executing...
               Position: $position
               Item Type: ${item.javaClass.name}
               ViewType ID: $viewType
               Found Delegate: ${delegate?.javaClass?.simpleName} @${System.identityHashCode(delegate)}
               Delegate Key: ${delegate?.getUniqueViewType()}
        """.trimIndent()
        }
        if (delegate == null) {
            logD("FusionCore") { "⚠️ [Ignored Bind] Pass-through foreign ViewType: $viewType" }
            return
        }
        delegate.onBindViewHolder(holder, item, position, payloads)
        logD("Bind") {
            // 这种多行字符串拼接在 Release 模式下是昂贵的，inline 完美解决了这个问题
            """
            >>> [OnBind] Pos=$position, ViewType=$viewType
                Holder Delegate: ${delegate.javaClass.simpleName}
                Actual Item:     ${item.javaClass.simpleName}
            """.trimIndent()
        }
    }

    // ========================================================================================
    // DiffUtil 代理 (Smart Diff 支持)
    // ========================================================================================

    /**
     * [关键修复] 代理 DiffUtil.areItemsTheSame
     * 必须确保 ViewType 相同，否则不能复用 ViewHolder
     */
    fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        // 1. 先获取两个 Item 的 ViewType
        val oldType = registry.getItemViewType(oldItem)
        val newType = registry.getItemViewType(newItem)

        // 2. 如果类型变了（比如从 Text 变 Image），绝对不是同一个 Item
        // 即使 ID 一样，也必须销毁重建
        if (oldType != newType) {
            logE("Diff") {
                val oldId = (oldItem as? StableId)?.stableId
                val newId = (newItem as? StableId)?.stableId
                "🔥🔥 [Diff Mismatch] ID相同但类型不同! Old: ${oldItem.javaClass.simpleName}($oldId) vs New: ${newItem.javaClass.simpleName}($newId)"
            }
            return false
        }

        // 3. 类型一样，再交给静态策略去比对 ID
        return SmartDiffCallback.areItemsTheSame(oldItem, newItem)
    }

    /**
     * 代理 DiffUtil.Callback.areContentsTheSame
     */
    fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        // 1. 获取 ViewType (O(1) 查找)
        val oldType = registry.getItemViewType(oldItem)
        val newType = registry.getItemViewType(newItem)

        // 2. 如果类型变了（比如从 Text 变成了 Image），肯定不是同一个内容
        if (oldType != newType) return false

        // 3. 找到 Delegate，让 Delegate 自己去比对内容
        val delegate = registry.getDelegate(oldType)
        return delegate.areContentsTheSame(oldItem, newItem)
    }

    /**
     * 代理 DiffUtil.Callback.getChangePayload
     */
    fun getChangePayload(oldItem: Any, newItem: Any): Any? {
        val oldType = registry.getItemViewType(oldItem)
        val newType = registry.getItemViewType(newItem)

        if (oldType != newType) return null

        val delegate = registry.getDelegate(oldType)
        return delegate.getChangePayload(oldItem, newItem)
    }

    fun getDelegate(item: Any): FusionDelegate<Any, RecyclerView.ViewHolder>? {
        val viewType = registry.getItemViewType(item)
        return registry.getDelegateOrNull(viewType)
    }

    // ========================================================================================
    // 生命周期分发 (防止内存泄漏)
    // ========================================================================================

    fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        registry.getDelegateOrNull(holder.itemViewType)?.onViewRecycled(holder)
    }

    fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        registry.getDelegateOrNull(holder.itemViewType)?.onViewAttachedToWindow(holder)
    }

    fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        registry.getDelegateOrNull(holder.itemViewType)?.onViewDetachedFromWindow(holder)
    }
}