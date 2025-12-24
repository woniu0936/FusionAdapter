package com.fusion.adapter.internal

import androidx.annotation.RestrictTo
import com.fusion.adapter.DiffKeyProvider
import com.fusion.adapter.delegate.FusionDelegate
import java.util.Collections

/**
 * [TypeRouter]
 * 运行时路由核心。
 * 极致重构版：不可变 (Immutable)，必须通过 DSL 或 Config 创建。
 */
class TypeRouter<T : Any> internal constructor(
    config: RouterConfiguration<T> // ✅ 构造函数接收 Config
) {
    private val keyMapper: DiffKeyProvider<T> = config.matcher
    private val delegatesMap: Map<Any?, FusionDelegate<T, *>>

    init {
        // 1. 防御性复制 (Defensive Copy)
        val mapCopy = HashMap(config.mappings)

        // 2. 注入全局 ID Provider
        val globalProvider = config.defaultIdProvider
        if (globalProvider != null) {
            mapCopy.values.forEach { it.attachDefaultKeyProvider(globalProvider) }
        }

        // 3. 冻结 Map (不可变)
        this.delegatesMap = Collections.unmodifiableMap(mapCopy)
    }

    /**
     * [Core 内部调用] 路由解析
     */
    internal fun resolve(item: T): FusionDelegate<T, *>? {
        val key = keyMapper.map(item)
        return delegatesMap[key]
    }

    /**
     * [Core 内部调用] 获取所有 Delegate 用于注册
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun getAllDelegates(): Collection<FusionDelegate<T, *>> {
        return delegatesMap.values
    }

    companion object {
        /**
         * [Internal Factory]
         * 允许 fusion-paging 等子模块创建包含单个 Delegate 的默认 Router。
         * 使用 @RestrictTo 限制外部用户调用。
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        fun <T : Any> create(delegate: FusionDelegate<T, *>): TypeRouter<T> {
            val config = RouterConfiguration<T>()
            config.mappings[Unit] = delegate // 默认 Key 为 Unit
            return TypeRouter(config)
        }
    }

}