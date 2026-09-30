package com.example.tiendita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendita.data.local.model.DatabaseSnapshot
import com.example.tiendita.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
sealed interface DatabaseStatusState {
    data object Loading : DatabaseStatusState
    data object Error : DatabaseStatusState
    data class Data(val snapshot: DatabaseSnapshot) : DatabaseStatusState
}
class DatabaseStatusViewModel(private val repository: DatabaseStatusRepository, private val actorId: Long) : ViewModel() {
    private val mutableState = MutableStateFlow<DatabaseStatusState>(DatabaseStatusState.Loading)
    val state = mutableState.asStateFlow()
    private var job: Job? = null
    init { refresh() }
    fun refresh() {
        if (job?.isActive == true) return
        mutableState.value = DatabaseStatusState.Loading
        job = viewModelScope.launch {
            mutableState.value = when (val result = repository.load(actorId)) {
                is DatabaseStatusResult.Success -> DatabaseStatusState.Data(result.snapshot)
                else -> DatabaseStatusState.Error
            }
        }
    }
}
