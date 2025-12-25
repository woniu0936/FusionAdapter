package com.fusion.example.delegate

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.adapter.extensions.getItem
import com.fusion.example.databinding.ItemSocialPostBinding
import com.fusion.example.model.SocialPost
import com.fusion.example.utils.M3ColorGenerator
import com.fusion.example.utils.applyRandomAvatar
import com.google.android.material.R as MaterialR

class SocialPostDelegate(
    private val onLikeClick: (SocialPost) -> Unit
) : BindingDelegate<SocialPost, ItemSocialPostBinding>(ItemSocialPostBinding::inflate) {

    init {
        // 使用 setter 注入业务主键
        setUniqueKey { it.id }

        onPayload(SocialPost::isLiked, SocialPost::likeCount) { isLiked, count ->
            updateLikeState(this, isLiked, count)
        }
    }

    override fun onCreate(binding: ItemSocialPostBinding) {
        binding.btnLike.setOnClickListener {
            // 已更新为 getItem
            val item = binding.root.getItem<SocialPost>()
            if (item != null) onLikeClick(item)
        }
    }

    override fun onBind(binding: ItemSocialPostBinding, item: SocialPost, position: Int) {
        binding.tvUsername.text = item.username
        binding.tvContent.text = item.content
        binding.ivAvatar.applyRandomAvatar()
        binding.ivMediaPlaceholder.setImageDrawable(M3ColorGenerator.randomRectDrawable(16f))
        updateLikeState(binding, item.isLiked, item.likeCount)
    }

    @SuppressLint("SetTextI18n")
    private fun updateLikeState(binding: ItemSocialPostBinding, isLiked: Boolean, count: Int) {
        binding.tvLikeCount.text = "$count Likes"
        if (isLiked) {
            binding.btnLike.iconTint = ColorStateList.valueOf(Color.RED)
            binding.tvLikeCount.setTextColor(Color.RED)
        } else {
            val color = resolveThemeColor(binding.root.context, MaterialR.attr.colorOnSurfaceVariant)
            binding.btnLike.iconTint = ColorStateList.valueOf(color)
            binding.tvLikeCount.setTextColor(color)
        }

        binding.btnLike.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100)
            .withEndAction {
                binding.btnLike.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
            }.start()
    }

    private fun resolveThemeColor(context: android.content.Context, attrRes: Int): Int {
        val typedValue = android.util.TypedValue()
        context.theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.data
    }
}
