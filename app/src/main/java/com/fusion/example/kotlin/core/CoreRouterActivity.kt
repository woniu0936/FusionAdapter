package com.fusion.example.kotlin.core

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.FusionListAdapter
import com.fusion.adapter.internal.TypeRouter
import com.fusion.example.databinding.ActivityRecyclerBinding
import com.fusion.example.kotlin.delegate.ImageMsgDelegate
import com.fusion.example.kotlin.delegate.SystemMsgDelegate
import com.fusion.example.kotlin.delegate.TextMsgDelegate
import com.fusion.example.kotlin.fullStatusBar
import com.fusion.example.model.FusionMessage
import com.fusion.example.utils.MockDataGenerator

class CoreRouterActivity : AppCompatActivity() {

    private val adapter = FusionListAdapter()
    private lateinit var binding: ActivityRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)
        binding.fabAdd.visibility = View.GONE

        val typeRouter = TypeRouter<FusionMessage>()
            .match { it.msgType }
            .map(FusionMessage.Companion.TYPE_TEXT, TextMsgDelegate())
            .map(FusionMessage.Companion.TYPE_IMAGE, ImageMsgDelegate())
            .map(FusionMessage.Companion.TYPE_SYSTEM, SystemMsgDelegate())
        adapter.attachLinker(FusionMessage::class.java, typeRouter)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        val list = MockDataGenerator.createChatList(50)
        adapter.submitList(list)
    }

}