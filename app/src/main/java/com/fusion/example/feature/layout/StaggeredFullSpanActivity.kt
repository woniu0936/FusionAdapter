package com.fusion.example.feature.layout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fusion.adapter.register
import com.fusion.adapter.setupFusion
import com.fusion.example.databinding.ActivityRecyclerBinding
import com.fusion.example.databinding.ItemImageBinding
import com.fusion.example.databinding.ItemTextBinding
import com.fusion.example.utils.ChatStyleHelper
import com.fusion.example.utils.fullStatusBar
import kotlin.random.Random

class StaggeredFullSpanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)

        // 1. 初始化 Fusion，传入 StaggeredGridLayoutManager
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE // 防止跳动

        val adapter = binding.recyclerView.setupFusion(layoutManager) {

            // 类型 A: 标题 (Header) -> 强制全宽
            register<HeaderItem, ItemTextBinding>(ItemTextBinding::inflate) {
                onBind { item ->
                    tvContent.text = "--- ${item.title} ---"
                    tvContent.textSize = 18f
                    cardRoot.setCardBackgroundColor(0xFFF0F0F0.toInt())
                }

                // [核心能力] 瀑布流全宽
                // 只要满足条件，FusionAdapter 会自动设置 StaggeredGridLayoutParams.isFullSpan = true
                fullSpanIf { true }
            }

            // 类型 B: 图片内容 (Content) -> 瀑布流
            register<StaggeredItem, ItemImageBinding>(ItemImageBinding::inflate) {
                onBind { item ->
                    ChatStyleHelper.bindStandaloneImage(this)
                    tvDesc.text = "H:${item.height}px"
                    // 模拟高度变化，展示瀑布流效果
                    root.layoutParams.height = item.height
                }
                // 默认 span 为 1，无需配置
            }
        }

        // 2. 生成分组数据
        val items = ArrayList<Any>()
        for (i in 1..6) {
            items.add(HeaderItem("Group $i"))
            // 每个组添加一些随机高度的 Item
            repeat(10) {
                items.add(StaggeredItem(height = Random.nextInt(200, 600)))
            }
        }

        adapter.submitList(items)
    }

    data class HeaderItem(val title: String)
    data class StaggeredItem(val height: Int)
}
