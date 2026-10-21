package com.example.androidsmartpredictparkinggihan.data.repository

import com.example.androidsmartpredictparkinggihan.data.local.UserDao
import com.example.androidsmartpredictparkinggihan.data.model.UserEntity
import com.example.androidsmartpredictparkinggihan.data.model.UserRole

class AuthRepository(private val userDao: UserDao) {

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val user = userDao.findByEmail(email.trim().lowercase())
            ?: return Result.failure(IllegalArgumentException("Account not found"))
        if (user.password != password) {
            return Result.failure(IllegalArgumentException("Incorrect password"))
        }
        return Result.success(user)
    }

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        role: UserRole,
    ): Result<UserEntity> {
        if (fullName.isBlank() || email.isBlank() || password.length < 6) {
            return Result.failure(IllegalArgumentException("Fill all fields (password min 6 characters)"))
        }
        val normalized = email.trim().lowercase()
        if (userDao.findByEmail(normalized) != null) {
            return Result.failure(IllegalArgumentException("Email already registered"))
        }
        val user = UserEntity(
            fullName = fullName.trim(),
            email = normalized,
            password = password,
            role = role,
        )
        val id = userDao.insert(user)
        return Result.success(user.copy(id = id))
    }

    suspend fun registerDriver(fullName: String, email: String, password: String): Result<UserEntity> =
        register(fullName, email, password, UserRole.USER)

    suspend fun registerHouseOwner(fullName: String, email: String, password: String): Result<UserEntity> =
        register(fullName, email, password, UserRole.HOUSE_OWNER)
}
