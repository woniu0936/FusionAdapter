package com.fusion.adapter.internal

import com.fusion.adapter.DiffKeyProvider
import com.fusion.adapter.delegate.FusionDelegate

/**
 * [RouterConfiguration]
 * 纯数据配置类，用于暂存 Router 的 DSL 配置。
 */
class RouterConfiguration<T : Any> {
    // 默认 Matcher (一对一)
    var matcher: DiffKeyProvider<T> = DiffKeyProvider { Unit }

    // 全局 ID 提供者
    var defaultIdProvider: ((T) -> Any?)? = null

    // 映射关系 (使用 LinkedHashMap 保持注册顺序)
    val mappings = LinkedHashMap<Any?, FusionDelegate<T, *>>()
}