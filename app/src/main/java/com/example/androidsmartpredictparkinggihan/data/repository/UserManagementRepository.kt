package com.example.androidsmartpredictparkinggihan.data.repository

import com.example.androidsmartpredictparkinggihan.data.local.UserDao
import com.example.androidsmartpredictparkinggihan.data.model.UserEntity
import com.example.androidsmartpredictparkinggihan.data.model.UserRole

class UserManagementRepository(private val userDao: UserDao) {

    suspend fun listUsers(): List<UserEntity> = userDao.getAll()

    suspend fun createUser(
        fullName: String,
        email: String,
        password: String,
        role: UserRole,
    ): Result<UserEntity> {
        if (fullName.isBlank() || email.isBlank() || password.length < 6) {
            return Result.failure(IllegalArgumentException("Name, email, and password (min 6) required"))
        }
        val normalized = email.trim().lowercase()
        if (userDao.findByEmail(normalized) != null) {
            return Result.failure(IllegalArgumentException("Email already exists"))
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

    suspend fun updateUser(
        userId: Long,
        fullName: String,
        email: String,
        password: String?,
        role: UserRole,
        actingAdminId: Long,
    ): Result<UserEntity> {
        val existing = userDao.findById(userId)
            ?: return Result.failure(IllegalArgumentException("User not found"))
        val normalized = email.trim().lowercase()
        val emailTaken = userDao.findByEmail(normalized)
        if (emailTaken != null && emailTaken.id != userId) {
            return Result.failure(IllegalArgumentException("Email already used by another account"))
        }
        if (existing.id == actingAdminId && role != UserRole.ADMIN) {
            return Result.failure(IllegalArgumentException("You cannot remove your own admin role"))
        }
        val updated = existing.copy(
            fullName = fullName.trim(),
            email = normalized,
            password = if (password.isNullOrBlank()) existing.password else password,
            role = role,
        )
        userDao.update(updated)
        return Result.success(updated)
    }

    suspend fun deleteUser(userId: Long, actingAdminId: Long): Result<Unit> {
        if (userId == actingAdminId) {
            return Result.failure(IllegalArgumentException("You cannot delete your own admin account"))
        }
        val user = userDao.findById(userId)
            ?: return Result.failure(IllegalArgumentException("User not found"))
        userDao.delete(user)
        return Result.success(Unit)
    }
}
