package com.fusion.adapter.intercept

import androidx.paging.PagingData
import com.fusion.adapter.FusionConfig
import com.fusion.adapter.internal.ViewTypeRegistry

/**
 * Paging 上下文
 * 保持与 List 版本一致的开发体验
 */
interface FusionPagingContext {
    val registry: ViewTypeRegistry
    val config: FusionConfig
    val isDebug: Boolean get() = config.isDebug
}

/**
 * Fusion Paging 拦截器
 * 核心职责：对 PagingData 数据流进行变换 (filter, map, insertSeparators)
 */
fun interface FusionPagingInterceptor<T : Any> {
    /**
     * @param input 输入的数据流
     * @param context 上下文工具箱
     * @return 变换后的数据流
     */
    fun intercept(input: PagingData<T>, context: FusionPagingContext): PagingData<T>
}