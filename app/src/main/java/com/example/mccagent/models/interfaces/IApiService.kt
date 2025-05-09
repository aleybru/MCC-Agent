package com.example.mccagent.models.interfaces
import com.example.mccagent.data.LoginResponse
import com.example.mccagent.data.LoginRequest

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface IApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
