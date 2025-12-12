package com.fusion.example.utils

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import kotlin.random.Random

/**
 * 严格遵循 Material 3 色彩规范的随机颜色生成器。
 * 主要模拟 Tonal Palette 中的 Level 90 (Light Mode Container) 或 Level 30 (Dark Mode Container)。
 * 这里为了通用美观，选用柔和的 Pastel 色系。
 */
object M3ColorGenerator {
    private val materialColors = listOf(
        "#FFD8E4", // Pink Container
        "#F2DAFF", // Purple Container
        "#D7E3FF", // Blue Container
        "#C4E7FF", // Light Blue Container
        "#C3EFD0", // Green Container
        "#E7DEBC", // Yellow Container
        "#FFDBCF", // Orange Container
        "#E6E0E9"  // Surface Variant
    )

    fun randomColor(): Int {
        val colorStr = materialColors[Random.nextInt(materialColors.size)]
        return Color.parseColor(colorStr)
    }

    // 生成一个圆形的随机色 Drawable（用于头像）
    fun randomAvatarDrawable(): Drawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        drawable.setColor(randomColor())
        return drawable
    }

    // 生成一个圆角的随机色 Drawable（用于图片占位）
    fun randomRectDrawable(radiusDp: Float = 12f): Drawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = radiusDp * 3 // 粗略转换 dp to px，实际建议传 px
        drawable.setColor(randomColor())
        return drawable
    }
}

// 扩展函数：方便直接给 View 设置 M3 风格随机背景
fun View.applyRandomM3Background() {
    this.setBackgroundColor(M3ColorGenerator.randomColor())
}

fun ImageView.applyRandomAvatar() {
    this.setImageDrawable(M3ColorGenerator.randomAvatarDrawable())
}