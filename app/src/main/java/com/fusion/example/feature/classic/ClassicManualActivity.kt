package com.fusion.example.feature.classic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.FusionAdapter
import com.fusion.adapter.internal.TypeDispatcher
import com.fusion.example.R
import com.fusion.example.databinding.ActivityRecyclerBinding
import com.fusion.example.delegate.ImageMsgDelegate
import com.fusion.example.delegate.SimpleLayoutDelegate
import com.fusion.example.delegate.SystemMsgDelegate
import com.fusion.example.delegate.TextMsgDelegate
import com.fusion.example.model.FusionMessage
import com.fusion.example.model.SimpleItem
import com.fusion.example.utils.MockDataGenerator
import com.fusion.example.utils.fullStatusBar

class ClassicManualActivity : AppCompatActivity() {
    private val adapter = FusionAdapter()
    private lateinit var binding: ActivityRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        setupRegistration()

        val initialItems = ArrayList<Any>()
        initialItems.add(SimpleItem("header", "手动注册示例：register + registerDispatcher"))
        initialItems.addAll(MockDataGenerator.createChatList(60))
        adapter.setItems(initialItems)
    }

    private fun setupRegistration() {
        // --- 1对1 ---
        adapter.register(SimpleItem::class.java, SimpleLayoutDelegate())

        // --- 1对多 ---
        val dispatcher = TypeDispatcher.Builder<FusionMessage>()
            .uniqueKey { it.id }
            .viewType { it.msgType }
            .delegate(FusionMessage.TYPE_TEXT, TextMsgDelegate())
            .delegate(FusionMessage.TYPE_IMAGE, ImageMsgDelegate())
            .delegate(FusionMessage.TYPE_SYSTEM, SystemMsgDelegate())
            .build()
        
        adapter.registerDispatcher(FusionMessage::class.java, dispatcher)

        // --- 占位符 ---
        adapter.registerPlaceholder(R.layout.item_simple_layout)
    }
}