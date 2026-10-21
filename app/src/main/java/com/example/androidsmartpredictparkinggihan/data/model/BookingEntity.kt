package com.example.androidsmartpredictparkinggihan.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PaymentStatus {
    PAID,
    PENDING,
}

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val driverUserId: Long,
    val driverName: String,
    val driverEmail: String,
    val parkingId: String,
    val spaceTitle: String,
    val location: String,
    val ownerUserId: Long?,
    val requestedHour: Int,
    val dayOfWeek: Int,
    val bookedHours: Int,
    val hourlyRateLkr: Double,
    val totalPaidLkr: Double,
    val paymentMethod: String,
    val paymentStatus: PaymentStatus,
    val ownerNotificationRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
