package com.fusion.example.paging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn

class PagingViewModel : ViewModel() {

    // 配置 Paging
    val pagingFlow = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false // Fusion 强依赖类型，建议关闭占位符
        ),
        pagingSourceFactory = { ChatPagingSource() }
    ).flow.cachedIn(viewModelScope) // 绑定生命周期
}