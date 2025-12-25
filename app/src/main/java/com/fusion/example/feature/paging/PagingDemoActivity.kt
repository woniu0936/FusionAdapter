package com.fusion.example.feature.paging

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.setup
import com.fusion.adapter.paging.setupFusionPaging
import com.fusion.example.databinding.ActivityRecyclerBinding
import com.fusion.example.databinding.ItemImageBinding
import com.fusion.example.databinding.ItemMsgImageBinding
import com.fusion.example.databinding.ItemMsgSystemBinding
import com.fusion.example.databinding.ItemMsgTextBinding
import com.fusion.example.model.FusionMessage
import com.fusion.example.model.ImageItem
import com.fusion.example.utils.ChatStyleHelper
import com.fusion.example.utils.fullStatusBar
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

        // 使用 setupFusionPaging DSL
        val pagingAdapter = binding.recyclerView.setupFusionPaging<FusionMessage> {

            // register -> setup
            setup<FusionMessage> {
                // stableId -> uniqueKey
                uniqueKey { it.id }
                // match -> viewType
                viewTypeKey { it.msgType }

                // map -> dispatch
                dispatch(FusionMessage.TYPE_TEXT, ItemMsgTextBinding::inflate) {
                    onBind { item ->
                        tvContent.text = item.content
                        ChatStyleHelper.bindTextMsg(this, item.isMe)
                    }
                    onClick(100) { item -> toast("Text: ${item.id}") }
                }

                dispatch(FusionMessage.TYPE_IMAGE, ItemMsgImageBinding::inflate) {
                    onBind { item ->
                        ivImage.contentDescription = item.content
                        ChatStyleHelper.bindImageMsg(this, item.isMe)
                    }
                    onClick(1000) { item -> toast("Image: ${item.id}") }
                }

                dispatch(FusionMessage.TYPE_SYSTEM, ItemMsgSystemBinding::inflate) {
                    onBind { item -> tvSystemMsg.text = item.content }
                }
            }

            setup<ImageItem, ItemImageBinding>(ItemImageBinding::inflate) {
                uniqueKey { it.id }
                onBind { item ->
                    ChatStyleHelper.bindStandaloneImage(this)
                    tvDesc.text = "Extra Type: ${item.id}"
                }
            }
        }

        binding.recyclerView.adapter = pagingAdapter.withLoadStateFooter(
            footer = SimpleLoadStateAdapter {
                pagingAdapter.retry()
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            viewModel.pagingFlow.collectLatest { pagingData ->
                pagingAdapter.submitData(pagingData)
            }
        }

        lifecycleScope.launch {
            pagingAdapter.loadStateFlow.collectLatest { loadStates ->
                val isRefreshing = loadStates.refresh is LoadState.Loading
                val isAppending = loadStates.append is LoadState.Loading

                title = when {
                    isRefreshing -> "Fusion Paging (Refreshing...)"
                    isAppending -> "Fusion Paging (Loading more...)"
                    else -> "Fusion Paging (Idle)"
                }
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
