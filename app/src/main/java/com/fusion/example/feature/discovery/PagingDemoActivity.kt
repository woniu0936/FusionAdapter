package com.fusion.example.feature.discovery

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.paging.setupFusionPaging
import com.fusion.adapter.paging.showPlaceholders
import com.fusion.adapter.register
import com.fusion.example.databinding.*
import com.fusion.example.utils.fullStatusBar
import com.fusion.example.utils.loadUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PagingDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBaseFixedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseFixedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)
        
        binding.toolbar.title = "Discovery Feed"

        val pagingAdapter = binding.recyclerView.setupFusionPaging<Any> {
            register<DiscoveryItem.Article, ItemDiscoveryCardBinding>(ItemDiscoveryCardBinding::inflate) {
                stableId { it.id }
                onBind { item ->
                    tvTitle.text = item.title
                    tvDesc.text = item.desc
                    ivCover.loadUrl("https://picsum.photos/seed/${item.id}/200/200")
                }
            }
            register<DiscoveryItem.Featured, ItemMomentCardBinding>(ItemMomentCardBinding::inflate) {
                stableId { it.id }
                onBind { item ->
                    tvName.text = "Featured"
                    tvContent.text = item.text
                    imageContainer.visibility = View.VISIBLE
                    ivImage.loadUrl(item.imageUrl)
                }
            }

            // [API] 使用专属骨架屏
            registerPlaceholder(ItemDiscoveryPlaceholderBinding::inflate) { onBind { _: Any -> } }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        pagingAdapter.showPlaceholders(this.lifecycle, 10)

        val flow = Pager(PagingConfig(15)) { DiscoverySource() }.flow
        lifecycleScope.launch { flow.collectLatest { @Suppress("UNCHECKED_CAST") pagingAdapter.submitData(it as PagingData<Any>) } }
    }

    sealed class DiscoveryItem {
        data class Article(val id: String, val title: String, val desc: String) : DiscoveryItem()
        data class Featured(val id: String, val text: String, val imageUrl: String) : DiscoveryItem()
    }

    class DiscoverySource : PagingSource<Int, DiscoveryItem>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DiscoveryItem> {
            val p = params.key ?: 0
            delay(1200)
            val data = List(15) { i ->
                if (i % 5 == 0) DiscoveryItem.Featured("f_${p}_$i", "M3 Design Trends.", "https://picsum.photos/seed/$i/800/400")
                else DiscoveryItem.Article("a_${p}_$i", "Insight #$i", "FusionAdapter paging showcase.")
            }
            return LoadResult.Page(data, if (p == 0) null else p - 1, p + 1)
        }
        override fun getRefreshKey(state: PagingState<Int, DiscoveryItem>) = null
    }
}