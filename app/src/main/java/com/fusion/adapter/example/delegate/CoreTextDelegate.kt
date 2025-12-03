package com.fusion.adapter.example.delegate

import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.adapter.example.databinding.ItemTextBinding
import com.fusion.adapter.example.model.TextItem

/**
 * 标准的 Delegate 写法。
 * 这种写法在 Core 模式下是必须的。
 */
class CoreTextDelegate : BindingDelegate<TextItem, ItemTextBinding>(ItemTextBinding::inflate) {
    override fun onBind(binding: ItemTextBinding, item: TextItem, position: Int) {
        binding.tvContent.text = "[Core] ${item.content}"
    }
}