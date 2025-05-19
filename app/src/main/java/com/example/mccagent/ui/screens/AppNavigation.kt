package com.example.mccagent.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.mccagent.MainActivity
import com.example.mccagent.services.SMSService

@Composable
fun AppNavigation(context: Context) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("home") {
            HomeScreen(onLogout = {
                // Logout que también navega
                val prefs = context.getSharedPreferences("mcc_prefs", Context.MODE_PRIVATE)
                prefs.edit().remove("token").apply()

                val stopIntent = Intent(context, SMSService::class.java)
                context.stopService(stopIntent)

                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            })
        }
    }
}
