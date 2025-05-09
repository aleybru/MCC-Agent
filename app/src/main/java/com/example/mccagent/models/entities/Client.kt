package com.example.mccagent.models.entities


data class Client(
    val cid: String,
    val name: String,
    val contact_email: String,
    val phone: String,
    val address: String,
    val status: Boolean
)
