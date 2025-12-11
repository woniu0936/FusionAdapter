package com.fusion.adapter.internal

import android.util.Log
import androidx.annotation.RestrictTo
import com.fusion.adapter.Fusion

/**
 * [Fusion 内部日志工具]
 *
 * 利用 Kotlin inline 特性，实现 Release 模式下的"零开销"日志。
 * 只有当 FusionConfig.isDebug == true 时，message lambda 才会执行字符串拼接。
 */
@PublishedApi
internal const val LIB_TAG = "FusionAdapter"

/**
 * [Debug Log]
 * @param tag 子模块名称，例如 "Core", "Diff", "Registry"
 * @param message 惰性消息构建器
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun logD(tag: String, message: () -> String) {
    if (Fusion.getConfig().isDebug) {
        // 只有开启 Debug 时，才会执行 message() 进行字符串拼接
        Log.d(LIB_TAG, "[${Thread.currentThread().name}][$tag] ${message()}")
    }
}

/**
 * [Info Log]
 * 关键流程信息
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun logI(tag: String, message: () -> String) {
    if (Fusion.getConfig().isDebug) {
        Log.i(LIB_TAG, "[${Thread.currentThread().name}][$tag] ${message()}")
    }
}

/**
 * [Warn Log]
 * 关键流程信息
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun logW(tag: String, message: () -> String) {
    if (Fusion.getConfig().isDebug) {
        Log.w(LIB_TAG, "[${Thread.currentThread().name}][$tag] ${message()}")
    }
}


/**
 * [Error Log]
 * 错误信息 (通常 Error 即使在 Release 也可以考虑保留，但为了纯净我们这里也受 isDebug 控制，
 * 或者你可以决定 Error 始终打印)
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun logE(tag: String, throwable: Throwable? = null, message: () -> String) {
    if (Fusion.getConfig().isDebug) {
        Log.e(LIB_TAG, "[${Thread.currentThread().name}][$tag] ${message()}", throwable)
    }
}