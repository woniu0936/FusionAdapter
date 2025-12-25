package com.fusion.example.feature.test

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.FusionListAdapter
import com.fusion.adapter.exception.UnregisteredTypeException
import com.fusion.adapter.setup
import com.fusion.example.R
import com.fusion.example.databinding.ItemTextBinding
import com.fusion.example.model.TextItem
import com.fusion.example.utils.fullStatusBar

class CrashTestActivity : AppCompatActivity() {

    data class UnknownItem(val id: Int, val reason: String)

    private val adapter = FusionListAdapter()
    private val items = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash_test)
        fullStatusBar(findViewById<View>(R.id.main))
        setupFusion()
        setupViews()
    }

    private fun setupFusion() {
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        adapter.setup<TextItem, ItemTextBinding>(ItemTextBinding::inflate) {
            uniqueKey { item -> item.id }
            onBind { item ->
                tvContent.text = item.content
                cardRoot.setCardBackgroundColor(0xFFF0F0F0.toInt())
            }
        }

        items.add(TextItem("1", "Initial Valid Item 1"))
        items.add(TextItem("1", "Initial Valid Item 2"))
        adapter.submitList(ArrayList(items))
    }

    private fun setupViews() {
        findViewById<Button>(R.id.btn_crash).setOnClickListener {
            items.add(UnknownItem(1, "I am a ghost item!"))
            Toast.makeText(this, "Adding UnknownItem... Watch out!", Toast.LENGTH_SHORT).show()
            adapter.submitList(ArrayList(items))
        }

        findViewById<Button>(R.id.btn_catch_crash).setOnClickListener {
            try {
                val riskyList = ArrayList(items)
                riskyList.add(UnknownItem(2, "I will be caught!"))
                adapter.setItems(riskyList)
            } catch (e: UnregisteredTypeException) {
                Log.e("CrashDemo", "Caught expected exception", e)
                showDialog(
                    "Crash Prevented!",
                    "Successfully caught expected exception:\n\n" +
                            "${e.javaClass.simpleName}\n\n" +
                            "Message: ${e.message}"
                )
            } catch (e: Exception) {
                showDialog("Unexpected Error", "Got wrong exception: $e")
            }
        }
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}