package com.example.data.repository

import com.example.data.dao.AppointmentDao
import com.example.data.dao.BillingDao
import com.example.data.dao.DoctorDao
import com.example.data.dao.LabResultDao
import com.example.data.dao.MedicalRecordDao
import com.example.data.dao.PatientDao
import com.example.data.dao.PrescriptionDao
import com.example.data.dao.TreatmentPlanDao
import com.example.data.dao.WardBedDao
import com.example.data.model.Appointment
import com.example.data.model.Billing
import com.example.data.model.BillingRecord
import com.example.data.model.Doctor
import com.example.data.model.LabResultRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.Patient
import com.example.data.model.Prescription
import com.example.data.model.TreatmentPlan
import com.example.data.model.WardBed
import kotlinx.coroutines.flow.Flow

class HospitalRepository(
    private val patientDao: PatientDao,
    private val doctorDao: DoctorDao,
    private val appointmentDao: AppointmentDao,
    private val wardBedDao: WardBedDao,
    private val billingDao: BillingDao,
    private val prescriptionDao: PrescriptionDao,
    private val medicalRecordDao: MedicalRecordDao,
    private val labResultDao: LabResultDao,
    private val treatmentPlanDao: TreatmentPlanDao
) {
    // Patients
    val allPatients: Flow<List<Patient>> = patientDao.getAllPatients()
    val patientCount: Flow<Int> = patientDao.getPatientCount()
    val admittedPatientCount: Flow<Int> = patientDao.getAdmittedCount()

    fun getPatientById(id: Long): Flow<Patient?> = patientDao.getPatientById(id)
    fun getPatientByMrn(mrn: String): Flow<Patient?> = patientDao.getPatientByMrn(mrn)
    fun searchPatients(query: String): Flow<List<Patient>> = patientDao.searchPatients(query)
    fun getPatientsByStatus(status: String): Flow<List<Patient>> = patientDao.getPatientsByStatus(status)

    suspend fun insertPatient(patient: Patient): Long = patientDao.insertPatient(patient)
    suspend fun updatePatient(patient: Patient) = patientDao.updatePatient(patient)
    suspend fun deletePatient(patient: Patient) = patientDao.deletePatient(patient)
    suspend fun deletePatientById(id: Long) = patientDao.deleteById(id)

    // Doctors
    val allDoctors: Flow<List<Doctor>> = doctorDao.getAllDoctors()
    val activeDoctorCount: Flow<Int> = doctorDao.getActiveDoctorCount()

    fun getDoctorById(id: Long): Flow<Doctor?> = doctorDao.getDoctorById(id)
    fun getDoctorsBySpecialty(specialty: String): Flow<List<Doctor>> = doctorDao.getDoctorsBySpecialty(specialty)

    suspend fun insertDoctor(doctor: Doctor): Long = doctorDao.insertDoctor(doctor)
    suspend fun updateDoctor(doctor: Doctor) = doctorDao.updateDoctor(doctor)
    suspend fun deleteDoctor(doctor: Doctor) = doctorDao.deleteDoctor(doctor)

    // Appointments
    val allAppointments: Flow<List<Appointment>> = appointmentDao.getAllAppointments()
    val activeAppointmentCount: Flow<Int> = appointmentDao.getActiveAppointmentCount()

    fun getAppointmentsForPatient(patientId: Long): Flow<List<Appointment>> = appointmentDao.getAppointmentsForPatient(patientId)
    fun getAppointmentsForDoctor(doctorId: Long): Flow<List<Appointment>> = appointmentDao.getAppointmentsForDoctor(doctorId)
    fun getAppointmentsForPatientAndDoctor(patientId: Long, doctorId: Long): Flow<List<Appointment>> = appointmentDao.getAppointmentsForPatientAndDoctor(patientId, doctorId)
    fun getAppointmentById(id: Long): Flow<Appointment?> = appointmentDao.getAppointmentById(id)

    suspend fun insertAppointment(appointment: Appointment): Long = appointmentDao.insertAppointment(appointment)
    suspend fun updateAppointment(appointment: Appointment) = appointmentDao.updateAppointment(appointment)
    suspend fun deleteAppointment(appointment: Appointment) = appointmentDao.deleteAppointment(appointment)
    suspend fun deleteAppointmentById(id: Long) = appointmentDao.deleteById(id)

    // Wards & Beds
    val allBeds: Flow<List<WardBed>> = wardBedDao.getAllBeds()
    val occupiedBedsCount: Flow<Int> = wardBedDao.getOccupiedBedsCount()
    val totalBedsCount: Flow<Int> = wardBedDao.getTotalBedsCount()

    fun getBedsByWard(wardName: String): Flow<List<WardBed>> = wardBedDao.getBedsByWard(wardName)

    suspend fun insertBed(bed: WardBed): Long = wardBedDao.insertBed(bed)
    suspend fun updateBed(bed: WardBed) = wardBedDao.updateBed(bed)
    suspend fun allocateBed(bedId: Long, patientId: Long, patientName: String, timestamp: Long) =
        wardBedDao.allocateBed(bedId, patientId, patientName, timestamp)
    suspend fun dischargeBed(bedId: Long) = wardBedDao.dischargeBed(bedId)

    // Billing
    val allBills: Flow<List<Billing>> = billingDao.getAllBills()
    val totalPaidRevenue: Flow<Double?> = billingDao.getTotalPaidRevenue()
    val totalPendingAmount: Flow<Double?> = billingDao.getTotalPendingAmount()

    fun getBillById(id: Long): Flow<Billing?> = billingDao.getBillById(id)
    fun getBillsForPatient(patientId: Long): Flow<List<Billing>> = billingDao.getBillsForPatient(patientId)
    fun getBillsByDate(date: String): Flow<List<Billing>> = billingDao.getBillsByDate(date)

    suspend fun insertBill(bill: Billing): Long = billingDao.insertBill(bill)
    suspend fun updateBill(bill: Billing) = billingDao.updateBill(bill)
    suspend fun markBillAsPaid(billId: Long) = billingDao.markBillAsPaid(billId)
    suspend fun updatePaymentStatus(billId: Long, status: String) = billingDao.updatePaymentStatus(billId, status)
    suspend fun deleteBill(bill: Billing) = billingDao.deleteBill(bill)

    // Prescriptions
    val allPrescriptions: Flow<List<Prescription>> = prescriptionDao.getAllPrescriptions()

    fun getPrescriptionsForPatient(patientId: Long): Flow<List<Prescription>> = prescriptionDao.getPrescriptionsForPatient(patientId)
    fun getPrescriptionsForDoctor(doctorId: Long): Flow<List<Prescription>> = prescriptionDao.getPrescriptionsForDoctor(doctorId)
    fun getPrescriptionsForPatientAndDoctor(patientId: Long, doctorId: Long): Flow<List<Prescription>> = prescriptionDao.getPrescriptionsForPatientAndDoctor(patientId, doctorId)
    fun getPrescriptionById(id: Long): Flow<Prescription?> = prescriptionDao.getPrescriptionById(id)

    suspend fun insertPrescription(prescription: Prescription): Long = prescriptionDao.insertPrescription(prescription)
    suspend fun updatePrescription(prescription: Prescription) = prescriptionDao.updatePrescription(prescription)
    suspend fun deletePrescription(prescription: Prescription) = prescriptionDao.deletePrescription(prescription)
    suspend fun deletePrescriptionById(id: Long) = prescriptionDao.deleteById(id)

    // Medical Records (Diagnoses & Notes)
    val allMedicalRecords: Flow<List<MedicalRecord>> = medicalRecordDao.getAllRecords()
    fun getRecordsForPatient(patientId: Long): Flow<List<MedicalRecord>> = medicalRecordDao.getRecordsForPatient(patientId)
    suspend fun insertMedicalRecord(record: MedicalRecord): Long = medicalRecordDao.insertRecord(record)
    suspend fun updateMedicalRecord(record: MedicalRecord) = medicalRecordDao.updateRecord(record)
    suspend fun deleteMedicalRecord(record: MedicalRecord) = medicalRecordDao.deleteRecord(record)

    // Lab Results
    val allLabResults: Flow<List<LabResultRecord>> = labResultDao.getAllLabResults()
    fun getLabResultsForPatient(patientId: Long): Flow<List<LabResultRecord>> = labResultDao.getLabResultsForPatient(patientId)
    suspend fun insertLabResult(result: LabResultRecord): Long = labResultDao.insertLabResult(result)
    suspend fun updateLabResult(result: LabResultRecord) = labResultDao.updateLabResult(result)
    suspend fun deleteLabResult(result: LabResultRecord) = labResultDao.deleteLabResult(result)

    // Treatment Plans
    val allTreatmentPlans: Flow<List<TreatmentPlan>> = treatmentPlanDao.getAllTreatmentPlans()
    fun getTreatmentPlansForPatient(patientId: Long): Flow<List<TreatmentPlan>> = treatmentPlanDao.getTreatmentPlansForPatient(patientId)
    suspend fun insertTreatmentPlan(plan: TreatmentPlan): Long = treatmentPlanDao.insertTreatmentPlan(plan)
    suspend fun updateTreatmentPlan(plan: TreatmentPlan) = treatmentPlanDao.updateTreatmentPlan(plan)
    suspend fun deleteTreatmentPlan(plan: TreatmentPlan) = treatmentPlanDao.deleteTreatmentPlan(plan)
}
