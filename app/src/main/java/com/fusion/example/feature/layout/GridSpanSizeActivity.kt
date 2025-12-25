package com.fusion.example.feature.layout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.fusion.adapter.setup
import com.fusion.adapter.setupFusion
import com.fusion.example.databinding.ActivityRecyclerBinding
import com.fusion.example.databinding.ItemTextBinding
import com.fusion.example.utils.fullStatusBar
import java.util.Random

class GridSpanSizeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)

        val spanCount = 3

        // 1. 初始化 Fusion，传入 GridLayoutManager
        val adapter = binding.recyclerView.setupFusion(GridLayoutManager(this, spanCount)) {
            setup<GridSpanItem, ItemTextBinding>(ItemTextBinding::inflate) {
                uniqueKey { it.id }
                onBind { item ->
                    tvContent.text = "${item.span} Span(s)"
                    // 视觉区分：不同 Span 不同颜色
                    val color = when (item.span) {
                        1 -> 0xFFE0E0E0.toInt() // 灰
                        2 -> 0xFFB3E5FC.toInt() // 蓝
                        else -> 0xFFFFCC80.toInt() // 橙
                    }
                    cardRoot.setCardBackgroundColor(color)
                }

                // [核心能力] 动态控制 SpanSize
                // 返回值为该 Item 占用的列数
                spanSize { item, position ->
                    item.span // 直接返回数据模型中定义的 span
                }
            }
        }

        // 2. 生成随机 Span 的数据
        val random = Random()
        val items = (0..60).map {
            // 随机生成 1 或 2 或 3 (全宽)
            val span = random.nextInt(3) + 1
            GridSpanItem("id_$it", span)
        }

        adapter.submitList(items)
    }

    // 简单的数据模型
    data class GridSpanItem(val id: String, val span: Int)
}