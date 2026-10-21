package com.example.androidsmartpredictparkinggihan.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parking_spaces")
data class ParkingSpaceEntity(
    @PrimaryKey val parkingId: String,
    val title: String,
    val location: String,
    val basePriceLkr: Double,
    val securityLevel: String,
    val ownerUserId: Long? = null,
)
