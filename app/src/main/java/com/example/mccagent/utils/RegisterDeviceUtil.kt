package com.example.mccagent.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings

import com.example.mccagent.data.DeviceRegisterRequest
import com.example.mccagent.network.RetrofitClient
import kotlinx.coroutines.*


@SuppressLint("HardwareIds")
fun registrarEsteDispositivo(
    context: Context,
    numero: String?,
    onRequestNumero: () -> Unit = {},
    onSuccess: () -> Unit,
    onError: (String) -> Unit
)
 {
    if (numero.isNullOrBlank()) {
        onRequestNumero()
        return
    }

    val imei = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    val name = Build.MODEL

    val api = RetrofitClient.getApiService(context)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = api.registerDevice(
                DeviceRegisterRequest(
                    name = name,
                    phone = numero,
                    imei = imei,
                    platform = "ANDROID",
                    notes = "Registrado automáticamente"
                )
            )

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                    onError("Registro fallido: $errorMsg")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Excepción: ${e.message}")
            }
        }
    }
}
