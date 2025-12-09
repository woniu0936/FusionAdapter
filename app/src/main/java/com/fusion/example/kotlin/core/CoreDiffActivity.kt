package com.fusion.example.kotlin.core

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.FusionListAdapter
import com.fusion.adapter.register
import com.fusion.example.databinding.ActivityRecyclerBinding
import com.fusion.example.kotlin.delegate.CoreImageDelegate
import com.fusion.example.kotlin.delegate.CoreTextDelegate
import com.fusion.example.kotlin.fullStatusBar
import com.fusion.example.model.ImageItem
import com.fusion.example.model.TextItem
import com.fusion.example.utils.MockDataGenerator

class CoreDiffActivity : AppCompatActivity() {
    // 使用 FusionListAdapter (自动挡)
    private val adapter = FusionListAdapter()
    private lateinit var binding: ActivityRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)

        // 1. 注册 (Core 模式)
        // 依然复用上面的 CoreTextDelegate
        adapter.attachDelegate(TextItem::class.java, CoreTextDelegate())
        adapter.attachDelegate(ImageItem::class.java, CoreImageDelegate())

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // 2. 初始数据
        val initData = MockDataGenerator.createMixedList(50)
        adapter.submitList(initData)

        // 3. 模拟数据变更 (Diff 演示)
        binding.fabAdd.setOnClickListener {
            // 获取当前数据作为基础，防止 ID 冲突
            val currentSize = adapter.currentList.size

            // 模拟下拉刷新或加载更多：生成新的随机列表
            // 这里我们追加 5 条数据，看看 Diff 的插入动画
            val newData = MockDataGenerator.createMixedList(5, startIndex = currentSize)

            // 因为 AsyncListDiffer 是不可变的，我们需要创建一个新集合并合并
            val totalList = ArrayList(adapter.currentList)
            totalList.addAll(newData)

            adapter.submitList(totalList)

            // 滚动到底部查看效果
            binding.recyclerView.smoothScrollToPosition(adapter.itemCount)
        }
    }
}