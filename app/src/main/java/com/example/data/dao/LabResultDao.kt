package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LabResultRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface LabResultDao {
    @Query("SELECT * FROM lab_results ORDER BY dateMillis DESC")
    fun getAllLabResults(): Flow<List<LabResultRecord>>

    @Query("SELECT * FROM lab_results WHERE patientId = :patientId ORDER BY dateMillis DESC")
    fun getLabResultsForPatient(patientId: Long): Flow<List<LabResultRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabResult(labResult: LabResultRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(results: List<LabResultRecord>)

    @Update
    suspend fun updateLabResult(labResult: LabResultRecord)

    @Delete
    suspend fun deleteLabResult(labResult: LabResultRecord)
}
