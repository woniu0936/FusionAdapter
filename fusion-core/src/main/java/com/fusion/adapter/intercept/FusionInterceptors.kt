package com.fusion.adapter.intercept

import android.util.Log

/**
 * Fusion 标准拦截器工厂 (Fusion Standard Library)
 *
 * 提供了一组经过深度性能优化的内置拦截器，覆盖 90% 的 Adapter 数据处理场景。
 * 包含：安全过滤、去重、条件过滤、周期插入、排序、头尾注入、日志监控、类型筛选。
 *
 * 使用方式：
 * ```kotlin
 * adapter.addInterceptor(FusionInterceptors.safeGuard())
 * adapter.addInterceptor(FusionInterceptors.log("MyTag"))
 * ```
 */
object FusionInterceptors {

    /**
     * 【核心场景：安全卫士】
     * 自动剔除未注册 ViewType 的数据，防止 RecyclerView 崩溃或出现 UI 错乱。
     *
     * **描述**：
     * 利用 Context 中的 Registry 检查每一个 Item 是否有对应的 Linker/Delegate。
     * 如果没有，该 Item 将被静默移除，不会进入 DiffUtil 和 RecyclerView。
     *
     * **推荐使用场景**：
     * 1. 几乎所有的 Adapter 都建议作为全局拦截器配置 (`Fusion.initialize`)。
     * 2. Paging 分页列表，防止后端下发未知的新类型导致 Crash。
     *
     * @param debugLog Boolean - 是否在 Debug 模式下打印被剔除数据的警告日志。默认为 true。
     *
     * **使用示例**：
     * ```kotlin
     * // 在全局初始化时添加
     * Fusion.initialize {
     *     addGlobalInterceptor(FusionInterceptors.safeGuard())
     * }
     * ```
     */
    fun safeGuard(debugLog: Boolean = true): FusionDataInterceptor {
        return FusionDataInterceptor { chain ->
            val registry = chain.context.registry
            val input = chain.input
            // 性能优化：预分配容量，减少 ArrayList 扩容开销
            val output = ArrayList<Any>(input.size)

            for (item in input) {
                if (registry.hasLinker(item)) {
                    output.add(item)
                } else if (debugLog && chain.context.config.isDebug) {
                    Log.w("Fusion", "⚠️ SafeGuard removed unregistered item: ${item.javaClass.simpleName}")
                }
            }
            chain.proceed(output)
        }
    }

    /**
     * 【核心场景：数据去重】
     * 根据指定的 Key 对列表进行去重，保留第一个出现的元素。
     *
     * **描述**：
     * 内部使用 `LinkedHashSet` 维护唯一性，时间复杂度接近 O(N)。
     *
     * **推荐使用场景**：
     * 1. 后端接口可能下发重复数据（如 Feed 流推荐）。
     * 2. 手动合并多个列表时需要去重。
     *
     * @param keySelector (Any) -> K - 键选择器函数。
     *        接收列表中的 Item，返回用于判断唯一的 Key (如 ID, UserID)。
     *
     * **使用示例**：
     * ```kotlin
     * // 根据 User 的 id 去重
     * adapter.addInterceptor(FusionInterceptors.distinct { item ->
     *     if (item is User) item.id else item
     * })
     * ```
     */
    fun <K> distinct(keySelector: (Any) -> K): FusionDataInterceptor {
        return FusionDataInterceptor { chain ->
            chain.proceed(chain.input.distinctBy(keySelector))
        }
    }

    /**
     * 【核心场景：条件过滤】
     * 根据指定条件保留数据，移除不符合条件的 Item。
     *
     * **描述**：
     * 遍历列表，只保留 `predicate` 返回 true 的元素。
     *
     * **推荐使用场景**：
     * 1. 根据业务状态隐藏内容（如：只显示已发布的文章）。
     * 2. 根据用户权限过滤内容（如：非会员不显示 VIP 视频）。
     * 3. 临时屏蔽某种类型的数据（A/B Test）。
     *
     * @param predicate (Any) -> Boolean - 判断函数。
     *        接收 Item，返回 true 表示**保留**，返回 false 表示**移除**。
     *
     * **使用示例**：
     * ```kotlin
     * // 过滤掉所有已删除的消息
     * adapter.addInterceptor(FusionInterceptors.filter { item ->
     *     if (item is Message) !item.isDeleted else true
     * })
     * ```
     */
    fun filter(predicate: (Any) -> Boolean): FusionDataInterceptor {
        return FusionDataInterceptor { chain ->
            val input = chain.input
            val output = ArrayList<Any>(input.size)
            for (item in input) {
                if (predicate(item)) {
                    output.add(item)
                }
            }
            chain.proceed(output)
        }
    }

    /**
     * 【核心场景：周期性插入】
     * 每隔固定数量的元素插入一个新的 Item。
     *
     * **描述**：
     * 按照频率 `frequency` 计算插入位置。注意：如果列表结束，不会在末尾强行插入。
     *
     * **推荐使用场景**：
     * 1. Feed 流中每隔 6 条内容插入一个广告。
     * 2. 网格布局中插入全跨度的分割栏。
     *
     * @param frequency Int - 插入频率（每隔多少个元素）。必须 > 0。
     * @param itemGenerator (Int) -> Any - 元素生成器函数。
     *        接收当前原始数据的索引(Index)，返回要插入的 Item 对象。
     *
     * **使用示例**：
     * ```kotlin
     * // 每 6 个条目插一个广告
     * adapter.addInterceptor(FusionInterceptors.injectPeriodic(6) { index ->
     *     AdItem(position = index)
     * })
     * ```
     */
    fun injectPeriodic(frequency: Int, itemGenerator: (index: Int) -> Any): FusionDataInterceptor {
        return FusionDataInterceptor { chain ->
            val input = chain.input
            if (input.isEmpty() || frequency <= 0) {
                return@FusionDataInterceptor chain.proceed(input)
            }

            // 精确计算最终大小，避免扩容
            val insertionCount = (input.size - 1) / frequency
            if (insertionCount <= 0) return@FusionDataInterceptor chain.proceed(input)

            val output = ArrayList<Any>(input.size + insertionCount)

            for (i in input.indices) {
                output.add(input[i])
                // 检查是否满足插入条件 (非最后一个元素)
                if ((i + 1) % frequency == 0 && i != input.lastIndex) {
                    output.add(itemGenerator(i))
                }
            }
            chain.proceed(output)
        }
    }

    /**
     * 【核心场景：数据排序】
     * 对列表数据进行排序。
     *
     * **描述**：
     * 使用稳定排序算法。
     * 注意：尽量在 ViewModel 层完成排序，此拦截器主要用于简单的 UI 层重排。
     *
     * **推荐使用场景**：
     * 1. 简单的按时间倒序、按名称排序。
     * 2. 对合并后的多种类型数据进行统一排序。
     *
     * @param comparator Comparator<Any> - 比较器。
     *
     * **使用示例**：
     * ```kotlin
     * // 按时间戳倒序排列
     * adapter.addInterceptor(FusionInterceptors.sort { o1, o2 ->
     *     val t1 = (o1 as? TimeAware)?.timestamp ?: 0
     *     val t2 = (o2 as? TimeAware)?.timestamp ?: 0
     *     t2.compareTo(t1)
     * })
     * ```
     */
    fun sort(comparator: Comparator<Any>): FusionDataInterceptor {
        return FusionDataInterceptor { chain ->
            // sortedWith 返回的是新 List，不会修改原 input
            chain.proceed(chain.input.sortedWith(comparator))
        }
    }

    /**
     * 【核心场景：头部注入】
     * 在列表的最顶部添加一个固定的 Header Item。
     *
     * **描述**：
     * 无论数据如何刷新，该 Item 始终位于 Index 0。
     *
     * **推荐使用场景**：
     * 1. 列表顶部的提示栏、搜索框、Banner。
     * 2. 下拉刷新时的 Loading 占位符。
     *
     * @param header Any - 要添加的 Header 数据对象。
     *
     * **使用示例**：
     * ```kotlin
     * adapter.addInterceptor(FusionInterceptors.addHeader(MyBannerItem()))
     * ```
     */
    fun addHeader(header: Any): FusionDataInterceptor {
        return FusionDataInterceptor { chain ->
            val output = ArrayList<Any>(chain.input.size + 1)
            output.add(header)
            output.addAll(chain.input)
            chain.proceed(output)
        }
    }

    /**
     * 【核心场景：底部注入】
     * 在列表的最底部添加一个固定的 Footer Item。
     *
     * **描述**：
     * 无论数据如何刷新，该 Item 始终位于列表末尾。
     *
     * **推荐使用场景**：
     * 1. "没有更多数据了" 的提示。
     * 2. 列表底部的免责声明、版本号。
     *
     * @param footer Any - 要添加的 Footer 数据对象。
     *
     * **使用示例**：
     * ```kotlin
     * adapter.addInterceptor(FusionInterceptors.addFooter(EndMarkerItem()))
     * ```
     */
    fun addFooter(footer: Any): FusionDataInterceptor {
        return FusionDataInterceptor { chain ->
            val output = ArrayList<Any>(chain.input.size + 1)
            output.addAll(chain.input)
            output.add(footer)
            chain.proceed(output)
        }
    }

    /**
     * 【核心场景：调试监控】
     * 监控数据流的变化过程和耗时。
     *
     * **描述**：
     * 仅在 Debug 模式下生效。记录输入数量、输出数量以及处理耗时。
     *
     * **推荐使用场景**：
     * 1. 开发阶段排查数据为什么变少了，或者为什么刷新慢。
     *
     * @param tag String - 日志 Tag，用于区分不同的列表。
     *
     * **使用示例**：
     * ```kotlin
     * adapter.addDebugInterceptor(FusionInterceptors.log("FeedList"))
     * ```
     */
    fun log(tag: String = "Fusion"): FusionDataInterceptor {
        return FusionDataInterceptor { chain ->
            // 如果不是 Debug，直接透传，不做任何计时操作
            if (!chain.context.config.isDebug) {
                return@FusionDataInterceptor chain.proceed(chain.input)
            }

            val start = System.nanoTime()
            val inputSize = chain.input.size

            val result = chain.proceed(chain.input)

            val cost = (System.nanoTime() - start) / 1000
            val outputSize = result.size

            Log.d(tag, "⛓️ Interceptor Chain: Input=$inputSize -> Output=$outputSize, Cost=${cost}us")
            result
        }
    }

    /**
     * 【特例场景：类型筛选】
     * 过滤列表，只保留特定类型 [T] 的数据。
     *
     * **描述**：
     * 这是一个 inline + reified 函数，利用泛型具体化进行过滤。
     *
     * **推荐使用场景**：
     * 1. 在混合列表中只提取视频数据。
     * 2. 数据清洗，移除所有非 UI Model 的杂质对象。
     *
     * **使用示例**：
     * ```kotlin
     * // 只保留 User 类型的对象
     * adapter.addInterceptor(FusionInterceptors.filterType<User>())
     * ```
     */
    inline fun <reified T> filterType(): FusionDataInterceptor {
        return FusionDataInterceptor { chain ->
            // filterIsInstance 是 Kotlin 标准库的高效实现
            chain.proceed(chain.input.filterIsInstance<T>() as List<Any>)
        }
    }
}