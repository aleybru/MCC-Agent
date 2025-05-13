package com.example.mccagent.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mccagent.repository.ClientRepositoryImpl
import com.example.mccagent.network.RetrofitClient
import com.example.mccagent.viewmodels.ClientViewModel
import com.example.mccagent.viewmodels.ClientViewModelFactory
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    val viewModel: ClientViewModel = viewModel(
        factory = ClientViewModelFactory(
            ClientRepositoryImpl(context)
        )
    )
    var menuExpanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val state by viewModel.clientState.collectAsState()

    // 🔄 Lógica para cargar los datos
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
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator()
                }

                state.error != null -> {
                    Text("❌ ${state.error}", color = MaterialTheme.colorScheme.error)
                }

                else -> {
                    // 👤 Datos del cliente
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🏢 Empresa", style = MaterialTheme.typography.titleMedium)
                            Text(text = state.clientName, style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("📱 Dispositivos registrados:", style = MaterialTheme.typography.titleMedium)

                    if (state.devices.isEmpty()) {
                        Text("⚠️ No hay dispositivos registrados.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(state.devices) { device ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhoneAndroid,
                                        contentDescription = "Dispositivo",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(device.imei ?: "Sin IMEI")
                                }
                            }
                        }
                    }
                }
            }
        }

        // 🧨 Diálogo de confirmación de logout
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
    }
}

//import androidx.compose.material.icons.Icons
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.mccagent.network.RetrofitClient
//import com.example.mccagent.repository.ClientRepositoryImpl
//import com.example.mccagent.viewmodels.ClientViewModel
//import com.example.mccagent.viewmodels.ClientViewModelFactory
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HomeScreen(
//    onLogout: () -> Unit
//) {
//    val context = LocalContext.current
//    val viewModel: ClientViewModel = viewModel(
//        factory = ClientViewModelFactory(ClientRepositoryImpl(RetrofitClient.getApiService(context) as Context))
//    )
//
//    var menuExpanded by remember { mutableStateOf(false) }
//    var showLogoutDialog by remember { mutableStateOf(false) }
//
//    val state by viewModel.clientState.collectAsState()
//
//    // Cargar datos si es necesario
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
//
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
//        }
//    ) { innerPadding ->
//        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
//            if (state.isLoading) {
//                CircularProgressIndicator()
//            } else if (state.error != null) {
//                Text("❌ ${state.error}", color = MaterialTheme.colorScheme.error)
//            } else {
//                Text("👤 Cliente: ${state.clientName}", style = MaterialTheme.typography.titleMedium)
//                Spacer(Modifier.height(16.dp))
//                Text("📱 Dispositivos registrados:")
//
//                if (state.devices.isEmpty()) {
//                    Text("⚠️ No hay dispositivos registrados.")
//                } else {
//                    state.devices.forEach {
//                        Text("• $it")
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
//    }
//}
//
//
