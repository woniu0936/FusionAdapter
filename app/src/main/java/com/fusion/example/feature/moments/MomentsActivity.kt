package com.fusion.example.feature.moments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion.adapter.extensions.getItem
import com.fusion.adapter.placeholder.showPlaceholders
import com.fusion.adapter.setup
import com.fusion.adapter.setupFusion
import com.fusion.example.R
import com.fusion.example.core.model.Moment
import com.fusion.example.databinding.ActivityBaseFixedBinding
import com.fusion.example.databinding.ItemMomentCardBinding
import com.fusion.example.databinding.ItemMomentPlaceholderBinding
import com.fusion.example.utils.fullStatusBar
import com.fusion.example.utils.loadUrl
import kotlinx.coroutines.launch

class MomentsActivity : AppCompatActivity() {
    private val vm: MomentsViewModel by viewModels()
    private lateinit var binding: ActivityBaseFixedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseFixedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullStatusBar(binding.root)
        
        binding.toolbar.title = "Social Flow"

        val adapter = binding.recyclerView.setupFusion {
            setup<Moment, ItemMomentCardBinding>(ItemMomentCardBinding::inflate) {
                uniqueKey { it.id }
                onBind { item ->
                    tvName.text = item.author.name
                    tvContent.text = item.content
                    ivAvatar.loadUrl(item.author.avatar, isCircle = true)
                    imageContainer.visibility = if (item.images.isNotEmpty()) View.VISIBLE else View.GONE
                    if (item.images.isNotEmpty()) ivImage.loadUrl(item.images[0])
                    btnLike.setOnClickListener { root.getItem<Moment>()?.let { vm.toggleLike(it) } }
                    updateLikeUI(this, item.isLiked, item.likes, animate = false)
                }
                onPayload(Moment::isLiked, Moment::likes) { isLiked, likes ->
                    updateLikeUI(this, isLiked, likes, animate = true)
                }
            }

            // [API] 使用专属骨架屏
            registerPlaceholder(ItemMomentPlaceholderBinding::inflate) { onBind { _: Any -> } }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { state ->
                    when (state) {
                        is MomentsState.Loading -> adapter.showPlaceholders(3)
                        is MomentsState.Success -> {
                            adapter.submitList(state.posts)
                            binding.recyclerView.post { binding.recyclerView.requestLayout() }
                        }
                    }
                }
            }
        }
    }

    private fun updateLikeUI(binding: ItemMomentCardBinding, isLiked: Boolean, likes: Int, animate: Boolean) {
        binding.btnLike.text = likes.toString()
        val color = if (isLiked) Color.parseColor("#E91E63") else Color.GRAY
        binding.btnLike.setIconResource(if (isLiked) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
        binding.btnLike.iconTint = ColorStateList.valueOf(color)
        binding.btnLike.setTextColor(color)
        if (animate) {
            binding.btnLike.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150)
                .setInterpolator(OvershootInterpolator())
                .withEndAction { binding.btnLike.animate().scaleX(1.0f).scaleY(1.0f).start() }.start()
        }
    }
}