package com.fusion.example.feature.plaza

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fusion.adapter.placeholder.showPlaceholders
import com.fusion.adapter.setup
import com.fusion.adapter.setupFusion
import com.fusion.example.core.model.Product
import com.fusion.example.core.model.SectionHeader
import com.fusion.example.core.repo.MockSource
import com.fusion.example.databinding.*
import com.fusion.example.utils.fullStatusBar
import com.fusion.example.utils.loadUrl
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class StaggeredFullSpanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBaseFixedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseFixedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)
        
        binding.toolbar.title = "Staggered Plaza"

        val adapter = binding.recyclerView.setupFusion(StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)) {
            setup<SectionHeader, ItemHeaderBinding>(ItemHeaderBinding::inflate) {
                uniqueKey { it.title }
                onBind { item -> tvTitle.text = item.title }
                fullSpanIf { true }
            }
            setup<Product, ItemImageBinding>(ItemImageBinding::inflate) {
                uniqueKey { it.id }
                onBind { item ->
                    tvDesc.text = item.name
                    ivImage.loadUrl(item.cover)
                    ivImage.layoutParams.height = item.height
                }
            }

            // [API] 使用专属骨架屏
            registerPlaceholder(ItemImagePlaceholderBinding::inflate) {
                onBind { _: Any ->
                    vPlaceholder.layoutParams.height = Random.nextInt(400, 700)
                }
            }
        }

        MainScope().launch {
            adapter.showPlaceholders(10)
            delay(1200)
            adapter.submitList(MockSource.getMarket())
            binding.recyclerView.post { binding.recyclerView.requestLayout() }
        }
    }
}