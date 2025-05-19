package com.example.mccagent.config

object ApiConfig {

    enum class Environment {
        DEV, PROD
    }

    private val currentEnv = Environment.DEV

    val BASE_URL: String
        get() = when (currentEnv) {
            Environment.DEV -> "http://192.168.1.16:5000/api/"
            Environment.PROD -> "https://tu-backend-real.com/api/"
        }
}
