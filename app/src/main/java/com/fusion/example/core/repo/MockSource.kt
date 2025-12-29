package com.fusion.example.core.repo

import com.fusion.example.core.model.ChatMessage
import com.fusion.example.core.model.FeedItem
import com.fusion.example.core.model.Moment
import com.fusion.example.core.model.Product
import com.fusion.example.core.model.SectionHeader
import com.fusion.example.core.model.User
import kotlinx.coroutines.delay
import kotlin.random.Random

object MockSource {
    private val elena = User("1", "Elena Vance", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150")
    private val gordon = User("2", "Gordon Freeman", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150")
    private val alyx = User("3", "Alyx Jenkins", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150")

    suspend fun getMomentsFeed(): List<FeedItem> {
        delay(1000)
        val list = mutableListOf<FeedItem>()

        list.add(FeedItem.TimelineHeader("Today"))

        // 混合数据
        repeat(20) { i ->
            when {
                i == 3 -> {
                    list.add(FeedItem.AdCampaign("ad_1", "FusionAdapter", "The ultimate list framework for Android.", "https://picsum.photos/seed/ad/800/400"))
                }

                i == 7 -> {
                    list.add(FeedItem.TimelineHeader("Yesterday"))
                }

                i % 8 == 0 && i != 0 -> {
                    list.add(FeedItem.UserSuggestion("s_$i", alyx, Random.nextInt(1, 10)))
                }

                else -> {
                    list.add(
                        FeedItem.MomentItem(
                            Moment(
                                "m_$i", if (i % 2 == 0) elena else gordon,
                                "Sample feed item #$i. Demonstrating multi-type support.",
                                if (i % 4 == 0) listOf("https://picsum.photos/seed/$i/800/600") else emptyList(),
                                Random.nextInt(10, 500)
                            )
                        )
                    )
                }
            }
        }
        return list
    }

    // 保持原有方法兼容性
    suspend fun getMoments() = (getMomentsFeed().filterIsInstance<FeedItem.MomentItem>().map { it.moment })

    suspend fun getMarket(): List<Any> {
        delay(800)
        return mutableListOf<Any>().apply {
            add(SectionHeader("Hot Deals"))
            addAll(List(4) { i -> Product("hp_$i", "Premium Product $i", "$1,999", "https://picsum.photos/seed/${i + 10}/500/500", true) })
            add(SectionHeader("All Collections"))
            addAll(List(20) { i ->
                Product(
                    "p_$i",
                    "Standard Item $i",
                    "$99",
                    "https://picsum.photos/seed/${i + 100}/500/500",
                    false,
                    Random.nextInt(400, 700)
                )
            })
        }
    }

    suspend fun getChat(): List<ChatMessage> {
        delay(500)
        return List(40) { i ->
            val isMe = i % 2 == 0
            val type = when {
                i % 10 == 0 -> 3 // System
                i % 4 == 0 -> 2  // Image
                else -> 1        // Text
            }
            
            val content = when (type) {
                3 -> "System maintenance scheduled for 2:00 AM."
                2 -> "https://picsum.photos/seed/chat_$i/400/300"
                else -> "Sample message #$i from Fusion Messenger. This is a text message."
            }

            ChatMessage(
                "c_$i",
                content,
                type,
                isMe,
                if (!isMe) elena else null
            )
        }
    }
}
