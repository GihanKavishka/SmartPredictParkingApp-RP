package com.example.androidsmartpredictparkinggihan.data.repository

import com.example.androidsmartpredictparkinggihan.data.local.ParkingSpaceDao
import com.example.androidsmartpredictparkinggihan.data.local.PredictionLogDao
import com.example.androidsmartpredictparkinggihan.data.local.UserDao
import com.example.androidsmartpredictparkinggihan.data.model.UserRole
import com.example.androidsmartpredictparkinggihan.ml.ModelMetrics

data class AdminDashboardStats(
    val totalUsers: Int,
    val totalDrivers: Int,
    val totalHouseOwners: Int,
    val totalAdmins: Int,
    val listedSpaces: Int,
    val totalBookings: Int,
    val totalPredictions: Int,
    val predictedAvailableCount: Int,
    val modelMetrics: ModelMetrics?,
    val csvRows: Int,
    val predictionsByHour: List<Pair<Int, Int>>,
    val predictionsByDay: List<Pair<Int, Int>>,
    val csvAvailabilityByHour: List<Pair<Int, Double>>,
    val occupancyBuckets: List<Pair<String, Double>>,
)

class StatsRepository(
    private val userDao: UserDao,
    private val predictionLogDao: PredictionLogDao,
    private val parkingSpaceDao: ParkingSpaceDao,
    private val bookingRepository: BookingRepository,
) {
    suspend fun loadDashboard(
        modelMetrics: ModelMetrics?,
        csvRows: Int,
        csvAvailabilityByHour: List<Pair<Int, Double>>,
        occupancyBuckets: List<Pair<String, Double>>,
    ): AdminDashboardStats {
        return AdminDashboardStats(
            totalUsers = userDao.countUsers(),
            totalDrivers = userDao.countByRole(UserRole.USER),
            totalHouseOwners = userDao.countByRole(UserRole.HOUSE_OWNER),
            totalAdmins = userDao.countByRole(UserRole.ADMIN),
            listedSpaces = parkingSpaceDao.count(),
            totalBookings = bookingRepository.totalBookings(),
            totalPredictions = predictionLogDao.count(),
            predictedAvailableCount = predictionLogDao.countPredictedAvailable(),
            modelMetrics = modelMetrics,
            csvRows = csvRows,
            predictionsByHour = predictionLogDao.predictionsByHour().map { it.bucket to it.total },
            predictionsByDay = predictionLogDao.predictionsByDay().map { it.bucket to it.total },
            csvAvailabilityByHour = csvAvailabilityByHour,
            occupancyBuckets = occupancyBuckets,
        )
    }
}
