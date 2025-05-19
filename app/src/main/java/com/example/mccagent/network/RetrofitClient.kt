package com.example.mccagent.network

import android.content.Context
import android.util.Log
import com.example.mccagent.config.ApiConfig
import com.example.mccagent.models.interfaces.IApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    fun getApiService(context: Context): IApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val prefs = context.getSharedPreferences("mcc_prefs", Context.MODE_PRIVATE)
                val token = prefs.getString("token", null)
                Log.d("RetrofitClient", "\uD83E\uDDEA Token utilizado: $token")

                val request = chain.request().newBuilder().apply {
                    if (!token.isNullOrEmpty()) {
                        addHeader("Authorization", "Bearer $token")
                    }
                }.build()

                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IApiService::class.java)
    }

    fun getApiWithValidToken(context: Context): IApiService {
        val prefs = context.getSharedPreferences("mcc_prefs", Context.MODE_PRIVATE)
        var token = prefs.getString("token", null)

        if (token.isNullOrBlank() || isTokenExpired(token)) {
            token = renewToken(context)
            if (!token.isNullOrBlank()) {
                prefs.edit().putString("token", token).apply()
                Log.d("RetrofitClient", "\uD83D\uDD10 Token renovado y guardado: $token")
            } else {
                Log.e("RetrofitClient", "\u274C No se pudo renovar el token")
            }
        }

        return getApiService(context)
    }

    private fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
            val regex = """"exp"\s*:\s*(\d+)""".toRegex()

            val match = regex.find(payload) ?: return true
            val exp = match.groupValues[1].toLong()
            val now = System.currentTimeMillis() / 1000
            exp < now
        } catch (e: Exception) {
            true
        }
    }

    private fun renewToken(context: Context): String? {
        return try {
            val prefs = context.getSharedPreferences("mcc_prefs", Context.MODE_PRIVATE)
            val oldToken = prefs.getString("token", null)
            val url = java.net.URL("${ApiConfig.BASE_URL}auth/renew")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $oldToken")
            conn.connectTimeout = 5000

            val response = try {
                conn.inputStream.bufferedReader().readText()
            } catch (e: Exception) {
                conn.errorStream?.bufferedReader()?.readText() ?: throw e
            }

            val regex = """"token"\s*:\s*"(.+?)""".toRegex()
            val match = regex.find(response) ?: return null
            match.groupValues[1]
        } catch (e: Exception) {
            Log.e("RetrofitClient", "\uD83D\uDCA5 Error al renovar token: ${e.message}", e)
            return null
        }
    }
}
