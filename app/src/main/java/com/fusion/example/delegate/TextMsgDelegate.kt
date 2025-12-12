package com.fusion.example.delegate

import android.widget.Toast
import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.example.databinding.ItemMsgTextBinding
import com.fusion.example.model.FusionMessage
import com.fusion.example.utils.ChatStyleHelper

class TextMsgDelegate : BindingDelegate<FusionMessage, ItemMsgTextBinding>(ItemMsgTextBinding::inflate) {

    init {
        // [可选] 设置点击事件
        // 这里的 item 是强类型的 FusionMessage
        onItemClick = { view, item, position ->
            Toast.makeText(view.root.context, "复制文本: ${item.content}", Toast.LENGTH_SHORT).show()
        }

        // [可选] 设置长按事件
        onItemLongClick = { view, item, position ->
            Toast.makeText(view.root.context, "长按了消息: ${item.id}", Toast.LENGTH_SHORT).show()
            true // 返回 true 表示消费事件
        }
    }

    override fun onBind(binding: ItemMsgTextBinding, item: FusionMessage, position: Int) {
        // 直接操作 Binding，无需 findViewById
        binding.tvContent.text = item.content
        ChatStyleHelper.bindTextMsg(binding, item.isMe)
    }

}