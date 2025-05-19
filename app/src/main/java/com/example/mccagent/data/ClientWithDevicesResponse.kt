package com.example.mccagent.data

import com.example.mccagent.models.entities.Client

data class ClientWithDevicesResponse(
    val ok: Boolean,
    val client: Client
)
