package com.fusion.example.feature.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fusion.example.databinding.ItemLoadStateFooterBinding

/**
 * [通用加载状态适配器]
 * 用于 Paging 3 底部显示：加载中、错误重试。
 *
 * @param retry 点击重试按钮时的回调
 */
class SimpleLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<SimpleLoadStateAdapter.LoadStateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val binding = ItemLoadStateFooterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LoadStateViewHolder(binding, retry)
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class LoadStateViewHolder(
        private val binding: ItemLoadStateFooterBinding,
        retry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnRetry.setOnClickListener { retry() }
        }

        fun bind(loadState: LoadState) {
            binding.apply {
                // 1. 处理 Loading 状态
                progressBar.isVisible = loadState is LoadState.Loading

                // 2. 处理 Error 状态
                btnRetry.isVisible = loadState is LoadState.Error
                tvError.isVisible = loadState is LoadState.Error

                if (loadState is LoadState.Error) {
                    tvError.text = loadState.error.localizedMessage ?: "Unknown error occurred"
                }
            }
        }
    }
}