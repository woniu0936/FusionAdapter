package com.fusion.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fusion.example.databinding.ActivityMainBinding
import com.fusion.example.java.JavaDemoActivity
import com.fusion.example.kotlin.core.CoreDiffActivity
import com.fusion.example.kotlin.core.CoreManualActivity
import com.fusion.example.kotlin.core.CoreRouterActivity
import com.fusion.example.kotlin.fullStatusBar
import com.fusion.example.kotlin.ktx.KtxManualActivity
import com.fusion.example.kotlin.ktx.KtxRouterActivity
import com.fusion.example.paging.PagingDemoActivity

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
        binding.btnCoreRouter.setOnClickListener {
            startActivity<CoreRouterActivity>()
        }

        binding.btnKtxManual.setOnClickListener {
            startActivity<KtxManualActivity>()
        }

        binding.btnKtxDiff.setOnClickListener {
            startActivity<KtxManualActivity>()
        }

        binding.btnKtxRouter.setOnClickListener {
            startActivity<KtxRouterActivity>()
        }

        binding.btnJavaDemo.setOnClickListener {
            startActivity<JavaDemoActivity>()
        }

        binding.btnPaging.setOnClickListener {
            startActivity<PagingDemoActivity>()
        }
    }

    inline fun <reified T : Any> Context.startActivity() {
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }
}