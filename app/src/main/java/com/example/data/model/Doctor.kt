package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctors")
data class Doctor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val specialty: String,
    val qualification: String,
    val experienceYears: Int,
    val consultationFee: Double,
    val roomNumber: String,
    val contactPhone: String,
    val email: String,
    val availableHours: String = "09:00 AM - 05:00 PM",
    val isDutyActive: Boolean = true,
    val status: String = "Available" // "Available", "In Surgery", "On Leave"
)
