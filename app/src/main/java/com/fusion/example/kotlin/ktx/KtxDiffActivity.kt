package com.fusion.example.kotlin.ktx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.autoScrollToBottom
import com.fusion.adapter.plusAssign
import com.fusion.adapter.register
import com.fusion.adapter.setupFusion
import com.fusion.example.databinding.ActivityRecyclerBinding
import com.fusion.example.databinding.ItemImageBinding
import com.fusion.example.databinding.ItemTextBinding
import com.fusion.example.kotlin.fullStatusBar
import com.fusion.example.model.ImageItem
import com.fusion.example.model.TextItem
import com.fusion.example.utils.MockDataGenerator

class KtxDiffActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)

        // 1. 终极初始化 (setupFusion)
        // 这里返回的是 FusionListAdapter
        val adapter = binding.recyclerView.setupFusion {

            // 使用 DSL 注册 TextItem
            register<TextItem, ItemTextBinding>(ItemTextBinding::inflate) {
                onBind { item ->
                    tvContent.text = "[KTX Diff] ${item.content}"
                }
            }

            // 使用 DSL 注册 ImageItem
            register<ImageItem, ItemImageBinding>(ItemImageBinding::inflate) {
                onBind { item ->
                    tvDesc.text = "Image ID: ${item.id}"
                }
            }
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        // 2. 初始数据
        val initData = MockDataGenerator.createMixedList(50)
        adapter.submitList(initData)

        // 一句话开启自动滚动
        binding.recyclerView.autoScrollToBottom(adapter)
        // 3. 极简操作
        binding.fabAdd.setOnClickListener {
            // 使用 += 操作符 (KTX 特性)
            // 这会自动创建新列表并 submitList
            adapter += TextItem("${System.currentTimeMillis()}", "Item via +=")
            adapter += ImageItem("img_${System.currentTimeMillis()}", "http://...")
        }
    }
}