package com.fusion.example.feature.classic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.FusionAdapter
import com.fusion.adapter.internal.TypeRouter
import com.fusion.adapter.register
import com.fusion.example.R
import com.fusion.example.databinding.ActivityRecyclerBinding
import com.fusion.example.delegate.ImageMsgDelegate
import com.fusion.example.delegate.SimpleLayoutDelegate
import com.fusion.example.delegate.SystemMsgDelegate
import com.fusion.example.delegate.TextMsgDelegate
import com.fusion.example.model.FusionMessage
import com.fusion.example.model.SimpleItem
import com.fusion.example.utils.MockDataGenerator
import com.fusion.example.utils.fullStatusBar

/**
 * 经典模式展示 (普通函数风格)
 * 
 * 本页面展示 FusionAdapter 的底层原始 API 用法：
 * 1. 显式实例化 FusionAdapter。
 * 2. 使用 registerDelegate 注册一对一映射。
 * 3. 使用 registerRouter 注册多类型路由（展示 Router 的手动构建与注册）。
 * 4. 显式设置 RecyclerView 的 LayoutManager 和 Adapter。
 */
class ClassicManualActivity : AppCompatActivity() {

    // 显式创建适配器实例 (手动管理数据与刷新)
    private val adapter = FusionAdapter()
    private lateinit var binding: ActivityRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)

        // 1. [显式配置] 传统的 RecyclerView 初始化方式
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // 2. [显式注册] 展示 registerDelegate 和 registerRouter 的底层调用
        setupRegistration()

        // 3. [数据操作] 手动设置初始列表
        val initialItems = ArrayList<Any>()
        initialItems.add(SimpleItem("header", "手动注册示例：registerDelegate + registerRouter"))
        initialItems.addAll(MockDataGenerator.createChatList(60))
        adapter.setItems(initialItems)

        setupInteractions()
    }

    private fun setupRegistration() {
        // --- 场景 1: 一对一注册 (Simple Registration) ---
        // 直接调用 registerDelegate 函数
        adapter.registerDelegate(SimpleItem::class.java, SimpleLayoutDelegate())

        // --- 场景 2: 多类型路由 (Router Registration) ---
        // 展示如何使用 TypeRouter.Builder 手动构建并注册 Router (非 DSL 方式)
        // 这种方式逻辑清晰，适合 Java 用户或不喜欢 DSL 的场景
        val messageRouter = TypeRouter.Builder<FusionMessage>()
            .stableId { it.id }
            .match { it.msgType }
            .map(FusionMessage.TYPE_TEXT, TextMsgDelegate())
            .map(FusionMessage.TYPE_IMAGE, ImageMsgDelegate())
            .map(FusionMessage.TYPE_SYSTEM, SystemMsgDelegate())
            .build()
        
        // 显式调用注册 Router
        adapter.registerRouter(FusionMessage::class.java, messageRouter)

        // --- 场景 3: 占位符注册 (Placeholder) ---
        // 使用普通函数注册布局资源作为占位符
        adapter.registerSkeleton(R.layout.item_simple_layout)
    }

    private fun setupInteractions() {
        binding.fabAdd.setOnClickListener {
            val newItem = SimpleItem("new_${System.currentTimeMillis()}", "手动插入的数据")
            // 直接操作 Adapter 内部列表并触发 notify
            adapter.insertItem(adapter.itemCount, newItem)
            binding.recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
        }
    }
}