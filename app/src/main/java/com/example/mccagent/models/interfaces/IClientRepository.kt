package com.example.mccagent.models.interfaces

import com.example.mccagent.data.ClientWithDevicesResponse
import com.example.mccagent.models.entities.Client
import com.example.mccagent.models.entities.Device
import retrofit2.Response

interface IClientRepository {
    suspend fun getClient(): Response<Client>
    suspend fun getClientWithDevices(): Response<ClientWithDevicesResponse>
    suspend fun getDevices(): Response<List<Device>>
}
