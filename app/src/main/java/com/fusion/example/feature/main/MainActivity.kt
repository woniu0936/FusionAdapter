package com.fusion.example.feature.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fusion.example.databinding.ActivityMainBinding
import com.fusion.example.feature.classic.ClassicManualActivity
import com.fusion.example.feature.dsl.DslComplexActivity
import com.fusion.example.feature.java.JavaInteropActivity
import com.fusion.example.feature.layout.GridSpanSizeActivity
import com.fusion.example.feature.layout.StaggeredFullSpanActivity
import com.fusion.example.feature.paging.PagingDemoActivity
import com.fusion.example.feature.payload.PayloadPerformanceActivity
import com.fusion.example.utils.fullStatusBar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)

        // 1. [经典模式] FusionAdapter + attachLinker + 手动数据管理
        binding.btnClassic.setOnClickListener {
            startActivity(Intent(this, ClassicManualActivity::class.java))
        }

        // 2. [现代 DSL] FusionListAdapter + setupFusion + 自动 Diff
        binding.btnDsl.setOnClickListener {
            startActivity(Intent(this, DslComplexActivity::class.java))
        }

        // 3.1 Grid SpanSize
        binding.btnGridSpan.setOnClickListener {
            startActivity(Intent(this, GridSpanSizeActivity::class.java))
        }

        // 3.2 Staggered FullSpan
        binding.btnStaggeredFull.setOnClickListener {
            startActivity(Intent(this, StaggeredFullSpanActivity::class.java))
        }

        // 4. [性能更新] bindPayload 局部刷新
        binding.btnPayload.setOnClickListener {
            startActivity(Intent(this, PayloadPerformanceActivity::class.java))
        }

        // 5. [分页集成] Paging3
        binding.btnPaging.setOnClickListener {
            startActivity(Intent(this, PagingDemoActivity::class.java))
        }

        // 6. [Java 兼容]
        binding.btnJava.setOnClickListener {
            startActivity(Intent(this, JavaInteropActivity::class.java))
        }

    }

}