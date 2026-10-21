package com.example.androidsmartpredictparkinggihan

import android.app.Application
import com.example.androidsmartpredictparkinggihan.data.local.AppDatabase
import com.example.androidsmartpredictparkinggihan.data.local.seedIfEmpty
import com.example.androidsmartpredictparkinggihan.data.local.syncParkingSpacesFromCsv
import com.example.androidsmartpredictparkinggihan.data.repository.AuthRepository
import com.example.androidsmartpredictparkinggihan.data.repository.ParkingRepository
import com.example.androidsmartpredictparkinggihan.data.repository.StatsRepository
import com.example.androidsmartpredictparkinggihan.data.repository.UserManagementRepository
import com.example.androidsmartpredictparkinggihan.ml.AvailabilityPredictionService
import com.example.androidsmartpredictparkinggihan.ml.CsvParkingStatsProvider
import com.example.androidsmartpredictparkinggihan.data.repository.BookingRepository
import com.example.androidsmartpredictparkinggihan.data.repository.ParkingSearchRepository
import com.example.androidsmartpredictparkinggihan.notifications.BookingNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmartParkingApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: AppDatabase by lazy { AppDatabase.build(this) }

    val authRepository: AuthRepository by lazy { AuthRepository(database.userDao()) }

    val userManagementRepository: UserManagementRepository by lazy {
        UserManagementRepository(database.userDao())
    }

    val parkingRepository: ParkingRepository by lazy {
        ParkingRepository(database.parkingSpaceDao(), database.predictionLogDao())
    }

    val bookingRepository: BookingRepository by lazy {
        BookingRepository(
            database.bookingDao(),
            database.parkingSpaceDao(),
            database.userDao(),
            applicationContext,
        )
    }

    val statsRepository: StatsRepository by lazy {
        StatsRepository(
            database.userDao(),
            database.predictionLogDao(),
            database.parkingSpaceDao(),
            bookingRepository,
        )
    }

    val predictionService: AvailabilityPredictionService by lazy {
        AvailabilityPredictionService(this)
    }

    val csvParkingStatsProvider: CsvParkingStatsProvider by lazy {
        CsvParkingStatsProvider(this)
    }

    val parkingSearchRepository: ParkingSearchRepository by lazy {
        ParkingSearchRepository(parkingRepository, predictionService, csvParkingStatsProvider)
    }

    override fun onCreate() {
        super.onCreate()
        BookingNotificationHelper.ensureChannel(this)
        appScope.launch {
            database.seedIfEmpty()
            database.syncParkingSpacesFromCsv(this@SmartParkingApp)
            predictionService.ensureModelTrained()
        }
    }
}
