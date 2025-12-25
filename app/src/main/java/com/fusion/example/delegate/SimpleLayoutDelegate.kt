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
        // 核心：设置业务唯一键
        setUniqueKey { it.id }

        // 局部刷新逻辑
        onPayload(SimpleItem::title) { title ->
            setText(R.id.tv_title, title)
        }

        setOnItemClick { _, item, _ ->
            // 点击事件逻辑保持
        }
    }

    override fun LayoutHolder.onBind(item: SimpleItem) {
        // 1. 设置标题
        setText(R.id.tv_title, item.title)

        // 2. 设置指示条随机颜色
        configure(R.id.v_indicator) {
            setBackgroundColor(M3ColorGenerator.randomColor())
        }
    }
}