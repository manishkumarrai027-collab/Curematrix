package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medical_records")
data class MedicalRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    val patientName: String,
    val recordType: String, // "Diagnosis", "Progress Note", "Clinical Consultation", "Discharge Summary"
    val title: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val doctorOrStaffName: String,
    val department: String,
    val content: String,
    val severityOrStatus: String = "Normal", // "Normal", "Attention Required", "Critical", "Resolved"
    val isConfidential: Boolean = false
)
