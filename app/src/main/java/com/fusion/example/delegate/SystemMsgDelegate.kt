package com.fusion.example.delegate

import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.example.databinding.ItemMsgSystemBinding
import com.fusion.example.core.model.ChatMessage

class SystemMsgDelegate : BindingDelegate<ChatMessage, ItemMsgSystemBinding>(ItemMsgSystemBinding::inflate) {

    override fun onBind(binding: ItemMsgSystemBinding, item: ChatMessage, position: Int) {
        binding.tvSystemMsg.text = item.content
    }

    override fun getUniqueKey(item: ChatMessage): Any {
        return item.id
    }
}