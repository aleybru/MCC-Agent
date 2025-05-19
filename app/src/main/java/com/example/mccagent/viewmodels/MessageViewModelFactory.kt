package com.example.mccagent.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mccagent.models.interfaces.IMessageRepository

class MessageViewModelFactory(private val repo: IMessageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MessageViewModel(repo) as T
    }
}
