package com.fusion.adapter.extensions

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.delegate.LayoutHolder

// =================================================================
// 1. 批量配置 (Batch Configuration) - 解决重复 ID 的终极方案
// =================================================================

/**
 * [通用 View 配置]
 * 进入 View 的作用域，批量修改属性。
 *
 * @sample
 * configure(R.id.view_divider) {
 *     alpha = 0.5f
 *     setBackgroundColor(Color.GRAY)
 * }
 */
inline fun LayoutHolder.configure(@IdRes id: Int, block: View.() -> Unit) {
    findView<View>(id).block()
}

/**
 * [TextView 专用配置]
 * 自动强转为 TextView，方便访问 text, textSize 等属性。
 *
 * @sample
 * configText(R.id.tv_title) {
 *     text = "Hello"
 *     textSize = 18f
 *     setTextColor(Color.RED)
 * }
 */
inline fun LayoutHolder.configText(@IdRes id: Int, block: TextView.() -> Unit) {
    findView<TextView>(id).block()
}

/**
 * [RecyclerView 专用配置]
 * 自动强转为 RecyclerView，方便访问 adapter, layoutManager 等属性。
 *
 * @sample
 * configList(R.id.rv_list) {
 *     adapter = MyAdapter()
 *     layoutManager = LinearLayoutManager(context)
 * }
 */
fun LayoutHolder.configList(@IdRes id: Int, block: RecyclerView.() -> Unit) {
    findView<RecyclerView>(id).block()
}

/**
 * [ImageView 专用配置]
 */
inline fun LayoutHolder.configImage(@IdRes id: Int, block: ImageView.() -> Unit) {
    findView<ImageView>(id).block()
}

// =================================================================
// 2. TextView 扩展 (Rich Text API)
// =================================================================

fun LayoutHolder.setText(@IdRes id: Int, value: CharSequence?) {
    findView<TextView>(id).text = value
}

fun LayoutHolder.setText(@IdRes id: Int, @StringRes resId: Int) {
    findView<TextView>(id).setText(resId)
}

fun LayoutHolder.setTextColor(@IdRes id: Int, @ColorInt color: Int) {
    findView<TextView>(id).setTextColor(color)
}

/**
 * 设置字体大小
 * @param sp 单位默认是 SP
 */
fun LayoutHolder.setTextSize(@IdRes id: Int, sp: Float) {
    findView<TextView>(id).textSize = sp
}

/**
 * 设置字体样式 (粗体/斜体)
 * @param style e.g. Typeface.BOLD
 */
fun LayoutHolder.setTextStyle(@IdRes id: Int, style: Int) {
    val textView = findView<TextView>(id)
    textView.setTypeface(textView.typeface, style)
}

// =================================================================
// 3. ImageView 扩展
// =================================================================

fun LayoutHolder.setImage(@IdRes id: Int, @DrawableRes resId: Int) {
    findView<ImageView>(id).setImageResource(resId)
}

fun LayoutHolder.setImage(@IdRes id: Int, drawable: Drawable?) {
    findView<ImageView>(id).setImageDrawable(drawable)
}

// =================================================================
// 4. 通用 View 属性 (General Properties)
// =================================================================

fun LayoutHolder.setVisible(@IdRes id: Int, isVisible: Boolean) {
    findView<View>(id).visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun LayoutHolder.setAlpha(@IdRes id: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float) {
    findView<View>(id).alpha = alpha
}

fun LayoutHolder.setBackground(@IdRes id: Int, @DrawableRes resId: Int) {
    findView<View>(id).setBackgroundResource(resId)
}

fun LayoutHolder.setBackgroundColor(@IdRes id: Int, @ColorInt color: Int) {
    findView<View>(id).setBackgroundColor(color)
}

fun LayoutHolder.setEnabled(@IdRes id: Int, isEnabled: Boolean) {
    findView<View>(id).isEnabled = isEnabled
}

fun LayoutHolder.setSelected(@IdRes id: Int, isSelected: Boolean) {
    findView<View>(id).isSelected = isSelected
}

// =================================================================
// 5. 交互事件 (Interactions)
// =================================================================

fun LayoutHolder.onClick(@IdRes id: Int, debounceMs: Long = 500L, action: (View) -> Unit) {
    findView<View>(id).setOnClickListener(ThrottledClickListener(debounceMs, action))
}

fun LayoutHolder.onLongClick(@IdRes id: Int, action: (View) -> Boolean) {
    findView<View>(id).setOnLongClickListener(action)
}

// =================================================================
// Internal Logic
// =================================================================

private class ThrottledClickListener(
    private val interval: Long,
    private val action: (View) -> Unit
) : View.OnClickListener {
    private var lastClickTime = 0L

    override fun onClick(v: View) {
        val now = System.currentTimeMillis()
        if (now - lastClickTime >= interval) {
            lastClickTime = now
            action(v)
        }
    }
}