package com.example.androidsmartpredictparkinggihan.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidsmartpredictparkinggihan.data.model.UserEntity
import com.example.androidsmartpredictparkinggihan.data.model.UserRole
import com.example.androidsmartpredictparkinggihan.data.repository.UserManagementRepository
import kotlinx.coroutines.launch

data class UserFormState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val role: UserRole = UserRole.USER,
)

data class UserManagementUiState(
    val loading: Boolean = true,
    val users: List<UserEntity> = emptyList(),
    val error: String? = null,
    val message: String? = null,
    val showEditor: Boolean = false,
    val editingUserId: Long? = null,
    val form: UserFormState = UserFormState(),
)

class UserManagementViewModel(
    private val adminId: Long,
    private val repository: UserManagementRepository,
) : ViewModel() {
    var uiState by mutableStateOf(UserManagementUiState())
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true, error = null)
            uiState = uiState.copy(loading = false, users = repository.listUsers())
        }
    }

    fun openCreate() {
        uiState = uiState.copy(
            showEditor = true,
            editingUserId = null,
            form = UserFormState(),
            error = null,
            message = null,
        )
    }

    fun openEdit(user: UserEntity) {
        uiState = uiState.copy(
            showEditor = true,
            editingUserId = user.id,
            form = UserFormState(
                fullName = user.fullName,
                email = user.email,
                password = "",
                role = user.role,
            ),
            error = null,
            message = null,
        )
    }

    fun closeEditor() {
        uiState = uiState.copy(showEditor = false, editingUserId = null, error = null)
    }

    fun updateFormName(value: String) {
        uiState = uiState.copy(form = uiState.form.copy(fullName = value))
    }

    fun updateFormEmail(value: String) {
        uiState = uiState.copy(form = uiState.form.copy(email = value))
    }

    fun updateFormPassword(value: String) {
        uiState = uiState.copy(form = uiState.form.copy(password = value))
    }

    fun updateFormRole(role: UserRole) {
        uiState = uiState.copy(form = uiState.form.copy(role = role))
    }

    fun saveUser() {
        val form = uiState.form
        viewModelScope.launch {
            val editing = uiState.editingUserId
            val result = if (editing == null) {
                repository.createUser(form.fullName, form.email, form.password, form.role)
            } else {
                repository.updateUser(
                    userId = editing,
                    fullName = form.fullName,
                    email = form.email,
                    password = form.password.ifBlank { null },
                    role = form.role,
                    actingAdminId = adminId,
                )
            }
            result.fold(
                onSuccess = {
                    uiState = uiState.copy(
                        showEditor = false,
                        editingUserId = null,
                        users = repository.listUsers(),
                        message = if (editing == null) "User created" else "User updated",
                        error = null,
                    )
                },
                onFailure = { err ->
                    uiState = uiState.copy(error = err.message ?: "Save failed")
                },
            )
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            val result = repository.deleteUser(userId, adminId)
            uiState = result.fold(
                onSuccess = {
                    uiState.copy(
                        users = repository.listUsers(),
                        message = "User deleted",
                        error = null,
                    )
                },
                onFailure = { err ->
                    uiState.copy(error = err.message ?: "Delete failed")
                },
            )
        }
    }
}
