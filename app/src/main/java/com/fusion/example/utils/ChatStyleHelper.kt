package com.fusion.example.utils

import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.fusion.example.databinding.ItemMsgImageBinding
import com.fusion.example.databinding.ItemMsgTextBinding
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.R as MaterialR

object ChatStyleHelper {

    fun bindTextMsg(binding: ItemMsgTextBinding, isMe: Boolean) {
        val context = binding.root.context
        val density = context.resources.displayMetrics.density
        val params = binding.cardBubble.layoutParams as ConstraintLayout.LayoutParams

        if (isMe) {
            binding.ivAvatarLeft.visibility = View.GONE
            binding.ivAvatarRight.visibility = View.VISIBLE
            params.horizontalBias = 1.0f
            
            val bg = resolveThemeColor(context, MaterialR.attr.colorPrimaryContainer)
            val txt = resolveThemeColor(context, MaterialR.attr.colorOnPrimaryContainer)

            binding.cardBubble.setCardBackgroundColor(bg)
            binding.tvContent.setTextColor(txt)
            binding.cardBubble.shapeAppearanceModel = getBubbleShape(density, isMe = true)
        } else {
            binding.ivAvatarLeft.visibility = View.VISIBLE
            binding.ivAvatarRight.visibility = View.GONE
            params.horizontalBias = 0.0f

            val bg = resolveThemeColor(context, MaterialR.attr.colorSecondaryContainer)
            val txt = resolveThemeColor(context, MaterialR.attr.colorOnSecondaryContainer)

            binding.cardBubble.setCardBackgroundColor(bg)
            binding.tvContent.setTextColor(txt)
            binding.cardBubble.shapeAppearanceModel = getBubbleShape(density, isMe = false)
        }
        binding.cardBubble.layoutParams = params
    }

    fun bindImageMsg(binding: ItemMsgImageBinding, isMe: Boolean) {
        val context = binding.root.context
        binding.ivImage.setImageDrawable(M3ColorGenerator.randomRectDrawable(0f))
        val params = binding.cardBubble.layoutParams as ConstraintLayout.LayoutParams

        if (isMe) {
            binding.ivAvatarLeft.visibility = View.GONE
            binding.ivAvatarRight.visibility = View.VISIBLE
            binding.ivAvatarRight.loadUrl("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150", isCircle = true)
            params.horizontalBias = 1.0f
            binding.cardBubble.strokeColor = resolveThemeColor(context, android.R.attr.colorPrimary)
        } else {
            binding.ivAvatarLeft.visibility = View.VISIBLE
            binding.ivAvatarRight.visibility = View.GONE
            binding.ivAvatarLeft.loadUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150", isCircle = true)
            params.horizontalBias = 0.0f
            binding.cardBubble.strokeColor = resolveThemeColor(context, android.R.attr.textColorSecondary)
        }
        binding.cardBubble.layoutParams = params
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