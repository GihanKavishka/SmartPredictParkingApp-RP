package com.example.androidsmartpredictparkinggihan.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidsmartpredictparkinggihan.data.repository.AdminDashboardStats
import com.example.androidsmartpredictparkinggihan.data.repository.StatsRepository
import com.example.androidsmartpredictparkinggihan.ml.AvailabilityPredictionService
import kotlinx.coroutines.launch

data class AdminDashboardUiState(
    val loading: Boolean = true,
    val stats: AdminDashboardStats? = null,
)

class AdminDashboardViewModel(
    private val statsRepository: StatsRepository,
    private val predictionService: AvailabilityPredictionService,
) : ViewModel() {
    var uiState by mutableStateOf(AdminDashboardUiState())
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            uiState = AdminDashboardUiState(loading = true)
            predictionService.ensureModelTrained()
            val stats = statsRepository.loadDashboard(
                modelMetrics = predictionService.getMetrics(),
                csvRows = predictionService.getDatasetSize(),
                csvAvailabilityByHour = predictionService.availabilityByHourFromCsv(),
                occupancyBuckets = predictionService.occupancyDistributionFromCsv(),
            )
            uiState = AdminDashboardUiState(loading = false, stats = stats)
        }
    }
}
