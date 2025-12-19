package com.fusion.example.utils

import com.fusion.example.model.FusionMessage
import com.fusion.example.model.ImageItem
import com.fusion.example.model.SimpleItem
import com.fusion.example.model.SocialPost
import com.fusion.example.model.TextItem
import kotlin.random.Random

object MockDataGenerator {

    // 生成一个极其复杂的异构列表
    // 包含：聊天消息(3种类型) + 广告(ImageItem) + 公告(TextItem) + 推荐(SocialPost)
    fun createComplexStream(count: Int): List<Any> {
        val list = ArrayList<Any>()
        for (i in 0 until count) {
            val id = "item_$i"
            val random = Random.nextInt(100)
            val item: Any = when {
                // 10% 概率插入纯图片 (展示 DSL 1对1)
                random < 20 -> ImageItem(id, "https://via.placeholder.com/150")
                // 10% 概率插入纯文本 (展示 DSL 1对1)
                random < 30 -> TextItem(id, "Notice: System maintenance at 00:00")
                // 70% 概率插入聊天消息 (展示 1对多 路由)
                else -> createRandomChatMessage(id)
            }
            list.add(item)
        }
        return list
    }

    fun createSocialPosts(count: Int): List<Any> {
        val list = ArrayList<Any>()
        for (i in 0 until count) {
            val id = "item_$i"
            val random = Random.nextInt(100)
            val item: Any = when {
                // 概率插入社交帖子 (展示 Payload)
                random < 60 -> SocialPost(id, "User_$i", "FusionAdapter is amazing!", Random.nextInt(500), false)
                // 70% 概率插入聊天消息 (展示 1对多 路由)
                else -> createImageItem(id, i)
            }
            list.add(item)
        }
        return list
    }

    // 生成 LayoutDelegate 演示用的混合数据
    fun createLayoutStream(count: Int): List<Any> {
        return (0 until count).map { i ->
            if (i % 3 == 0) {
                // 插入一个 TextItem 捣乱，证明 LayoutDelegate 能和其他类型共存
                TextItem("text_$i", "我是混入 LayoutDelegate 列表的普通 ViewBinding Item")
            } else {
                SimpleItem("simple_$i", "LayoutDelegate Item #$i")
            }
        }
    }

    /**
     * 生成混合列表数据
     * @param count 要生成的数量
     * @param startIndex 起始 ID (用于"加载更多"时不重复 ID)
     * @return 包含 TextItem 和 ImageItem 的列表
     */
    fun createMixedList(count: Int, startIndex: Int = 0): List<Any> {
        val list = ArrayList<Any>()

        for (i in 0 until count) {
            val realIndex = startIndex + i
            val id = "$realIndex" // 保证 ID 唯一

            // 随机决定是文本还是图片 (30% 概率是图片)
            if (Random.nextInt(10) < 3) {
                list.add(createImageItem(id, realIndex))
            } else {
                list.add(createTextItem(id, realIndex))
            }
        }
        return list
    }

    private fun createTextItem(id: String, index: Int): TextItem {
        // 随机生成不同长度的文本，测试 Item 高度自适应
        val content = when (Random.nextInt(3)) {
            0 -> "FusionAdapter Item #$index - 短文本"
            1 -> "FusionAdapter Item #$index - 这是一段中等长度的文本，用来测试自动换行是否正常显示。"
            else -> "FusionAdapter Item #$index - 这是一段非常非常长的长文本！它不仅要测试换行，还要测试 DiffUtil 在内容变化时的比对性能。Kotlin DSL 写起来真的很爽，告别 ViewHolder 样板代码！"
        }
        return TextItem(id, content)
    }

    private fun createImageItem(id: String, index: Int): ImageItem {
        // 使用 picsum 生成随机图片，注意 url 带上 index 防止图片缓存导致重复
        val width = 400
        val height = (300..600).random() // 随机高度
        val url = "https://picsum.photos/$width/$height?random=$index"
        return ImageItem(id, url)
    }

    private fun createSimpleItem(id: String): SimpleItem {
        // 使用 picsum 生成随机图片，注意 url 带上 index 防止图片缓存导致重复
        val contents = listOf(
            "Hi there!",
            "FusionAdapter 真的很好用，强烈推荐给 Android 开发者。",
            "Could you please review my code? I think there is a bug in the MockDataGenerator.",
            "OK.",
            "这是一条比较长的文本消息，用于测试气泡的自适应高度。KTX DSL 让代码变得非常简洁，配合 FusionCore 的高性能，简直是开发利器！",
            "哈哈哈哈哈哈",
            "Tonight?"
        )
        val title = contents.random()
        return SimpleItem(id, title)
    }

    private fun createTextMsg(id: String): FusionMessage {
        val contents = listOf(
            "Hi there!",
            "FusionAdapter 真的很好用，强烈推荐给 Android 开发者。",
            "Could you please review my code? I think there is a bug in the MockDataGenerator.",
            "OK.",
            "这是一条比较长的文本消息，用于测试气泡的自适应高度。KTX DSL 让代码变得非常简洁，配合 FusionCore 的高性能，简直是开发利器！",
            "哈哈哈哈哈哈",
            "Tonight?"
        )
        val content = contents.random()
        return FusionMessage(id, FusionMessage.TYPE_TEXT, content)
    }

    private fun createImageMsg(id: String, index: Int): FusionMessage {
        // 模拟图片文件名
        return FusionMessage(id, FusionMessage.TYPE_IMAGE, "photo_${index}.jpg")
    }

    // 在 MockDataGenerator object 中修改以下方法：

    private fun createRandomChatMessage(id: String): FusionMessage {
        val type = when (Random.nextInt(3)) {
            0 -> FusionMessage.TYPE_TEXT
            1 -> FusionMessage.TYPE_IMAGE
            else -> FusionMessage.TYPE_SYSTEM
        }
        // 系统消息没有"左右"之分，文本和图片随机 isMe
        val isMe = if (type == FusionMessage.TYPE_SYSTEM) false else Random.nextBoolean()
        return FusionMessage(id, type, "Message Content $id", isMe)
    }

    fun createChatList(count: Int, startIndex: Int = 0): List<FusionMessage> {
        val list = ArrayList<FusionMessage>()
        for (i in 0 until count) {
            val realIndex = startIndex + i
            val id = "msg_$realIndex"
            val randomVal = Random.nextInt(100)

            // 随机生成发送者 (约50%概率)
            val isMe = Random.nextBoolean()

            val message = when {
                randomVal < 10 -> createSystemMsg(id)
                randomVal < 30 -> createImageMsg(id, realIndex, isMe)
                else -> createTextMsg(id, isMe)
            }
            list.add(message)
        }
        return list
    }

    private fun createTextMsg(id: String, isMe: Boolean = false): FusionMessage {
        val contents = listOf(
            "Hi there!",
            "Did you see the new Material 3 specs?",
            "Yes, the dynamic colors look amazing.",
            "OK.",
            "This is a longer message to test the bubble resizing behavior correctly on both sides.",
            "Haha",
            "Shall we meet tomorrow?"
        )
        val content = contents.random()
        return FusionMessage(id, FusionMessage.TYPE_TEXT, content, isMe)
    }

    private fun createImageMsg(id: String, index: Int, isMe: Boolean = false): FusionMessage {
        return FusionMessage(id, FusionMessage.TYPE_IMAGE, "photo_${index}.jpg", isMe)
    }

    // 系统消息不需要 isMe
    private fun createSystemMsg(id: String): FusionMessage {
        val notices = listOf(
            "User joined the chat",
            "User left the chat",
            "Today 12:00 PM"
        )
        return FusionMessage(id, FusionMessage.TYPE_SYSTEM, notices.random(), false)
    }
}