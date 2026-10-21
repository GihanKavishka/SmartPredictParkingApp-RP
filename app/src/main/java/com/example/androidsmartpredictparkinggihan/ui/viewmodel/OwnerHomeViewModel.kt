package com.example.androidsmartpredictparkinggihan.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidsmartpredictparkinggihan.data.model.BookingEntity
import com.example.androidsmartpredictparkinggihan.data.model.ParkingSpaceEntity
import com.example.androidsmartpredictparkinggihan.data.model.UserEntity
import com.example.androidsmartpredictparkinggihan.data.repository.BookingRepository
import com.example.androidsmartpredictparkinggihan.data.repository.ParkingRepository
import kotlinx.coroutines.launch

data class OwnerHomeUiState(
    val loading: Boolean = true,
    val mySpaces: List<ParkingSpaceEntity> = emptyList(),
    val bookingAlerts: List<BookingEntity> = emptyList(),
    val unreadBookingCount: Int = 0,
    val parkingId: String = "",
    val title: String = "",
    val location: String = "",
    val priceLkr: String = "200",
    val securityLevel: String = "Standard",
    val saving: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

class OwnerHomeViewModel(
    private val owner: UserEntity,
    private val parkingRepository: ParkingRepository,
    private val bookingRepository: BookingRepository,
) : ViewModel() {
    var uiState by mutableStateOf(OwnerHomeUiState())
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true, error = null)
            val bookings = bookingRepository.getOwnerBookings(owner.id)
            uiState = uiState.copy(
                loading = false,
                mySpaces = parkingRepository.getSpacesByOwner(owner.id),
                bookingAlerts = bookings,
                unreadBookingCount = bookingRepository.countUnreadOwnerNotifications(owner.id),
            )
        }
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            bookingRepository.markOwnerNotificationsRead(owner.id)
            refresh()
        }
    }

    fun updateParkingId(value: String) {
        uiState = uiState.copy(parkingId = value.uppercase(), message = null, error = null)
    }

    fun updateTitle(value: String) {
        uiState = uiState.copy(title = value, message = null, error = null)
    }

    fun updateLocation(value: String) {
        uiState = uiState.copy(location = value, message = null, error = null)
    }

    fun updatePrice(value: String) {
        uiState = uiState.copy(priceLkr = value, message = null, error = null)
    }

    fun updateSecurity(value: String) {
        uiState = uiState.copy(securityLevel = value, message = null, error = null)
    }

    fun addSpace() {
        val state = uiState
        val price = state.priceLkr.toDoubleOrNull()
        if (price == null) {
            uiState = state.copy(error = "Enter a valid price")
            return
        }
        viewModelScope.launch {
            uiState = state.copy(saving = true, error = null, message = null)
            val result = parkingRepository.addOwnerSpace(
                ownerId = owner.id,
                parkingId = state.parkingId,
                title = state.title,
                location = state.location,
                basePriceLkr = price,
                securityLevel = state.securityLevel,
            )
            result.fold(
                onSuccess = { space ->
                    uiState = uiState.copy(
                        saving = false,
                        mySpaces = parkingRepository.getSpacesByOwner(owner.id),
                        parkingId = "",
                        title = "",
                        location = "",
                        message = "Parking space ${space.parkingId} added",
                    )
                },
                onFailure = { err ->
                    uiState = uiState.copy(saving = false, error = err.message ?: "Could not add space")
                },
            )
        }
    }

    fun removeSpace(parkingId: String) {
        viewModelScope.launch {
            val result = parkingRepository.removeOwnerSpace(owner.id, parkingId)
            result.fold(
                onSuccess = {
                    uiState = uiState.copy(
                        mySpaces = parkingRepository.getSpacesByOwner(owner.id),
                        message = "Removed $parkingId",
                        error = null,
                    )
                },
                onFailure = { err ->
                    uiState = uiState.copy(error = err.message ?: "Could not remove space")
                },
            )
        }
    }
}
