package com.example.mccagent

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.example.mccagent.ui.theme.MCCAgentTheme
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.compose.ui.tooling.preview.Preview
import com.example.mccagent.ui.screens.AppNavigation


class MainActivity : ComponentActivity() {
    private val permisos = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.FOREGROUND_SERVICE
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)


        requestPermissionsIfNeeded()

        setContent {
            MCCAgentTheme {
                AppNavigation(this@MainActivity)
            }
        }
    }
    private fun requestPermissionsIfNeeded() {
        val notGranted = permisos.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            requestPermissionLauncher.launch(notGranted.toTypedArray())
        } else {
            iniciarServicioSMS()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.all { it.value }) {
            iniciarServicioSMS()
        } else {
            Toast.makeText(this, "🚫 Permisos denegados", Toast.LENGTH_LONG).show()
        }
    }

    private fun iniciarServicioSMS() {
        val intent = Intent(this, SMSService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    fun handleLogout(context: Context) {
        // Limpiar el token
        val prefs = context.getSharedPreferences("mcc_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("token").apply()

        // Detener el servicio
        val stopIntent = Intent(context, SMSService::class.java)
        context.stopService(stopIntent)

        // Navegar a login (si usás navegación programática)
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}



