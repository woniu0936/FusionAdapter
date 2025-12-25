package com.fusion.adapter.internal

import android.os.Handler
import android.os.Looper
import com.fusion.adapter.Fusion
import java.util.concurrent.*

/**
 * [FusionExecutor]
 * 架构级异常拦截器。
 *
 * 遵循 JDK 标准：通过重写 [afterExecute] 来解包 Future 并捕获所有后台异常。
 * 这保证了无论是 execute() 还是 submit() 提交的任务，异常都会被“抛出”到主线程。
 */
internal class FusionExecutor(
    corePoolSize: Int,
    maximumPoolSize: Int,
    keepAliveTime: Long,
    unit: TimeUnit,
    workQueue: BlockingQueue<Runnable>,
    threadFactory: ThreadFactory
) : ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory) {

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * [JDK Hook] 任务执行完毕后的回调（无论成功、失败还是取消）
     */
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
            // 策略 1: Debug 模式下，强制在主线程抛出异常，让 App 崩溃以暴露问题
            mainHandler.post {
                throw RuntimeException(
                    "Fusion Background Exception: ${t.javaClass.simpleName}. \n" +
                            "This crash is intentional in DEBUG mode to help you fix bugs early.",
                    t
                )
            }
        } else {
            // Release 模式下，静默处理或回调 ErrorListener
            config.errorListener?.onError("Fusion Background Task", Exception(t))
        }
    }

}