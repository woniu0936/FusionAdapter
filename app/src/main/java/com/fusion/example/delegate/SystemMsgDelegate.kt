package com.fusion.example.delegate

import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.example.databinding.ItemMsgSystemBinding
import com.fusion.example.core.model.ChatMessage

class SystemMsgDelegate : BindingDelegate<ChatMessage, ItemMsgSystemBinding>(ItemMsgSystemBinding::inflate) {
    init { setUniqueKey { it.id } }
    override fun onBind(binding: ItemMsgSystemBinding, item: ChatMessage, position: Int) {
        binding.tvSystemMsg.text = item.content
    }
}