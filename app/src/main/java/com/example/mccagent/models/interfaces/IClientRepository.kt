package com.example.mccagent.models.interfaces

import com.example.mccagent.models.entities.Client
import com.example.mccagent.models.entities.Device
import retrofit2.Response

interface IClientRepository {
    suspend fun getClient(): Response<Client>
    suspend fun getDevices(): Response<List<Device>>
}
