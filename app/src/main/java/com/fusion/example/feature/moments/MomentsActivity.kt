package com.fusion.example.feature.moments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
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
import com.fusion.example.core.model.FeedItem
import com.fusion.example.core.model.Moment
import com.fusion.example.databinding.ActivityBaseFixedBinding
import com.fusion.example.databinding.ItemFeedAdBinding
import com.fusion.example.databinding.ItemHeaderBinding
import com.fusion.example.databinding.ItemMomentCardBinding
import com.fusion.example.databinding.ItemMomentPlaceholderBinding
import com.fusion.example.databinding.ItemUserSuggestionBinding
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

        binding.toolbar.title = "Feed Experience"

        val adapter = binding.recyclerView.setupFusion {

            setup<FeedItem.TimelineHeader, ItemHeaderBinding>(ItemHeaderBinding::inflate) {
                uniqueKey { it.label }
                onBind { item -> tvTitle.text = item.label }
            }

            setup<FeedItem.AdCampaign, ItemFeedAdBinding>(ItemFeedAdBinding::inflate) {
                uniqueKey { it.id }
                onCreate {
                    btnAction.setOnClickListener { Toast.makeText(this@MomentsActivity, "Ad Clicked!", Toast.LENGTH_SHORT).show() }
                }
                onBind { item ->
                    tvAdTitle.text = item.title
                    tvAdDesc.text = item.description
                    ivBanner.loadUrl(item.bannerUrl)
                    btnAction.text = item.actionText
                }
            }

            setup<FeedItem.UserSuggestion, ItemUserSuggestionBinding>(ItemUserSuggestionBinding::inflate) {
                uniqueKey { it.id }
                onCreate {
                    btnFollow.setOnClickListener {
                        btnFollow.text = "Requested"
                        btnFollow.isEnabled = false
                    }
                }
                onBind { item ->
                    tvSugName.text = item.user.name
                    tvSugFriends.text = "${item.mutualFriends} mutual friends"
                    ivSugAvatar.loadUrl(item.user.avatar, isCircle = true)
                }
            }

            setup<FeedItem.MomentItem, ItemMomentCardBinding>(ItemMomentCardBinding::inflate) {
                uniqueKey { it.moment.id }

                onCreate {
                    btnLike.setOnClickListener {
                        val currentItem = root.getItem<FeedItem.MomentItem>()
                        currentItem?.let { vm.toggleLike(it.moment.id) }
                    }
                }

                onBind { item ->
                    val m = item.moment
                    tvName.text = m.author.name
                    tvContent.text = m.content
                    ivAvatar.loadUrl(m.author.avatar, isCircle = true)

                    imageContainer.visibility = if (m.images.isNotEmpty()) View.VISIBLE else View.GONE
                    if (m.images.isNotEmpty()) ivImage.loadUrl(m.images[0])

                    updateLikeUI(this, m.isLiked, m.likes, animate = false)
                }

                onPayload(FeedItem.MomentItem::moment) { m: Moment ->
                    updateLikeUI(this, m.isLiked, m.likes, animate = true)
                }
            }

            registerPlaceholder(ItemMomentPlaceholderBinding::inflate) {
                onBind { _: Any -> }
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { state ->
                    when (state) {
                        is MomentsState.Loading -> adapter.showPlaceholders(3)
                        is MomentsState.Success -> {
                            adapter.submitList(state.items)
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
