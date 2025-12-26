package com.fusion.example.feature.lab

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.FusionAdapter
import com.fusion.adapter.internal.TypeDispatcher
import com.fusion.adapter.placeholder.showPlaceholders
import com.fusion.example.R
import com.fusion.example.core.model.ChatMessage
import com.fusion.example.core.model.SectionHeader
import com.fusion.example.core.repo.MockSource
import com.fusion.example.databinding.ActivityBaseFixedBinding
import com.fusion.example.databinding.ItemLabRecordBinding
import com.fusion.example.utils.fullStatusBar
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ClassicManualActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityBaseFixedBinding
    private val adapter = FusionAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseFixedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)
        
        binding.toolbar.title = "Manual Registry"

        setupAdapter()
        
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        MainScope().launch {
            adapter.showPlaceholders(10)
            delay(1000)

            val items = mutableListOf<Any>()
            items.add(SectionHeader("Registry Source: Native API"))
            items.addAll(MockSource.getChat())
            adapter.setItems(items)
        }
    }

    private fun setupAdapter() {
        // 使用实验室专用 Delegate
        adapter.register(SectionHeader::class.java, object : com.fusion.adapter.delegate.BindingDelegate<SectionHeader, ItemLabRecordBinding>(ItemLabRecordBinding::inflate) {
            init { setUniqueKey { it.title } }
            override fun onBind(binding: ItemLabRecordBinding, item: SectionHeader, position: Int) {
                binding.tvTitle.text = item.title
                binding.tvId.text = "CLASSIC_BINDING"
                binding.vStatusIndicatorBox.setCardBackgroundColor(android.graphics.Color.DKGRAY)
            }
        })

        val dispatcher = TypeDispatcher.Builder<ChatMessage>()
            .uniqueKey { it.id }
            .viewType { if (it.type == 3) 3 else if (it.isMe) 1 else 2 }
            .delegate(1, com.fusion.example.delegate.TextMsgDelegate())
            .delegate(2, com.fusion.example.delegate.TextMsgDelegate()) 
            .delegate(3, com.fusion.example.delegate.SystemMsgDelegate())
            .build()
        adapter.registerDispatcher(ChatMessage::class.java, dispatcher)

        adapter.registerPlaceholder(R.layout.item_lab_record)
    }
}