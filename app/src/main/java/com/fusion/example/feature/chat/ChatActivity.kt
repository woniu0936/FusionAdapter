package com.fusion.example.feature.chat

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.placeholder.showPlaceholders
import com.fusion.adapter.setup
import com.fusion.adapter.setupFusion
import com.fusion.example.core.model.ChatMessage
import com.fusion.example.core.repo.MockSource
import com.fusion.example.databinding.*
import com.fusion.example.utils.fullStatusBar
import com.fusion.example.utils.loadUrl
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private val vm: ChatViewModel by viewModels()
    private lateinit var binding: ActivityBaseFixedBinding

    companion object {
        private const val TAG = "ChatActivity"
        private const val INITIAL_SKELETON_COUNT = 16
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseFixedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)
        
        binding.toolbar.title = "Messenger"

        val adapter = binding.recyclerView.setupFusion {
            setup<ChatMessage> {
                uniqueKey { it.id }
                viewTypeKey { 
                    when {
                        it.type == 3 -> 3 // System
                        it.isMe -> 1      // Me (Right)
                        else -> 2         // Other (Left)
                    }
                }

                dispatch(1, ItemMsgTextMeBinding::inflate) {
                    onBind { item -> 
                        tvContent.text = item.content 
                        ivAvatarMe.loadUrl("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150", isCircle = true)
                    }
                }

                dispatch(2, ItemMsgTextOtherBinding::inflate) {
                    onBind { item -> 
                        tvContent.text = item.content
                        val avatarUrl = item.sender?.avatar ?: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150"
                        ivAvatar.loadUrl(avatarUrl, isCircle = true)
                    }
                }

                dispatch(3, ItemMsgSystemBinding::inflate) {
                    onBind { item -> tvSystemMsg.text = item.content }
                }
            }

            registerPlaceholder(ItemChatPlaceholderBinding::inflate) {
                onBind { _: Any -> }
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { state ->
                    when (state) {
                        is ChatState.Loading -> {
                            Log.d(TAG, "Showing $INITIAL_SKELETON_COUNT skeletons...")
                            adapter.showPlaceholders(INITIAL_SKELETON_COUNT)
                        }
                        is ChatState.Success -> {
                            Log.d(TAG, "Received ${state.msgs.size} messages.")
                            adapter.submitList(state.msgs)
                        }
                    }
                }
            }
        }
    }
}