package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lab_results")
data class LabResultRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    val patientName: String,
    val testName: String,
    val category: String, // "Hematology", "Biochemistry", "Radiology", "Cardiology", "Pathology"
    val dateMillis: Long = System.currentTimeMillis(),
    val performedBy: String,
    val specimen: String,
    val parametersSummary: String, // e.g. "Troponin-I: 1.45 ng/mL [Ref: <0.04] (HIGH)\nCPK-MB: 48 U/L [Ref: 0-25]"
    val conclusion: String,
    val status: String = "Normal", // "Normal", "Abnormal", "Critical High", "Critical Low"
    val isVerified: Boolean = true
)
