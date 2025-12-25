package com.fusion.example.delegate

import android.widget.Toast
import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.example.databinding.ItemMsgTextBinding
import com.fusion.example.model.FusionMessage
import com.fusion.example.utils.ChatStyleHelper

class TextMsgDelegate : BindingDelegate<FusionMessage, ItemMsgTextBinding>(ItemMsgTextBinding::inflate) {

    init {
        setUniqueKey { it.id }

        setOnItemClick { view, item, position ->
            Toast.makeText(view.root.context, "复制文本: ${item.content}", Toast.LENGTH_SHORT).show()
        }

        setOnItemLongClick { view, item, position ->
            Toast.makeText(view.root.context, "长按了消息: ${item.id}", Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun onBind(binding: ItemMsgTextBinding, item: FusionMessage, position: Int) {
        binding.tvContent.text = item.content
        ChatStyleHelper.bindTextMsg(binding, item.isMe)
    }
}
