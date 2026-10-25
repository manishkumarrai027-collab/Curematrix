package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Prescription
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Prescription records in Room.
 * Provides reactive queries to link prescriptions to patients and doctors.
 */
@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions ORDER BY id DESC")
    fun getAllPrescriptions(): Flow<List<Prescription>>

    @Query("SELECT * FROM prescriptions WHERE id = :id LIMIT 1")
    fun getPrescriptionById(id: Long): Flow<Prescription?>

    @Query("SELECT * FROM prescriptions WHERE patientId = :patientId ORDER BY id DESC")
    fun getPrescriptionsForPatient(patientId: Long): Flow<List<Prescription>>

    @Query("SELECT * FROM prescriptions WHERE doctorId = :doctorId ORDER BY id DESC")
    fun getPrescriptionsForDoctor(doctorId: Long): Flow<List<Prescription>>

    @Query("SELECT * FROM prescriptions WHERE patientId = :patientId AND doctorId = :doctorId ORDER BY id DESC")
    fun getPrescriptionsForPatientAndDoctor(patientId: Long, doctorId: Long): Flow<List<Prescription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: Prescription): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prescriptions: List<Prescription>)

    @Update
    suspend fun updatePrescription(prescription: Prescription)

    @Delete
    suspend fun deletePrescription(prescription: Prescription)

    @Query("DELETE FROM prescriptions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
