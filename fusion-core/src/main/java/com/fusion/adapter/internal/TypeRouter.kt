package com.fusion.adapter.internal

import androidx.annotation.RestrictTo
import com.fusion.adapter.DiffKeyProvider
import com.fusion.adapter.delegate.FusionDelegate

/**
 * [TypeRouter]
 * 路由连接器。定义了 "数据 -> Key" 和 "Key -> Delegate" 的双重映射规则。
 *
 * 特性：
 * 1. O(1) 路由查找
 * 2. 支持链式调用 (Fluent API)
 *
 * @param T 数据类型
 */
class TypeRouter<T : Any> {

    // Key 生成器：从 Item 中提取特征 Key
    // 默认实现：返回 Unit (适用于一对一场景)
    private var keyMapper: DiffKeyProvider<T> = DiffKeyProvider { Unit }

    // 映射表: Key -> Delegate
    private val keyToDelegate = HashMap<Any?, FusionDelegate<T, *>>()

    /**
     * 配置 Key 提取规则 (Match)
     * 支持链式调用。
     *
     * @param mapper Key 提取函数，例如 { it.type }
     * @return this
     */
    fun match(mapper: DiffKeyProvider<T>): TypeRouter<T> {
        this.keyMapper = mapper
        return this
    }

    /**
     * 建立映射关系 (Map)
     * 支持链式调用。
     *
     * @param key 路由 Key，例如 1, "header", Enum.TYPE
     * @param delegate 对应的委托实例
     * @return this
     */
    fun map(key: Any?, delegate: FusionDelegate<T, *>): TypeRouter<T> {
        keyToDelegate[key] = delegate
        return this
    }

    /**
     * [Core 内部调用] 解析 Item 对应的 Delegate
     */
    internal fun resolve(item: T): FusionDelegate<T, *>? {
        val key = keyMapper.map(item)
        return keyToDelegate[key]
    }

    /**
     * [Core 内部调用] 获取所有持有的 Delegate (用于全局注册)
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun getAllDelegates(): Collection<FusionDelegate<T, *>> {
        return keyToDelegate.values
    }
}