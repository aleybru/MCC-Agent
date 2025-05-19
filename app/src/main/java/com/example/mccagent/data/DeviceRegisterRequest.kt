package com.example.mccagent.data

data class DeviceRegisterRequest(
    val name: String,
    val phone: String,
    val imei: String,
    val platform: String,
    val notes: String? = null
)
