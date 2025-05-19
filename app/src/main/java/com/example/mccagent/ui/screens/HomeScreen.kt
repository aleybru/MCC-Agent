package com.example.mccagent.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mccagent.repository.ClientRepositoryImpl
import com.example.mccagent.utils.registrarEsteDispositivo
import com.example.mccagent.viewmodels.ClientViewModel
import com.example.mccagent.viewmodels.ClientViewModelFactory
import com.example.mccagent.ui.components.DialogRegistrarTelefono
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val viewModel: ClientViewModel = viewModel(
        factory = ClientViewModelFactory(ClientRepositoryImpl(context))
    )

    var menuExpanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val mostrarDialogoNumero = remember { mutableStateOf(false) }
    val isRefreshing = remember { mutableStateOf(false) }

    val serviceRunning = remember {
        val prefs = context.getSharedPreferences("mcc_prefs", Context.MODE_PRIVATE)
        prefs.getBoolean("sms_service_running", false)
    }

    val state by viewModel.clientState.collectAsState()
    val currentDeviceId = remember { getCurrentDeviceId(context) }

    LaunchedEffect(Unit) {
        viewModel.loadClientInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📨 MCC Agent") },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menú")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = {
                                menuExpanded = false
                                showLogoutDialog = true
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            val deviceRegistered = state.devices.any { it.imei == currentDeviceId }
            if (!deviceRegistered) {
                FloatingActionButton(
                    onClick = {
                        registrarEsteDispositivo(
                            context = context,
                            numero = "",
                            onRequestNumero = {
                                mostrarDialogoNumero.value = true
                            },
                            onSuccess = {
                                mostrarDialogoNumero.value = false
                                viewModel.loadClientInfo()
                            },
                            onError = {
                                Log.e("Registrar", it)
                            }
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar dispositivo")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            Surface(
                color = Color(0xFFF8F8F8),
                tonalElevation = 4.dp,
                shadowElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (serviceRunning) "🟢 SMS activo" else "🔴 SMS detenido",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (serviceRunning) Color(0xFF2E7D32) else Color.Red
                    )
                }
            }
        }
    ) { innerPadding ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing.value),
            onRefresh = {
                isRefreshing.value = true
                viewModel.loadClientInfo()
                isRefreshing.value = false
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                } else if (state.error != null) {
                    Text("❌ ${state.error}", color = MaterialTheme.colorScheme.error)
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🏢 Empresa", style = MaterialTheme.typography.titleMedium)
                            Text(state.clientName, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = if (state.devices.isNotEmpty()) "🟢 ACTIVO" else "🔴 INACTIVO",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("📱 Dispositivos registrados:", style = MaterialTheme.typography.titleMedium)

                    if (state.devices.isEmpty()) {
                        Text("⚠️ No hay dispositivos registrados.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(state.devices) { device ->
                                val isCurrent = device.imei == currentDeviceId
                                val border = if (isCurrent) BorderStroke(2.dp, Color.Green) else null
                                val bgColor = if (isCurrent) Color(0xFFDFFFE0) else Color.Transparent

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    border = border,
                                    colors = CardDefaults.cardColors(containerColor = bgColor)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.PhoneAndroid,
                                            contentDescription = "Dispositivo",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(device.name)
                                            Text("IMEI: ${device.imei}")
                                            if (isCurrent) {
                                                Text("📍 Este dispositivo", color = Color.Green)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("¿Cerrar sesión?") },
                text = { Text("¿Estás seguro que querés cerrar sesión y detener el servicio de mensajería?") },
                confirmButton = {
                    TextButton(onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }) {
                        Text("Sí, cerrar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (mostrarDialogoNumero.value) {
            DialogRegistrarTelefono(
                onDismiss = { mostrarDialogoNumero.value = false },
                onSuccess = {
                    mostrarDialogoNumero.value = false
                    viewModel.loadClientInfo()
                }
            )
        }
    }
}

@SuppressLint("HardwareIds")
fun getCurrentDeviceId(context: Context): String {
    return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
}

//package com.example.mccagent.ui.screens
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.provider.Settings
//import android.util.Log
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.MoreVert
//import androidx.compose.material.icons.outlined.PhoneAndroid
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.mccagent.repository.ClientRepositoryImpl
//import com.example.mccagent.utils.registrarEsteDispositivo
//import com.example.mccagent.viewmodels.ClientViewModel
//import com.example.mccagent.viewmodels.ClientViewModelFactory
//import com.example.mccagent.ui.components.DialogRegistrarTelefono
//import com.google.accompanist.swiperefresh.SwipeRefresh
//import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HomeScreen(
//    onLogout: () -> Unit
//) {
//    val context = LocalContext.current
//    val isRefreshing = remember { mutableStateOf(false) }
//
//    val viewModel: ClientViewModel = viewModel(
//        factory = ClientViewModelFactory(
//            ClientRepositoryImpl(context)
//        )
//    )
//    var menuExpanded by remember { mutableStateOf(false) }
//    var showLogoutDialog by remember { mutableStateOf(false) }
//    val mostrarDialogoNumero = remember { mutableStateOf(false) }
//
//    val serviceRunning = remember {
//        val prefs = context.getSharedPreferences("mcc_prefs", Context.MODE_PRIVATE)
//        prefs.getBoolean("sms_service_running", false)
//    }
//
//    val state by viewModel.clientState.collectAsState()
//    val currentDeviceId = remember { getCurrentDeviceId(context) }
//
//    LaunchedEffect(Unit) {
//        viewModel.loadClientInfo()
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("📨 MCC Agent") },
//                actions = {
//                    IconButton(onClick = { menuExpanded = true }) {
//                        Icon(Icons.Default.MoreVert, contentDescription = "Menú")
//                    }
//                    DropdownMenu(
//                        expanded = menuExpanded,
//                        onDismissRequest = { menuExpanded = false }
//                    ) {
//                        DropdownMenuItem(
//                            text = { Text("Cerrar sesión") },
//                            onClick = {
//                                menuExpanded = false
//                                showLogoutDialog = true
//                            }
//                        )
//                    }
//                }
//            )
//        },
//        floatingActionButton = {
//            val deviceRegistered = state.devices.any { it.imei == currentDeviceId }
//            if (!deviceRegistered) {
//                FloatingActionButton(
//                    onClick = {
//                        registrarEsteDispositivo(
//                            context = context,
//                            numero = "",
//                            onRequestNumero = {
//                                mostrarDialogoNumero.value = true
//                            },
//                            onSuccess = {
//                                mostrarDialogoNumero.value = false
//                                viewModel.loadClientInfo()
//                            },
//                            onError = {
//                                Log.e("Registrar", it)
//                            }
//                        )
//                    },
//                    containerColor = MaterialTheme.colorScheme.primary
//                ) {
//                    Icon(Icons.Default.Add, contentDescription = "Agregar dispositivo")
//                }
//            }
//        },
//        floatingActionButtonPosition = FabPosition.Center,
//        bottomBar = {
//            Surface(
//                color = Color(0xFFF8F8F8),
//                tonalElevation = 4.dp,
//                shadowElevation = 6.dp
//            ) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(100.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = if (serviceRunning) "🟢 SMS activo"
//                        else "🔴 SMS detenido",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = if (serviceRunning) Color(0xFF2E7D32) else Color.Red
//                    )
//                }
//            }
//        }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .padding(innerPadding)
//                .padding(16.dp)
//        ) {
//            if (state.isLoading) {
//                CircularProgressIndicator()
//            } else if (state.error != null) {
//                Text("❌ ${state.error}", color = MaterialTheme.colorScheme.error)
//            } else {
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE))
//                ) {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Text("🏢 Empresa", style = MaterialTheme.typography.titleMedium)
//                        Text(state.clientName, style = MaterialTheme.typography.bodyLarge)
//                        Text(
//                            text = if (state.devices.isNotEmpty()) "🟢 ACTIVO" else "🔴 INACTIVO",
//                            style = MaterialTheme.typography.bodyMedium
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//                Text("📱 Dispositivos registrados:", style = MaterialTheme.typography.titleMedium)
//
//                if (state.devices.isEmpty()) {
//                    Text("⚠️ No hay dispositivos registrados.")
//                } else {
//                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
//                        items(state.devices) { device ->
//                            val isCurrent = device.imei == currentDeviceId
//                            val border = if (isCurrent) BorderStroke(2.dp, Color.Green) else null
//                            val bgColor = if (isCurrent) Color(0xFFDFFFE0) else Color.Transparent
//
//                            Card(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(vertical = 6.dp),
//                                border = border,
//                                colors = CardDefaults.cardColors(containerColor = bgColor)
//                            ) {
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    modifier = Modifier.padding(12.dp)
//                                ) {
//                                    Icon(
//                                        imageVector = Icons.Outlined.PhoneAndroid,
//                                        contentDescription = "Dispositivo",
//                                        tint = MaterialTheme.colorScheme.primary
//                                    )
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    Column {
//                                        Text(device.name)
//                                        Text("IMEI: ${device.imei}")
//                                        if (isCurrent) {
//                                            Text("📍 Este dispositivo", color = Color.Green)
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        if (showLogoutDialog) {
//            AlertDialog(
//                onDismissRequest = { showLogoutDialog = false },
//                title = { Text("¿Cerrar sesión?") },
//                text = { Text("¿Estás seguro que querés cerrar sesión y detener el servicio de mensajería?") },
//                confirmButton = {
//                    TextButton(onClick = {
//                        showLogoutDialog = false
//                        onLogout()
//                    }) {
//                        Text("Sí, cerrar", color = MaterialTheme.colorScheme.error)
//                    }
//                },
//                dismissButton = {
//                    TextButton(onClick = { showLogoutDialog = false }) {
//                        Text("Cancelar")
//                    }
//                }
//            )
//        }
//
//        if (mostrarDialogoNumero.value) {
//            DialogRegistrarTelefono(
//                onDismiss = { mostrarDialogoNumero.value = false },
//                onSuccess = {
//                    mostrarDialogoNumero.value = false
//                    viewModel.loadClientInfo()
//                }
//            )
//        }
//    }
//}
//
//@SuppressLint("HardwareIds")
//fun getCurrentDeviceId(context: Context): String {
//    return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
//}