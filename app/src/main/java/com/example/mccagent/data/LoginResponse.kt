package com.example.mccagent.data
import com.example.mccagent.models.entities.UserData


data class LoginResponse(
    val token: String,
    val user: UserData
)

