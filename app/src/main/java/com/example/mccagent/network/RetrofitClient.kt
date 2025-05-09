package com.example.mccagent.network

import com.example.mccagent.models.interfaces.IApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.mccagent.config.ApiConfig
import java.net.URL

object RetrofitClient {

    //private const val BASE_URL = "http://localhost:5000/api/" // 🔧 Cambiar por IP real del server o usar `10.0.2.2` en emulador
    private val BASE_URL = URL(ApiConfig.BASE_URL)

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: IApiService by lazy {
        retrofit.create(IApiService::class.java)
    }
}
