package com.fusion.adapter.internal

import android.util.Log
import androidx.annotation.RestrictTo
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.Fusion
import com.fusion.adapter.delegate.FunctionalBindingDelegate
import com.fusion.adapter.delegate.FusionDelegate
import com.fusion.adapter.exception.MissingStableIdException

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun checkStableIdRequirement(
    adapter: RecyclerView.Adapter<*>,
    itemClass: Class<*>,
    delegates: Collection<FusionDelegate<*, *>>
) {
    // 只有当全局开启了默认 StableId 时才进行检查
    if (!Fusion.getConfig().defaultStableId) return

    // 检查是否有 Delegate 缺少 idProvider
    val hasMissingId = delegates.any { delegate ->
        // 使用 <*, *> 解决编译错误
        if (delegate is FunctionalBindingDelegate<*, *>) {
            // 情况 A: DSL 创建的 Delegate
            // 检查内部的 keyProvider 是否为 null (注意：这里要对应我们刚重命名的变量名)
            delegate.idProvider == null
        } else {
            // 情况 B: 自定义继承的 Delegate (无法静态检查，放行)
            false
        }
    }

    if (hasMissingId) {
        if (Fusion.getConfig().isDebug) {
            // [Debug模式]：严厉策略，直接崩溃，帮助用户快速定位问题
            throw MissingStableIdException(itemClass)
        } else {
            // [Release模式]：宽容策略，尝试关闭 StableId 以防止 Crash
            // 注意：如果 Adapter 已经 setAdapter 给 RecyclerView 并且有观察者，
            // setHasStableIds(false) 可能会抛异常，这里我们需要捕获并忽略，
            // 依赖 getItemId 的运行时兜底。
            try {
                if (adapter.hasStableIds()) {
                    adapter.setHasStableIds(false)
                    Log.w("Fusion", "⚠️ [Safety] Disabled stable IDs because '${itemClass.simpleName}' is missing configuration.")
                }
            } catch (e: Exception) {
                Log.e("Fusion", "⚠️ [Safety] Failed to disable stable IDs. Will rely on runtime fallback.", e)
            }
        }
    }
}