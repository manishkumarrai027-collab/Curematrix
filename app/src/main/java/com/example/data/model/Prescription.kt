package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Prescription Entity with Room database support.
 * Links prescribed medications to specific patients and doctors,
 * including structured fields for dosage and frequency.
 */
@Entity(tableName = "prescriptions")
data class Prescription(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    val patientName: String,
    val doctorId: Long = 0,
    val doctorName: String,
    val medicationName: String = "",
    val dosage: String = "",       // e.g. "500 mg", "10 ml", "1 tablet"
    val frequency: String = "",    // e.g. "Twice daily (BID)", "Once daily at bedtime (QHS)"
    val duration: String = "",     // e.g. "7 days", "14 days", "Ongoing"
    val dateMillis: Long = System.currentTimeMillis(),
    val diagnosis: String = "",
    val medicines: String = "",    // Detailed prescription multi-item list or summary
    val instructions: String = "Take with warm water after meals."
)
