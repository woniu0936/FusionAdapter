package com.fusion.adapter.core


import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.delegate.FusionItemDelegate
import java.lang.IllegalStateException

/**
 * [DelegateRegistry]
 * 负责管理 Delegate 的注册、查找和缓存。
 * 包含核心的混合查找算法 (O(1) / O(N)) 和严格冲突检测。
 */
class DelegateRegistry {

    // 所有注册的 Delegate 列表
    private val delegates = mutableListOf<FusionItemDelegate<Any, RecyclerView.ViewHolder>>()

    // ViewType (int) -> Delegate 映射
    private val viewTypeToDelegate = SparseArrayCompat<FusionItemDelegate<*, *>>()

    // Class -> Delegates 列表映射
    private val classToDelegates = HashMap<Class<*>, MutableList<FusionItemDelegate<Any, *>>>()

    // 【性能缓存】 Class -> ViewType (int)
    // 仅当 Class 只有唯一 Delegate 匹配时，才写入此缓存，实现 O(1) 查找
    private val classToViewTypeCache = HashMap<Class<*>, Int>()

    fun register(clazz: Class<*>, delegate: FusionItemDelegate<*, *>) {
        @Suppress("UNCHECKED_CAST")
        val castedDelegate = delegate as FusionItemDelegate<Any, RecyclerView.ViewHolder>

        delegates.add(castedDelegate)
        val list = classToDelegates.getOrPut(clazz) { mutableListOf() }
        list.add(castedDelegate)

        // ViewType 就是 delegates 列表的索引
        val viewType = delegates.size - 1
        viewTypeToDelegate.put(viewType, castedDelegate)

        // 注册表变更，清除缓存防止脏数据
        classToViewTypeCache.remove(clazz)
    }

    /**
     * 核心查找逻辑
     */
    fun getItemViewType(item: Any, position: Int): Int {
        val clazz = item.javaClass

        // 1. 尝试从缓存获取 (最快路径)
        classToViewTypeCache[clazz]?.let { return it }

        val delegatesForClass = classToDelegates[clazz]
            ?: throw IllegalStateException("未注册的类型: ${clazz.name}")

        // 2. 单一匹配 (绝大多数场景)
        if (delegatesForClass.size == 1) {
            val delegate = delegatesForClass[0]
            // 依然需要检查 isFor，虽然一般都为 true
            if (delegate.isFor(item, position)) {
                val viewType = delegates.indexOf(delegate)
                classToViewTypeCache[clazz] = viewType // 安全缓存
                return viewType
            }
        } else {
            // 3. 多重匹配 (一对多场景) - 严苛模式
            val matched = delegatesForClass.filter { it.isFor(item, position) }

            if (matched.isEmpty()) {
                throw IllegalStateException("类型 ${clazz.name} 有注册 Delegate，但没有一个 isFor 返回 true。Item: $item")
            }

            if (matched.size > 1) {
                // 【冲突保护】拒绝模糊匹配
                val names = matched.joinToString { it.javaClass.simpleName }
                throw IllegalStateException("Fusion 冲突警告: 类型 ${clazz.name} 同时被多个 Delegate 匹配 ($names)。请检查 isFor() 逻辑。")
            }

            // 无法缓存 ViewType，因为取决于 item 内容
            return delegates.indexOf(matched[0])
        }

        throw IllegalStateException("未找到匹配 Delegate: $item")
    }

    fun getDelegate(viewType: Int): FusionItemDelegate<Any, RecyclerView.ViewHolder> {
        @Suppress("UNCHECKED_CAST")
        return viewTypeToDelegate[viewType] as FusionItemDelegate<Any, RecyclerView.ViewHolder>
    }

    fun getDelegatePayload(viewType: Int, oldItem: Any, newItem: Any): Any? {
        return getDelegate(viewType).getChangePayload(oldItem, newItem)
    }
}

