package com.fusion.example.core.model

/**
 * [FeedItem] 社交流顶级密封类
 */
sealed class FeedItem {
    // 1. 标准动态
    data class MomentItem(val moment: Moment) : FeedItem()
    
    // 2. 商业推广
    data class AdCampaign(
        val id: String, 
        val title: String, 
        val description: String, 
        val bannerUrl: String,
        val actionText: String = "Learn More"
    ) : FeedItem()
    
    // 3. 关注推荐
    data class UserSuggestion(
        val id: String,
        val user: User,
        val mutualFriends: Int = 0
    ) : FeedItem()

    // 4. 时光分隔线
    data class TimelineHeader(val label: String) : FeedItem()
}

data class User(
    val id: String,
    val name: String,
    val avatar: String,
    val bio: String = ""
)

data class Moment(
    val id: String,
    val author: User,
    val content: String,
    val images: List<String> = emptyList(),
    val likes: Int = 0,
    val isLiked: Boolean = false,
    val timestamp: String = "10 mins ago"
)

data class ChatMessage(
    val id: String,
    val content: String,
    val type: Int, // 1: Text, 2: Image, 3: System
    val isMe: Boolean = false,
    val sender: User? = null
)

data class Product(
    val id: String,
    val name: String,
    val price: String,
    val cover: String,
    val isHot: Boolean = false,
    val height: Int = 0 
)

data class SectionHeader(val title: String)
