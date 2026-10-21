package com.example.androidsmartpredictparkinggihan.data.repository

import com.example.androidsmartpredictparkinggihan.data.model.ParkingListingResult
import com.example.androidsmartpredictparkinggihan.data.model.ParkingSearchCriteria
import com.example.androidsmartpredictparkinggihan.data.model.ParkingSpaceEntity
import com.example.androidsmartpredictparkinggihan.ml.AvailabilityPredictionService
import com.example.androidsmartpredictparkinggihan.ml.CsvParkingStatsProvider
import com.example.androidsmartpredictparkinggihan.ml.PredictionInput

class ParkingSearchRepository(
    private val parkingRepository: ParkingRepository,
    private val predictionService: AvailabilityPredictionService,
    private val csvStats: CsvParkingStatsProvider,
) {
    suspend fun search(
        criteria: ParkingSearchCriteria,
        userId: Long,
        logPredictions: Boolean = true,
    ): List<ParkingListingResult> {
        predictionService.ensureModelTrained()
        val spaces = parkingRepository.getParkingSpaces()
            .filter { space -> matchesCriteria(space, criteria) }

        val results = spaces.map { space ->
            val stats = csvStats.statsFor(space.parkingId)
            val input = PredictionInput(
                parkingId = space.parkingId,
                requestedHour = criteria.requestedHour,
                dayOfWeek = criteria.dayOfWeek,
                publicHoliday = if (criteria.publicHoliday) 1 else 0,
                avgOccupancy = stats.avgOccupancy,
                avgParkingDuration = stats.avgParkingDurationMinutes,
                cancellationRate = stats.avgCancellationRate,
                eventNearby = if (criteria.eventNearby) 1 else 0,
            )
            val prediction = predictionService.predict(input)
            if (logPredictions) {
                parkingRepository.logPrediction(userId, input, prediction)
            }
            ParkingListingResult(space = space, prediction = prediction)
        }

        return results
            .filter { !criteria.onlyShowPredictedAvailable || it.prediction.available }
            .sortedByDescending { it.prediction.probability }
    }

    private fun matchesCriteria(space: ParkingSpaceEntity, criteria: ParkingSearchCriteria): Boolean {
        val locationOk = criteria.locationQuery.isBlank() ||
            space.location.contains(criteria.locationQuery, ignoreCase = true) ||
            space.title.contains(criteria.locationQuery, ignoreCase = true) ||
            space.parkingId.contains(criteria.locationQuery, ignoreCase = true)
        val priceOk = space.basePriceLkr <= criteria.maxPriceLkr
        return locationOk && priceOk
    }
}
