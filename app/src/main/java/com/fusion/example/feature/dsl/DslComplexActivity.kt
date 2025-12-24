package com.fusion.example.feature.dsl

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fusion.adapter.register
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

/**
 * 现代 DSL 模式
 * 1. 自动 Diff (FusionListAdapter)
 * 2. 声明式注册 (register)
 * 3. 混合 1对1 和 1对多
 */
class DslComplexActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)

        val adapter = binding.recyclerView.setupFusion {
            // [1对多] 路由
            register<FusionMessage> {
                stableId { it.id }
                match { it.msgType }
                
                // ✅ 现在库支持直接在 map 中书写逻辑了
                map(FusionMessage.TYPE_TEXT, ItemMsgTextBinding::inflate) {
                    onBind { item ->
                        tvContent.text = item.content
                        ChatStyleHelper.bindTextMsg(this, item.isMe)
                    }
                    onItemClick(100) { item -> toast("Text: ${item.id}") }
                }

                map(FusionMessage.TYPE_IMAGE, ItemMsgImageBinding::inflate) {
                    onBind { item ->
                        ivImage.contentDescription = item.content
                        ChatStyleHelper.bindImageMsg(this, item.isMe)
                    }
                    onItemClick(1000) { item -> toast("Image: ${item.id}") }
                }

                map(FusionMessage.TYPE_SYSTEM, ItemMsgSystemBinding::inflate) {
                    onBind { item -> tvSystemMsg.text = item.content }
                }
            }

            // [1对1] 直接绑定
            register<TextItem, ItemTextBinding>(ItemTextBinding::inflate) {
                stableId { it.id }
                onBind { item ->
                    tvContent.text = item.content
                    cardRoot.setCardBackgroundColor(0xFFF0F0F0.toInt())
                }
            }

            register<ImageItem, ItemImageBinding>(ItemImageBinding::inflate) {
                stableId { it.id }
                onBind { item ->
                    ChatStyleHelper.bindStandaloneImage(this)
                    tvDesc.text = "Image ID: ${item.id}"
                    ivImage.layoutParams.height = 400
                }
            }
        }

        // 提交异构数据
        adapter.submitList(MockDataGenerator.createComplexStream(60))
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}