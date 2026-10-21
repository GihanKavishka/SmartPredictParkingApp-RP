package com.example.androidsmartpredictparkinggihan.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.androidsmartpredictparkinggihan.data.model.PredictionLogEntity

@Dao
interface PredictionLogDao {
    @Insert
    suspend fun insert(log: PredictionLogEntity): Long

    @Query("SELECT COUNT(*) FROM prediction_logs")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM prediction_logs WHERE predictedAvailable = 1")
    suspend fun countPredictedAvailable(): Int

    @Query(
        """
        SELECT requestedHour AS bucket, COUNT(*) AS total
        FROM prediction_logs
        GROUP BY requestedHour
        ORDER BY requestedHour
        """,
    )
    suspend fun predictionsByHour(): List<HourBucket>

    @Query(
        """
        SELECT dayOfWeek AS bucket, COUNT(*) AS total
        FROM prediction_logs
        GROUP BY dayOfWeek
        ORDER BY dayOfWeek
        """,
    )
    suspend fun predictionsByDay(): List<HourBucket>
}

data class HourBucket(val bucket: Int, val total: Int)
