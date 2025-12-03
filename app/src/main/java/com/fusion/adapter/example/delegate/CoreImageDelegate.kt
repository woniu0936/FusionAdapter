package com.fusion.adapter.example.delegate

import android.widget.Toast
import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.adapter.example.databinding.ItemImageBinding
import com.fusion.adapter.example.model.ImageItem

/**
 * [Core 演示专用] 图片委托类
 * 在不使用 KTX DSL 的情况下，你需要手动写这个类。
 */
class CoreImageDelegate : BindingDelegate<ImageItem, ItemImageBinding>(ItemImageBinding::inflate) {

    init {
        // Core 模式下，点击事件通常在这里写，或者在 onBind 里写
        onItemClick = { binding, item, _ ->
            Toast.makeText(binding.root.context, "点击了图片: ${item.id}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBind(binding: ItemImageBinding, item: ImageItem, position: Int) {
        binding.tvDesc.text = "图片 ID: ${item.id}"

        // 实际开发中请使用 Glide/Coil 加载图片
        // Glide.with(binding.ivImage).load(item.url).into(binding.ivImage)

        // 这里为了演示简单，我们只设个背景色区分
        binding.ivImage.setBackgroundColor(if (position % 2 == 0) 0xFFE0E0E0.toInt() else 0xFFCCCCCC.toInt())
    }
}