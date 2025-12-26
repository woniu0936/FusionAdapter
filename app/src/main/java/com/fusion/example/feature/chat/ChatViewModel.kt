package com.fusion.example.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fusion.example.core.model.ChatMessage
import com.fusion.example.core.repo.MockSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ChatState {
    object Loading : ChatState()
    data class Success(val msgs: List<ChatMessage>) : ChatState()
}

class ChatViewModel : ViewModel() {
    private val _state = MutableStateFlow<ChatState>(ChatState.Loading)
    val state: StateFlow<ChatState> = _state

    init {
        viewModelScope.launch {
            _state.value = ChatState.Success(MockSource.getChat())
        }
    }
}
