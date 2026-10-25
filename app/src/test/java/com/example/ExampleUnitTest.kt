package com.example

import com.example.util.DateUtils
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testDateUtils_isSameDay() {
    val now = System.currentTimeMillis()
    val startOfDay = DateUtils.getStartOfDay(now)
    assertTrue(DateUtils.isSameDay(now, startOfDay))

    val tomorrow = startOfDay + 86400000L * 2
    assertFalse(DateUtils.isSameDay(now, tomorrow))
  }

  @Test
  fun testDateUtils_monthMatrix() {
    // October 2026 (month 9 in 0-indexed)
    val matrix = DateUtils.getMonthMatrix(2026, 9)
    assertTrue(matrix.size == 35 || matrix.size == 42)
    // Check that October has 31 days
    val octDays = matrix.filter { it.month == 9 && it.year == 2026 }
    assertEquals(31, octDays.size)
  }

  @Test
  fun testDateUtils_relativeTitle() {
    val today = System.currentTimeMillis()
    assertEquals("Today", DateUtils.getRelativeDateTitle(today))

    val tomorrow = today + 86400000L
    assertEquals("Tomorrow", DateUtils.getRelativeDateTitle(tomorrow))

    val yesterday = today - 86400000L
    assertEquals("Yesterday", DateUtils.getRelativeDateTitle(yesterday))
  }

  @Test
  fun testDoctorEntity() {
    val doctor = com.example.data.model.Doctor(
      id = 10L,
      fullName = "Dr. Alice Smith",
      specialty = "Cardiology",
      qualification = "MD, FACC",
      experienceYears = 12,
      consultationFee = 150.0,
      roomNumber = "OPD-101",
      contactPhone = "+1 555-0100",
      email = "alice@hospital.org"
    )
    assertEquals("Dr. Alice Smith", doctor.fullName)
    assertEquals("Cardiology", doctor.specialty)
    assertEquals("+1 555-0100", doctor.contactPhone)
    assertEquals("alice@hospital.org", doctor.email)
  }

  @Test
  fun testPrescriptionEntity_linkingPatientAndDoctorWithDosageAndFrequency() {
    val prescription = com.example.data.model.Prescription(
      id = 1L,
      patientId = 101L,
      patientName = "Arthur Pendelton",
      doctorId = 10L,
      doctorName = "Dr. Alice Smith",
      medicationName = "Amoxicillin",
      dosage = "500 mg",
      frequency = "Twice daily (BID)",
      duration = "7 days",
      diagnosis = "Bacterial Sinusitis"
    )
    assertEquals(101L, prescription.patientId)
    assertEquals(10L, prescription.doctorId)
    assertEquals("Amoxicillin", prescription.medicationName)
    assertEquals("500 mg", prescription.dosage)
    assertEquals("Twice daily (BID)", prescription.frequency)
    assertEquals("7 days", prescription.duration)
  }

  @Test
  fun testAppointmentEntity_linkingPatientAndDoctorWithDateAndTime() {
    val appointment = com.example.data.model.Appointment(
      id = 1L,
      patientId = 101L,
      patientName = "Arthur Pendelton",
      doctorId = 10L,
      doctorName = "Dr. Alice Smith",
      doctorSpecialty = "Cardiology",
      appointmentDate = "2026-10-02",
      appointmentTime = "10:30 AM",
      tokenNumber = 1,
      reason = "Cardiac review"
    )
    assertEquals(101L, appointment.patientId)
    assertEquals(10L, appointment.doctorId)
    assertEquals("2026-10-02", appointment.appointmentDate)
    assertEquals("10:30 AM", appointment.appointmentTime)
    assertEquals("Scheduled", appointment.status)
  }

  @Test
  fun testBillingEntity_trackingMedicalCostsWithDateAmountAndPaymentStatus() {
    val billing = com.example.data.model.Billing(
      id = 1L,
      patientId = 101L,
      patientName = "Arthur Pendelton",
      date = "2026-10-02",
      amount = 325.50,
      paymentStatus = "Pending",
      paymentMethod = "Health Insurance",
      billNumber = "INV-2026-999",
      consultationFee = 150.0,
      medicineCharges = 175.50,
      notes = "Initial cardiology workup and prescription fulfillment"
    )
    assertEquals(101L, billing.patientId)
    assertEquals("Arthur Pendelton", billing.patientName)
    assertEquals("2026-10-02", billing.date)
    assertEquals(325.50, billing.amount, 0.001)
    assertEquals("Pending", billing.paymentStatus)
    assertEquals("Health Insurance", billing.paymentMethod)
    assertEquals("INV-2026-999", billing.billNumber)

    val updatedBilling = billing.copy(paymentStatus = "Paid")
    assertEquals("Paid", updatedBilling.paymentStatus)
  }

  @Test
  fun testMonthlyBillingAggregation() {
    val bills = listOf(
      com.example.data.model.Billing(patientId = 1L, patientName = "A", date = "2026-10-01", amount = 500.0, paymentStatus = "Paid"),
      com.example.data.model.Billing(patientId = 2L, patientName = "B", date = "2026-10-02", amount = 300.0, paymentStatus = "Pending"),
      com.example.data.model.Billing(patientId = 3L, patientName = "C", date = "2026-09-15", amount = 700.0, paymentStatus = "Paid")
    )
    val totalRevenue = bills.filter { it.paymentStatus == "Paid" }.sumOf { it.amount }
    val totalPending = bills.filter { it.paymentStatus == "Pending" }.sumOf { it.amount }

    assertEquals(1200.0, totalRevenue, 0.001)
    assertEquals(300.0, totalPending, 0.001)
    assertEquals(1500.0, bills.sumOf { it.amount }, 0.001)
  }

  @Test
  fun testPatientAdmissionDistribution() {
    val patients = listOf(
      com.example.data.model.Patient(mrn = "MRN-1", fullName = "P1", age = 45, gender = "M", bloodGroup = "O+", contactPhone = "123", status = "ICU"),
      com.example.data.model.Patient(mrn = "MRN-2", fullName = "P2", age = 30, gender = "F", bloodGroup = "A+", contactPhone = "456", status = "IPD (Admitted)"),
      com.example.data.model.Patient(mrn = "MRN-3", fullName = "P3", age = 60, gender = "M", bloodGroup = "B+", contactPhone = "789", status = "OPD")
    )
    val icuCount = patients.count { it.status == "ICU" }
    val ipdCount = patients.count { it.status == "IPD (Admitted)" }
    val opdCount = patients.count { it.status == "OPD" }

    assertEquals(1, icuCount)
    assertEquals(1, ipdCount)
    assertEquals(1, opdCount)
    assertEquals(3, patients.size)
  }

  @Test
  fun testPatientMedicalReportData_forPrintablePdfExport() {
    val patient = com.example.data.model.Patient(
      id = 10L,
      mrn = "MRN-2026-001",
      fullName = "Arthur Pendelton",
      age = 58,
      gender = "Male",
      bloodGroup = "O+",
      contactPhone = "+1 (555) 123-4567",
      status = "ICU",
      diagnosis = "Acute Coronary Syndrome",
      allergies = "Penicillin, NSAIDs",
      primaryMedicalHistory = "Prior PCI in 2022, Type-2 Diabetes Mellitus"
    )

    val prescriptions = listOf(
      com.example.data.model.Prescription(
        id = 1L,
        patientId = 10L,
        patientName = "Arthur Pendelton",
        doctorName = "Dr. Elena Rostova",
        medicationName = "Aspirin & Clopidogrel (DAPT)",
        dosage = "81 mg / 75 mg",
        frequency = "Once daily with food",
        duration = "12 months",
        instructions = "Take after breakfast with water"
      ),
      com.example.data.model.Prescription(
        id = 2L,
        patientId = 10L,
        patientName = "Arthur Pendelton",
        doctorName = "Dr. Elena Rostova",
        medicationName = "Atorvastatin Calcium",
        dosage = "80 mg",
        frequency = "Once daily at bedtime (QHS)",
        duration = "Ongoing",
        instructions = "High-intensity statin therapy"
      )
    )

    val records = listOf(
      com.example.data.model.MedicalRecord(
        id = 1L,
        patientId = 10L,
        patientName = "Arthur Pendelton",
        recordType = "Diagnosis",
        title = "Acute Coronary Syndrome - LAD Occlusion",
        doctorOrStaffName = "Dr. Elena Rostova",
        department = "Cardiology",
        content = "Drug-eluting stent placed in mid-LAD. Good distal runoff restored.",
        severityOrStatus = "Critical"
      )
    )

    val reportData = com.example.util.PatientMedicalReportData(
      patient = patient,
      medicalRecords = records,
      prescriptions = prescriptions,
      generatedByStaff = "Dr. Elena Rostova"
    )

    assertEquals("Arthur Pendelton", reportData.patient.fullName)
    assertEquals("MRN-2026-001", reportData.patient.mrn)
    assertEquals("Penicillin, NSAIDs", reportData.patient.allergies)
    assertEquals(2, reportData.prescriptions.size)
    assertEquals(1, reportData.medicalRecords.size)
    assertEquals("Aspirin & Clopidogrel (DAPT)", reportData.prescriptions[0].medicationName)
    assertEquals("80 mg", reportData.prescriptions[1].dosage)
    assertEquals("Dr. Elena Rostova", reportData.generatedByStaff)
  }
}

