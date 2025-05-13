package com.example.mccagent.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mccagent.models.interfaces.IClientRepository

class ClientViewModelFactory(
    private val repository: IClientRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientViewModel::class.java)) {
            return ClientViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


