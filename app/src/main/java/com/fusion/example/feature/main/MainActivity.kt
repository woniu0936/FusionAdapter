package com.fusion.example.feature.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.setup
import com.fusion.adapter.setupFusion
import com.fusion.example.R
import com.fusion.example.databinding.ActivityBaseFixedBinding
import com.fusion.example.databinding.ItemSimpleLayoutBinding
import com.fusion.example.feature.chat.ChatActivity
import com.fusion.example.feature.moments.MomentsActivity
import com.fusion.example.feature.plaza.GridSpanSizeActivity
import com.fusion.example.feature.plaza.StaggeredFullSpanActivity
import com.fusion.example.feature.discovery.PagingDemoActivity
import com.fusion.example.feature.lab.ClassicManualActivity
import com.fusion.example.feature.lab.LabJavaActivity
import com.fusion.example.feature.lab.CrashTestActivity
import com.fusion.example.utils.fullStatusBar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBaseFixedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseFixedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)

        binding.toolbar.title = "Fusion Example"

        binding.recyclerView.setupFusion {
            setup<MenuEntry, ItemSimpleLayoutBinding>(ItemSimpleLayoutBinding::inflate) {
                uniqueKey { it.title }
                onBind { item ->
                    tvTitle.text = item.title
                    tvSubtitle.text = item.desc
                    ivIcon.setImageResource(item.icon)
                }
                onClick { item -> startActivity(Intent(this@MainActivity, item.target)) }
            }
        }.submitList(listOf(
            MenuEntry("Social Feed", "Payloads & M3 Social Cards", R.drawable.ic_moments, MomentsActivity::class.java),
            MenuEntry("Messenger", "Routing & Custom Bubbles", R.drawable.ic_chat, ChatActivity::class.java),
            MenuEntry("Market Grid", "Dynamic SpanSize & Headers", R.drawable.ic_market, GridSpanSizeActivity::class.java),
            MenuEntry("Visual Plaza", "Staggered Layout & FullSpan", R.drawable.ic_visuals, StaggeredFullSpanActivity::class.java),
            MenuEntry("Discovery", "Paging 3 & Skeleton Screens", R.drawable.ic_discovery, PagingDemoActivity::class.java),
            MenuEntry("Manual Registry", "Classic register() API showcase", R.drawable.ic_lab, ClassicManualActivity::class.java),
            MenuEntry("Java Interop", "Full compatibility for Java projects", R.drawable.ic_java, LabJavaActivity::class.java),
            MenuEntry("Sanitization Lab", "Fail-fast & Type Safety testing", R.drawable.ic_crash, CrashTestActivity::class.java)
        ))
        
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    data class MenuEntry(val title: String, val desc: String, val icon: Int, val target: Class<*>)
}