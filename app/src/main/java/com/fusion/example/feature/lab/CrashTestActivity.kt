package com.fusion.example.feature.lab

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.Fusion
import com.fusion.adapter.FusionListAdapter
import com.fusion.adapter.placeholder.showPlaceholders
import com.fusion.adapter.register
import com.fusion.example.core.model.SectionHeader
import com.fusion.example.databinding.ActivityCrashTestM3Binding
import com.fusion.example.databinding.ItemLabPlaceholderBinding
import com.fusion.example.databinding.ItemLabRecordBinding
import com.fusion.example.utils.fullStatusBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CrashTestActivity : AppCompatActivity() {

    data class Unregistered(val data: String)

    private lateinit var binding: ActivityCrashTestM3Binding
    private lateinit var adapter: FusionListAdapter
    private val items = mutableListOf<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCrashTestM3Binding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)

        binding.toolbar.title = "Sanitization Lab"

        adapter = FusionListAdapter()

        setupAdapter()
        setupLogic()
        loadData()
    }

    private fun setupAdapter() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        adapter.register<SectionHeader, ItemLabRecordBinding>(ItemLabRecordBinding::inflate) {
            stableId { it.title }
            onBind { item ->
                tvTitle.text = item.title
                vStatusIndicatorBox.setCardBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
            }
        }

        adapter.registerPlaceholder(ItemLabPlaceholderBinding::inflate) { onBind { _: Any -> } }
    }

    private fun loadData() {
        lifecycleScope.launch {
            adapter.showPlaceholders(8)
            delay(1000)

            items.clear()
            items.add(SectionHeader("Registry Security: Verified"))
            repeat(30) {
                items.add(SectionHeader("Encrypted Audit Log #$it"))
            }
            adapter.submitList(ArrayList(items))
        }
    }

    private fun setupLogic() {
        binding.btnCrash.setOnClickListener {
            val list = ArrayList(adapter.currentList)
            list.add(if (list.size > 1) 1 else 0, Unregistered("BOOM"))
            adapter.submitList(list)
        }

        binding.btnCatchCrash.setOnClickListener {
            val list = ArrayList(adapter.currentList)
            list.add(if (list.size > 1) 1 else 0, Unregistered("EXTERNAL_DATA"))

            try {
                adapter.setItems(list)
            } catch (e: Exception) {
                Toast.makeText(this, "Safe Caught: ${e.javaClass.simpleName}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}