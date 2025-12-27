package com.fusion.adapter.internal.engine

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

import com.fusion.adapter.log.FusionLogger

/**
 * [FusionDispatcher]
 * 工业级任务分发中心。
 */
internal object FusionDispatcher {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    private val POOL_SIZE = max(2, CPU_COUNT + 1)

    // [架构升级] 使用自定义的 FusionExecutor 替代原生 ThreadPoolExecutor
    private val backgroundService = FusionExecutor(
        corePoolSize = POOL_SIZE,
        maximumPoolSize = POOL_SIZE,
        keepAliveTime = 0L,
        unit = TimeUnit.MILLISECONDS,
        workQueue = LinkedBlockingQueue(),
        threadFactory = object : ThreadFactory {
            private val count = AtomicInteger(1)
            override fun newThread(r: Runnable): Thread {
                return Thread({
                    FusionLogger.d("Trace") { "Thread ${Thread.currentThread().name} starting task" }
                    try {
                        r.run()
                    } finally {
                        FusionLogger.d("Trace") { "Thread ${Thread.currentThread().name} finished task" }
                    }
                }, "Fusion-Dispatcher-${count.getAndIncrement()}").apply {
                    // 降低优先级，防止后台 Diff 抢占 UI 渲染资源
                    priority = Thread.NORM_PRIORITY - 1
                    isDaemon = true
                }
            }
        }
    )

    fun interface Cancellable {
        fun cancel()
    }

    /**
     * [Core API] 分发后台任务
     */
    fun dispatch(task: Runnable): Cancellable {
        FusionLogger.d("Trace") { "FusionDispatcher.dispatch called" }
        val future = backgroundService.submit(task)
        return Cancellable { future.cancel(true) }
    }

    /**
     * [Core API] 分发主线程任务
     */
    fun runOnMain(task: Runnable): Cancellable {
        if (isMainThread()) {
            task.run()
            return Cancellable { }
        }
        val wrapper = Runnable { task.run() }
        mainHandler.post(wrapper)
        return Cancellable { mainHandler.removeCallbacks(wrapper) }
    }

    fun isMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()

    /**
     * [Interop] 暴露给 AsyncListDiffer 使用的 Executor。
     */
    val backgroundExecutorAdapter = Executor { command -> backgroundService.execute(command) }
}
