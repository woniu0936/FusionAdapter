package com.fusion.adapter.log

import android.util.Log
import com.fusion.adapter.Fusion
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

/**
 * [FusionLogger]
 * Commercial-grade high-performance logging system.
 * Supports Logcat output and asynchronous file writing.
 */
object FusionLogger {

    private const val TAG_PREFIX = "Fusion-"
    private val DATE_FORMAT = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)
    
    private val fileExecutor by lazy { Executors.newSingleThreadExecutor() }

    inline fun i(tag: String, msg: () -> String) {
        if (shouldLog()) {
            print(Log.INFO, tag, msg())
        }
    }

    inline fun d(tag: String, msg: () -> String) {
        if (shouldLog()) {
            print(Log.DEBUG, tag, msg())
        }
    }

    inline fun w(tag: String, msg: () -> String) {
        if (shouldLog()) {
            print(Log.WARN, tag, msg())
        }
    }

    inline fun e(tag: String, error: Throwable? = null, msg: () -> String) {
        if (shouldLog()) {
            print(Log.ERROR, tag, msg(), error)
        }
    }

    inline fun v(tag: String, msg: () -> String) {
        if (shouldLog()) {
            print(Log.VERBOSE, tag, msg())
        }
    }

    @PublishedApi
    internal fun shouldLog(): Boolean {
        return Fusion.getConfig().isDebug
    }

    @PublishedApi
    internal fun print(priority: Int, tag: String, message: String, t: Throwable? = null) {
        val fullTag = "$TAG_PREFIX$tag"
        
        when (priority) {
            Log.INFO -> Log.i(fullTag, message)
            Log.DEBUG -> Log.d(fullTag, message)
            Log.WARN -> Log.w(fullTag, message)
            Log.ERROR -> Log.e(fullTag, message, t)
        }

        val config = Fusion.getConfig()
        if (config.logToFile && config.logDir != null) {
            val logMessage = buildFileLog(priority, fullTag, message, t)
            writeAsync(config.logDir, logMessage)
        }
    }

    private fun buildFileLog(priority: Int, tag: String, message: String, t: Throwable?): String {
        val time = DATE_FORMAT.format(Date())
        val thread = Thread.currentThread().name
        val levelStr = when (priority) {
            Log.INFO -> "I"
            Log.DEBUG -> "D"
            Log.WARN -> "W"
            Log.ERROR -> "E"
            else -> "V"
        }
        val stackTrace = t?.stackTraceToString() ?: ""
        return "$time [$thread] $levelStr/$tag: $message\n$stackTrace"
    }

    private fun writeAsync(dir: String, content: String) {
        fileExecutor.execute {
            try {
                val fileName = "fusion_${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())}.log"
                val file = File(dir, fileName)
                
                FileWriter(file, true).use { writer ->
                    writer.append(content).append("\n")
                }
            } catch (e: Exception) {
                Log.e("FusionLogger", "Failed to write log", e)
            }
        }
    }
}
