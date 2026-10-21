package com.example.androidsmartpredictparkinggihan.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidsmartpredictparkinggihan.data.model.UserEntity
import com.example.androidsmartpredictparkinggihan.data.repository.AuthRepository
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val loggedInUser: UserEntity? = null,
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    var uiState by mutableStateOf(AuthUiState())
        private set

    fun login(email: String, password: String) {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true, error = null)
            val result = authRepository.login(email, password)
            uiState = result.fold(
                onSuccess = { user ->
                    AuthUiState(loggedInUser = user)
                },
                onFailure = { err ->
                    AuthUiState(error = err.message ?: "Login failed")
                },
            )
        }
    }

    fun registerDriver(fullName: String, email: String, password: String) {
        registerInternal { authRepository.registerDriver(fullName, email, password) }
    }

    fun registerHouseOwner(fullName: String, email: String, password: String) {
        registerInternal { authRepository.registerHouseOwner(fullName, email, password) }
    }

    private fun registerInternal(block: suspend () -> Result<com.example.androidsmartpredictparkinggihan.data.model.UserEntity>) {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true, error = null)
            val result = block()
            uiState = result.fold(
                onSuccess = { user ->
                    AuthUiState(loggedInUser = user)
                },
                onFailure = { err ->
                    AuthUiState(error = err.message ?: "Registration failed")
                },
            )
        }
    }

    fun clearSession() {
        uiState = AuthUiState()
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }
}
