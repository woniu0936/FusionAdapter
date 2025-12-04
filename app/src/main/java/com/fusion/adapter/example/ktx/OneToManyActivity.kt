package com.fusion.adapter.example.ktx

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fusion.adapter.example.databinding.ActivityRecyclerBinding
import com.fusion.adapter.example.databinding.ItemMsgImageBinding
import com.fusion.adapter.example.databinding.ItemMsgSystemBinding
import com.fusion.adapter.example.databinding.ItemMsgTextBinding
import com.fusion.adapter.example.fullStatusBar
import com.fusion.adapter.example.model.FusionMessage
import com.fusion.adapter.example.utils.MockDataGenerator
import com.fusion.adapter.ktx.register
import com.fusion.adapter.ktx.setupFusion

class OneToManyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)
        binding.fabAdd.visibility = View.GONE

        // 使用 setupFusion 启动 (自动挡 ListAdapter)
        val adapter = binding.recyclerView.setupFusion {

            // =================================================================
            // 场景：同一个 FusionMessage 类，注册了 3 次！
            // FusionAdapter 会根据 isFor 的返回值，自动分发给正确的 Delegate
            // =================================================================

            // 1. 注册文本消息 (Type = 1)
            register<FusionMessage, ItemMsgTextBinding>(ItemMsgTextBinding::inflate) {

                // 【核心】匹配逻辑：只有 type == 1 才由我处理
                isFor { item -> item.msgType == FusionMessage.TYPE_TEXT }

                onBind { item ->
                    tvContent.text = item.content // ItemMsgTextBinding 的控件
                }

                onClick { item ->
                    toast("点击了文本: ${item.content}")
                }
            }

            // 2. 注册图片消息 (Type = 2)
            register<FusionMessage, ItemMsgImageBinding>(ItemMsgImageBinding::inflate) {

                // 【核心】匹配逻辑：只有 type == 2 才由我处理
                isFor { item -> item.msgType == FusionMessage.TYPE_IMAGE }

                onBind { item ->
                    tvDesc.text = "图片描述: ${item.content}"
                    // ivImage.setImageResource(...)
                }

                onClick { item ->
                    toast("查看大图: ${item.id}")
                }
            }

            // 3. 注册系统通知 (Type = 3)
            register<FusionMessage, ItemMsgSystemBinding>(ItemMsgSystemBinding::inflate) {

                // 【核心】匹配逻辑
                isFor { item -> item.msgType == FusionMessage.TYPE_SYSTEM }

                onBind { item ->
                    tvSystemMsg.text = "--- ${item.content} ---"
                }
                // 系统消息通常不可点击，所以不写 onClick
            }
        }

        val list = MockDataGenerator.createChatList(50)
        adapter.submitList(list)
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}