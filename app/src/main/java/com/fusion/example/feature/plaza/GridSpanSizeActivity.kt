package com.fusion.example.feature.plaza

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.fusion.adapter.placeholder.showPlaceholders
import com.fusion.adapter.setup
import com.fusion.adapter.setupFusion
import com.fusion.example.core.model.Product
import com.fusion.example.core.model.SectionHeader
import com.fusion.example.databinding.*
import com.fusion.example.utils.fullStatusBar
import com.fusion.example.utils.loadUrl
import kotlinx.coroutines.launch

class GridSpanSizeActivity : AppCompatActivity() {
    private val viewModel: PlazaViewModel by viewModels()
    private lateinit var binding: ActivityBaseFixedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseFixedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)
        
        binding.toolbar.title = "Market Plaza"

        val adapter = binding.recyclerView.setupFusion(GridLayoutManager(this, 2)) {
            setup<SectionHeader, ItemHeaderBinding>(ItemHeaderBinding::inflate) {
                uniqueKey { it.title }
                onBind { item -> tvTitle.text = item.title }
                spanSize { _, _ -> 2 } 
            }
            setup<Product, ItemMarketProductBinding>(ItemMarketProductBinding::inflate) {
                uniqueKey { it.id }
                onBind { item ->
                    tvName.text = item.name
                    tvPrice.text = item.price
                    ivCover.loadUrl(item.cover)
                    hotTag.visibility = if (item.isHot) View.VISIBLE else View.GONE
                }
                spanSize { item, _ -> if (item.isHot) 2 else 1 }
            }

            // [API] 使用专属骨架屏
            registerPlaceholder(ItemMarketPlaceholderBinding::inflate) { onBind { _: Any -> } }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is PlazaState.Loading -> adapter.showPlaceholders(6)
                        is PlazaState.Success -> {
                            adapter.submitList(state.items)
                            binding.recyclerView.post { binding.recyclerView.requestLayout() }
                        }
                    }
                }
            }
        }
        viewModel.loadMarketData()
    }
}
