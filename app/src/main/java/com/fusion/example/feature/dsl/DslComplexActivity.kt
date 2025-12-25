package com.fusion.example.feature.dsl

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fusion.adapter.setup
import com.fusion.adapter.setupFusion
import com.fusion.example.databinding.ActivityRecyclerBinding
import com.fusion.example.databinding.ItemImageBinding
import com.fusion.example.databinding.ItemMsgImageBinding
import com.fusion.example.databinding.ItemMsgSystemBinding
import com.fusion.example.databinding.ItemMsgTextBinding
import com.fusion.example.databinding.ItemTextBinding
import com.fusion.example.model.FusionMessage
import com.fusion.example.model.ImageItem
import com.fusion.example.model.TextItem
import com.fusion.example.utils.ChatStyleHelper
import com.fusion.example.utils.MockDataGenerator
import com.fusion.example.utils.fullStatusBar

class DslComplexActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)

        val adapter = binding.recyclerView.setupFusion {
            // 1. 注册 FusionMessage (1对多路由)
            setup<FusionMessage> {
                uniqueKey { it.id }
                viewTypeKey { it.msgType }

                dispatch(FusionMessage.TYPE_TEXT, ItemMsgTextBinding::inflate) {
                    onBind { item ->
                        tvContent.text = item.content
                        ChatStyleHelper.bindTextMsg(this, item.isMe)
                    }
                    onClick(100) { item -> toast("Text: ${item.id}") }
                }

                dispatch(FusionMessage.TYPE_IMAGE, ItemMsgImageBinding::inflate) {
                    onBind { item ->
                        ivImage.contentDescription = item.content
                        ChatStyleHelper.bindImageMsg(this, item.isMe)
                    }
                    onClick(1000) { item -> toast("Image: ${item.id}") }
                }

                dispatch(FusionMessage.TYPE_SYSTEM, ItemMsgSystemBinding::inflate) {
                    onBind { item -> tvSystemMsg.text = item.content }
                }
            }

            // 2. 注册 TextItem (1对1)
            setup<TextItem, ItemTextBinding>(ItemTextBinding::inflate) {
                uniqueKey { it.id }
                onBind { item ->
                    tvContent.text = item.content
                }
            }

            setup<ImageItem, ItemImageBinding>(ItemImageBinding::inflate) {
                uniqueKey { it.id }
                onBind { item ->
                    ChatStyleHelper.bindStandaloneImage(this)
                    tvDesc.text = "Image ID: ${item.id}"
                    // 演示修改高度
                    ivImage.layoutParams.height = 400
                }
            }
        }

        adapter.submitList(MockDataGenerator.createComplexStream(60))
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}