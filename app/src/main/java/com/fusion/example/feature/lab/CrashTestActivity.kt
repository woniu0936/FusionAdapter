package com.fusion.example.feature.lab

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.Fusion
import com.fusion.adapter.FusionConfig
import com.fusion.adapter.FusionListAdapter
import com.fusion.adapter.placeholder.showPlaceholders
import com.fusion.adapter.setup
import com.fusion.example.core.model.SectionHeader
import com.fusion.example.databinding.ActivityCrashTestM3Binding
import com.fusion.example.databinding.ItemLabPlaceholderBinding
import com.fusion.example.databinding.ItemLabRecordBinding
import com.fusion.example.utils.fullStatusBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CrashTestActivity : AppCompatActivity() {

    data class Unregistered(val data: String)

    private lateinit var binding: ActivityCrashTestM3Binding

    // 延迟初始化 Adapter，确保它能拿到 onCreate 中通过 Fusion.initialize 设置的最新的 debug 配置
    private lateinit var adapter: FusionListAdapter
    private val items = mutableListOf<Any>()
    private var originalConfig: FusionConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 在创建任何 Adapter 之前，先强制更新全局配置为 debug = true
        originalConfig = Fusion.getConfig()

        binding = ActivityCrashTestM3Binding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)

        binding.toolbar.title = "Sanitization Lab"

        // 2. 现在初始化 Adapter，它内部的 FusionCore 会读取到最新的 debug=true 状态
        adapter = FusionListAdapter()

        setupAdapter()
        setupLogic()
        loadData()
    }

    private fun setupAdapter() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        adapter.setup<SectionHeader, ItemLabRecordBinding>(ItemLabRecordBinding::inflate) {
            uniqueKey { it.title }
            onBind { item ->
                tvTitle.text = item.title
                vStatusIndicatorBox.setCardBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
            }
        }

        adapter.registerPlaceholder(ItemLabPlaceholderBinding::inflate) { onBind { _: Any -> } }
    }

    private fun loadData() {
        lifecycleScope.launch {
            adapter.showPlaceholders(8)
            delay(1000)

            items.clear()
            items.add(SectionHeader("Registry Security: Verified"))
            repeat(30) {
                items.add(SectionHeader("Encrypted Audit Log #$it"))
            }
            adapter.submitList(ArrayList(items))
        }
    }

    private fun setupLogic() {
        // 无保护触发 (会崩溃，因为 debug=true)
        binding.btnCrash.setOnClickListener {
            val list = ArrayList(items)
            list.add(1, Unregistered("BOOM"))
            adapter.submitList(list)
        }

        // 有保护触发 (捕获异常)
        binding.btnCatchCrash.setOnClickListener {
            val list = ArrayList(items)
            // 修复：确保索引安全。如果 items 为空（加载中），则添加到末尾；否则添加到索引 1。
            val targetIndex = if (list.size > 1) 1 else list.size
            list.add(targetIndex, Unregistered("EXTERNAL_DATA"))
            try {
                // setItems 会立即执行 core.filter()，在 debug=true 时抛出异常
                adapter.setItems(list)
            } catch (e: Exception) {
                // 捕获任意异常以防万一，但重点是 UnregisteredTypeException
                Toast.makeText(this, "Safe Caught: ${e.javaClass.simpleName}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 恢复原始配置，避免污染其他页面
        originalConfig?.let { Fusion.initialize(it) }
    }
}
