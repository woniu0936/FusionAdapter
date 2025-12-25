package com.fusion.example.delegate

import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.example.databinding.ItemMsgImageBinding
import com.fusion.example.model.FusionMessage
import com.fusion.example.utils.ChatStyleHelper

class ImageMsgDelegate : BindingDelegate<FusionMessage, ItemMsgImageBinding>(ItemMsgImageBinding::inflate) {

    init {
        setUniqueKey { it.id }
    }

    override fun onBind(binding: ItemMsgImageBinding, item: FusionMessage, position: Int) {
        binding.ivImage.contentDescription = item.content
        ChatStyleHelper.bindImageMsg(binding, item.isMe)
    }
}
