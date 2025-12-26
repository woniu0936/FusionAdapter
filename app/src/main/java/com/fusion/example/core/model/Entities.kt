package com.fusion.example.core.model

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
    val height: Int = 0 // For Staggered items
)

data class SectionHeader(val title: String)