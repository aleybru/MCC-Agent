package com.example.mccagent.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mccagent.data.LoginRequest
import com.example.mccagent.model.interfaces.IAuthRepository
import com.example.mccagent.data.LoginResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val response: LoginResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: IAuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response: Response<LoginResponse> = repository.login(request)
                if (response.isSuccessful && response.body() != null) {
                    _authState.value = AuthState.Success(response.body()!!)
                } else {
                    _authState.value = AuthState.Error("Credenciales inválidas o error del servidor")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Excepción: ${e.message}")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
