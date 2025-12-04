package com.fusion.adapter.core


import com.fusion.adapter.delegate.FusionItemDelegate

/**
 * [FusionLinker]
 * 路由连接器。定义了 "数据 -> Key" 和 "Key -> Delegate" 的双重映射规则。
 *
 * @param T 数据类型
 */
class FusionLinker<T : Any> {

    // Key 生成器：从 Item 中提取特征 Key
    // 默认实现：返回 Unit (适用于一对一场景)
    private var keyMapper: (T) -> Any? = { Unit }

    // 映射表: Key -> Delegate
    private val keyToDelegate = HashMap<Any?, FusionItemDelegate<T, *>>()

    /**
     * 配置 Key 提取规则 (Match)
     * 例如: linker.match { it.type }
     */
    fun match(mapper: (T) -> Any?) {
        this.keyMapper = mapper
    }

    /**
     * 建立映射关系 (Map)
     * 例如: linker.map(1, textDelegate)
     */
    fun map(key: Any?, delegate: FusionItemDelegate<T, *>) {
        keyToDelegate[key] = delegate
    }

    /**
     * [Core 内部调用] 解析 Item 对应的 Delegate
     */
    internal fun resolve(item: T): FusionItemDelegate<T, *>? {
        val key = keyMapper(item)
        return keyToDelegate[key]
    }

    /**
     * [Core 内部调用] 获取所有持有的 Delegate (用于全局注册)
     */
    internal fun getAllDelegates(): Collection<FusionItemDelegate<T, *>> {
        return keyToDelegate.values
    }
}