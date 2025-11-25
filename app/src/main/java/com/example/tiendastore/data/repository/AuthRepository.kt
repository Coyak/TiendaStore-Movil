package com.example.tiendastore.data.repository

import android.content.Context
import com.example.tiendastore.data.remote.AuthTokenStore
import com.example.tiendastore.data.remote.RemoteAuthDataSource
import com.example.tiendastore.data.remote.dto.AuthDtos
import com.example.tiendastore.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(
    private val appContext: Context,
    private val remote: RemoteAuthDataSource = RemoteAuthDataSource()
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    init {
        AuthTokenStore.init(appContext)
        _token.value = AuthTokenStore.get()
    }

    suspend fun login(email: String, password: String): Result<User> = runCatching {
        val res = remote.login(email, password)
        updateSession(res)
        _currentUser.value!!
    }

    suspend fun register(name: String, email: String, password: String, isAdmin: Boolean): Result<User> = runCatching {
        val res = remote.register(name, email, password, isAdmin)
        updateSession(res)
        _currentUser.value!!
    }

    fun logout() {
        _currentUser.value = null
        _token.value = null
        AuthTokenStore.set(appContext, null)
    }

    private fun updateSession(res: AuthDtos.AuthResponse) {
        _token.value = res.token
        AuthTokenStore.set(appContext, res.token)
        _currentUser.value = User(
            username = res.email,
            password = "",
            isAdmin = res.isAdmin,
            name = res.name,
            email = res.email,
            address = "",
            city = ""
        )
    }
}
