package com.fusion.example.feature.lab

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.FusionAdapter
import com.fusion.adapter.delegate.BindingDelegate
import com.fusion.adapter.internal.registry.TypeDispatcher
import com.fusion.adapter.placeholder.showPlaceholders
import com.fusion.example.R
import com.fusion.example.core.model.ChatMessage
import com.fusion.example.core.model.SectionHeader
import com.fusion.example.core.repo.MockSource
import com.fusion.example.databinding.ActivityBaseFixedBinding
import com.fusion.example.databinding.ItemLabRecordBinding
import com.fusion.example.delegate.ImageMsgDelegate
import com.fusion.example.delegate.SystemMsgDelegate
import com.fusion.example.delegate.TextMsgDelegate
import com.fusion.example.utils.fullStatusBar
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

        lifecycleScope.launch {
            adapter.showPlaceholders(10)
            delay(1000)

            val items = mutableListOf<Any>()
            items.add(SectionHeader("Registry Source: Native API"))
            items.addAll(MockSource.getChat())
            adapter.setItems(items)
        }
    }

    private fun setupAdapter() {
        adapter.register(SectionHeader::class.java, object : BindingDelegate<SectionHeader, ItemLabRecordBinding>(ItemLabRecordBinding::inflate) {

            override fun onBind(binding: ItemLabRecordBinding, item: SectionHeader, position: Int) {
                binding.tvTitle.text = item.title
                binding.tvId.text = "CLASSIC_BINDING"
                binding.vStatusIndicatorBox.setCardBackgroundColor(android.graphics.Color.DKGRAY)
            }

            override fun getUniqueKey(item: SectionHeader): Any {
                return item.hashCode()
            }
        })

        val dispatcher = TypeDispatcher.Builder<ChatMessage>()
            .uniqueKey { it.id }
            .viewType { it.type }
            .delegate(1, TextMsgDelegate())
            .delegate(2, ImageMsgDelegate())
            .delegate(3, SystemMsgDelegate())
            .build()
        adapter.registerDispatcher(ChatMessage::class.java, dispatcher)

        adapter.registerPlaceholder(R.layout.item_lab_record)
    }
}
