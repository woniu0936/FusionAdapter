package com.fusion.example.paging

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.paging.ktx.registerRoute
import com.fusion.adapter.paging.ktx.setupFusionPaging
import com.fusion.example.databinding.ActivityRecyclerBinding
import com.fusion.example.databinding.ItemMsgImageBinding
import com.fusion.example.databinding.ItemMsgSystemBinding
import com.fusion.example.databinding.ItemMsgTextBinding
import com.fusion.example.kotlin.fullStatusBar
import com.fusion.example.model.FusionMessage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PagingDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecyclerBinding
    private val viewModel by viewModels<PagingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)
        binding.fabAdd.visibility = View.GONE

        // =================================================================
        // 1. 初始化 FusionPagingAdapter
        // =================================================================
        // 使用 setupFusionPaging DSL，体验与普通列表完全一致
        val pagingAdapter = binding.recyclerView.setupFusionPaging<FusionMessage> {

            // --- 注册多类型路由 (复用之前的逻辑) ---
            registerRoute<FusionMessage> {
                match { it.msgType }

                // 文本消息
                map(FusionMessage.TYPE_TEXT, ItemMsgTextBinding::inflate) {
                    onBind { item -> tvContent.text = item.content }
                    onClick { item -> toast("Text: ${item.id}") }
                }

                // 图片消息
                map(FusionMessage.TYPE_IMAGE, ItemMsgImageBinding::inflate) {
                    onBind { item -> tvDesc.text = item.content }
                    onClick { item -> toast("Image: ${item.id}") }
                }

                // 系统消息
                map(FusionMessage.TYPE_SYSTEM, ItemMsgSystemBinding::inflate) {
                    onBind { item -> tvSystemMsg.text = item.content }
                }
            }
        }

        binding.recyclerView.adapter = pagingAdapter
//        binding.recyclerView.adapter = pagingAdapter.withLoadStateFooter(
//            footer = SimpleLoadStateAdapter {
//                // 这里的 retry 会触发 PagingAdapter 的重试逻辑
//                pagingAdapter.retry()
//            }
//        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // =================================================================
        // 2. 绑定数据流
        // =================================================================
        lifecycleScope.launch {
            viewModel.pagingFlow.collectLatest { pagingData ->
                // 提交 PagingData
                pagingAdapter.submitData(pagingData)
            }
        }

        // =================================================================
        // 3. 处理加载状态 (Paging 3 标准操作)
        // =================================================================
        // 这里我们简单演示如何监听状态，也可以使用 withLoadStateFooter 添加底部 Loading 条
        lifecycleScope.launch {
            pagingAdapter.loadStateFlow.collectLatest { loadStates ->
                val isRefreshing = loadStates.refresh is LoadState.Loading
                val isAppending = loadStates.append is LoadState.Loading

                // 简单的 Title 提示状态
                title = when {
                    isRefreshing -> "Fusion Paging (Refreshing...)"
                    isAppending -> "Fusion Paging (Loading more...)"
                    else -> "Fusion Paging (Idle)"
                }
            }
        }

        // 下拉刷新
        // (假设你的布局里套了 SwipeRefreshLayout，这里只是示意)
        // binding.swipeRefresh.setOnRefreshListener { pagingAdapter.refresh() }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}