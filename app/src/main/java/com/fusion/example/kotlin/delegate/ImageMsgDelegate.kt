package com.fusion.example.kotlin.delegate


import android.R
import android.widget.Toast
import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.example.databinding.ItemMsgImageBinding
import com.fusion.example.model.FusionMessage

class ImageMsgDelegate : BindingDelegate<FusionMessage, ItemMsgImageBinding>(ItemMsgImageBinding::inflate) {

    init {
        onItemClick = { view, item, _ ->
            Toast.makeText(view.root.context, "查看大图: ${item.content}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBind(binding: ItemMsgImageBinding, item: FusionMessage, position: Int) {
        // 绑定描述文字
        binding.tvDesc.text = item.content

        // 绑定图片
        // 在实际项目中，这里应该使用 Glide 或 Coil
        // 例如: Coil.load(binding.ivImage, item.imageUrl)

        // 这里仅做演示，模拟设置一个占位图
        binding.ivImage.setImageResource(R.drawable.ic_menu_gallery)
    }
}