package com.fusion.example.delegate

import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.example.databinding.ItemMsgSystemBinding
import com.fusion.example.model.FusionMessage
import com.fusion.example.utils.ChatStyleHelper

class SystemMsgDelegate : BindingDelegate<FusionMessage, ItemMsgSystemBinding>(ItemMsgSystemBinding::inflate) {

    // 系统消息通常不需要点击事件，所以这里不设置 onItemClick

    override fun onBind(binding: ItemMsgSystemBinding, item: FusionMessage, position: Int) {
        binding.tvSystemMsg.text = item.content
    }
}