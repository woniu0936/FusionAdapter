package com.fusion.example.utils

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import kotlin.random.Random

object M3ColorGenerator {
    private val m3Colors = listOf(
        "#D5E3FF", // Primary Container
        "#DAE2F9", // Secondary Container
        "#FAD8FD", // Tertiary Container
        "#E0E2EC"  // Surface Variant
    )

    fun randomM3Color() = Color.parseColor(m3Colors.random())

    fun randomRectDrawable(radiusDp: Float = 20f): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radiusDp * 3 // px approx
            setColor(randomM3Color())
        }
    }
}
