package com.fusion.example.delegate

import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.example.databinding.ItemMsgImageBinding
import com.fusion.example.core.model.ChatMessage
import com.fusion.example.utils.loadUrl

class ImageMsgDelegate : BindingDelegate<ChatMessage, ItemMsgImageBinding>(ItemMsgImageBinding::inflate) {
    init { setUniqueKey { it.id } }
    override fun onBind(binding: ItemMsgImageBinding, item: ChatMessage, position: Int) {
        // Mock image message
        binding.ivImage.setBackgroundColor(android.graphics.Color.LTGRAY)
    }
}