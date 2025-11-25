package com.example.tiendastore.data.remote.dto

object AuthDtos {
    data class LoginRequest(val email: String, val password: String)
    data class RegisterRequest(val name: String, val email: String, val password: String, val isAdmin: Boolean)
    data class AuthResponse(
        val token: String,
        val userId: Long,
        val name: String,
        val email: String,
        val isAdmin: Boolean
    )
}
