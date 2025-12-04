package com.fusion.example.kotlin.ktx

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.FusionAdapter
import com.fusion.example.kotlin.fullStatusBar
import com.fusion.example.model.ImageItem
import com.fusion.example.model.TextItem
import com.fusion.example.utils.MockDataGenerator
import com.fusion.adapter.ktx.autoScrollToBottom
import com.fusion.adapter.ktx.register
import com.fusion.example.databinding.ActivityRecyclerBinding
import com.fusion.example.databinding.ItemImageBinding
import com.fusion.example.databinding.ItemTextBinding

class KtxManualActivity : AppCompatActivity() {
    // 依然使用手动挡 Adapter
    private val adapter = FusionAdapter()
    private lateinit var binding: ActivityRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)

        // 1. 注册 (KTX DSL 模式)
        // 使用 fusionDelegate 快速创建，无需新建文件
        adapter.register<TextItem, ItemTextBinding>(ItemTextBinding::inflate) {
            onBind { item ->
                tvContent.text = "[KTX Manual] ${item.content}"
            }
            onClick { item ->
                Toast.makeText(this@KtxManualActivity, "Clicked: ${item.id}", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. 注册另一种类型
        adapter.register<ImageItem, ItemImageBinding>(ItemImageBinding::inflate) {
            onBind { item ->
                // binding.ivImage.load(item.url)
            }
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // 3. 数据操作 (手动控制)
        val initData = MockDataGenerator.createMixedList(50)
        adapter.setItems(initData)

        // 一句话开启自动滚动
        binding.recyclerView.autoScrollToBottom(adapter)
        binding.fabAdd.setOnClickListener {
            // KTX 也可以给手动挡 Adapter 扩展类似 += 的操作符 (需自行在 KTX 定义或直接调用 addItems)
            adapter.addItems(listOf(TextItem("3", "New DSL Item")))
        }
    }
}