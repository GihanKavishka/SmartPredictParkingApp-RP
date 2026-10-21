package com.example.androidsmartpredictparkinggihan.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidsmartpredictparkinggihan.data.model.BookingEntity
import com.example.androidsmartpredictparkinggihan.data.repository.BookingRepository
import kotlinx.coroutines.launch

data class AdminBookingsUiState(
    val loading: Boolean = true,
    val bookings: List<BookingEntity> = emptyList(),
)

class AdminBookingsViewModel(
    private val bookingRepository: BookingRepository,
) : ViewModel() {
    var uiState by mutableStateOf(AdminBookingsUiState())
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            uiState = AdminBookingsUiState(loading = true)
            uiState = AdminBookingsUiState(
                loading = false,
                bookings = bookingRepository.getAllBookings(),
            )
        }
    }
}
