package com.fusion.example.utils

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import com.fusion.example.databinding.ItemMsgImageBinding
import com.fusion.example.databinding.ItemMsgTextBinding
import com.google.android.material.shape.ShapeAppearanceModel

object ChatStyleHelper {

    fun bindTextMsg(binding: ItemMsgTextBinding, isMe: Boolean) {
        val context = binding.root.context
        val density = context.resources.displayMetrics.density

        if (isMe) {
            binding.rootLayout.gravity = Gravity.END
            binding.ivAvatarLeft.visibility = View.GONE
            binding.ivAvatarRight.visibility = View.VISIBLE
            
            val bg = resolveThemeColor(context, com.google.android.material.R.attr.colorPrimaryContainer)
            val txt = resolveThemeColor(context, com.google.android.material.R.attr.colorOnPrimaryContainer)

            binding.cardBubble.setCardBackgroundColor(bg)
            binding.tvContent.setTextColor(txt)
            binding.tvTime.gravity = Gravity.END
            binding.cardBubble.shapeAppearanceModel = getBubbleShape(density, isMe = true)
        } else {
            binding.rootLayout.gravity = Gravity.START
            binding.ivAvatarLeft.visibility = View.VISIBLE
            binding.ivAvatarRight.visibility = View.GONE

            val bg = resolveThemeColor(context, com.google.android.material.R.attr.colorSecondaryContainer)
            val txt = resolveThemeColor(context, com.google.android.material.R.attr.colorOnSecondaryContainer)

            binding.cardBubble.setCardBackgroundColor(bg)
            binding.tvContent.setTextColor(txt)
            binding.tvTime.gravity = Gravity.START
            binding.cardBubble.shapeAppearanceModel = getBubbleShape(density, isMe = false)
        }
    }

    fun bindImageMsg(binding: ItemMsgImageBinding, isMe: Boolean) {
        val context = binding.root.context
        binding.ivImage.setImageDrawable(M3ColorGenerator.randomRectDrawable(0f))

        if (isMe) {
            binding.rootLayout.gravity = Gravity.END
            binding.ivAvatarLeft.visibility = View.GONE
            binding.ivAvatarRight.visibility = View.VISIBLE
            binding.cardBubble.strokeColor = resolveThemeColor(context, android.R.attr.colorPrimary)
        } else {
            binding.rootLayout.gravity = Gravity.START
            binding.ivAvatarLeft.visibility = View.VISIBLE
            binding.ivAvatarRight.visibility = View.GONE
            binding.cardBubble.strokeColor = resolveThemeColor(context, android.R.attr.textColorSecondary)
        }
    }

    private fun getBubbleShape(density: Float, isMe: Boolean): ShapeAppearanceModel {
        val builder = ShapeAppearanceModel.builder().setAllCornerSizes(20f * density)
        if (isMe) builder.setTopRightCornerSize(4f * density)
        else builder.setTopLeftCornerSize(4f * density)
        return builder.build()
    }

    private fun resolveThemeColor(context: Context, attrRes: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.data
    }

    fun bindStandaloneImage(binding: com.fusion.example.databinding.ItemImageBinding) {
        binding.ivImage.setImageDrawable(M3ColorGenerator.randomRectDrawable(0f))
    }
}