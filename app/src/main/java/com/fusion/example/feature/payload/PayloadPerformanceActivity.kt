package com.fusion.example.feature.payload

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.FusionListAdapter
import com.fusion.adapter.register
import com.fusion.example.databinding.ActivityRecyclerBinding
import com.fusion.example.databinding.ItemImageBinding
import com.fusion.example.delegate.SocialPostDelegate
import com.fusion.example.model.ImageItem
import com.fusion.example.model.SocialPost
import com.fusion.example.utils.ChatStyleHelper
import com.fusion.example.utils.MockDataGenerator
import com.fusion.example.utils.fullStatusBar

class PayloadPerformanceActivity : AppCompatActivity() {
    private val adapter = FusionListAdapter()
    private lateinit var binding: ActivityRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)
        binding.fabAdd.visibility = View.GONE

        // 1. 注册核心 Delegate (Class 方式)
        adapter.registerDelegate(SocialPost::class.java, SocialPostDelegate { post ->
            toggleLike(post)
        })

        // 2. 混入另一种类型 (DSL 方式)，证明兼容性
        adapter.register<ImageItem, ItemImageBinding>(ItemImageBinding::inflate) {
            stableId { it.id }
            onBind { item ->
                ChatStyleHelper.bindStandaloneImage(this)
                tvDesc.text = "Image ID: ${item.id}"
                ivImage.layoutParams.height = 400
            }
        }

        binding.recyclerView.itemAnimator = null // 关闭 RecyclerView 默认动画以展示 Payload 动画
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // 生成混合数据：每隔 5 个帖子插入一个分割线
        val posts = MockDataGenerator.createSocialPosts(60)
        adapter.submitList(posts)
    }

    private fun toggleLike(post: SocialPost) {
        val currentList = ArrayList(adapter.currentList)
        val index = currentList.indexOfFirst { (it as? SocialPost)?.id == post.id }
        if (index != -1) {
            val newIsLiked = !post.isLiked
            val newCount = if (newIsLiked) post.likeCount + 1 else post.likeCount - 1
            // 只修改数据，FusionAdapter 自动触发 bindPayload
            currentList[index] = post.copy(isLiked = newIsLiked, likeCount = newCount)
            adapter.submitList(currentList)
        }
    }
}
