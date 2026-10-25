package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Doctor
import kotlinx.coroutines.flow.Flow

@Dao
interface DoctorDao {
    @Query("SELECT * FROM doctors ORDER BY fullName ASC")
    fun getAllDoctors(): Flow<List<Doctor>>

    @Query("SELECT * FROM doctors WHERE id = :id LIMIT 1")
    fun getDoctorById(id: Long): Flow<Doctor?>

    @Query("SELECT * FROM doctors WHERE specialty = :specialty ORDER BY fullName ASC")
    fun getDoctorsBySpecialty(specialty: String): Flow<List<Doctor>>

    @Query("SELECT * FROM doctors WHERE fullName LIKE '%' || :query || '%' OR specialty LIKE '%' || :query || '%' OR roomNumber LIKE '%' || :query || '%' ORDER BY fullName ASC")
    fun searchDoctors(query: String): Flow<List<Doctor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctor(doctor: Doctor): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(doctors: List<Doctor>)

    @Update
    suspend fun updateDoctor(doctor: Doctor)

    @Delete
    suspend fun deleteDoctor(doctor: Doctor)

    @Query("DELETE FROM doctors WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM doctors WHERE isDutyActive = 1")
    fun getActiveDoctorCount(): Flow<Int>
}
