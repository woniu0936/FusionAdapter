package com.fusion.example.feature.moments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fusion.example.core.model.Moment
import com.fusion.example.core.repo.MockSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MomentsState {
    object Loading : MomentsUiState()
    data class Success(val posts: List<Moment>) : MomentsUiState()
}
// Renamed to avoid old conflict
open class MomentsUiState

class MomentsViewModel : ViewModel() {
    private val _state = MutableStateFlow<MomentsUiState>(MomentsState.Loading)
    val state: StateFlow<MomentsUiState> = _state
    private var posts = mutableListOf<Moment>()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            posts = MockSource.getMoments().toMutableList()
            _state.value = MomentsState.Success(posts)
        }
    }

    fun toggleLike(moment: Moment) {
        val index = posts.indexOfFirst { it.id == moment.id }
        if (index != -1) {
            val old = posts[index]
            posts[index] = old.copy(isLiked = !old.isLiked, likes = if (!old.isLiked) old.likes + 1 else old.likes - 1)
            _state.value = MomentsState.Success(ArrayList(posts))
        }
    }
}
