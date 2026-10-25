package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Appointment
import com.example.data.model.Billing
import com.example.data.model.BillingRecord
import com.example.data.model.Doctor
import com.example.data.model.LabResultRecord
import com.example.data.model.MedicalPersonnel
import com.example.data.model.MedicalRecord
import com.example.data.model.Patient
import com.example.data.model.Prescription
import com.example.data.model.TreatmentPlan
import com.example.data.model.WardBed
import com.example.data.repository.HospitalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardMetrics(
    val totalPatients: Int = 0,
    val admittedPatients: Int = 0,
    val activeDoctors: Int = 0,
    val activeAppointments: Int = 0,
    val occupiedBeds: Int = 0,
    val totalBeds: Int = 0,
    val totalRevenue: Double = 0.0,
    val pendingAmount: Double = 0.0
)

class HospitalViewModel(private val repository: HospitalRepository) : ViewModel() {

    // Patients
    val rawPatients: StateFlow<List<Patient>> = repository.allPatients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _patientSearchQuery = MutableStateFlow("")
    val patientSearchQuery = _patientSearchQuery.asStateFlow()

    private val _patientStatusFilter = MutableStateFlow("All")
    val patientStatusFilter = _patientStatusFilter.asStateFlow()

    val filteredPatients: StateFlow<List<Patient>> = combine(
        rawPatients,
        _patientSearchQuery,
        _patientStatusFilter
    ) { patients, query, filter ->
        val cleanQuery = query.trim()
        val digitsOnly = cleanQuery.replace(Regex("[^0-9]"), "")
        val queryWithoutPrefix = cleanQuery
            .removePrefix("#")
            .removePrefix("ID:")
            .removePrefix("id:")
            .removePrefix("Id:")
            .removePrefix("ID")
            .removePrefix("id")
            .removePrefix("PAT-")
            .removePrefix("pat-")
            .removePrefix("MRN-")
            .removePrefix("mrn-")
            .trim()

        patients.filter { patient ->
            val matchesQuery = cleanQuery.isBlank() ||
                patient.fullName.contains(cleanQuery, ignoreCase = true) ||
                patient.id.toString() == cleanQuery ||
                patient.id.toString() == queryWithoutPrefix ||
                (digitsOnly.isNotEmpty() && (patient.id.toString() == digitsOnly || patient.mrn.contains(digitsOnly))) ||
                patient.mrn.contains(cleanQuery, ignoreCase = true) ||
                patient.mrn.contains(queryWithoutPrefix, ignoreCase = true) ||
                patient.diagnosis.contains(cleanQuery, ignoreCase = true) ||
                patient.contactPhone.contains(cleanQuery) ||
                patient.email.contains(cleanQuery, ignoreCase = true)

            val matchesFilter = when (filter) {
                "All" -> true
                "OPD" -> patient.status == "OPD"
                "IPD" -> patient.status == "IPD (Admitted)" || patient.status == "IPD"
                "ICU" -> patient.status == "ICU"
                "Discharged" -> patient.status == "Discharged"
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Doctors
    val rawDoctors: StateFlow<List<Doctor>> = repository.allDoctors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _doctorSearchQuery = MutableStateFlow("")
    val doctorSearchQuery = _doctorSearchQuery.asStateFlow()

    private val _doctorSpecialtyFilter = MutableStateFlow("All")
    val doctorSpecialtyFilter = _doctorSpecialtyFilter.asStateFlow()

    val filteredDoctors: StateFlow<List<Doctor>> = combine(
        rawDoctors,
        _doctorSearchQuery,
        _doctorSpecialtyFilter
    ) { doctors, query, filter ->
        doctors.filter { doc ->
            val matchesQuery = query.isBlank() ||
                doc.fullName.contains(query, ignoreCase = true) ||
                doc.specialty.contains(query, ignoreCase = true) ||
                doc.roomNumber.contains(query, ignoreCase = true)

            val matchesFilter = (filter == "All" || doc.specialty == filter)
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Appointments
    val rawAppointments: StateFlow<List<Appointment>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _appointmentStatusFilter = MutableStateFlow("All")
    val appointmentStatusFilter = _appointmentStatusFilter.asStateFlow()

    private val _appointmentDoctorFilter = MutableStateFlow("All Doctors")
    val appointmentDoctorFilter = _appointmentDoctorFilter.asStateFlow()

    private val _appointmentSearchQuery = MutableStateFlow("")
    val appointmentSearchQuery = _appointmentSearchQuery.asStateFlow()

    val filteredAppointments: StateFlow<List<Appointment>> = combine(
        rawAppointments,
        _appointmentStatusFilter,
        _appointmentDoctorFilter,
        _appointmentSearchQuery
    ) { appointments, statusFilter, docFilter, query ->
        appointments.filter { appt ->
            val matchesStatus = statusFilter == "All" || appt.status.equals(statusFilter, ignoreCase = true)
            val matchesDoctor = docFilter == "All Doctors" || appt.doctorName.equals(docFilter, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                appt.patientName.contains(query, ignoreCase = true) ||
                appt.doctorName.contains(query, ignoreCase = true) ||
                appt.doctorSpecialty.contains(query, ignoreCase = true) ||
                appt.reason.contains(query, ignoreCase = true) ||
                appt.notes.contains(query, ignoreCase = true) ||
                appt.tokenNumber.toString().contains(query)
            matchesStatus && matchesDoctor && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Wards & Beds
    val rawBeds: StateFlow<List<WardBed>> = repository.allBeds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _wardFilter = MutableStateFlow("All Wards")
    val wardFilter = _wardFilter.asStateFlow()

    val filteredBeds: StateFlow<List<WardBed>> = combine(
        rawBeds,
        _wardFilter
    ) { beds, filter ->
        if (filter == "All Wards") {
            beds
        } else {
            beds.filter { it.wardName.equals(filter, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Billing
    val rawBills: StateFlow<List<Billing>> = repository.allBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _billingStatusFilter = MutableStateFlow("All")
    val billingStatusFilter = _billingStatusFilter.asStateFlow()

    val filteredBills: StateFlow<List<Billing>> = combine(
        rawBills,
        _billingStatusFilter
    ) { bills, filter ->
        if (filter == "All") {
            bills
        } else {
            bills.filter { it.paymentStatus.equals(filter, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Prescriptions
    val allPrescriptions: StateFlow<List<Prescription>> = repository.allPrescriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Medical Records (Diagnoses & Notes)
    val allMedicalRecords: StateFlow<List<MedicalRecord>> = repository.allMedicalRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lab Results
    val allLabResults: StateFlow<List<LabResultRecord>> = repository.allLabResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Treatment Plans
    val allTreatmentPlans: StateFlow<List<TreatmentPlan>> = repository.allTreatmentPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Medical Personnel Security & Authorization
    val authorizedStaffList = listOf(
        MedicalPersonnel("DOC-101", "Dr. Elena Rostova", "Senior Interventional Cardiologist", "Cardiology", "Full Clinical Access", "1234"),
        MedicalPersonnel("DOC-102", "Dr. Marcus Vance", "Head of Neurology", "Neurology", "Full Clinical Access", "1234"),
        MedicalPersonnel("DOC-103", "Dr. Priya Patel", "Pediatric Specialist", "Pediatrics", "Full Clinical Access", "1234"),
        MedicalPersonnel("NUR-201", "Evelyn Ward, RN", "Head Clinical Nurse", "Inpatient Care", "Full Clinical Access", "1234"),
        MedicalPersonnel("LAB-301", "Dr. David Sterling", "Director of Laboratory Medicine", "Diagnostics", "Diagnostics Only", "1234"),
        MedicalPersonnel("CMO-001", "Dr. Arthur Campbell", "Chief Medical Officer", "Administration", "Full Clinical Access", "1234")
    )

    private val _currentPersonnel = MutableStateFlow(authorizedStaffList[0])
    val currentPersonnel = _currentPersonnel.asStateFlow()

    private val _isAuthorizedSession = MutableStateFlow(true)
    val isAuthorizedSession = _isAuthorizedSession.asStateFlow()

    fun switchPersonnel(personnel: MedicalPersonnel) {
        _currentPersonnel.value = personnel
        _isAuthorizedSession.value = true
    }

    // Selected Patient Filter for EMR
    private val _selectedEmrPatientId = MutableStateFlow<Long?>(null)
    val selectedEmrPatientId = _selectedEmrPatientId.asStateFlow()

    fun selectEmrPatient(patientId: Long?) {
        _selectedEmrPatientId.value = patientId
    }

    // Combined Dashboard Metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        rawPatients,
        rawDoctors,
        rawAppointments,
        rawBeds,
        rawBills
    ) { patients, doctors, appointments, beds, bills ->
        val totalPatients = patients.size
        val admittedPatients = patients.count { it.status == "IPD (Admitted)" || it.status == "ICU" }
        val activeDoctors = doctors.count { it.isDutyActive && it.status == "Available" }
        val activeAppointments = appointments.count { it.status == "Scheduled" || it.status == "In Progress" }
        val occupiedBeds = beds.count { it.isOccupied }
        val totalBeds = beds.size
        val totalRevenue = bills.filter { it.paymentStatus == "Paid" }.sumOf { it.totalAmount }
        val pendingAmount = bills.filter { it.paymentStatus == "Pending" }.sumOf { it.totalAmount }

        DashboardMetrics(
            totalPatients = totalPatients,
            admittedPatients = admittedPatients,
            activeDoctors = activeDoctors,
            activeAppointments = activeAppointments,
            occupiedBeds = occupiedBeds,
            totalBeds = totalBeds,
            totalRevenue = totalRevenue,
            pendingAmount = pendingAmount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // Active Selected Patient for Detail Dialog / Editing
    private val _selectedPatient = MutableStateFlow<Patient?>(null)
    val selectedPatient = _selectedPatient.asStateFlow()

    fun selectPatient(patient: Patient?) {
        _selectedPatient.value = patient
    }

    // Filter controls
    fun setPatientSearchQuery(query: String) { _patientSearchQuery.value = query }
    fun setPatientStatusFilter(filter: String) { _patientStatusFilter.value = filter }
    fun setDoctorSearchQuery(query: String) { _doctorSearchQuery.value = query }
    fun setDoctorSpecialtyFilter(filter: String) { _doctorSpecialtyFilter.value = filter }
    fun setAppointmentStatusFilter(filter: String) { _appointmentStatusFilter.value = filter }
    fun setAppointmentDoctorFilter(filter: String) { _appointmentDoctorFilter.value = filter }
    fun setAppointmentSearchQuery(query: String) { _appointmentSearchQuery.value = query }
    fun setWardFilter(ward: String) { _wardFilter.value = ward }
    fun setBillingStatusFilter(filter: String) { _billingStatusFilter.value = filter }

    // Patient Actions (Comprehensive Registration Module)
    fun registerPatient(
        name: String,
        dateOfBirth: String,
        age: Int,
        gender: String,
        bloodGroup: String,
        phone: String,
        email: String,
        address: String,
        emergencyContact: String,
        primaryMedicalHistory: String,
        currentMedications: String = "",
        status: String,
        diagnosis: String,
        allergies: String,
        doctorId: Long?,
        doctorName: String?,
        bloodPressure: String = "120/80",
        pulseRate: Int = 72,
        oxygenLevel: Int = 98,
        temperature: Float = 98.6f
    ) {
        viewModelScope.launch {
            val mrnNumber = 1000 + (rawPatients.value.maxOfOrNull { it.id } ?: 0) + 1
            val newPatient = Patient(
                mrn = "MRN-$mrnNumber",
                fullName = name,
                dateOfBirth = dateOfBirth,
                age = age,
                gender = gender,
                bloodGroup = bloodGroup,
                contactPhone = phone,
                email = email,
                address = address,
                emergencyContact = emergencyContact,
                primaryMedicalHistory = primaryMedicalHistory,
                currentMedications = currentMedications,
                status = status,
                diagnosis = diagnosis,
                allergies = allergies.ifBlank { "None Known" },
                assignedDoctorId = doctorId,
                assignedDoctorName = doctorName,
                bloodPressure = bloodPressure,
                pulseRate = pulseRate,
                oxygenLevel = oxygenLevel,
                temperature = temperature
            )
            val newId = repository.insertPatient(newPatient)

            // Also create an initial medical diagnosis record if diagnosis provided
            if (diagnosis.isNotBlank()) {
                repository.insertMedicalRecord(
                    MedicalRecord(
                        patientId = newId,
                        patientName = name,
                        recordType = "Diagnosis",
                        title = "Initial Clinical Intake Diagnosis",
                        doctorOrStaffName = doctorName ?: _currentPersonnel.value.name,
                        department = "Intake & Triage",
                        content = "Patient registered with: $diagnosis. Medical History: ${primaryMedicalHistory.ifBlank { "None documented" }}. Active Medications: ${currentMedications.ifBlank { "None reported" }}.",
                        severityOrStatus = if (status == "ICU") "Critical" else "Normal"
                    )
                )
            }
        }
    }

    fun updatePatientVitals(
        patientId: Long,
        bp: String,
        pulse: Int,
        spo2: Int,
        temperature: Float
    ) {
        viewModelScope.launch {
            val current = rawPatients.value.find { it.id == patientId } ?: return@launch
            val updated = current.copy(
                bloodPressure = bp,
                pulseRate = pulse,
                oxygenLevel = spo2,
                temperature = temperature
            )
            repository.updatePatient(updated)
            if (_selectedPatient.value?.id == patientId) {
                _selectedPatient.value = updated
            }
        }
    }

    fun dischargePatient(patientId: Long) {
        viewModelScope.launch {
            val patient = rawPatients.value.find { it.id == patientId } ?: return@launch
            val bed = rawBeds.value.find { it.occupiedPatientId == patientId }
            if (bed != null) {
                repository.dischargeBed(bed.id)
            }
            val updated = patient.copy(
                status = "Discharged",
                assignedWard = null,
                assignedBedNumber = null
            )
            repository.updatePatient(updated)
            if (_selectedPatient.value?.id == patientId) {
                _selectedPatient.value = updated
            }

            // Create a Discharge Summary record in EMR
            repository.insertMedicalRecord(
                MedicalRecord(
                    patientId = patientId,
                    patientName = patient.fullName,
                    recordType = "Discharge Summary",
                    title = "Clinical Discharge & Care Plan",
                    doctorOrStaffName = _currentPersonnel.value.name,
                    department = "Clinical Care",
                    content = "Patient vitals stable upon discharge. Diagnosis resolved/managed: ${patient.diagnosis}. Advised follow-up consultation in 10 days.",
                    severityOrStatus = "Resolved"
                )
            )
        }
    }

    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            val bed = rawBeds.value.find { it.occupiedPatientId == patient.id }
            if (bed != null) {
                repository.dischargeBed(bed.id)
            }
            repository.deletePatient(patient)
            if (_selectedPatient.value?.id == patient.id) {
                _selectedPatient.value = null
            }
        }
    }

    // Doctor Actions
    fun addDoctor(
        fullName: String,
        specialty: String,
        qualification: String,
        experienceYears: Int,
        consultationFee: Double,
        roomNumber: String,
        contactPhone: String,
        email: String,
        availableHours: String
    ) {
        viewModelScope.launch {
            val doctor = Doctor(
                fullName = fullName,
                specialty = specialty,
                qualification = qualification,
                experienceYears = experienceYears,
                consultationFee = consultationFee,
                roomNumber = roomNumber,
                contactPhone = contactPhone,
                email = email,
                availableHours = availableHours,
                isDutyActive = true,
                status = "Available"
            )
            repository.insertDoctor(doctor)
        }
    }

    fun toggleDoctorDuty(doctor: Doctor) {
        viewModelScope.launch {
            val newDuty = !doctor.isDutyActive
            val newStatus = if (newDuty) "Available" else "On Leave"
            repository.updateDoctor(doctor.copy(isDutyActive = newDuty, status = newStatus))
        }
    }

    fun setDoctorStatus(doctor: Doctor, status: String) {
        viewModelScope.launch {
            val isDuty = status != "On Leave"
            repository.updateDoctor(doctor.copy(status = status, isDutyActive = isDuty))
        }
    }

    // Appointment Scheduling Actions (with Confirmation & Reminders)
    fun scheduleAppointment(
        patientId: Long,
        patientName: String,
        doctorId: Long,
        doctorName: String,
        doctorSpecialty: String,
        dateMillis: Long = System.currentTimeMillis(),
        dateString: String = "Today",
        timeSlot: String,
        reason: String,
        notes: String
    ) {
        viewModelScope.launch {
            val nextToken = (rawAppointments.value.maxOfOrNull { it.tokenNumber } ?: 0) + 1
            val appointment = Appointment(
                patientId = patientId,
                patientName = patientName,
                doctorId = doctorId,
                doctorName = doctorName,
                doctorSpecialty = doctorSpecialty,
                tokenNumber = nextToken,
                dateMillis = dateMillis,
                dateString = dateString,
                timeSlot = timeSlot,
                reason = reason,
                status = "Scheduled",
                isConfirmed = true,
                reminderSent = true,
                notes = notes
            )
            repository.insertAppointment(appointment)
        }
    }

    fun rescheduleAppointment(
        appointment: Appointment,
        newDateMillis: Long,
        newDateString: String,
        newTimeSlot: String
    ) {
        viewModelScope.launch {
            repository.updateAppointment(
                appointment.copy(
                    dateMillis = newDateMillis,
                    dateString = newDateString,
                    timeSlot = newTimeSlot,
                    reminderSent = false,
                    isConfirmed = true
                )
            )
        }
    }

    fun sendAppointmentReminder(appointment: Appointment) {
        viewModelScope.launch {
            repository.updateAppointment(appointment.copy(reminderSent = true))
        }
    }

    fun confirmAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.updateAppointment(appointment.copy(isConfirmed = true))
        }
    }

    fun updateAppointmentStatus(appointment: Appointment, newStatus: String) {
        viewModelScope.launch {
            repository.updateAppointment(appointment.copy(status = newStatus))
        }
    }

    fun cancelAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.updateAppointment(appointment.copy(status = "Cancelled"))
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.deleteAppointment(appointment)
        }
    }

    // Ward Bed Actions
    fun allocateBed(
        bedId: Long,
        patientId: Long,
        patientName: String
    ) {
        viewModelScope.launch {
            repository.allocateBed(bedId, patientId, patientName, System.currentTimeMillis())
            val bed = rawBeds.value.find { it.id == bedId }
            val patient = rawPatients.value.find { it.id == patientId }
            if (patient != null && bed != null) {
                val newStatus = if (bed.wardName == "ICU") "ICU" else "IPD (Admitted)"
                repository.updatePatient(
                    patient.copy(
                        status = newStatus,
                        assignedWard = bed.wardName,
                        assignedBedNumber = bed.bedNumber
                    )
                )
            }
        }
    }

    fun vacateBed(bedId: Long) {
        viewModelScope.launch {
            val bed = rawBeds.value.find { it.id == bedId }
            val patientId = bed?.occupiedPatientId
            repository.dischargeBed(bedId)
            if (patientId != null) {
                val patient = rawPatients.value.find { it.id == patientId }
                if (patient != null) {
                    repository.updatePatient(
                        patient.copy(
                            assignedWard = null,
                            assignedBedNumber = null
                        )
                    )
                }
            }
        }
    }

    // Billing Actions
    fun createBill(
        patientId: Long,
        patientName: String,
        amount: Double,
        paymentStatus: String = "Pending",
        date: String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
        paymentMethod: String = "Cash",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val invNumber = "INV-2026-${(rawBills.value.size + 101)}"
            val bill = Billing(
                billNumber = invNumber,
                patientId = patientId,
                patientName = patientName,
                date = date,
                dateMillis = System.currentTimeMillis(),
                amount = amount,
                totalAmount = amount,
                consultationFee = amount,
                paymentStatus = paymentStatus,
                paymentMethod = paymentMethod,
                notes = notes
            )
            repository.insertBill(bill)
        }
    }

    fun generateBill(
        patientId: Long,
        patientName: String,
        consultationFee: Double,
        wardCharges: Double,
        medicineCharges: Double,
        labTestCharges: Double,
        discount: Double,
        paymentStatus: String,
        paymentMethod: String,
        notes: String,
        date: String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    ) {
        viewModelScope.launch {
            val total = (consultationFee + wardCharges + medicineCharges + labTestCharges - discount).coerceAtLeast(0.0)
            val invNumber = "INV-2026-${(rawBills.value.size + 101)}"
            val bill = Billing(
                billNumber = invNumber,
                patientId = patientId,
                patientName = patientName,
                date = date,
                dateMillis = System.currentTimeMillis(),
                amount = total,
                totalAmount = total,
                consultationFee = consultationFee,
                wardCharges = wardCharges,
                medicineCharges = medicineCharges,
                labTestCharges = labTestCharges,
                discount = discount,
                paymentStatus = paymentStatus,
                paymentMethod = paymentMethod,
                notes = notes
            )
            repository.insertBill(bill)
        }
    }

    fun markBillPaid(billId: Long) {
        viewModelScope.launch {
            repository.markBillAsPaid(billId)
        }
    }

    fun updateBillPaymentStatus(billId: Long, status: String) {
        viewModelScope.launch {
            repository.updatePaymentStatus(billId, status)
        }
    }

    fun deleteBill(bill: Billing) {
        viewModelScope.launch {
            repository.deleteBill(bill)
        }
    }

    // Medical Records Management Actions
    fun addMedicalRecord(
        patientId: Long,
        patientName: String,
        recordType: String,
        title: String,
        content: String,
        severity: String,
        doctorOrStaff: String,
        department: String
    ) {
        viewModelScope.launch {
            val record = MedicalRecord(
                patientId = patientId,
                patientName = patientName,
                recordType = recordType,
                title = title,
                content = content,
                severityOrStatus = severity,
                doctorOrStaffName = doctorOrStaff,
                department = department
            )
            repository.insertMedicalRecord(record)
        }
    }

    fun deleteMedicalRecord(record: MedicalRecord) {
        viewModelScope.launch {
            repository.deleteMedicalRecord(record)
        }
    }

    // Lab Results Actions
    fun addLabResult(
        patientId: Long,
        patientName: String,
        testName: String,
        category: String,
        performedBy: String,
        specimen: String,
        parameters: String,
        conclusion: String,
        status: String
    ) {
        viewModelScope.launch {
            val lab = LabResultRecord(
                patientId = patientId,
                patientName = patientName,
                testName = testName,
                category = category,
                performedBy = performedBy,
                specimen = specimen,
                parametersSummary = parameters,
                conclusion = conclusion,
                status = status
            )
            repository.insertLabResult(lab)
        }
    }

    // Treatment Plans Actions
    fun addTreatmentPlan(
        patientId: Long,
        patientName: String,
        title: String,
        attendingDoctor: String,
        goals: String,
        interventions: String,
        dietary: String
    ) {
        viewModelScope.launch {
            val plan = TreatmentPlan(
                patientId = patientId,
                patientName = patientName,
                planTitle = title,
                attendingDoctor = attendingDoctor,
                clinicalGoals = goals,
                activeInterventions = interventions,
                dietaryRestrictions = dietary,
                status = "Active"
            )
            repository.insertTreatmentPlan(plan)
        }
    }

    fun updateTreatmentPlanStatus(plan: TreatmentPlan, newStatus: String) {
        viewModelScope.launch {
            repository.updateTreatmentPlan(plan.copy(status = newStatus))
        }
    }

    // Prescription Actions
    fun addPrescription(
        patientId: Long,
        patientName: String,
        doctorId: Long = 0,
        doctorName: String,
        medicationName: String = "",
        dosage: String = "",
        frequency: String = "",
        duration: String = "",
        diagnosis: String,
        medicines: String,
        instructions: String = "Take with warm water after meals."
    ) {
        viewModelScope.launch {
            val pres = Prescription(
                patientId = patientId,
                patientName = patientName,
                doctorId = doctorId,
                doctorName = doctorName,
                medicationName = medicationName,
                dosage = dosage,
                frequency = frequency,
                duration = duration,
                diagnosis = diagnosis,
                medicines = medicines,
                instructions = instructions
            )
            repository.insertPrescription(pres)
        }
    }
}

class HospitalViewModelFactory(private val repository: HospitalRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HospitalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HospitalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
