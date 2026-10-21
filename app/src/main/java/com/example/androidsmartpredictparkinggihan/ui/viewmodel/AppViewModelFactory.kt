package com.example.androidsmartpredictparkinggihan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.androidsmartpredictparkinggihan.SmartParkingApp
import com.example.androidsmartpredictparkinggihan.data.model.UserEntity

class AppViewModelFactory(
    private val app: SmartParkingApp,
    private val sessionUser: UserEntity? = null,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(app.authRepository) as T
            modelClass.isAssignableFrom(UserHomeViewModel::class.java) ->
                UserHomeViewModel(
                    user = requireNotNull(sessionUser),
                    parkingRepository = app.parkingRepository,
                    parkingSearchRepository = app.parkingSearchRepository,
                    bookingRepository = app.bookingRepository,
                    predictionService = app.predictionService,
                ) as T
            modelClass.isAssignableFrom(OwnerHomeViewModel::class.java) ->
                OwnerHomeViewModel(
                    owner = requireNotNull(sessionUser),
                    parkingRepository = app.parkingRepository,
                    bookingRepository = app.bookingRepository,
                ) as T
            modelClass.isAssignableFrom(PredictionViewModel::class.java) ->
                PredictionViewModel(
                    app.parkingRepository,
                    app.predictionService,
                    requireNotNull(sessionUser),
                ) as T
            modelClass.isAssignableFrom(AdminDashboardViewModel::class.java) ->
                AdminDashboardViewModel(app.statsRepository, app.predictionService) as T
            modelClass.isAssignableFrom(UserManagementViewModel::class.java) ->
                UserManagementViewModel(
                    adminId = requireNotNull(sessionUser).id,
                    repository = app.userManagementRepository,
                ) as T
            modelClass.isAssignableFrom(AdminBookingsViewModel::class.java) ->
                AdminBookingsViewModel(app.bookingRepository) as T
            else -> error("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
