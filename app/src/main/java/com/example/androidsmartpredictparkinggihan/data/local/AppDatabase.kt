package com.example.androidsmartpredictparkinggihan.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.androidsmartpredictparkinggihan.data.model.BookingEntity
import com.example.androidsmartpredictparkinggihan.data.model.ParkingSpaceEntity
import com.example.androidsmartpredictparkinggihan.data.model.PaymentStatus
import com.example.androidsmartpredictparkinggihan.data.model.PredictionLogEntity
import com.example.androidsmartpredictparkinggihan.data.model.UserEntity
import com.example.androidsmartpredictparkinggihan.data.model.UserRole
import com.example.androidsmartpredictparkinggihan.ml.CsvDatasetLoader

@Database(
    entities = [UserEntity::class, ParkingSpaceEntity::class, PredictionLogEntity::class, BookingEntity::class],
    version = 3,
    exportSchema = false,
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun parkingSpaceDao(): ParkingSpaceDao
    abstract fun predictionLogDao(): PredictionLogDao
    abstract fun bookingDao(): BookingDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun build(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_parking.db",
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { instance = it }
            }
        }
    }
}

class RoomConverters {
    @TypeConverter
    fun fromRole(role: UserRole): String = role.name

    @TypeConverter
    fun toRole(value: String): UserRole = UserRole.valueOf(value)

    @TypeConverter
    fun fromPaymentStatus(status: PaymentStatus): String = status.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus = PaymentStatus.valueOf(value)
}

suspend fun AppDatabase.syncParkingSpacesFromCsv(context: Context) {
    val rows = CsvDatasetLoader.load(context)
    if (rows.isEmpty()) return
    val existing = parkingSpaceDao().getAll().associateBy { it.parkingId }
    val fromCsv = rows.map { it.parkingId }.distinct().sorted()
    val missing = fromCsv.filter { it !in existing }.map { id ->
        ParkingSpaceEntity(
            parkingId = id,
            title = "Shared Slot $id",
            location = "Colombo area",
            basePriceLkr = 200.0,
            securityLevel = "Standard",
        )
    }
    if (missing.isNotEmpty()) {
        parkingSpaceDao().insertAll(missing)
    }
}

suspend fun AppDatabase.seedIfEmpty() {
    if (userDao().countUsers() == 0) {
        userDao().insert(
            UserEntity(
                fullName = "System Admin",
                email = "admin@smartpark.lk",
                password = "admin123",
                role = UserRole.ADMIN,
            ),
        )
        userDao().insert(
            UserEntity(
                fullName = "Demo Driver",
                email = "user@smartpark.lk",
                password = "user123",
                role = UserRole.USER,
            ),
        )
        userDao().insert(
            UserEntity(
                fullName = "Demo House Owner",
                email = "owner@smartpark.lk",
                password = "owner123",
                role = UserRole.HOUSE_OWNER,
            ),
        )
    }
    if (parkingSpaceDao().count() == 0) {
        val spaces = (1..20).map { i ->
            val id = "P%03d".format(i)
            ParkingSpaceEntity(
                parkingId = id,
                title = "Shared Slot $id",
                location = when {
                    i <= 5 -> "Colombo Fort"
                    i <= 10 -> "Bambalapitiya"
                    i <= 15 -> "Nugegoda"
                    else -> "Malabe"
                },
                basePriceLkr = 150.0 + (i * 12.5),
                securityLevel = if (i % 3 == 0) "CCTV + Gate" else "Standard",
            )
        }
        parkingSpaceDao().insertAll(spaces)
    }
}
