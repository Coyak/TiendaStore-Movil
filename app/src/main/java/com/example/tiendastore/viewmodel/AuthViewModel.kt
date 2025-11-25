package com.example.tiendastore.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendastore.data.repository.AuthRepository
import com.example.tiendastore.domain.validation.AuthValidator
import com.example.tiendastore.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.tiendastore.data.remote.AuthTokenStore

data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val errors: Map<String, String> = emptyMap(),
    val message: String? = null
)

class AuthViewModel(
    application: Application,
    private val repo: AuthRepository = AuthRepository(application.applicationContext)
) : AndroidViewModel(application) {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _profile = MutableStateFlow<User?>(null)
    val profile: StateFlow<User?> = _profile.asStateFlow()

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    init {
        // Inicializa token persistido
        AuthTokenStore.init(application.applicationContext)
        viewModelScope.launch {
            repo.currentUser.collect { user ->
                _currentUser.value = user?.copy(password = "")
                _profile.value = user
            }
        }
    }

    fun clearMessages() {
        _ui.value = _ui.value.copy(errors = emptyMap(), message = null)
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val errors = AuthValidator.validateLogin(email, password)
            if (errors.isNotEmpty()) {
                _ui.value = _ui.value.copy(errors = errors, message = null)
                return@launch
            }
            val result = repo.login(email.trim(), password)
            if (result.isSuccess) {
                _ui.value = _ui.value.copy(errors = emptyMap(), message = null)
            } else {
                _ui.value = _ui.value.copy(message = "Credenciales inválidas", errors = emptyMap())
            }
        }
    }

    fun register(name: String, email: String, password: String, isAdmin: Boolean) {
        viewModelScope.launch {
            val n = name.trim()
            val e = email.trim()
            val errors = AuthValidator.validateRegister(n, e, password, password).toMutableMap()
            if (errors.isNotEmpty()) {
                _ui.value = _ui.value.copy(errors = errors, message = null)
                return@launch
            }
            val result = repo.register(n, e, password, isAdmin)
            if (result.isSuccess) {
                _ui.value = _ui.value.copy(errors = emptyMap(), message = "Cuenta creada, ahora ingresa")
            } else {
                _ui.value = _ui.value.copy(message = result.exceptionOrNull()?.message ?: "Error al registrar", errors = emptyMap())
            }
        }
    }

    fun updateProfile(name: String, email: String, address: String, city: String) {
        // Sin backend específico para perfil, solo se actualiza en memoria para la sesión.
        val current = _profile.value ?: return
        val errors = mutableMapOf<String, String>()
        val n = name.trim()
        val e = email.trim()
        if (n.length < 3) errors["name"] = "Nombre mínimo 3 caracteres"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches()) errors["email"] = "Correo inválido"
        if (errors.isNotEmpty()) {
            _ui.value = _ui.value.copy(errors = errors)
            return
        }
        val updated = current.copy(name = n, email = e, address = address.trim(), city = city.trim())
        _profile.value = updated
        _currentUser.value = updated.copy(password = "")
        _ui.value = _ui.value.copy(message = "Perfil actualizado (local)")
    }

    fun logout() {
        repo.logout()
    }
}
