package com.fusion.example.utils

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import com.fusion.example.databinding.ItemMsgImageBinding
import com.fusion.example.databinding.ItemMsgTextBinding
import com.google.android.material.shape.ShapeAppearanceModel
import com.fusion.example.R // 确保引用你的 R 文件

object ChatStyleHelper {

    /**
     * 绑定文本消息样式 (M3 规范)
     */
    fun bindTextMsg(binding: ItemMsgTextBinding, isMe: Boolean) {
        val context = binding.root.context
        val density = context.resources.displayMetrics.density

        if (isMe) {
            // --- 我 (右侧) ---
            binding.rootLayout.gravity = Gravity.END
            binding.ivAvatarLeft.visibility = View.GONE
            binding.ivAvatarRight.visibility = View.VISIBLE
            binding.ivAvatarRight.applyRandomAvatar()

            // 颜色：PrimaryContainer
            val bg = resolveThemeColor(context, com.google.android.material.R.attr.colorPrimaryContainer)
            val txt = resolveThemeColor(context, com.google.android.material.R.attr.colorOnPrimaryContainer)

            binding.cardBubble.setCardBackgroundColor(bg)
            binding.tvContent.setTextColor(txt)
            binding.tvTime.gravity = Gravity.END

            // 形状：右上角小圆角
            binding.cardBubble.shapeAppearanceModel = getBubbleShape(density, isMe = true)

        } else {
            // --- 对方 (左侧) ---
            binding.rootLayout.gravity = Gravity.START
            binding.ivAvatarLeft.visibility = View.VISIBLE
            binding.ivAvatarRight.visibility = View.GONE
            binding.ivAvatarLeft.applyRandomAvatar()

            // 颜色：SecondaryContainer
            val bg = resolveThemeColor(context, com.google.android.material.R.attr.colorSecondaryContainer)
            val txt = resolveThemeColor(context, com.google.android.material.R.attr.colorOnSecondaryContainer)

            binding.cardBubble.setCardBackgroundColor(bg)
            binding.tvContent.setTextColor(txt)
            binding.tvTime.gravity = Gravity.START

            // 形状：左上角小圆角
            binding.cardBubble.shapeAppearanceModel = getBubbleShape(density, isMe = false)
        }
    }

    /**
     * 绑定图片消息样式 (M3 规范)
     */
    fun bindImageMsg(binding: ItemMsgImageBinding, isMe: Boolean) {
        val context = binding.root.context

        // 设置随机色块占位
        binding.ivImage.setImageDrawable(M3ColorGenerator.randomRectDrawable(0f))

        if (isMe) {
            binding.rootLayout.gravity = Gravity.END
            binding.ivAvatarLeft.visibility = View.GONE
            binding.ivAvatarRight.visibility = View.VISIBLE
            binding.ivAvatarRight.applyRandomAvatar()

            val outlineColor = resolveThemeColor(context, R.color.colorPrimary)
            binding.cardBubble.strokeColor = outlineColor
        } else {
            binding.rootLayout.gravity = Gravity.START
            binding.ivAvatarLeft.visibility = View.VISIBLE
            binding.ivAvatarRight.visibility = View.GONE
            binding.ivAvatarLeft.applyRandomAvatar()

            val outlineColor = resolveThemeColor(context, R.color.colorOutline)
            binding.cardBubble.strokeColor = outlineColor
        }
    }

    // --- 内部辅助方法 ---

    private fun getBubbleShape(density: Float, isMe: Boolean): ShapeAppearanceModel {
        val builder = ShapeAppearanceModel.builder().setAllCornerSizes(20f * density)
        if (isMe) {
            builder.setTopRightCornerSize(4f * density)
        } else {
            builder.setTopLeftCornerSize(4f * density)
        }
        return builder.build()
    }

    private fun resolveThemeColor(context: Context, attrRes: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.data
    }

    /**
     * 绑定独立图片项样式 (用于 Grid/Staggered 列表)
     * 样式：M3 圆角卡片 + 随机色占位 + OutlineVariant 边框
     */
    fun bindStandaloneImage(binding: com.fusion.example.databinding.ItemImageBinding) {
        // 设置随机色块作为图片内容/占位
        // 注意：这里使用 randomRectDrawable 生成纯色块
        binding.ivImage.setImageDrawable(M3ColorGenerator.randomRectDrawable(0f))

        // 如果需要，可以在这里动态修改 Card 的边框颜色，但通常 XML 里的 outlineVariant 足够了
        // 也可以在这里处理点击水波纹等通用逻辑
    }
}