package com.fusion.example.delegate

import com.fusion.adapter.delegate.LayoutDelegate
import com.fusion.adapter.delegate.LayoutHolder
import com.fusion.adapter.extensions.configure
import com.fusion.adapter.extensions.setText
import com.fusion.example.R
import com.fusion.example.model.SimpleItem
import com.fusion.example.utils.M3ColorGenerator

class SimpleLayoutDelegate : LayoutDelegate<SimpleItem>(R.layout.item_simple_layout) {

    init {
        // Payload 更新逻辑 (保持不变)
        bindPayload(SimpleItem::title) { title ->
            setText(R.id.tv_title, title)
            // Payload 更新时也可以选择更新颜色，或者保持不变
        }

        setOnItemClick { _, item, _ ->
            // 点击事件逻辑
        }
    }

    override fun getStableId(item: SimpleItem): Any? {
        return item.id
    }

    override fun LayoutHolder.onBind(item: SimpleItem) {
        // 1. 设置标题
        setText(R.id.tv_title, item.title)

        // 2. 颜色设置 (原本你是设置字体颜色，现在我们改回标准的 M3 文字颜色，改动指示条颜色)
        // 获取 TextView 并重置颜色 (防止复用问题)
        // 注意：XML 里已经设了 ?attr/colorOnSurface，这里其实不需要动文字颜色，除非你想高亮
        // setTextColor(R.id.tv_title, Color.BLACK) // 删掉或注释这行，让 XML 主题生效

        // 3. 设置左侧指示条的随机颜色
        // 使用 getView<View> 获取 View 实例
        configure(R.id.v_indicator) {
            // 为了保持滚动时颜色一致，建议使用 hash code 作为 seed (如果 M3ColorGenerator 支持)
            // 或者简单地每次随机 (演示效果好)
            setBackgroundColor(M3ColorGenerator.randomColor())
        }

    }
}