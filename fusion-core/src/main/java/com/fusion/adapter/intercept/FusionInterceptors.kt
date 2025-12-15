package com.fusion.adapter.intercept

import android.os.SystemClock
import android.util.Log

/**
 * Fusion 标准拦截器库 (Ultimate Edition)
 *
 * 剔除了所有应由 ViewModel 处理的业务逻辑 (Sort/Filter/Distinct)，
 * 专注于 ViewModel 难以处理的 "UI 装饰逻辑" 和 "工程质量监控"。
 */
object FusionInterceptors {

    // ============================================================================================
    //  工程质量与调试类 (Quality Assurance & Debugging)
    //  这是区别于普通库的“核武器”，帮助开发者排查疑难杂症。
    // ============================================================================================

    /**
     * 【QA 核武器：数据契约验证 (Data Validator)】
     * 强迫执行数据规则。如果后端下发了脏数据（如 id 为空，或者必须字段缺失），
     * 在 Debug 模式下直接报错，Release 模式下自动剔除。
     *
     * **核心价值**：
     * 这是 "Design by Contract" (契约式编程) 在列表层的落地。
     * 避免了脏数据进入 ViewHolder 导致空指针异常。
     *
     * @param validator (Any) -> Boolean 返回 false 表示数据非法，会被剔除。
     */
    fun validate(validator: (Any) -> Boolean): FusionDataInterceptor {
        return FusionDataInterceptor { chain ->
            val input = chain.input
            val output = ArrayList<Any>(input.size)
            var dirtyCount = 0

            for (item in input) {
                if (validator(item)) {
                    output.add(item)
                } else {
                    dirtyCount++
                    if (chain.context.config.isDebug) {
                        Log.e("Fusion", "❌ [Validator] Data integrity check failed! Item dropped: $item")
                    }
                }
            }

            if (dirtyCount > 0 && chain.context.config.isDebug) {
                // 可选：在 Debug 模式下抛出异常，倒逼后端修 Bug
                 throw IllegalStateException("Found $dirtyCount invalid items!")
            }

            chain.proceed(output)
        }
    }

    /**
     * 【调试神器：性能透视 (Performance Profiler)】
     * 详细分析当前 Adapter 的数据分布情况和处理耗时。
     *
     * **核心价值**：
     * 当列表卡顿或数据不对时，这个拦截器能打印出 "数据构成表"。
     * 比如：UserItem: 50个, AdItem: 5个, Header: 1个。
     */
    fun trace(tag: String = "FusionTrace"): FusionDataInterceptor {
        return FusionDataInterceptor { chain ->
            if (!chain.context.config.isDebug) return@FusionDataInterceptor chain.proceed(chain.input)

            val start = SystemClock.elapsedRealtimeNanos()

            // 执行后续链条
            val result = chain.proceed(chain.input)

            val costNs = SystemClock.elapsedRealtimeNanos() - start
            val costMs = costNs / 1_000_000f

            // 统计数据分布
            val distribution = result.groupingBy { it.javaClass.simpleName }.eachCount()

            // 打印漂亮的表格日志
            val sb = StringBuilder()
            sb.append("\n╔════════════════════════════════════════════════════")
            sb.append("\n║ Fusion Adapter Trace [$tag]")
            sb.append("\n╠════════════════════════════════════════════════════")
            sb.append("\n║ ⚡ Process Cost : ${String.format("%.3f", costMs)} ms")
            sb.append("\n║ 📥 Input Size   : ${chain.input.size}")
            sb.append("\n║ 📤 Output Size  : ${result.size}")
            sb.append("\n║ 📊 Distribution :")
            distribution.forEach { (type, count) ->
                sb.append("\n║    - $type : $count")
            }
            sb.append("\n╚════════════════════════════════════════════════════")
            Log.d("Fusion", sb.toString())

            result
        }
    }

    /**
     * 【QA 黑科技：混沌测试 (Chaos Monkey)】
     * 仅在 Debug 模式下生效。随机打乱数据、随机丢弃数据、或随机重复数据。
     *
     * **核心价值**：
     * 用于测试 DiffUtil 的健壮性，以及 UI 应对空状态、异常状态的表现。
     * 它可以模拟弱网丢包、后端乱序等极端情况。
     */
    fun chaosMonkey(): FusionDataInterceptor {
        return FusionDataInterceptor { chain ->
            if (!chain.context.config.isDebug) return@FusionDataInterceptor chain.proceed(chain.input)

            val input = ArrayList(chain.input)
            val mode = (System.currentTimeMillis() % 3).toInt()

            Log.w("Fusion", "🔥 Chaos Monkey Activated! Mode: $mode")

            val result = when (mode) {
                0 -> { // 随机乱序
                    input.shuffle()
                    input
                }

                1 -> { // 随机丢弃 20%
                    input.filter { Math.random() > 0.2 }
                }

                2 -> { // 随机重复某些数据
                    val noisyList = ArrayList<Any>()
                    input.forEach {
                        noisyList.add(it)
                        if (Math.random() > 0.9) noisyList.add(it) // 10% 概率重复
                    }
                    noisyList
                }

                else -> input
            }
            chain.proceed(result)
        }
    }
}
