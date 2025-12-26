package com.fusion.example.feature.plaza

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fusion.example.core.repo.MockSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PlazaState {
    object Loading : PlazaState()
    data class Success(val items: List<Any>) : PlazaState()
}

class PlazaViewModel : ViewModel() {
    private val repo = MockSource
    private val _state = MutableStateFlow<PlazaState>(PlazaState.Loading)
    val state: StateFlow<PlazaState> = _state

    fun loadMarketData() {
        viewModelScope.launch {
            _state.value = PlazaState.Loading
            _state.value = PlazaState.Success(repo.getMarket())
        }
    }
}