package com.example.data.model

data class MedicalPersonnel(
    val staffId: String,
    val name: String,
    val role: String, // "Chief Medical Officer", "Senior Attending Physician", "Resident Doctor", "Head Clinical Nurse", "Laboratory Specialist"
    val department: String,
    val accessLevel: String, // "Full Clinical Access", "Restricted Clinical Access", "Diagnostics Only"
    val pin: String = "1234"
)
