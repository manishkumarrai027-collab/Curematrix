package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 'Billing' entity class to track medical costs for patients,
 * including date, amount, and payment status.
 *
 * @param id Unique database identifier (auto-generated primary key)
 * @param patientId Reference ID of the associated Patient
 * @param patientName Full name of the patient
 * @param date Billing date formatted as YYYY-MM-DD
 * @param amount Total medical cost amount for the billing statement
 * @param paymentStatus Current status of payment ("Pending", "Paid", "Partially Paid", "Insurance Claim")
 * @param dateMillis Timestamp in milliseconds for date sorting and queries
 * @param billNumber Hospital invoice identifier (e.g. INV-2026-101)
 * @param totalAmount Synonymous with amount for breakdown compatibility
 * @param paymentMethod Payment channel used (Cash, Credit Card, Health Insurance, UPI)
 * @param consultationFee Doctor consultation charges component
 * @param wardCharges Inpatient bed and ward accommodation charges component
 * @param medicineCharges Pharmacy and medications cost component
 * @param labTestCharges Diagnostic and laboratory test charges component
 * @param discount Concession, subsidy or waiver applied
 * @param notes Additional billing remarks or payment transaction reference
 */
@Entity(tableName = "billing")
data class Billing(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long = 0,
    val patientName: String = "",
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val amount: Double = 0.0,
    val paymentStatus: String = "Pending",
    val dateMillis: Long = System.currentTimeMillis(),
    val billNumber: String = "",
    val totalAmount: Double = amount,
    val paymentMethod: String = "Cash",
    val consultationFee: Double = 0.0,
    val wardCharges: Double = 0.0,
    val medicineCharges: Double = 0.0,
    val labTestCharges: Double = 0.0,
    val discount: Double = 0.0,
    val notes: String = ""
)
