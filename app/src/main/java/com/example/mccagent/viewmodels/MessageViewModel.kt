package com.example.mccagent.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mccagent.models.entities.Message
import com.example.mccagent.models.interfaces.IMessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MessageViewModel(private val repo: IMessageRepository) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    fun loadPending() {
        viewModelScope.launch {
            _messages.value = repo.getPendingMessages()
        }
    }

    fun markAsSent(mid: String) {
        viewModelScope.launch {
            repo.updateMessageStatus(mid, "ENVIADO")
        }
    }
}
