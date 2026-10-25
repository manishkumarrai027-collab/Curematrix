package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Patient
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients ORDER BY id DESC")
    fun getAllPatients(): Flow<List<Patient>>

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    fun getPatientById(id: Long): Flow<Patient?>

    @Query("SELECT * FROM patients WHERE mrn = :mrn LIMIT 1")
    fun getPatientByMrn(mrn: String): Flow<Patient?>

    @Query("SELECT * FROM patients WHERE fullName LIKE '%' || :query || '%' OR mrn LIKE '%' || :query || '%' OR contactPhone LIKE '%' || :query || '%' OR diagnosis LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchPatients(query: String): Flow<List<Patient>>

    @Query("SELECT * FROM patients WHERE status = :status ORDER BY fullName ASC")
    fun getPatientsByStatus(status: String): Flow<List<Patient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patients: List<Patient>)

    @Update
    suspend fun updatePatient(patient: Patient)

    @Delete
    suspend fun deletePatient(patient: Patient)

    @Query("DELETE FROM patients WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM patients")
    fun getPatientCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM patients WHERE status = 'IPD (Admitted)' OR status = 'ICU'")
    fun getAdmittedCount(): Flow<Int>
}
