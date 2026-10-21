package com.example.androidsmartpredictparkinggihan.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prediction_logs")
data class PredictionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val parkingId: String,
    val requestedHour: Int,
    val dayOfWeek: Int,
    val publicHoliday: Int,
    val avgOccupancy: Double,
    val avgParkingDuration: Double,
    val cancellationRate: Double,
    val eventNearby: Int,
    val predictedAvailable: Boolean,
    val confidence: Double,
    val createdAt: Long = System.currentTimeMillis(),
)
