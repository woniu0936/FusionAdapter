package com.fusion.example.delegate

import com.fusion.adapter.delegate.LayoutDelegate
import com.fusion.adapter.delegate.LayoutHolder
import com.fusion.adapter.extensions.setText
import com.fusion.example.R
import com.fusion.example.core.model.SectionHeader

class SimpleLayoutDelegate : LayoutDelegate<SectionHeader>(R.layout.item_simple_layout) {
    init { setUniqueKey { it.title } }
    override fun LayoutHolder.onBind(item: SectionHeader) {
        setText(R.id.tv_title, item.title)
        setText(R.id.tv_subtitle, "Classic Layout Delegate")
    }
}