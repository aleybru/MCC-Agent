package com.example.mccagent.viewmodels

import com.example.mccagent.models.entities.Device

data class ClientState(
    val isLoading: Boolean = false,
    val clientName: String = "",
    val devices: List<Device> = emptyList(),
    val error: String? = null
)

