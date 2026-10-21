package com.example.androidsmartpredictparkinggihan.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidsmartpredictparkinggihan.data.model.ParkingSpaceEntity
import com.example.androidsmartpredictparkinggihan.data.model.UserEntity
import com.example.androidsmartpredictparkinggihan.data.repository.ParkingRepository
import com.example.androidsmartpredictparkinggihan.ml.AvailabilityPredictionService
import com.example.androidsmartpredictparkinggihan.ml.PredictionInput
import com.example.androidsmartpredictparkinggihan.ml.PredictionResult
import kotlinx.coroutines.launch
import java.util.Calendar

private fun mondayZeroDayIndex(): Int = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7

data class PredictionUiState(
    val spaces: List<ParkingSpaceEntity> = emptyList(),
    val selectedParkingId: String = "",
    val requestedHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    val dayOfWeek: Int = mondayZeroDayIndex(),
    val publicHoliday: Boolean = false,
    val avgOccupancy: Float = 0.45f,
    val avgParkingDurationHours: Float = 3f,
    val cancellationCountWeek: Float = 1f,
    val eventNearby: Boolean = false,
    val loading: Boolean = false,
    val result: PredictionResult? = null,
    val error: String? = null,
)

class PredictionViewModel(
    private val parkingRepository: ParkingRepository,
    private val predictionService: AvailabilityPredictionService,
    private val user: UserEntity,
) : ViewModel() {
    var uiState by mutableStateOf(PredictionUiState())
        private set

    init {
        viewModelScope.launch {
            val spaces = parkingRepository.getParkingSpaces()
            uiState = uiState.copy(
                spaces = spaces,
                selectedParkingId = spaces.firstOrNull()?.parkingId.orEmpty(),
            )
        }
    }

    fun updateParking(id: String) {
        uiState = uiState.copy(selectedParkingId = id, result = null)
    }

    fun updateHour(hour: Int) {
        uiState = uiState.copy(requestedHour = hour.coerceIn(0, 23), result = null)
    }

    fun updateDay(day: Int) {
        uiState = uiState.copy(dayOfWeek = day.coerceIn(0, 6), result = null)
    }

    fun toggleHoliday(value: Boolean) {
        uiState = uiState.copy(publicHoliday = value, result = null)
    }

    fun toggleEvent(value: Boolean) {
        uiState = uiState.copy(eventNearby = value, result = null)
    }

    fun updateOccupancy(value: Float) {
        uiState = uiState.copy(avgOccupancy = value, result = null)
    }

    fun updateDurationHours(value: Float) {
        uiState = uiState.copy(avgParkingDurationHours = value, result = null)
    }

    fun updateCancellationCount(value: Float) {
        uiState = uiState.copy(cancellationCountWeek = value, result = null)
    }

    fun applyLowDemandPreset() {
        uiState = uiState.copy(
            avgOccupancy = 0.22f,
            avgParkingDurationHours = 2f,
            cancellationCountWeek = 0f,
            eventNearby = false,
            publicHoliday = false,
            requestedHour = 10,
            result = null,
            error = null,
        )
    }

    fun applyHighDemandPreset() {
        uiState = uiState.copy(
            avgOccupancy = 0.78f,
            avgParkingDurationHours = 6f,
            cancellationCountWeek = 5f,
            eventNearby = true,
            publicHoliday = false,
            requestedHour = 18,
            result = null,
            error = null,
        )
    }

    fun runPrediction() {
        val state = uiState
        if (state.selectedParkingId.isBlank()) {
            uiState = state.copy(error = "Select a parking space")
            return
        }
        viewModelScope.launch {
            uiState = state.copy(loading = true, error = null)
            try {
                val input = PredictionInput(
                    parkingId = state.selectedParkingId,
                    requestedHour = state.requestedHour,
                    dayOfWeek = state.dayOfWeek,
                    publicHoliday = if (state.publicHoliday) 1 else 0,
                    avgOccupancy = state.avgOccupancy.toDouble(),
                    avgParkingDuration = state.avgParkingDurationHours.toDouble() * 60.0,
                    cancellationRate = (state.cancellationCountWeek / 7.0).coerceIn(0.0, 1.0),
                    eventNearby = if (state.eventNearby) 1 else 0,
                )
                val result = predictionService.predict(input)
                parkingRepository.logPrediction(user.id, input, result)
                uiState = uiState.copy(loading = false, result = result, error = null)
            } catch (e: Exception) {
                uiState = uiState.copy(loading = false, error = e.message ?: "Prediction failed")
            }
        }
    }
}
