package com.example.mccagent.data

import com.example.mccagent.models.entities.Message

data class MessageListResponse(
    val ok: Boolean,
    val count: Int,
    val from: Int,
    val limit: Int,
    val messages: List<Message>
)
