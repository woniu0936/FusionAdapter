package com.fusion.example.delegate

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import com.fusion.adapter.core.R
import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.adapter.delegate.BindingHolder
import com.fusion.example.databinding.ItemSocialPostBinding
import com.fusion.example.model.SocialPost
import com.fusion.example.utils.M3ColorGenerator
import com.fusion.example.utils.applyRandomAvatar
import com.google.android.material.R as MaterialR

class SocialPostDelegate(
    private val onLikeClick: (SocialPost) -> Unit
) : BindingDelegate<SocialPost, ItemSocialPostBinding>(ItemSocialPostBinding::inflate) {

    init {
        // [核心展示] 声明式 Payload 监听
        // 同时监听两个属性，任何一个变化都触发局部刷新
        bindPayload(SocialPost::isLiked, SocialPost::likeCount) { isLiked, count ->
            updateLikeState(this, isLiked, count)
        }
    }

    override fun onViewHolderCreated(holder: BindingHolder<ItemSocialPostBinding>) {
        holder.binding.btnLike.setOnClickListener {
            val item = holder.itemView.getTag(R.id.fusion_item_tag) as? SocialPost
            if (item != null) onLikeClick(item)
        }
    }

    override fun onBind(binding: ItemSocialPostBinding, item: SocialPost, position: Int) {
        binding.tvUsername.text = item.username
        binding.tvContent.text = item.content
        // 1. 设置头像为随机颜色的圆形
        binding.ivAvatar.applyRandomAvatar()

        // 2. 设置中间的大图为随机颜色的圆角矩形
        // 注意：为了让列表滚动时颜色不闪烁，建议将颜色值存入 SocialPost 模型中。
        // 这里演示简单效果，每次 bind 可能会变色，如果是 Mock 数据建议存 ID 对应的颜色。
        // 此处使用 StableId (item.id.hashCode()) 来生成固定的随机色，保证滚动一致性。
        // 这里稍微 hack 一下 M3ColorGenerator 让它支持 seed，或者直接随机:
        binding.ivMediaPlaceholder.setImageDrawable(
            M3ColorGenerator.randomRectDrawable(16f) // 16dp 圆角
        )
        updateLikeState(binding, item.isLiked, item.likeCount)
    }

    @SuppressLint("SetTextI18n")
    private fun updateLikeState(binding: ItemSocialPostBinding, isLiked: Boolean, count: Int) {
        binding.tvLikeCount.text = "$count Likes"
        // 1. 切换图标形状 (实心 vs 空心)
        if (isLiked) {
            binding.btnLike.iconTint = ColorStateList.valueOf(Color.RED)
            binding.tvLikeCount.setTextColor(Color.RED)
        } else {
            val color = resolveThemeColor(binding.root.context, MaterialR.attr.colorOnSurfaceVariant)
            binding.btnLike.iconTint = ColorStateList.valueOf(color)
            binding.tvLikeCount.setTextColor(color)
        }

        // 局部刷新时的动画效果
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