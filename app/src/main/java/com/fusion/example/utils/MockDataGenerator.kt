package com.fusion.example.utils

import com.fusion.example.model.FusionMessage
import com.fusion.example.model.ImageItem
import com.fusion.example.model.TextItem
import kotlin.random.Random

object MockDataGenerator {

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

    /**
     * [新增] 生成聊天列表数据 (IsFor Demo 专用)
     * 包含：文本、图片、系统消息
     *
     * @param count 生成数量
     * @param startIndex 起始 ID
     */
    fun createChatList(count: Int, startIndex: Int = 0): List<FusionMessage> {
        val list = ArrayList<FusionMessage>()

        for (i in 0 until count) {
            val realIndex = startIndex + i
            val id = "msg_$realIndex"

            // 随机生成类型 (模拟真实聊天分布)
            val randomVal = Random.nextInt(100)
            val message = when {
                randomVal < 10 -> createSystemMsg(id) // 10% 系统消息
                randomVal < 30 -> createImageMsg(id, realIndex)  // 20% 图片消息
                else -> createTextMsg(id)             // 70% 文本消息
            }
            list.add(message)
        }
        return list
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
        return FusionMessage(id, FusionMessage.Companion.TYPE_TEXT, content)
    }

    private fun createImageMsg(id: String, index: Int): FusionMessage {
        // 模拟图片文件名
        return FusionMessage(id, FusionMessage.Companion.TYPE_IMAGE, "photo_${index}.jpg")
    }

    private fun createSystemMsg(id: String): FusionMessage {
        val notices = listOf(
            "User joined the chat",
            "User left the chat",
            "You recalled a message",
        )
        return FusionMessage(id, FusionMessage.Companion.TYPE_SYSTEM, notices.random())
    }
}