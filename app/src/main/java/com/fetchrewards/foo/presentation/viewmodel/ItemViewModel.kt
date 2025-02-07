package com.fetchrewards.foo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fetchrewards.foo.domain.usecase.GetItemsUseCase
import com.fetchrewards.foo.presentation.state.ItemsIntent
import com.fetchrewards.foo.presentation.state.ItemsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.fetchrewards.foo.util.Result

@HiltViewModel
class ItemViewModel @Inject constructor(
    private val getItemsUseCase: GetItemsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ItemsState())
    val state: StateFlow<ItemsState> = _state.asStateFlow()

    fun handleIntent(intent: ItemsIntent) {
        when (intent) {
            is ItemsIntent.LoadItems -> loadItems()
            is ItemsIntent.RefreshItems -> loadItems(forceRefresh = true)
        }
    }

    private fun loadItems(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            updateState(state.value.copy(isLoading = true, error = null))

            when (val result = getItemsUseCase(forceRefresh)) {
                is Result.Success -> {
                    updateState(state.value.copy(
                        items = result.data.groupBy { it.listId }, // Ensure Item has listId
                        isLoading = false,
                        error = null
                    ))
                }
                is Result.Error -> {
                    updateState(state.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Unknown error occurred"
                    ))
                }
                is Result.Loading -> {
                    updateState(state.value.copy(isLoading = true))
                }
            }
        }
    }

    private fun updateState(newState: ItemsState) {
        if (_state.value != newState) {
            _state.value = newState
        }
    }
    
} 