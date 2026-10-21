package com.example.androidsmartpredictparkinggihan.data.model

import com.example.androidsmartpredictparkinggihan.ml.PredictionResult

data class ParkingSearchCriteria(
    val locationQuery: String = "",
    val maxPriceLkr: Double = 500.0,
    val requestedHour: Int = 12,
    val dayOfWeek: Int = 0,
    val publicHoliday: Boolean = false,
    val eventNearby: Boolean = false,
    val onlyShowPredictedAvailable: Boolean = false,
)

data class ParkingListingResult(
    val space: ParkingSpaceEntity,
    val prediction: PredictionResult,
)
