package com.example.tiendastore.data.remote

import com.example.tiendastore.data.remote.api.AuthApiService
import com.example.tiendastore.data.remote.dto.AuthDtos

class RemoteAuthDataSource(
    private val api: AuthApiService = RetrofitProvider.authApi
) {
    suspend fun login(email: String, password: String): AuthDtos.AuthResponse =
        api.login(AuthDtos.LoginRequest(email = email, password = password))

    suspend fun register(name: String, email: String, password: String, isAdmin: Boolean): AuthDtos.AuthResponse =
        api.register(AuthDtos.RegisterRequest(name = name, email = email, password = password, isAdmin = isAdmin))
}
