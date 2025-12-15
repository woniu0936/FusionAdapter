package com.fusion.adapter.internal

import com.fusion.adapter.intercept.FusionContext
import com.fusion.adapter.intercept.FusionDataInterceptor

/**
 * 真实的拦截器链条实现
 * 只有在 processData 执行时才会创建，且生命周期极短
 */
internal class RealInterceptorChain(
    private val interceptors: List<FusionDataInterceptor>,
    private val index: Int,
    override val input: List<Any>,
    override val context: FusionContext
) : FusionDataInterceptor.Chain {

    override fun proceed(processedList: List<Any>): List<Any> {
        // 递归终止条件
        if (index >= interceptors.size) {
            return processedList
        }

        // 构建下一环
        val next = RealInterceptorChain(interceptors, index + 1, processedList, context)
        val interceptor = interceptors[index]

        // 执行当前环
        return interceptor.intercept(next)
    }
}