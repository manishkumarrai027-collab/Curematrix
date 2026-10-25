package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "treatment_plans")
data class TreatmentPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    val patientName: String,
    val planTitle: String,
    val attendingDoctor: String,
    val startDateMillis: Long = System.currentTimeMillis(),
    val targetEndDateMillis: Long? = null,
    val clinicalGoals: String,
    val activeInterventions: String,
    val dietaryRestrictions: String,
    val status: String = "Active" // "Active", "In Progress", "Completed", "Modified"
)
