package com.example.mccagent.services

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.mccagent.R
import com.example.mccagent.models.entities.Message
import com.example.mccagent.repository.MessageRepositoryImpl
import kotlinx.coroutines.*

class SMSService : Service() {

    private val CHANNEL_ID = "MCCAgentSMSChannel"
    private lateinit var messageRepository: MessageRepositoryImpl
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        messageRepository = MessageRepositoryImpl(this)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MCC Agent activo")
            .setContentText("Enviando mensajes SMS pendientes")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)

        ContextCompat.registerReceiver(
            this,
            smsSentReceiver,
            IntentFilter("SMS_SENT"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        scope.launch {
            while (true) {
                try {
                    val messages = messageRepository.getPendingMessages()
                    Log.d("SMSService", "\uD83D\uDCE5 Mensajes obtenidos: ${messages.size}")
                    val packageManager = packageManager
                    val hasSms = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
                    Log.d("SMSService", "¿Puede enviar SMS? $hasSms")
                    messages.forEach {
                        Log.d("SMSService", "➡ Enviando SMS a ${it.recipient} con mid: ${it.mid}")
                        sendSMS(it.mid, it.recipient, it.body)
                        delay(2000)
                    }
                } catch (e: Exception) {
                    Log.e("MCCAgent", "Error: ${e.message}")
                }
                delay(10000)
            }
        }

        getSharedPreferences("mcc_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("sms_service_running", true).apply()
    }

    override fun onDestroy() {
        unregisterReceiver(smsSentReceiver)
        scope.cancel()
        getSharedPreferences("mcc_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("sms_service_running", false).apply()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "MCC Agent SMS Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun sendSMS(mid: String, phone: String, body: String) {
        val intent = Intent("SMS_SENT").putExtra("mid", mid)
        val sentIntent = PendingIntent.getBroadcast(
            this, mid.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            Log.d("SMSService", "\uD83D\uDCEC Preparando envío a: $phone | Contenido: $body")
            Log.d("SMSService", "Intentando enviar a $phone con texto: $body")

            SmsManager.getDefault().sendTextMessage(phone, null, body, sentIntent, null)
            Log.i("MCCAgent", "\uD83D\uDCE4 Enviando SMS a $phone (pendiente confirmación)")
            Log.d("SMSService", "Permiso SEND_SMS: ${
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            }")
        } catch (e: Exception) {
            Log.e("MCCAgent", "❌ Error al enviar SMS: ${e.message}")
        }
    }

    private val smsSentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val mid = intent.getStringExtra("mid") ?: run {
                Log.e("SMSService", "❌ MID faltante en Intent")
                return
            }
            val resultado = resultCode
            val status = when (resultado) {
                Activity.RESULT_OK -> "ENVIADO"
                SmsManager.RESULT_ERROR_GENERIC_FAILURE,
                SmsManager.RESULT_ERROR_NO_SERVICE,
                SmsManager.RESULT_ERROR_NULL_PDU,
                SmsManager.RESULT_ERROR_RADIO_OFF -> "FALLIDO"
                else -> "FALLIDO"
            }

            Log.d("SMSService", "\uD83D\uDCE1 resultCode recibido: $resultCode → status: $status")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val repo = MessageRepositoryImpl(context)
                    val result = repo.updateMessageStatus(mid, status)
                    Log.i("SMSService", "✅ Estado actualizado a '$status' para mensaje $mid: $result")
                } catch (e: Exception) {
                    Log.e("SMSService", "\uD83D\uDCA5 Error en updateMessageStatus: ${e.message}", e)
                }
            }
        }
    }
}
