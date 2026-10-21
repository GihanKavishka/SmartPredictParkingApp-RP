package com.example.androidsmartpredictparkinggihan.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.androidsmartpredictparkinggihan.data.model.BookingEntity

@Dao
interface BookingDao {
    @Insert
    suspend fun insert(booking: BookingEntity): Long

    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    suspend fun getAll(): List<BookingEntity>

    @Query("SELECT * FROM bookings WHERE driverUserId = :driverId ORDER BY createdAt DESC")
    suspend fun getByDriver(driverId: Long): List<BookingEntity>

    @Query("SELECT * FROM bookings WHERE ownerUserId = :ownerId ORDER BY createdAt DESC")
    suspend fun getByOwner(ownerId: Long): List<BookingEntity>

    @Query("SELECT COUNT(*) FROM bookings WHERE ownerUserId = :ownerId AND ownerNotificationRead = 0")
    suspend fun countUnreadForOwner(ownerId: Long): Int

    @Query("UPDATE bookings SET ownerNotificationRead = 1 WHERE ownerUserId = :ownerId AND ownerNotificationRead = 0")
    suspend fun markAllReadForOwner(ownerId: Long)

    @Query("SELECT COUNT(*) FROM bookings")
    suspend fun count(): Int
}
