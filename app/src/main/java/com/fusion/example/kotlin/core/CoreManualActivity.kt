package com.fusion.example.kotlin.core

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.FusionAdapter
import com.fusion.example.databinding.ActivityRecyclerBinding
import com.fusion.example.kotlin.delegate.CoreImageDelegate
import com.fusion.example.kotlin.delegate.CoreTextDelegate
import com.fusion.example.kotlin.fullStatusBar
import com.fusion.example.model.ImageItem
import com.fusion.example.model.TextItem
import com.fusion.example.utils.MockDataGenerator

class CoreManualActivity : AppCompatActivity() {
    // 使用 FusionAdapter (手动挡)
    private val adapter = FusionAdapter()
    private lateinit var binding: ActivityRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)

        // 1. 注册 (Core 模式需要实例化 Delegate 类)
        // 注意：这里调用的是 FusionAdapter 的 register
        adapter.register(TextItem::class.java, CoreTextDelegate())
        adapter.register(ImageItem::class.java, CoreImageDelegate())

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // 2. 设置数据 (无 Diff，直接替换)
        val initData = MockDataGenerator.createMixedList(50)
        adapter.setItems(initData)

        // 3. 模拟插入 (手动通知)
        binding.fabAdd.setOnClickListener {
            val newItem = TextItem("${System.currentTimeMillis()}", "新增 Item")
            // FusionAdapter 的 addItems 内部封装了 notifyItemRangeInserted
            adapter.addItems(listOf(newItem))
            binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
        }
    }
}