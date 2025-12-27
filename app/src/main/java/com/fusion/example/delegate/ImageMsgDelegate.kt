package com.fusion.example.delegate

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.example.core.model.ChatMessage
import com.fusion.example.databinding.ItemMsgImageBinding
import com.fusion.example.utils.loadUrl
import com.fusion.example.utils.M3ColorGenerator

class ImageMsgDelegate : BindingDelegate<ChatMessage, ItemMsgImageBinding>(ItemMsgImageBinding::inflate) {
    init { setUniqueKey { it.id } }
    override fun onBind(binding: ItemMsgImageBinding, item: ChatMessage, position: Int) {
        binding.apply {
            ivImage.setImageDrawable(M3ColorGenerator.randomRectDrawable(0f))
            val params = cardBubble.layoutParams as ConstraintLayout.LayoutParams
            
            if (item.isMe) {
                ivAvatarLeft.visibility = View.GONE
                ivAvatarRight.visibility = View.VISIBLE
                ivAvatarRight.loadUrl("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150", isCircle = true)
                params.horizontalBias = 1.0f
            } else {
                ivAvatarLeft.visibility = View.VISIBLE
                ivAvatarRight.visibility = View.GONE
                val avatarUrl = item.sender?.avatar ?: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150"
                ivAvatarLeft.loadUrl(avatarUrl, isCircle = true)
                params.horizontalBias = 0.0f
            }
            cardBubble.layoutParams = params
        }
    }
}
