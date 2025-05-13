package com.example.mccagent.network

import android.content.Context
import com.example.mccagent.models.interfaces.IApiService
import com.example.mccagent.config.ApiConfig
import com.example.mccagent.repository.ClientRepositoryImpl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {

    fun getApiService(context: Context): IApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val prefs = context.getSharedPreferences("mcc_prefs", Context.MODE_PRIVATE)
                val token = prefs.getString("token", null)

                val request = chain.request().newBuilder()
                    .apply {
                        if (!token.isNullOrEmpty()) {
                            addHeader("Authorization", "Bearer $token")
                        }
                    }
                    .build()

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
}
