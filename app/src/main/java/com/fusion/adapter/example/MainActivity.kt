package com.fusion.adapter.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fusion.adapter.example.core.CoreDiffActivity
import com.fusion.adapter.example.core.CoreManualActivity
import com.fusion.adapter.example.databinding.ActivityMainBinding
import com.fusion.adapter.example.ktx.KtxManualActivity
import com.fusion.adapter.example.ktx.OneToManyActivity

/**
 * com.fusion.adapter.example
 * ├── model                    // [公共] 数据模型
 * │   ├── DemoItems.kt         // 定义 TextItem, ImageItem 等
 * │
 * ├── delegate                 // [公共] 传统的 Class 形式 Delegate
 * │   └── CoreTextDelegate.kt  // 供 Core 演示使用
 * │   └── CoreImageDelegate.kt // 供 Core 演示使用
 * │
 * ├── core                     // [演示 1 & 2] 纯 Core 库演示
 * │   ├── CoreManualActivity.kt // 1. Core + 手动挡 (无 Diff)
 * │   └── CoreDiffActivity.kt   // 2. Core + 自动挡 (有 Diff)
 * │
 * ├── ktx                      // [演示 3 & 4] KTX 扩展库演示
 * │   ├── KtxManualActivity.kt  // 3. KTX + 手动挡 (无 Diff)
 * │   └── KtxDiffActivity.kt    // 4. KTX + 自动挡 (有 Diff)
 * │
 * └── MainActivity.kt          // 首页入口 (菜单)
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)

        binding.btnCoreManual.setOnClickListener {
            startActivity<CoreManualActivity>()
        }

        binding.btnCoreDiff.setOnClickListener {
            startActivity<CoreDiffActivity>()
        }

        binding.btnKtxManual.setOnClickListener {
            startActivity<KtxManualActivity>()
        }

        binding.btnKtxDiff.setOnClickListener {
            startActivity<KtxManualActivity>()
        }

        binding.btnKtxOneToMany.setOnClickListener {
            startActivity<OneToManyActivity>()
        }
    }

    inline fun <reified T : Any> Context.startActivity() {
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }
}