package com.example.androidsmartpredictparkinggihan.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.androidsmartpredictparkinggihan.data.model.ParkingSpaceEntity

@Dao
interface ParkingSpaceDao {
    @Query("SELECT * FROM parking_spaces ORDER BY parkingId")
    suspend fun getAll(): List<ParkingSpaceEntity>

    @Query("SELECT * FROM parking_spaces WHERE ownerUserId = :ownerId ORDER BY parkingId")
    suspend fun getByOwner(ownerId: Long): List<ParkingSpaceEntity>

    @Query("SELECT * FROM parking_spaces WHERE parkingId = :id LIMIT 1")
    suspend fun findById(id: String): ParkingSpaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(space: ParkingSpaceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(spaces: List<ParkingSpaceEntity>)

    @Query("DELETE FROM parking_spaces WHERE parkingId = :parkingId AND ownerUserId = :ownerId")
    suspend fun deleteOwned(parkingId: String, ownerId: Long): Int

    @Query("SELECT COUNT(*) FROM parking_spaces")
    suspend fun count(): Int
}
