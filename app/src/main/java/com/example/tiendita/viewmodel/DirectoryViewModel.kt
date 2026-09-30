package com.example.tiendita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendita.repository.*
import com.example.tiendita.ui.model.DirectoryItemUi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
sealed interface DirectoryState {
    data object Loading : DirectoryState
    data object Error : DirectoryState
    data class Data(val items: List<DirectoryItemUi>) : DirectoryState
}
class DirectoryViewModel(private val repository: DirectoryRepository, private val kind: DirectoryKind,
    private val actorId: Long) : ViewModel() {
    private val mutableState = MutableStateFlow<DirectoryState>(DirectoryState.Loading)
    val state = mutableState.asStateFlow()
    private var job: Job? = null
    init { refresh() }
    fun refresh() {
        job?.cancel(); mutableState.value = DirectoryState.Loading
        job = viewModelScope.launch {
            repository.observe(kind, actorId).catch { if (it is CancellationException) throw it; mutableState.value = DirectoryState.Error }
                .collect { mutableState.value = DirectoryState.Data(it) }
        }
    }
}
