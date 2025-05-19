package com.example.mccagent.repository

import android.content.Context
import android.util.Log
import com.example.mccagent.data.MessageStatusUpdateRequest
import com.example.mccagent.models.interfaces.IMessageRepository
import com.example.mccagent.models.entities.Message
import com.example.mccagent.models.interfaces.IApiService
import com.example.mccagent.network.RetrofitClient
import retrofit2.Response

class MessageRepositoryImpl(private val context: Context) : IMessageRepository {

    val api = RetrofitClient.getApiWithValidToken(context)


    override suspend fun getPendingMessages(): List<Message> {
        return try {
            val response = api.getPendingMessages()
            if (response.isSuccessful) {
                response.body()?.messages ?: emptyList()
            } else {
                Log.e("MessageRepo", "❌ Error al obtener mensajes: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MessageRepo", "💥 Excepción al obtener mensajes: ${e.message}")
            emptyList()
        }
    }

    override suspend fun updateMessageStatus(mid: String, status: String): Boolean {
        return try {
            val response = api.updateMessageStatus(mid, MessageStatusUpdateRequest(status))
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("MessageRepo", "💥 Excepción al actualizar estado: ${e.message}")
            false
        }
    }
}


