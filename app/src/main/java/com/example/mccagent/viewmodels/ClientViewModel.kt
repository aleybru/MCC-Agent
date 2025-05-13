package com.example.mccagent.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mccagent.repository.ClientRepositoryImpl
import com.example.mccagent.models.entities.Device
import com.example.mccagent.models.interfaces.IClientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClientViewModel(private val repository: IClientRepository) : ViewModel() {

    private val _clientState = MutableStateFlow(ClientState())
    val clientState: StateFlow<ClientState> = _clientState

    fun loadClientInfo() {
        viewModelScope.launch {
            _clientState.value = _clientState.value.copy(isLoading = true)

            try {
                val clientResponse = repository.getClient()
                val devicesResponse = repository.getDevices()

                if (clientResponse.isSuccessful && devicesResponse.isSuccessful) {
                    val clientName = clientResponse.body()?.name ?: "Desconocido"
                    val deviceList: List<Device> = devicesResponse.body() ?: emptyList()

                    _clientState.value = ClientState(
                        isLoading = false,
                        clientName = clientName,
                        devices = deviceList
                    )
                } else {
                    _clientState.value = ClientState(
                        isLoading = false,
                        error = "Error al obtener datos del servidor"
                    )
                }
            } catch (e: Exception) {
                _clientState.value = ClientState(
                    isLoading = false,
                    error = "Error inesperado: ${e.message}"
                )
            }
        }
    }
}
