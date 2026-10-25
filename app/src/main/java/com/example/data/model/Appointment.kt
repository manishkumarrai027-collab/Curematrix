package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Appointment Entity with Room database support.
 * Manages appointment scheduling, linking a Patient and a Doctor
 * with explicit date and time fields.
 */
@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,                         // Linked Patient ID
    val patientName: String,
    val doctorId: Long,                          // Linked Doctor ID
    val doctorName: String,
    val doctorSpecialty: String = "",
    val appointmentDate: String = "Today",       // Date field (e.g. "2026-10-02" or "Today")
    val appointmentTime: String = "10:00 AM",     // Time field (e.g. "10:00 AM")
    val dateString: String = "Today",            // Formatted date label
    val timeSlot: String = "10:00 AM",           // Consultation time slot
    val dateMillis: Long = System.currentTimeMillis(),
    val tokenNumber: Int = 1,
    val reason: String = "",
    val status: String = "Scheduled",            // "Scheduled", "In Progress", "Completed", "Cancelled"
    val isConfirmed: Boolean = true,
    val reminderSent: Boolean = true,
    val notes: String = ""
)
