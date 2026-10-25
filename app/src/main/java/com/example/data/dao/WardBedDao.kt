package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WardBed
import kotlinx.coroutines.flow.Flow

@Dao
interface WardBedDao {
    @Query("SELECT * FROM ward_beds ORDER BY wardName ASC, bedNumber ASC")
    fun getAllBeds(): Flow<List<WardBed>>

    @Query("SELECT * FROM ward_beds WHERE wardName = :wardName ORDER BY bedNumber ASC")
    fun getBedsByWard(wardName: String): Flow<List<WardBed>>

    @Query("SELECT * FROM ward_beds WHERE id = :id LIMIT 1")
    fun getBedById(id: Long): Flow<WardBed?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBed(bed: WardBed): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(beds: List<WardBed>)

    @Update
    suspend fun updateBed(bed: WardBed)

    @Delete
    suspend fun deleteBed(bed: WardBed)

    @Query("UPDATE ward_beds SET isOccupied = 1, occupiedPatientId = :patientId, occupiedPatientName = :patientName, admissionDateMillis = :timestamp WHERE id = :bedId")
    suspend fun allocateBed(bedId: Long, patientId: Long, patientName: String, timestamp: Long)

    @Query("UPDATE ward_beds SET isOccupied = 0, occupiedPatientId = NULL, occupiedPatientName = NULL, admissionDateMillis = NULL WHERE id = :bedId")
    suspend fun dischargeBed(bedId: Long)

    @Query("SELECT COUNT(*) FROM ward_beds WHERE isOccupied = 1")
    fun getOccupiedBedsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ward_beds")
    fun getTotalBedsCount(): Flow<Int>
}
