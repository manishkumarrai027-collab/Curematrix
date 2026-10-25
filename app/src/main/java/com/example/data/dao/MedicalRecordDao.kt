package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MedicalRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalRecordDao {
    @Query("SELECT * FROM medical_records ORDER BY dateMillis DESC")
    fun getAllRecords(): Flow<List<MedicalRecord>>

    @Query("SELECT * FROM medical_records WHERE patientId = :patientId ORDER BY dateMillis DESC")
    fun getRecordsForPatient(patientId: Long): Flow<List<MedicalRecord>>

    @Query("SELECT * FROM medical_records WHERE recordType = :recordType ORDER BY dateMillis DESC")
    fun getRecordsByType(recordType: String): Flow<List<MedicalRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MedicalRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<MedicalRecord>)

    @Update
    suspend fun updateRecord(record: MedicalRecord)

    @Delete
    suspend fun deleteRecord(record: MedicalRecord)

    @Query("DELETE FROM medical_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
