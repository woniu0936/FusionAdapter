package com.fusion.adapter.internal.engine

import android.os.Handler
import android.os.Looper
import com.fusion.adapter.Fusion
import com.fusion.adapter.log.FusionLogger
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

/**
 * [FusionExecutor]
 * 工业级任务分发中心。
 */
internal object FusionExecutor {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    private val POOL_SIZE = max(2, CPU_COUNT + 1)

    // [架构升级] 使用自定义的 FusionTaskExecutor 替代原生 ThreadPoolExecutor
    private val backgroundService = FusionTaskExecutor(
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
                }, "Fusion-Executor-${count.getAndIncrement()}").apply {
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
    fun execute(task: Runnable): Cancellable {
        FusionLogger.d("Trace") { "FusionExecutor.execute called" }
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

    /**
     * [FusionTaskExecutor]
     * 内部自定义线程池实现，支持异常全局捕获
     */
    private class FusionTaskExecutor(
        corePoolSize: Int,
        maximumPoolSize: Int,
        keepAliveTime: Long,
        unit: TimeUnit,
        workQueue: BlockingQueue<Runnable>,
        threadFactory: ThreadFactory
    ) : ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory) {

        override fun afterExecute(r: Runnable?, t: Throwable?) {
            super.afterExecute(r, t)
            var exception: Throwable? = t
            if (t == null && r is Future<*>) {
                try {
                    if (r.isDone) r.get()
                } catch (ce: CancellationException) {
                    exception = null
                } catch (ee: ExecutionException) {
                    exception = ee.cause
                } catch (ie: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }

            if (exception != null) {
                handleGlobalError(exception)
            }
        }

        private fun handleGlobalError(t: Throwable) {
            val config = Fusion.getConfig()
            if (config.isDebug) {
                mainHandler.post {
                    throw RuntimeException(
                        "Fusion Background Exception: ${t.javaClass.simpleName}. \n" +
                                "This crash is intentional in DEBUG mode to help you fix bugs early.",
                        t
                    )
                }
            } else {
                config.errorListener?.onError("Fusion Background Task", Exception(t))
            }
        }
    }
}