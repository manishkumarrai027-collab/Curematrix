package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ward_beds")
data class WardBed(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wardName: String, // "ICU", "General Ward A", "Semi-Private", "Pediatric Ward", "Emergency Unit"
    val bedNumber: String, // "Bed 01", "ICU-02", etc.
    val isOccupied: Boolean = false,
    val occupiedPatientId: Long? = null,
    val occupiedPatientName: String? = null,
    val admissionDateMillis: Long? = null,
    val dailyRate: Double = 150.0
)
