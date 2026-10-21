package com.example.androidsmartpredictparkinggihan.ml

import android.content.Context

data class ParkingIdStats(
    val avgOccupancy: Double,
    val avgParkingDurationMinutes: Double,
    val avgCancellationRate: Double,
)

class CsvParkingStatsProvider(context: Context) {
    private val rows: List<ParkingTrainingRow> = CsvDatasetLoader.load(context)
    private val byParkingId: Map<String, ParkingIdStats> = buildMap()
    private val global: ParkingIdStats = computeGlobal()

    fun statsFor(parkingId: String): ParkingIdStats = byParkingId[parkingId] ?: global

    private fun buildMap(): Map<String, ParkingIdStats> {
        if (rows.isEmpty()) return emptyMap()
        return rows.groupBy { it.parkingId }.mapValues { (_, group) ->
            ParkingIdStats(
                avgOccupancy = group.map { it.avgOccupancy }.average(),
                avgParkingDurationMinutes = group.map { it.avgParkingDuration }.average(),
                avgCancellationRate = group.map { it.cancellationRate }.average(),
            )
        }
    }

    private fun computeGlobal(): ParkingIdStats {
        if (rows.isEmpty()) {
            return ParkingIdStats(0.45, 180.0, 0.12)
        }
        return ParkingIdStats(
            avgOccupancy = rows.map { it.avgOccupancy }.average(),
            avgParkingDurationMinutes = rows.map { it.avgParkingDuration }.average(),
            avgCancellationRate = rows.map { it.cancellationRate }.average(),
        )
    }
}
