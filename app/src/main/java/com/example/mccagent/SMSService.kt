package com.example.mccagent

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import com.example.mccagent.config.ApiConfig

class SMSService : Service() {

    private val CHANNEL_ID = "MCCAgentSMSChannel"
    private val BACKEND_URL = URL("${ApiConfig.BASE_URL}messages/pending") // Cambiar si estás en prod
   // private val TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1aWQiOiI2ODE1MTRlMDk5ZjIxNWYwYzY2MDRkYjkiLCJpYXQiOjE3NDY4MTUyMjIsImV4cCI6MTc0NjgyOTYyMn0.-2F6Ym6EiCInZqEZnj7OoLhsCIMny760ykVq0Cg4eFo"

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MCC Agent activo")
            .setContentText("Enviando mensajes SMS pendientes")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)

        scope.launch {
            while (true) {
                try {
                    val messages = getPendingMessages()
                    messages.forEach {
                        sendSMS(it.recipient, it.body)
                        reportAsSent(it.mid)
                    }
                } catch (e: Exception) {
                    Log.e("MCCAgent", "Error: ${e.message}")
                }
                delay(10000) // Reintento cada 10 segundos
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "MCC Agent SMS Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun getPendingMessages(): List<Message> {
        var token = getToken(this) ?: throw Exception("Token no encontrado")

        if (isTokenExpired(token)) {
            val renewed = renewToken(this)
            token = renewed ?: throw Exception("No se pudo renovar el token")
        }

        val url = URL(BACKEND_URL.toString())
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.connectTimeout = 5000

        val response = connection.inputStream.bufferedReader().readText()
        val jsonArray = JSONArray(response)

        val list = mutableListOf<Message>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                Message(
                    mid = obj.getString("mid"),
                    recipient = obj.getString("recipient"),
                    body = obj.getString("body")
                )
            )
        }
        return list
    }


    private fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences("mcc_prefs", Context.MODE_PRIVATE)
        return prefs.getString("token", null)
    }

    fun saveToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences("mcc_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("token", token).apply()
    }

    private fun renewToken(context: Context): String? {
        try {
            val url = URL("${ApiConfig.BASE_URL}auth/renew")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer ${getToken(context)}")
            conn.connectTimeout = 5000

            val response = conn.inputStream.bufferedReader().readText()
            val regex = """"token"\s*:\s*"(.+?)"""".toRegex()
            val match = regex.find(response) ?: return null
            val newToken = match.groupValues[1]
            saveToken(context, newToken)
            return newToken
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    private fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
            val regex = """"exp":\s*(\d+)""".toRegex()
            val match = regex.find(payload) ?: return true
            val exp = match.groupValues[1].toLong()
            val now = System.currentTimeMillis() / 1000
            exp < now
        } catch (e: Exception) {
            true
        }
    }


    private fun sendSMS(phone: String, body: String) {
        val sms = SmsManager.getDefault()
        sms.sendTextMessage(phone, null, body, null, null)
        Log.i("MCCAgent", "SMS enviado a $phone")
    }

    private fun reportAsSent(mid: String) {
        // Podés hacer un POST al backend para decir que se envió
    }

    data class Message(val mid: String, val recipient: String, val body: String)
}