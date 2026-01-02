package com.fusion.adapter

import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.router.TypeRouter

/**
 * [FusionRegistry]
 * 定义适配器注册条目代理的能力。
 */
interface FusionRegistry {

    /**
     * 注册路由分发器（用于同一个数据类对应多个布局的情况）
     */
    fun <T : Any> register(clazz: Class<T>, router: TypeRouter<T>)

    /**
     * 注册一对一映射代理（最常用）
     */
    fun <T : Any> register(clazz: Class<T>, delegate: FusionDelegate<T, *>)
}