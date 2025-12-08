package com.fusion.example.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.fusion.example.model.FusionMessage
import com.fusion.example.utils.MockDataGenerator
import kotlinx.coroutines.delay

/**
 * [数据源] 模拟网络请求
 * 每次加载 20 条聊天记录，总共加载 100 条。
 */
class ChatPagingSource : PagingSource<Int, FusionMessage>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FusionMessage> {
        val page = params.key ?: 1
        val pageSize = params.loadSize

        // 模拟网络延迟 1秒
        delay(1000)

        return try {
            // 使用工具类生成数据 (复用之前的 Mock 逻辑)
            // startIndex 保证 ID 不重复
            val data = MockDataGenerator.createChatList(pageSize, startIndex = (page - 1) * pageSize)

            // 模拟总共只有 5 页数据
            val nextKey = if (page < 5) page + 1 else null

            LoadResult.Page(
                data = data,
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, FusionMessage>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}