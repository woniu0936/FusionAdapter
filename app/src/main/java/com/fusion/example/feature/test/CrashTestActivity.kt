package com.fusion.example.feature.test

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusion.adapter.FusionListAdapter
import com.fusion.adapter.exception.UnregisteredTypeException
import com.fusion.adapter.register
import com.fusion.example.R
import com.fusion.example.databinding.ItemTextBinding
import com.fusion.example.model.TextItem

class CrashTestActivity : AppCompatActivity() {

    // Define an item that is INTENTIONALLY NOT registered
    data class UnknownItem(val id:Int, val reason: String)

    private val adapter = FusionListAdapter()
    private val items = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash_test)

        setupFusion()
        setupViews()
    }

    private fun setupFusion() {
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        adapter.register<TextItem, ItemTextBinding>(ItemTextBinding::inflate) {
            onBind { item ->
                tvContent.text = item.content
                cardRoot.setCardBackgroundColor(0xFFF0F0F0.toInt())
            }
        }

        // 3. Initialize with some valid data
        items.add(TextItem("1", "Initial Valid Item 1"))
        items.add(TextItem("1", "Initial Valid Item 2"))
        adapter.submitList(ArrayList(items))
    }

    private fun setupViews() {
        // Button 1: The "Real World" Crash
        findViewById<Button>(R.id.btn_crash).setOnClickListener {
            // Add an unregistered item
            items.add(UnknownItem(1,"I am a ghost item!"))

            Toast.makeText(this, "Adding UnknownItem... Watch out!", Toast.LENGTH_SHORT).show()

            // 🚨 This line will CRASH the app in DEBUG mode!
            // Because sanitize() is called synchronously inside submitList()
            adapter.submitList(ArrayList(items))
        }

        // Button 2: Catching the Crash (For verify the exception type)
        findViewById<Button>(R.id.btn_catch_crash).setOnClickListener {
            try {
                val riskyList = ArrayList(items)
                riskyList.add(UnknownItem(2,"I will be caught!"))

                // Attempt to submit
                adapter.submitList(riskyList)

            } catch (e: UnregisteredTypeException) {
                // 🛡️ Caught the exception!
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