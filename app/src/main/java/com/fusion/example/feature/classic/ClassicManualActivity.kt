package com.fusion.example.feature.classic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.FusionAdapter
import com.fusion.adapter.internal.TypeRouter
import com.fusion.example.databinding.ActivityRecyclerBinding
import com.fusion.example.delegate.SimpleLayoutDelegate
import com.fusion.example.delegate.ImageMsgDelegate
import com.fusion.example.delegate.SystemMsgDelegate
import com.fusion.example.delegate.TextMsgDelegate
import com.fusion.example.model.FusionMessage
import com.fusion.example.model.SimpleItem
import com.fusion.example.utils.MockDataGenerator
import com.fusion.example.utils.fullStatusBar

/**
 * 经典模式展示
 * 1. 使用 FusionAdapter (基类)，而非 FusionListAdapter (DiffUtil)
 * 2. 显式调用 attachDelegate 和 attachLinker
 * 3. 手动操作数据 (setItems, addItems, removeItem)
 * 4. 混合使用 LayoutDelegate (无 ViewBinding) 和 BindingDelegate
 */
class ClassicManualActivity : AppCompatActivity() {

    // [注意] 这里使用的是 FusionAdapter，它没有 AsyncListDiffer，数据由用户自己管理
    private val adapter = FusionAdapter()
    private lateinit var binding: ActivityRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)

        setupRegistration()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // [手动管理数据] 初始化
        val initialItems = ArrayList<Any>()
        initialItems.add(SimpleItem("header", "我是通过 attachDelegate 注册的 LayoutDelegate"))
        initialItems.addAll(MockDataGenerator.createChatList(60)) // 包含 FusionMessage
        adapter.setItems(initialItems)

        setupInteractions()
    }

    private fun setupRegistration() {
        // 1. [一对一] 显式注册 LayoutDelegate (无 ViewBinding)
        adapter.attachDelegate(SimpleItem::class.java, SimpleLayoutDelegate())

        // 2. [一对多] 显式注册 Linker (Router)
        // 这种写法适合不喜欢 DSL 或者需要动态构建 Router 的场景
        val messageRouter = TypeRouter<FusionMessage>()
            .match { it.msgType } // 定义分发规则
            .map(FusionMessage.TYPE_TEXT, TextMsgDelegate()) // 复用已有的 Delegate
            .map(FusionMessage.TYPE_IMAGE, ImageMsgDelegate()) // 复用已有的 Delegate
            .map(FusionMessage.TYPE_SYSTEM, SystemMsgDelegate()) // 复用已有的 Delegate
        // 为了演示简单，这里只注册了 Text 类型，其他类型会走全局 Fallback (如果有) 或者报错
        // 在实际项目中应该 map 完所有类型

        adapter.attachLinker(FusionMessage::class.java, messageRouter)
    }

    private fun setupInteractions() {
        binding.fabAdd.setOnClickListener {
            // [手动管理数据] 插入
            val newItem = SimpleItem("new_${System.currentTimeMillis()}", "手动插入的新数据")
            // FusionAdapter 特有的 API，直接操作内部列表并 notify
            adapter.insertItem(adapter.itemCount, newItem)
            binding.recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
        }
    }
}