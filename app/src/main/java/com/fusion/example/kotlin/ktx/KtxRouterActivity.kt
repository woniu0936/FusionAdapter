package com.fusion.example.kotlin.ktx

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fusion.adapter.register
import com.fusion.adapter.setupFusion
import com.fusion.example.kotlin.fullStatusBar
import com.fusion.example.model.FusionMessage
import com.fusion.example.utils.MockDataGenerator
import com.fusion.example.databinding.ActivityRecyclerBinding
import com.fusion.example.databinding.ItemMsgImageBinding
import com.fusion.example.databinding.ItemMsgSystemBinding
import com.fusion.example.databinding.ItemMsgTextBinding

class KtxRouterActivity : AppCompatActivity() {

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


            register<FusionMessage> {
                match { item -> item.msgType }

                // 1. 注册文本消息 (Type = 1)
                map(FusionMessage.TYPE_TEXT, ItemMsgTextBinding::inflate) {
                    onBind { item ->
                        tvContent.text = item.content // ItemMsgTextBinding 的控件
                    }

                    onItemClick { item ->
                        toast("点击了文本: ${item.content}")
                    }
                }

                // 2. 注册图片消息 (Type = 2)
                map(FusionMessage.TYPE_IMAGE, ItemMsgImageBinding::inflate) {
                    onBind { item ->
                        tvDesc.text = "图片描述: ${item.content}"
                        // ivImage.setImageResource(...)
                    }

                    onItemClick { item ->
                        toast("查看大图: ${item.id}")
                    }
                }

                // 3. 注册系统通知 (Type = 3)
                map(FusionMessage.TYPE_SYSTEM, ItemMsgSystemBinding::inflate) {
                    onBind { item ->
                        tvSystemMsg.text = "--- ${item.content} ---"
                    }
                    // 系统消息通常不可点击，所以不写 onClick
                }
            }

        }

        val list = MockDataGenerator.createChatList(50)
        adapter.submitList(list)
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}