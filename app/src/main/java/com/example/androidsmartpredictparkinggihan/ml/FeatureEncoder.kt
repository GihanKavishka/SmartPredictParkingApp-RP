package com.example.androidsmartpredictparkinggihan.ml

import kotlin.math.sin

object FeatureEncoder {
    const val FEATURE_SIZE = 10

    fun encode(input: PredictionInput): DoubleArray {
        val parkingNumeric = input.parkingId.filter { it.isDigit() }.toIntOrNull()?.div(50.0) ?: 0.5
        val hourSin = sin(2.0 * Math.PI * input.requestedHour / 24.0)
        val hourCos = kotlin.math.cos(2.0 * Math.PI * input.requestedHour / 24.0)
        return doubleArrayOf(
            input.requestedHour / 23.0,
            input.dayOfWeek / 6.0,
            input.publicHoliday.toDouble(),
            input.avgOccupancy.coerceIn(0.0, 1.0),
            (input.avgParkingDuration / 300.0).coerceIn(0.0, 1.0),
            input.cancellationRate.coerceIn(0.0, 1.0),
            input.eventNearby.toDouble(),
            parkingNumeric.coerceIn(0.0, 1.0),
            hourSin,
            hourCos,
        )
    }

    fun encode(row: ParkingTrainingRow): DoubleArray =
        encode(
            PredictionInput(
                parkingId = row.parkingId,
                requestedHour = row.requestedHour,
                dayOfWeek = row.dayOfWeek,
                publicHoliday = row.publicHoliday,
                avgOccupancy = row.avgOccupancy,
                avgParkingDuration = row.avgParkingDuration,
                cancellationRate = row.cancellationRate,
                eventNearby = row.eventNearby,
            ),
        )
}
