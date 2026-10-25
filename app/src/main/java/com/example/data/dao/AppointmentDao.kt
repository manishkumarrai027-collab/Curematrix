package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Appointment
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Appointment scheduling in Room.
 * Links Patients and Doctors across date and time fields.
 */
@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY id DESC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    fun getAppointmentById(id: Long): Flow<Appointment?>

    @Query("SELECT * FROM appointments WHERE patientId = :patientId ORDER BY id DESC")
    fun getAppointmentsForPatient(patientId: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId ORDER BY id DESC")
    fun getAppointmentsForDoctor(doctorId: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE patientId = :patientId AND doctorId = :doctorId ORDER BY id DESC")
    fun getAppointmentsForPatientAndDoctor(patientId: Long, doctorId: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE appointmentDate = :dateString OR dateString = :dateString ORDER BY appointmentTime ASC")
    fun getAppointmentsByDate(dateString: String): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE status = :status ORDER BY id DESC")
    fun getAppointmentsByStatus(status: String): Flow<List<Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appointments: List<Appointment>)

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM appointments WHERE status = 'Scheduled' OR status = 'In Progress'")
    fun getActiveAppointmentCount(): Flow<Int>
}
