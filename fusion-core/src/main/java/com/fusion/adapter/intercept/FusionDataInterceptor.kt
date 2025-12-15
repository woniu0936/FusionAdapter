package com.fusion.adapter.intercept

import com.fusion.adapter.FusionConfig
import com.fusion.adapter.internal.ViewTypeRegistry

/**
 * 拦截器上下文工具箱
 * 遵循 DIP (依赖倒置)，拦截器只依赖上下文，不依赖 Adapter 具体实现
 */
interface FusionContext {
    val registry: ViewTypeRegistry
    val config: FusionConfig

    // 语法糖：直接获取 debug 状态
    val isDebug: Boolean get() = config.isDebug
}

/**
 * Fusion 数据处理拦截器
 * 核心职责：在数据进入 DiffUtil/RecyclerView 之前，进行清洗、转换、监控。
 */
fun interface FusionDataInterceptor {
    fun intercept(chain: Chain): List<Any>

    interface Chain {
        /** 当前待处理的数据快照 */
        val input: List<Any>

        /** 能够感知环境的上下文 */
        val context: FusionContext

        /** 继续执行责任链 */
        fun proceed(processedList: List<Any>): List<Any>
    }
}