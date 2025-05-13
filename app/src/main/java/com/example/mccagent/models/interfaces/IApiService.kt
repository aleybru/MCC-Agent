package com.example.mccagent.models.interfaces
import com.example.mccagent.data.LoginResponse
import com.example.mccagent.data.LoginRequest
import com.example.mccagent.models.entities.Client
import com.example.mccagent.models.entities.Device

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface IApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/clients/me")
    suspend fun getClient(): Response<Client>

    @GET("api/devices")
    suspend fun getDevices(): Response<List<Device>>

}
