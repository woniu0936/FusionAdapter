package com.fusion.example.delegate

import android.view.Gravity
import android.view.View
import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.example.databinding.ItemMsgTextBinding
import com.fusion.example.core.model.ChatMessage
import com.fusion.example.utils.loadUrl

class TextMsgDelegate : BindingDelegate<ChatMessage, ItemMsgTextBinding>(ItemMsgTextBinding::inflate) {
    init { setUniqueKey { it.id } }
    override fun onBind(binding: ItemMsgTextBinding, item: ChatMessage, position: Int) {
        binding.tvContent.text = item.content
        if (item.isMe) {
            binding.rootLayout.gravity = Gravity.END
            binding.ivAvatarLeft.visibility = View.GONE
            binding.ivAvatarRight.visibility = View.VISIBLE
        } else {
            binding.rootLayout.gravity = Gravity.START
            binding.ivAvatarLeft.visibility = View.VISIBLE
            binding.ivAvatarRight.visibility = View.GONE
            item.sender?.let { binding.ivAvatarLeft.loadUrl(it.avatar, isCircle = true) }
        }
    }
}