package com.fusion.adapter.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding

/**
 * 为 ViewBinding 提供直接的 Context 访问入口
 */
val ViewBinding.context: Context
    get() = this.root.context

// 修正：移除 inline，避免字节码膨胀。JVM 足够智能处理这种简单方法的内联。
fun ViewBinding.string(@StringRes id: Int): String = context.getString(id)
fun ViewBinding.string(@StringRes id: Int, vararg args: Any): String = context.getString(id, *args)
fun ViewBinding.color(@ColorRes id: Int): Int = ContextCompat.getColor(context, id)
fun ViewBinding.drawable(@DrawableRes id: Int): Drawable? = ContextCompat.getDrawable(context, id)
fun ViewBinding.dimen(@DimenRes id: Int): Float = context.resources.getDimension(id)