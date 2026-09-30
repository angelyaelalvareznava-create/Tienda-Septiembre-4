package com.example.tiendita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
class ExplicitViewModelFactory<T : ViewModel>(private val type: Class<T>, private val create: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST") override fun <V : ViewModel> create(modelClass: Class<V>): V {
        require(modelClass == type)
        return create() as V
    }
}
