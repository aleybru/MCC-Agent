package com.example.mccagent.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mccagent.viewmodels.*
import com.example.mccagent.repository.AuthRepositoryImpl
import com.example.mccagent.network.RetrofitClient
import com.example.mccagent.data.LoginRequest
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction





@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(AuthRepositoryImpl(RetrofitClient.apiService))
    )
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isProd by remember { mutableStateOf(false) }

    // ✅ Cargar preferencia inicial
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("mcc_prefs", Context.MODE_PRIVATE)
        isProd = prefs.getString("env", "DEV") == "PROD"
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("✅ Bienvenido, ${ (authState as AuthState.Success).response.user.fullname }")
                }
                viewModel.resetState()
                onLoginSuccess()
            }
            is AuthState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar("❌ ${(authState as AuthState.Error).message}")
                }
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(padding),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Email / Usuario") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    autoCorrect = false,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Switch para PROD/DEV
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Modo Producción")
                Switch(
                    checked = isProd,
                    onCheckedChange = {
                        isProd = it
                        val prefs = context.getSharedPreferences("mcc_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("env", if (isProd) "PROD" else "DEV").apply()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.login(LoginRequest(username.trim(), password))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar sesión")
            }

            if (authState is AuthState.Loading) {
                Spacer(modifier = Modifier.height(20.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

