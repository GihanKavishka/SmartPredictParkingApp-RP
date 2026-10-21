package com.example.androidsmartpredictparkinggihan.ml

data class ParkingTrainingRow(
    val parkingId: String,
    val requestedHour: Int,
    val dayOfWeek: Int,
    val publicHoliday: Int,
    val avgOccupancy: Double,
    val avgParkingDuration: Double,
    val cancellationRate: Double,
    val eventNearby: Int,
    val available: Int,
)

data class PredictionInput(
    val parkingId: String,
    val requestedHour: Int,
    val dayOfWeek: Int,
    val publicHoliday: Int,
    val avgOccupancy: Double,
    val avgParkingDuration: Double,
    val cancellationRate: Double,
    val eventNearby: Int,
)

data class PredictionResult(
    val available: Boolean,
    val confidence: Double,
    val probability: Double,
)

data class ModelMetrics(
    val accuracy: Double,
    val precision: Double,
    val recall: Double,
    val f1Score: Double,
    val trainingSamples: Int,
    val testSamples: Int,
    val csvAvailabilityRate: Double,
)
