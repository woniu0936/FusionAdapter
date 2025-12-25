package com.fusion.example.delegate

import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.example.databinding.ItemMsgSystemBinding
import com.fusion.example.model.FusionMessage

class SystemMsgDelegate : BindingDelegate<FusionMessage, ItemMsgSystemBinding>(ItemMsgSystemBinding::inflate) {

    init {
        setUniqueKey { it.id }
    }

    override fun onBind(binding: ItemMsgSystemBinding, item: FusionMessage, position: Int) {
        binding.tvSystemMsg.text = item.content
    }
}
