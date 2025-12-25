package com.fusion.adapter.internal

import android.os.Handler
import android.os.Looper
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

/**
 * 工业级线程调度中心
 *
 * 特性：
 * 1. CPU 密集型并发线程池：根据核心数动态调整，不再是单线程串行，大幅提升 ViewPager 等多列表场景性能。
 * 2. 智能主线程调度：避免无谓的 Handler 消息开销。
 * 3. 完整的任务取消机制：支持 Future 中断。
 */
internal object FusionExecutors {

    private val mainHandler = Handler(Looper.getMainLooper())

    // 核心数 + 1，典型的计算密集型配置
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    private val POOL_SIZE = max(2, CPU_COUNT + 1)

    private val backgroundService: ExecutorService = ThreadPoolExecutor(
        POOL_SIZE, POOL_SIZE,
        30L, TimeUnit.SECONDS,
        LinkedBlockingQueue(),
        object : ThreadFactory {
            private val count = AtomicInteger(1)
            override fun newThread(r: Runnable): Thread {
                return Thread(r, "Fusion-Pool-${count.getAndIncrement()}").apply {
                    priority = Thread.NORM_PRIORITY
                    isDaemon = true
                }
            }
        }
    )

    fun interface Cancellable {
        fun cancel()
    }

    /**
     * 在后台执行任务
     * @param task 任务逻辑
     * @return Cancellable 用于取消。如果任务正在运行，会尝试中断线程。
     */
    fun runInBackground(task: Runnable): Cancellable {
        val future = backgroundService.submit(task)
        // allowMayInterruptIfRunning = true
        // 允许打断正在运行的线程（需要配合 AdapterController 中的 Thread.interrupted() 检查）
        return Cancellable { future.cancel(true) }
    }

    /**
     * 在主线程执行任务
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

    fun isMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    // 暴露给 AsyncListDiffer 使用的 Executor 适配器
    val backgroundExecutorAdapter = Executor { command -> backgroundService.execute(command) }
    val mainExecutorAdapter = Executor { command -> runOnMain(command) }
}