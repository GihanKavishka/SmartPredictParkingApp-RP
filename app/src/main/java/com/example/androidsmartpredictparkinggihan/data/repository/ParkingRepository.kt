package com.example.androidsmartpredictparkinggihan.data.repository

import com.example.androidsmartpredictparkinggihan.data.local.ParkingSpaceDao
import com.example.androidsmartpredictparkinggihan.data.local.PredictionLogDao
import com.example.androidsmartpredictparkinggihan.data.model.ParkingSpaceEntity
import com.example.androidsmartpredictparkinggihan.data.model.PredictionLogEntity
import com.example.androidsmartpredictparkinggihan.ml.PredictionInput
import com.example.androidsmartpredictparkinggihan.ml.PredictionResult

class ParkingRepository(
    private val parkingSpaceDao: ParkingSpaceDao,
    private val predictionLogDao: PredictionLogDao,
) {
    suspend fun getParkingSpaces(): List<ParkingSpaceEntity> = parkingSpaceDao.getAll()

    suspend fun getSpacesByOwner(ownerId: Long): List<ParkingSpaceEntity> =
        parkingSpaceDao.getByOwner(ownerId)

    suspend fun addOwnerSpace(
        ownerId: Long,
        parkingId: String,
        title: String,
        location: String,
        basePriceLkr: Double,
        securityLevel: String,
    ): Result<ParkingSpaceEntity> {
        val id = parkingId.trim().uppercase()
        if (id.isBlank() || title.isBlank() || location.isBlank()) {
            return Result.failure(IllegalArgumentException("Parking ID, title, and location are required"))
        }
        if (basePriceLkr <= 0) {
            return Result.failure(IllegalArgumentException("Price must be greater than zero"))
        }
        if (parkingSpaceDao.findById(id) != null) {
            return Result.failure(IllegalArgumentException("Parking ID already exists"))
        }
        val space = ParkingSpaceEntity(
            parkingId = id,
            title = title.trim(),
            location = location.trim(),
            basePriceLkr = basePriceLkr,
            securityLevel = securityLevel.trim().ifBlank { "Standard" },
            ownerUserId = ownerId,
        )
        parkingSpaceDao.insert(space)
        return Result.success(space)
    }

    suspend fun removeOwnerSpace(ownerId: Long, parkingId: String): Result<Unit> {
        val deleted = parkingSpaceDao.deleteOwned(parkingId.trim().uppercase(), ownerId)
        return if (deleted > 0) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Space not found or not owned by you"))
        }
    }

    suspend fun logPrediction(
        userId: Long,
        input: PredictionInput,
        result: PredictionResult,
    ) {
        predictionLogDao.insert(
            PredictionLogEntity(
                userId = userId,
                parkingId = input.parkingId,
                requestedHour = input.requestedHour,
                dayOfWeek = input.dayOfWeek,
                publicHoliday = input.publicHoliday,
                avgOccupancy = input.avgOccupancy,
                avgParkingDuration = input.avgParkingDuration,
                cancellationRate = input.cancellationRate,
                eventNearby = input.eventNearby,
                predictedAvailable = result.available,
                confidence = result.confidence,
            ),
        )
    }
}
