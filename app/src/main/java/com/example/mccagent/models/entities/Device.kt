package com.example.mccagent.models.entities

data class Device(
    val did: String,
    val deviceId: String,
    val name: String,
    val phone: String,
    val platform: String,
    val client: Client,
    val notes: String,
    val status: Boolean
)