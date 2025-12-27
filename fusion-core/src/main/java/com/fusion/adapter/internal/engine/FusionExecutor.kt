package com.fusion.adapter.internal.engine

import android.os.Handler
import android.os.Looper
import com.fusion.adapter.Fusion
import java.util.concurrent.*

/**
 * [FusionExecutor]
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
