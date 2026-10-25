package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Patient Entity with Room database support.
 * Stores patient demographics (Name, DOB, Age, Gender, Blood Group),
 * Contact Information (Phone, Email, Residential Address, Emergency Contact),
 * and Comprehensive Medical History (Chronic illnesses, surgeries, allergies,
 * current medications, and clinical diagnoses).
 */
@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mrn: String,
    val fullName: String,
    val dateOfBirth: String = "",
    val age: Int,
    val gender: String,
    val bloodGroup: String,
    val contactPhone: String,
    val email: String = "",
    val address: String = "",
    val emergencyContact: String = "",
    val primaryMedicalHistory: String = "",
    val currentMedications: String = "",
    val allergies: String = "None Known",
    val status: String = "OPD", // "OPD", "IPD (Admitted)", "ICU", "Discharged"
    val assignedDoctorId: Long? = null,
    val assignedDoctorName: String? = null,
    val assignedWard: String? = null,
    val assignedBedNumber: String? = null,
    val diagnosis: String = "",
    val admissionDateMillis: Long = System.currentTimeMillis(),
    val bloodPressure: String = "120/80",
    val pulseRate: Int = 72,
    val oxygenLevel: Int = 98,
    val temperature: Float = 98.6f
)
