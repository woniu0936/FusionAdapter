package com.fusion.adapter.internal

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.Fusion
import com.fusion.adapter.FusionConfig
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.diff.SmartDiffCallback
import com.fusion.adapter.diff.StableId
import com.fusion.adapter.intercept.FusionContext
import com.fusion.adapter.intercept.FusionDataInterceptor
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

    // 性能阈值：16ms (保证 60fps)
    private val TIME_THRESHOLD_NS = 16_000_000L

    val registry = ViewTypeRegistry()

    // 局部拦截器 (Local)
    private val localInterceptors = ArrayList<FusionDataInterceptor>()

    // 🔥 性能核心：缓存合并后的拦截器列表快照
    // 使用 volatile 保证多线程（如预加载线程）可见性
    @Volatile
    private var cachedSnapshot: List<FusionDataInterceptor>? = null

    // 缓存 Context，避免每次 processData 都 new 对象
    private val cachedContext by lazy {
        object : FusionContext {
            override val registry: ViewTypeRegistry
                get() = this@AdapterController.registry
            override val config: FusionConfig
                get() = Fusion.getConfig()
        }
    }

    /**
     * 添加拦截器 (Write Path - Low Frequency)
     * 策略：写时置空缓存 (Copy-On-Write 思想)
     */
    fun addInterceptor(interceptor: FusionDataInterceptor) {
        synchronized(localInterceptors) {
            localInterceptors.add(interceptor)
            cachedSnapshot = null // 脏标记：缓存失效
        }
    }

    /**
     * 数据处理管道 (Hot Path)
     * 流程：[用户拦截器 (可选)] -> [性能监控 (仅拦截器)] -> [强制安全检查 (内置)]
     */
    fun processData(rawList: List<Any>): List<Any> {
        // 1. 获取当前快照 (局部变量引用，线程安全)
        var interceptors = cachedSnapshot

        // 2. 如果缓存失效 (初始化或配置变更)，则重建
        if (interceptors == null) {
            val global = Fusion.getConfig().globalInterceptors
            val local = localInterceptors // 读取最新的 local

            if (global.isEmpty() && local.isEmpty()) {
                interceptors = emptyList()
            } else {
                val combined = ArrayList<FusionDataInterceptor>(global.size + local.size)
                combined.addAll(global)
                combined.addAll(local)
                interceptors = Collections.unmodifiableList(combined)
            }
            cachedSnapshot = interceptors
        }

        // ------------------------------------------------------------
        // Phase 1: 执行拦截器管道 (Interceptor Pipeline)
        // ------------------------------------------------------------

        val processedList: List<Any>

        if (interceptors!!.isEmpty()) {
            // 🔥 极速短路 (Fast Path): 无拦截器时，跳过 Chain 创建和性能监控
            processedList = rawList
        } else {
            // --- 性能监控开始 ---
            val start = System.nanoTime()

            val chain = RealInterceptorChain(interceptors, 0, rawList, cachedContext)
            processedList = chain.proceed(rawList)

            // --- 性能监控结束 ---
            val cost = System.nanoTime() - start

            // 机制：如果耗时过长，进行干预
            if (cost > TIME_THRESHOLD_NS) {
                val costMs = cost / 1_000_000f
                val message = "⚠️ [Fusion Performance Alert] Interceptor chain took ${String.format("%.2f", costMs)}ms on Main Thread! " +
                        "This may cause UI jank. Please check for heavy operations in your interceptors."

                if (Fusion.getConfig().isDebug) {
                    android.util.Log.e("Fusion", message)
                } else {
                    android.util.Log.w("Fusion", message)
                }
            }
        }

        // ------------------------------------------------------------
        // Phase 2: 强制安全检查 (Hardcoded Safety Net)
        // 无论用户是否配置拦截器，这一步永远执行，确保 UI 绝对安全
        // ------------------------------------------------------------

        // 优化：如果列表为空，直接返回，避免创建 ArrayList
        if (processedList.isEmpty()) {
            return processedList
        }

        val finalSafeList = ArrayList<Any>(processedList.size)
        val isDebug = Fusion.getConfig().isDebug

        for (item in processedList) {
            if (registry.hasLinker(item)) {
                finalSafeList.add(item)
            } else {
                // 仅在 Debug 模式下警告，Release 模式下静默剔除
                if (isDebug) {
                    android.util.Log.w("Fusion", "⚠️ Fusion 剔除了未注册类型: ${item.javaClass.simpleName}。请检查是否忘记 register()。")
                }
            }
        }

        return finalSafeList
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