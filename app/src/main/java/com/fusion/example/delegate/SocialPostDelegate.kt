package com.fusion.example.delegate

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.adapter.extensions.getItem
import com.fusion.example.core.model.Moment
import com.fusion.example.databinding.ItemMomentCardBinding
import com.fusion.example.utils.loadUrl

class SocialPostDelegate(
    private val onLikeClick: (Moment) -> Unit
) : BindingDelegate<Moment, ItemMomentCardBinding>(ItemMomentCardBinding::inflate) {

    init {

        onPayload(Moment::isLiked, Moment::likes) { isLiked, count ->
            updateLikeState(this, isLiked, count)
        }
    }

    override fun onCreate(binding: ItemMomentCardBinding) {
        binding.btnLike.setOnClickListener {
            val item = binding.root.getItem<Moment>()
            if (item != null) onLikeClick(item)
        }
    }

    override fun onBind(binding: ItemMomentCardBinding, item: Moment, position: Int) {
        binding.tvName.text = item.author.name
        binding.tvContent.text = item.content
        binding.ivAvatar.loadUrl(item.author.avatar, isCircle = true)

        if (item.images.isNotEmpty()) {
            binding.imageContainer.visibility = View.VISIBLE
            binding.ivImage.loadUrl(item.images[0])
        } else {
            binding.imageContainer.visibility = View.GONE
        }

        updateLikeState(binding, item.isLiked, item.likes)
    }

    @SuppressLint("SetTextI18n")
    private fun updateLikeState(binding: ItemMomentCardBinding, isLiked: Boolean, count: Int) {
        binding.btnLike.text = count.toString()
        if (isLiked) {
            binding.btnLike.setIconResource(android.R.drawable.btn_star_big_on)
            binding.btnLike.iconTint = ColorStateList.valueOf(Color.RED)
            binding.btnLike.setTextColor(Color.RED)
        } else {
            binding.btnLike.setIconResource(android.R.drawable.btn_star_big_off)
            binding.btnLike.iconTint = ColorStateList.valueOf(Color.GRAY)
            binding.btnLike.setTextColor(Color.GRAY)
        }
    }

    override fun getUniqueKey(item: Moment): Any {
        return item.id
    }
}
