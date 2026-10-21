package com.example.androidsmartpredictparkinggihan.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidsmartpredictparkinggihan.data.model.BookingEntity
import com.example.androidsmartpredictparkinggihan.data.model.ParkingListingResult
import com.example.androidsmartpredictparkinggihan.data.model.ParkingSearchCriteria
import com.example.androidsmartpredictparkinggihan.data.model.ParkingSpaceEntity
import com.example.androidsmartpredictparkinggihan.data.model.UserEntity
import com.example.androidsmartpredictparkinggihan.data.repository.BookingRepository
import com.example.androidsmartpredictparkinggihan.data.repository.ParkingRepository
import com.example.androidsmartpredictparkinggihan.data.repository.ParkingSearchRepository
import com.example.androidsmartpredictparkinggihan.ml.AvailabilityPredictionService
import com.example.androidsmartpredictparkinggihan.ml.ModelMetrics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

private fun mondayZeroDayIndex(): Int = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7

data class UserHomeUiState(
    val loading: Boolean = true,
    val searching: Boolean = false,
    val allSpaces: List<ParkingSpaceEntity> = emptyList(),
    val searchResults: List<ParkingListingResult> = emptyList(),
    val hasSearched: Boolean = false,
    val criteria: ParkingSearchCriteria = ParkingSearchCriteria(
        requestedHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        dayOfWeek = mondayZeroDayIndex(),
    ),
    val modelMetrics: ModelMetrics? = null,
    val csvRows: Int = 0,
    val modelError: String? = null,
    val searchError: String? = null,
    val myBookings: List<BookingEntity> = emptyList(),
    val bookingTarget: ParkingListingResult? = null,
    val bookHours: Int = 2,
    val bookingProcessing: Boolean = false,
    val bookingMessage: String? = null,
    val bookingError: String? = null,
)

class UserHomeViewModel(
    private val user: UserEntity,
    private val parkingRepository: ParkingRepository,
    private val parkingSearchRepository: ParkingSearchRepository,
    private val bookingRepository: BookingRepository,
    private val predictionService: AvailabilityPredictionService,
) : ViewModel() {
    var uiState by mutableStateOf(UserHomeUiState())
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true, searchError = null)
            predictionService.ensureModelTrained()
            val training = predictionService.getTrainingState()
            val spaces = parkingRepository.getParkingSpaces()
            uiState = uiState.copy(
                loading = false,
                allSpaces = spaces,
                modelMetrics = predictionService.getMetrics(),
                csvRows = predictionService.getDatasetSize(),
                modelError = training.errorMessage,
                myBookings = bookingRepository.getDriverBookings(user.id),
            )
            if (training.isReady) {
                search(logToDatabase = false)
            }
        }
    }

    fun updateLocationQuery(value: String) {
        uiState = uiState.copy(criteria = uiState.criteria.copy(locationQuery = value))
    }

    fun updateMaxPrice(value: Float) {
        uiState = uiState.copy(criteria = uiState.criteria.copy(maxPriceLkr = value.toDouble()))
    }

    fun updateHour(hour: Int) {
        uiState = uiState.copy(criteria = uiState.criteria.copy(requestedHour = hour.coerceIn(0, 23)))
    }

    fun updateDay(day: Int) {
        uiState = uiState.copy(criteria = uiState.criteria.copy(dayOfWeek = day.coerceIn(0, 6)))
    }

    fun toggleHoliday(value: Boolean) {
        uiState = uiState.copy(criteria = uiState.criteria.copy(publicHoliday = value))
    }

    fun toggleEvent(value: Boolean) {
        uiState = uiState.copy(criteria = uiState.criteria.copy(eventNearby = value))
    }

    fun toggleOnlyAvailable(value: Boolean) {
        uiState = uiState.copy(criteria = uiState.criteria.copy(onlyShowPredictedAvailable = value))
    }

    fun search(logToDatabase: Boolean = true) {
        viewModelScope.launch {
            uiState = uiState.copy(searching = true, searchError = null)
            try {
                val results = parkingSearchRepository.search(uiState.criteria, user.id, logToDatabase)
                uiState = uiState.copy(
                    searching = false,
                    searchResults = results,
                    hasSearched = true,
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    searching = false,
                    searchError = e.message ?: "Search failed",
                )
            }
        }
    }

    fun openBooking(listing: ParkingListingResult) {
        uiState = uiState.copy(
            bookingTarget = listing,
            bookHours = 2,
            bookingError = null,
            bookingMessage = null,
        )
    }

    fun closeBooking() {
        uiState = uiState.copy(bookingTarget = null, bookingProcessing = false, bookingError = null)
    }

    fun updateBookHours(hours: Int) {
        uiState = uiState.copy(bookHours = hours.coerceIn(1, 12))
    }

    fun payAndBook() {
        val listing = uiState.bookingTarget ?: return
        viewModelScope.launch {
            uiState = uiState.copy(bookingProcessing = true, bookingError = null, bookingMessage = null)
            delay(900)
            val result = bookingRepository.createPaidBooking(
                driver = user,
                listing = listing,
                criteria = uiState.criteria,
                bookedHours = uiState.bookHours,
                paymentMethod = "Simulated card payment",
            )
            result.fold(
                onSuccess = { booking ->
                    uiState = uiState.copy(
                        bookingProcessing = false,
                        bookingTarget = null,
                        bookingMessage = "Booked ${booking.parkingId} · LKR ${booking.totalPaidLkr.format(0)} paid",
                        myBookings = bookingRepository.getDriverBookings(user.id),
                    )
                },
                onFailure = { err ->
                    uiState = uiState.copy(
                        bookingProcessing = false,
                        bookingError = err.message ?: "Booking failed",
                    )
                },
            )
        }
    }

    private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
}
