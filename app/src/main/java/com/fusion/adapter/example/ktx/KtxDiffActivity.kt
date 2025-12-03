package com.fusion.adapter.example.ktx

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.example.databinding.ActivityRecyclerBinding
import com.fusion.adapter.example.databinding.ItemImageBinding
import com.fusion.adapter.example.databinding.ItemTextBinding
import com.fusion.adapter.example.model.ImageItem
import com.fusion.adapter.example.model.TextItem
import com.fusion.adapter.example.utils.MockDataGenerator
import com.fusion.adapter.ktx.autoScrollToBottom
import com.fusion.adapter.ktx.fusionDelegate
import com.fusion.adapter.ktx.plusAssign
import com.fusion.adapter.ktx.setupFusion

class KtxDiffActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. 终极初始化 (setupFusion)
        // 这里返回的是 FusionListAdapter
        val adapter = binding.recyclerView.setupFusion {

            // 使用 DSL 注册 TextItem
            register(fusionDelegate<TextItem, ItemTextBinding>(ItemTextBinding::inflate) {
                onBind { item ->
                    tvContent.text = "[KTX Diff] ${item.content}"
                }
            })

            // 使用 DSL 注册 ImageItem
            register(fusionDelegate<ImageItem, ItemImageBinding>(ItemImageBinding::inflate) {
                onBind { item ->
                    tvDesc.text = "Image ID: ${item.id}"
                }
            })
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