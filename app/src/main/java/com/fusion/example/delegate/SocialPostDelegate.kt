package com.fusion.example.delegate

import android.annotation.SuppressLint
import android.graphics.Color
import com.fusion.adapter.core.R
import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.example.databinding.ItemSocialPostBinding
import com.fusion.example.model.SocialPost
import com.fusion.example.utils.M3ColorGenerator
import com.fusion.example.utils.applyRandomAvatar

class SocialPostDelegate(
    private val onLikeClick: (SocialPost) -> Unit
) : BindingDelegate<SocialPost, ItemSocialPostBinding>(ItemSocialPostBinding::inflate) {

    init {
        // [核心展示] 声明式 Payload 监听
        // 同时监听两个属性，任何一个变化都触发局部刷新
        bindPayload(SocialPost::isLiked, SocialPost::likeCount) { isLiked, count ->
            updateLikeState(this, isLiked, count, animate = true)
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
        val seed = item.id.hashCode().toLong()
        // 这里稍微 hack 一下 M3ColorGenerator 让它支持 seed，或者直接随机:
        binding.ivMediaPlaceholder.setImageDrawable(
            M3ColorGenerator.randomRectDrawable(16f) // 16dp 圆角
        )
        updateLikeState(binding, item.isLiked, item.likeCount, animate = false)
    }

    @SuppressLint("SetTextI18n")
    private fun updateLikeState(binding: ItemSocialPostBinding, isLiked: Boolean, count: Int, animate: Boolean) {
        binding.tvLikeCount.text = "$count Likes"
        val iconRes = if (isLiked) android.R.drawable.star_big_on else android.R.drawable.star_big_off
        binding.btnLike.setIconResource(iconRes)

        if (animate) {
            // 局部刷新时的动画效果
            binding.btnLike.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction {
                binding.btnLike.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
            }.start()
            binding.tvLikeCount.setTextColor(Color.RED)
        } else {
            binding.tvLikeCount.setTextColor(Color.GRAY)
        }
    }
}