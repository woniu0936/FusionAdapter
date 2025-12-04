package com.fusion.adapter.example.core

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.FusionListAdapter
import com.fusion.adapter.core.FusionLinker
import com.fusion.adapter.example.databinding.ActivityRecyclerBinding
import com.fusion.adapter.example.delegate.ImageMsgDelegate
import com.fusion.adapter.example.delegate.SystemMsgDelegate
import com.fusion.adapter.example.delegate.TextMsgDelegate
import com.fusion.adapter.example.fullStatusBar
import com.fusion.adapter.example.model.FusionMessage
import com.fusion.adapter.example.utils.MockDataGenerator

class CoreRouterActivity : AppCompatActivity() {

    private val adapter = FusionListAdapter()
    private lateinit var binding: ActivityRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)
        binding.fabAdd.visibility = View.GONE

        val fusionLinker = FusionLinker<FusionMessage>()
            .match { it.msgType }
            .map(FusionMessage.TYPE_TEXT, TextMsgDelegate())
            .map(FusionMessage.TYPE_IMAGE, ImageMsgDelegate())
            .map(FusionMessage.TYPE_SYSTEM, SystemMsgDelegate())
        adapter.registerLinker(FusionMessage::class.java, fusionLinker)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        val list = MockDataGenerator.createChatList(50)
        adapter.submitList(list)
    }

}