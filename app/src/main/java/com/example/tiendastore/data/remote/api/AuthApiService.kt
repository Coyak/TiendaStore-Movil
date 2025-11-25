package com.example.tiendastore.data.remote.api

import com.example.tiendastore.data.remote.dto.AuthDtos.AuthResponse
import com.example.tiendastore.data.remote.dto.AuthDtos.LoginRequest
import com.example.tiendastore.data.remote.dto.AuthDtos.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("/api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("/api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse
}
