package com.fusion.example.feature.moments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fusion.example.core.model.FeedItem
import com.fusion.example.core.repo.MockSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MomentsState {
    object Loading : MomentsUiState()
    data class Success(val items: List<FeedItem>) : MomentsUiState()
}
open class MomentsUiState

class MomentsViewModel : ViewModel() {
    private val _state = MutableStateFlow<MomentsUiState>(MomentsState.Loading)
    val state: StateFlow<MomentsUiState> = _state
    private var feedItems = mutableListOf<FeedItem>()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            feedItems = MockSource.getMomentsFeed().toMutableList()
            _state.value = MomentsState.Success(feedItems)
        }
    }

    fun toggleLike(momentId: String) {
        val index = feedItems.indexOfFirst { it is FeedItem.MomentItem && it.moment.id == momentId }
        if (index != -1) {
            val item = feedItems[index] as FeedItem.MomentItem
            val old = item.moment
            val newMoment = old.copy(
                isLiked = !old.isLiked, 
                likes = if (!old.isLiked) old.likes + 1 else old.likes - 1
            )
            feedItems[index] = FeedItem.MomentItem(newMoment)
            _state.value = MomentsState.Success(ArrayList(feedItems))
        }
    }
}