package com.example.mccagent.repository

import android.content.Context
import com.example.mccagent.models.entities.Client
import com.example.mccagent.models.entities.Device
import com.example.mccagent.models.interfaces.IClientRepository
import com.example.mccagent.models.interfaces.IApiService
import com.example.mccagent.network.RetrofitClient
import retrofit2.Response

class ClientRepositoryImpl(context: Context) : IClientRepository {
    private val api: IApiService = RetrofitClient.getApiService(context)

    override suspend fun getClient(): Response<Client> = api.getClient()
    override suspend fun getDevices(): Response<List<Device>> = api.getDevices()
}


