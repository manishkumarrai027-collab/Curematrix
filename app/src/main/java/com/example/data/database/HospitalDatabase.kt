package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.example.data.model.Doctor
import com.example.data.model.LabResultRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.Patient
import com.example.data.model.Prescription
import com.example.data.model.TreatmentPlan
import com.example.data.model.WardBed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Patient::class,
        Doctor::class,
        Appointment::class,
        WardBed::class,
        Billing::class,
        Prescription::class,
        MedicalRecord::class,
        LabResultRecord::class,
        TreatmentPlan::class
    ],
    version = 6,
    exportSchema = false
)
abstract class HospitalDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun doctorDao(): DoctorDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun wardBedDao(): WardBedDao
    abstract fun billingDao(): BillingDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun medicalRecordDao(): MedicalRecordDao
    abstract fun labResultDao(): LabResultDao
    abstract fun treatmentPlanDao(): TreatmentPlanDao

    companion object {
        @Volatile
        private var INSTANCE: HospitalDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): HospitalDatabase {
            return INSTANCE ?: synchronized(this) {
                // Remove legacy version 3 database if present to prevent any unhandled migration crashes
                try {
                    context.applicationContext.deleteDatabase("care_hospital_database")
                } catch (_: Exception) {
                    // Ignore deletion failure if not present
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HospitalDatabase::class.java,
                    "care_hospital_db_v6"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration(true)
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                scope.launch(Dispatchers.IO) {
                    var attempts = 0
                    while (INSTANCE == null && attempts < 20) {
                        kotlinx.coroutines.delay(50)
                        attempts++
                    }
                    INSTANCE?.let { database ->
                        populateInitialHospitalData(database)
                    }
                }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                scope.launch(Dispatchers.IO) {
                    var attempts = 0
                    while (INSTANCE == null && attempts < 20) {
                        kotlinx.coroutines.delay(50)
                        attempts++
                    }
                    INSTANCE?.let { database ->
                        populateInitialHospitalData(database)
                    }
                }
            }
        }

        suspend fun populateInitialHospitalData(database: HospitalDatabase) {
            val doctorDao = database.doctorDao()
            val patientDao = database.patientDao()
            val wardBedDao = database.wardBedDao()
            val appointmentDao = database.appointmentDao()
            val billingDao = database.billingDao()
            val prescriptionDao = database.prescriptionDao()
            val medicalRecordDao = database.medicalRecordDao()
            val labResultDao = database.labResultDao()
            val treatmentPlanDao = database.treatmentPlanDao()

            val doctors = listOf(
                Doctor(
                    fullName = "Dr. Elena Rostova",
                    specialty = "Cardiology",
                    qualification = "MD, FACC - Senior Interventional Cardiologist",
                    experienceYears = 16,
                    consultationFee = 160.0,
                    roomNumber = "OPD Room 102 (Cardio Wing)",
                    contactPhone = "+1 (555) 234-5678",
                    email = "elena.rostova@carehospital.org",
                    availableHours = "08:30 AM - 02:00 PM",
                    isDutyActive = true,
                    status = "Available"
                ),
                Doctor(
                    fullName = "Dr. Marcus Vance",
                    specialty = "Neurology",
                    qualification = "MBBS, MD - Neurosciences & Spine",
                    experienceYears = 14,
                    consultationFee = 180.0,
                    roomNumber = "OPD Room 204 (Neuro Tower)",
                    contactPhone = "+1 (555) 345-6789",
                    email = "marcus.vance@carehospital.org",
                    availableHours = "10:00 AM - 04:30 PM",
                    isDutyActive = true,
                    status = "In Surgery"
                ),
                Doctor(
                    fullName = "Dr. Priya Patel",
                    specialty = "Pediatrics",
                    qualification = "MD, FAAP - Child Health & Neonatal Care",
                    experienceYears = 10,
                    consultationFee = 120.0,
                    roomNumber = "Pediatric Suite 12",
                    contactPhone = "+1 (555) 456-7890",
                    email = "priya.patel@carehospital.org",
                    availableHours = "09:00 AM - 05:00 PM",
                    isDutyActive = true,
                    status = "Available"
                ),
                Doctor(
                    fullName = "Dr. James Sterling",
                    specialty = "Orthopedics",
                    qualification = "MS (Ortho), Fellowship in Joint Replacement",
                    experienceYears = 18,
                    consultationFee = 150.0,
                    roomNumber = "OPD Room 115 (Ortho Clinic)",
                    contactPhone = "+1 (555) 567-8901",
                    email = "james.sterling@carehospital.org",
                    availableHours = "09:30 AM - 03:30 PM",
                    isDutyActive = true,
                    status = "Available"
                ),
                Doctor(
                    fullName = "Dr. Sophia Chen",
                    specialty = "Emergency & Trauma",
                    qualification = "MD - Emergency Critical Medicine",
                    experienceYears = 11,
                    consultationFee = 140.0,
                    roomNumber = "ER Trauma Bay A",
                    contactPhone = "+1 (555) 678-9012",
                    email = "sophia.chen@carehospital.org",
                    availableHours = "24/7 ER Shift",
                    isDutyActive = true,
                    status = "Available"
                ),
                Doctor(
                    fullName = "Dr. Robert Gallagher",
                    specialty = "General Surgery",
                    qualification = "FRCS, FACS - Laparoscopic & GI Surgery",
                    experienceYears = 21,
                    consultationFee = 200.0,
                    roomNumber = "Surgical Suite 04",
                    contactPhone = "+1 (555) 789-0123",
                    email = "robert.gallagher@carehospital.org",
                    availableHours = "11:00 AM - 04:00 PM",
                    isDutyActive = false,
                    status = "On Leave"
                )
            )
            doctorDao.insertAll(doctors)

            val patients = listOf(
                Patient(
                    mrn = "MRN-1001",
                    fullName = "Arthur Pendelton",
                    dateOfBirth = "1964-03-12",
                    age = 62,
                    gender = "Male",
                    bloodGroup = "O+",
                    contactPhone = "+1 (555) 901-1122",
                    email = "arthur.p@outlook.com",
                    address = "42 Evergreen Terrace, Metro City",
                    emergencyContact = "Martha Pendelton (Wife): +1 (555) 901-1123",
                    primaryMedicalHistory = "Hypertension (10 yrs), Hyperlipidemia, Ex-smoker (quit 2018). No prior cardiac bypass.",
                    status = "ICU",
                    assignedDoctorId = 1,
                    assignedDoctorName = "Dr. Elena Rostova",
                    assignedWard = "ICU",
                    assignedBedNumber = "ICU-02",
                    diagnosis = "Acute Anterior ST-Elevation Myocardial Infarction - Post Primary PCI",
                    allergies = "Penicillin, Sulfa drugs",
                    bloodPressure = "135/88",
                    pulseRate = 84,
                    oxygenLevel = 97,
                    temperature = 98.7f
                ),
                Patient(
                    mrn = "MRN-1002",
                    fullName = "Camila Rodriguez",
                    dateOfBirth = "1997-08-25",
                    age = 29,
                    gender = "Female",
                    bloodGroup = "A+",
                    contactPhone = "+1 (555) 812-3344",
                    email = "camila.r@gmail.com",
                    address = "718 Maple Blvd, Apt 4B",
                    emergencyContact = "Diego Rodriguez (Brother): +1 (555) 812-3345",
                    primaryMedicalHistory = "No chronic illnesses. Previous tonsillectomy (2014). Active amateur athlete.",
                    status = "IPD (Admitted)",
                    assignedDoctorId = 4,
                    assignedDoctorName = "Dr. James Sterling",
                    assignedWard = "General Ward A",
                    assignedBedNumber = "Bed 03",
                    diagnosis = "Compound Right Tibia-Fibula Fracture - Post Internal Fixation",
                    allergies = "Aspirin",
                    bloodPressure = "118/76",
                    pulseRate = 74,
                    oxygenLevel = 99,
                    temperature = 98.4f
                ),
                Patient(
                    mrn = "MRN-1003",
                    fullName = "Lucas Bennett",
                    dateOfBirth = "2018-11-04",
                    age = 8,
                    gender = "Male",
                    bloodGroup = "B+",
                    contactPhone = "+1 (555) 723-4455",
                    email = "sarah.bennett@family.org",
                    address = "120 Pinecrest Way",
                    emergencyContact = "Sarah Bennett (Mother): +1 (555) 723-4456",
                    primaryMedicalHistory = "Mild childhood asthma, fully immunized up to date. Hospitalized at age 3 for croup.",
                    status = "IPD (Admitted)",
                    assignedDoctorId = 3,
                    assignedDoctorName = "Dr. Priya Patel",
                    assignedWard = "Pediatric Ward",
                    assignedBedNumber = "Ped-01",
                    diagnosis = "Severe Acute Viral Bronchiolitis with Wheezing",
                    allergies = "Peanuts",
                    bloodPressure = "102/65",
                    pulseRate = 96,
                    oxygenLevel = 96,
                    temperature = 99.8f
                ),
                Patient(
                    mrn = "MRN-1004",
                    fullName = "Eleanor Wright",
                    dateOfBirth = "1972-01-19",
                    age = 54,
                    gender = "Female",
                    bloodGroup = "AB+",
                    contactPhone = "+1 (555) 634-5566",
                    email = "eleanor.w@wrightlaw.com",
                    address = "89 Harbor View Road",
                    emergencyContact = "David Wright (Husband): +1 (555) 634-5567",
                    primaryMedicalHistory = "Chronic recurrent migraine with visual aura (15 yrs), Cervical spondylosis at C5-C6.",
                    status = "OPD",
                    assignedDoctorId = 2,
                    assignedDoctorName = "Dr. Marcus Vance",
                    diagnosis = "Intractable Migraine with Visual Aura & Occipital Neuralgia",
                    allergies = "None Known",
                    bloodPressure = "124/82",
                    pulseRate = 70,
                    oxygenLevel = 99,
                    temperature = 98.6f
                ),
                Patient(
                    mrn = "MRN-1005",
                    fullName = "Samuel O'Connor",
                    dateOfBirth = "1983-05-30",
                    age = 43,
                    gender = "Male",
                    bloodGroup = "O-",
                    contactPhone = "+1 (555) 545-6677",
                    email = "s.oconnor@techcorp.io",
                    address = "314 Sunset Boulevard",
                    emergencyContact = "Grace O'Connor (Sister): +1 (555) 545-6678",
                    primaryMedicalHistory = "Essential Hypertension, Type 2 Diabetes Mellitus (HbA1c 6.8%), Gout.",
                    status = "Discharged",
                    assignedDoctorId = 1,
                    assignedDoctorName = "Dr. Elena Rostova",
                    diagnosis = "Hypertensive Urgency with Mild Renal Elevation - Resolved",
                    allergies = "None Known",
                    bloodPressure = "122/80",
                    pulseRate = 68,
                    oxygenLevel = 98,
                    temperature = 98.4f
                )
            )
            patientDao.insertAll(patients)

            val beds = listOf(
                WardBed(wardName = "ICU", bedNumber = "ICU-01", isOccupied = false, dailyRate = 450.0),
                WardBed(wardName = "ICU", bedNumber = "ICU-02", isOccupied = true, occupiedPatientId = 1, occupiedPatientName = "Arthur Pendelton", admissionDateMillis = System.currentTimeMillis() - 86400000L * 2, dailyRate = 450.0),
                WardBed(wardName = "ICU", bedNumber = "ICU-03", isOccupied = false, dailyRate = 450.0),
                WardBed(wardName = "General Ward A", bedNumber = "Bed 01", isOccupied = false, dailyRate = 120.0),
                WardBed(wardName = "General Ward A", bedNumber = "Bed 02", isOccupied = false, dailyRate = 120.0),
                WardBed(wardName = "General Ward A", bedNumber = "Bed 03", isOccupied = true, occupiedPatientId = 2, occupiedPatientName = "Camila Rodriguez", admissionDateMillis = System.currentTimeMillis() - 86400000L, dailyRate = 120.0),
                WardBed(wardName = "General Ward A", bedNumber = "Bed 04", isOccupied = false, dailyRate = 120.0),
                WardBed(wardName = "General Ward A", bedNumber = "Bed 05", isOccupied = false, dailyRate = 120.0),
                WardBed(wardName = "Pediatric Ward", bedNumber = "Ped-01", isOccupied = true, occupiedPatientId = 3, occupiedPatientName = "Lucas Bennett", admissionDateMillis = System.currentTimeMillis() - 43200000L, dailyRate = 200.0),
                WardBed(wardName = "Pediatric Ward", bedNumber = "Ped-02", isOccupied = false, dailyRate = 200.0),
                WardBed(wardName = "Semi-Private", bedNumber = "SP-101", isOccupied = false, dailyRate = 250.0),
                WardBed(wardName = "Semi-Private", bedNumber = "SP-102", isOccupied = false, dailyRate = 250.0),
                WardBed(wardName = "Emergency Unit", bedNumber = "ER-Bay 1", isOccupied = false, dailyRate = 300.0),
                WardBed(wardName = "Emergency Unit", bedNumber = "ER-Bay 2", isOccupied = false, dailyRate = 300.0)
            )
            wardBedDao.insertAll(beds)

            val oneDayMillis = 86400000L
            val nowMillis = System.currentTimeMillis()

            val appointments = listOf(
                Appointment(
                    patientId = 2,
                    patientName = "Camila Rodriguez",
                    doctorId = 4,
                    doctorName = "Dr. James Sterling",
                    doctorSpecialty = "Orthopedics",
                    tokenNumber = 1,
                    dateMillis = nowMillis,
                    dateString = "Today",
                    timeSlot = "09:00 AM",
                    reason = "Post-op fracture cast evaluation & suture line check",
                    status = "Completed",
                    isConfirmed = true,
                    reminderSent = true,
                    notes = "Surgical site clean, wound healing without signs of infection."
                ),
                Appointment(
                    patientId = 4,
                    patientName = "Eleanor Wright",
                    doctorId = 2,
                    doctorName = "Dr. Marcus Vance",
                    doctorSpecialty = "Neurology",
                    tokenNumber = 2,
                    dateMillis = nowMillis,
                    dateString = "Today",
                    timeSlot = "10:30 AM",
                    reason = "Follow-up MRI Brain review for migraines",
                    status = "In Progress",
                    isConfirmed = true,
                    reminderSent = true,
                    notes = "Patient reports reduction in frequency after medication."
                ),
                Appointment(
                    patientId = 1,
                    patientName = "Arthur Pendelton",
                    doctorId = 1,
                    doctorName = "Dr. Elena Rostova",
                    doctorSpecialty = "Cardiology",
                    tokenNumber = 3,
                    dateMillis = nowMillis,
                    dateString = "Today",
                    timeSlot = "11:45 AM",
                    reason = "Daily bedside ICU cardiac telemetry evaluation",
                    status = "Scheduled",
                    isConfirmed = true,
                    reminderSent = true,
                    notes = "Check telemetry, cardiac enzymes, and troponin levels."
                ),
                Appointment(
                    patientId = 3,
                    patientName = "Lucas Bennett",
                    doctorId = 3,
                    doctorName = "Dr. Priya Patel",
                    doctorSpecialty = "Pediatrics",
                    tokenNumber = 4,
                    dateMillis = nowMillis,
                    dateString = "Today",
                    timeSlot = "03:30 PM",
                    reason = "Pediatric viral bronchiolitis wheezing review",
                    status = "Scheduled",
                    isConfirmed = true,
                    reminderSent = true,
                    notes = "Evaluate breath sounds and SpO2 levels."
                ),
                Appointment(
                    patientId = 4,
                    patientName = "Eleanor Wright",
                    doctorId = 2,
                    doctorName = "Dr. Marcus Vance",
                    doctorSpecialty = "Neurology",
                    tokenNumber = 5,
                    dateMillis = nowMillis + oneDayMillis,
                    dateString = "Tomorrow",
                    timeSlot = "09:30 AM",
                    reason = "Occipital nerve block procedure planning",
                    status = "Scheduled",
                    isConfirmed = true,
                    reminderSent = true,
                    notes = "Discuss nerve block options and patient consent."
                ),
                Appointment(
                    patientId = 5,
                    patientName = "Samuel O'Connor",
                    doctorId = 1,
                    doctorName = "Dr. Elena Rostova",
                    doctorSpecialty = "Cardiology",
                    tokenNumber = 6,
                    dateMillis = nowMillis + oneDayMillis,
                    dateString = "Tomorrow",
                    timeSlot = "02:15 PM",
                    reason = "Post-discharge blood pressure stability check",
                    status = "Scheduled",
                    isConfirmed = true,
                    reminderSent = true,
                    notes = "Review home BP logbook."
                ),
                Appointment(
                    patientId = 2,
                    patientName = "Camila Rodriguez",
                    doctorId = 4,
                    doctorName = "Dr. James Sterling",
                    doctorSpecialty = "Orthopedics",
                    tokenNumber = 7,
                    dateMillis = nowMillis + oneDayMillis * 2,
                    dateString = "In 2 Days",
                    timeSlot = "11:00 AM",
                    reason = "Physical therapy progress & gait training review",
                    status = "Scheduled",
                    isConfirmed = true,
                    reminderSent = false,
                    notes = "Assess crutch walking ability and range of motion."
                ),
                Appointment(
                    patientId = 1,
                    patientName = "Arthur Pendelton",
                    doctorId = 1,
                    doctorName = "Dr. Elena Rostova",
                    doctorSpecialty = "Cardiology",
                    tokenNumber = 8,
                    dateMillis = nowMillis + oneDayMillis * 4,
                    dateString = "Next Week",
                    timeSlot = "10:00 AM",
                    reason = "Phase-1 cardiac rehabilitation exercise clearance",
                    status = "Scheduled",
                    isConfirmed = true,
                    reminderSent = false,
                    notes = "Review treadmill ECG stress test if stable."
                ),
                Appointment(
                    patientId = 5,
                    patientName = "Samuel O'Connor",
                    doctorId = 1,
                    doctorName = "Dr. Elena Rostova",
                    doctorSpecialty = "Cardiology",
                    tokenNumber = 9,
                    dateMillis = nowMillis - oneDayMillis,
                    dateString = "Yesterday",
                    timeSlot = "04:00 PM",
                    reason = "Urgent hypertension triage & medication titration",
                    status = "Completed",
                    isConfirmed = true,
                    reminderSent = true,
                    notes = "BP reduced to 122/80 after adjusting antihypertensives."
                )
            )
            appointmentDao.insertAll(appointments)

            val bills = listOf(
                Billing(
                    billNumber = "INV-2026-001",
                    patientId = 5,
                    patientName = "Samuel O'Connor",
                    date = "2026-10-01",
                    amount = 825.0,
                    totalAmount = 825.0,
                    paymentStatus = "Paid",
                    paymentMethod = "Health Insurance",
                    consultationFee = 160.0,
                    wardCharges = 360.0,
                    medicineCharges = 145.0,
                    labTestCharges = 210.0,
                    discount = 50.0,
                    notes = "Emergency stabilization & cardiac monitoring 3 days."
                ),
                Billing(
                    billNumber = "INV-2026-002",
                    patientId = 4,
                    patientName = "Eleanor Wright",
                    date = "2026-10-01",
                    amount = 665.0,
                    totalAmount = 665.0,
                    paymentStatus = "Pending",
                    paymentMethod = "Credit Card",
                    consultationFee = 180.0,
                    wardCharges = 0.0,
                    medicineCharges = 65.0,
                    labTestCharges = 420.0,
                    discount = 0.0,
                    notes = "MRI Cervical spine & neurological consultation."
                ),
                Billing(
                    billNumber = "INV-2026-003",
                    patientId = 2,
                    patientName = "Camila Rodriguez",
                    date = "2026-10-02",
                    amount = 570.0,
                    totalAmount = 570.0,
                    paymentStatus = "Pending",
                    paymentMethod = "Cash",
                    consultationFee = 150.0,
                    wardCharges = 240.0,
                    medicineCharges = 95.0,
                    labTestCharges = 110.0,
                    discount = 25.0,
                    notes = "Intermediate ward charges and orthopedic dressings."
                ),
                Billing(
                    billNumber = "INV-2026-004",
                    patientId = 1,
                    patientName = "Arthur Pendelton",
                    date = "2026-09-18",
                    amount = 1450.0,
                    totalAmount = 1450.0,
                    paymentStatus = "Paid",
                    paymentMethod = "Health Insurance",
                    consultationFee = 250.0,
                    wardCharges = 800.0,
                    medicineCharges = 250.0,
                    labTestCharges = 150.0,
                    discount = 0.0,
                    notes = "Coronary angiogram and ICU telemetry monitoring."
                ),
                Billing(
                    billNumber = "INV-2026-005",
                    patientId = 3,
                    patientName = "Lucas Bennett",
                    date = "2026-09-22",
                    amount = 420.0,
                    totalAmount = 420.0,
                    paymentStatus = "Paid",
                    paymentMethod = "Credit Card",
                    consultationFee = 140.0,
                    wardCharges = 0.0,
                    medicineCharges = 120.0,
                    labTestCharges = 160.0,
                    discount = 0.0,
                    notes = "Pediatric asthma nebulization & allergy panel."
                ),
                Billing(
                    billNumber = "INV-2026-006",
                    patientId = 4,
                    patientName = "Eleanor Wright",
                    date = "2026-08-14",
                    amount = 980.0,
                    totalAmount = 980.0,
                    paymentStatus = "Paid",
                    paymentMethod = "Credit Card",
                    consultationFee = 180.0,
                    wardCharges = 400.0,
                    medicineCharges = 200.0,
                    labTestCharges = 200.0,
                    discount = 0.0,
                    notes = "Neurology inpatient workup and physical therapy."
                ),
                Billing(
                    billNumber = "INV-2026-007",
                    patientId = 5,
                    patientName = "Samuel O'Connor",
                    date = "2026-07-20",
                    amount = 1200.0,
                    totalAmount = 1200.0,
                    paymentStatus = "Paid",
                    paymentMethod = "Health Insurance",
                    consultationFee = 200.0,
                    wardCharges = 600.0,
                    medicineCharges = 250.0,
                    labTestCharges = 150.0,
                    discount = 0.0,
                    notes = "Cardiology follow-up and echo stress test."
                ),
                Billing(
                    billNumber = "INV-2026-008",
                    patientId = 2,
                    patientName = "Camila Rodriguez",
                    date = "2026-06-11",
                    amount = 890.0,
                    totalAmount = 890.0,
                    paymentStatus = "Paid",
                    paymentMethod = "Cash",
                    consultationFee = 150.0,
                    wardCharges = 440.0,
                    medicineCharges = 150.0,
                    labTestCharges = 150.0,
                    discount = 0.0,
                    notes = "Post-op wound debridement and orthopedic splints."
                ),
                Billing(
                    billNumber = "INV-2026-009",
                    patientId = 1,
                    patientName = "Arthur Pendelton",
                    date = "2026-05-15",
                    amount = 750.0,
                    totalAmount = 750.0,
                    paymentStatus = "Paid",
                    paymentMethod = "Health Insurance",
                    consultationFee = 150.0,
                    wardCharges = 300.0,
                    medicineCharges = 150.0,
                    labTestCharges = 150.0,
                    discount = 0.0,
                    notes = "Cardiology checkup and blood electrolyte screening."
                )
            )
            billingDao.insertAll(bills)

            val prescriptions = listOf(
                Prescription(
                    patientId = 4,
                    patientName = "Eleanor Wright",
                    doctorId = 2,
                    doctorName = "Dr. Marcus Vance",
                    medicationName = "Sumatriptan",
                    dosage = "50 mg",
                    frequency = "1 tablet at onset of migraine",
                    duration = "PRN (30 days)",
                    diagnosis = "Migraine with Aura",
                    medicines = "1. Sumatriptan 50mg - Take 1 tablet at onset of headache\n2. Propranolol 40mg - 1 tablet once daily in morning\n3. Magnesium Glycinate 400mg - 1 capsule at night",
                    instructions = "Avoid trigger foods (caffeine, aged cheese). Keep a headache diary."
                ),
                Prescription(
                    patientId = 1,
                    patientName = "Arthur Pendelton",
                    doctorId = 1,
                    doctorName = "Dr. Elena Rostova",
                    medicationName = "Atorvastatin & Metoprolol",
                    dosage = "80 mg & 25 mg",
                    frequency = "Daily at bedtime & BID",
                    duration = "Ongoing",
                    diagnosis = "Post Acute MI Maintenance",
                    medicines = "1. Aspirin 81mg - 1 tablet daily with lunch\n2. Clopidogrel 75mg - 1 tablet daily\n3. Atorvastatin 80mg - 1 tablet daily at bedtime\n4. Metoprolol Tartrate 25mg - 1 tablet twice daily",
                    instructions = "Continuous telemetry in ICU. Strict low-sodium, heart-healthy diet."
                )
            )
            prescriptionDao.insertAll(prescriptions)

            // Medical Records (Diagnoses & Clinical Notes)
            val records = listOf(
                MedicalRecord(
                    patientId = 1,
                    patientName = "Arthur Pendelton",
                    recordType = "Diagnosis",
                    title = "Acute Coronary Syndrome - LAD Occlusion",
                    doctorOrStaffName = "Dr. Elena Rostova",
                    department = "Cardiology",
                    content = "Patient presented with severe substernal pressure radiating to left jaw and diaphoresis. Emergency angiography revealed 95% stenosis of mid-LAD. Successful drug-eluting stent (DES) placed. TIMI-3 flow restored.",
                    severityOrStatus = "Critical"
                ),
                MedicalRecord(
                    patientId = 1,
                    patientName = "Arthur Pendelton",
                    recordType = "Progress Note",
                    title = "ICU Day 2 Hemodynamic Review",
                    doctorOrStaffName = "Dr. Elena Rostova",
                    department = "Cardiology",
                    content = "Telemetry rhythm sinus at 78 bpm without ectopy. Lung bases clear bilaterally. Urine output adequate (>45 mL/hr). Echo EF estimated at 48%. Step-down transfer planned within 24 hours.",
                    severityOrStatus = "Attention Required"
                ),
                MedicalRecord(
                    patientId = 2,
                    patientName = "Camila Rodriguez",
                    recordType = "Diagnosis",
                    title = "Open Grade-I Right Tibia Shaft Fracture",
                    doctorOrStaffName = "Dr. James Sterling",
                    department = "Orthopedics",
                    content = "Sports injury during high-impact football tackle. Debridement and intramedullary interlocking nail performed under spinal anesthesia. Distal pulses intact and motor function preserved.",
                    severityOrStatus = "Normal"
                ),
                MedicalRecord(
                    patientId = 4,
                    patientName = "Eleanor Wright",
                    recordType = "Clinical Consultation",
                    title = "Neurology Follow-up & Occipital Nerve Block Evaluation",
                    doctorOrStaffName = "Dr. Marcus Vance",
                    department = "Neurology",
                    content = "Brain MRI with contrast reveals non-specific subcortical white matter hyperintensities consistent with chronic microvascular migraines; no mass lesion or acute infarct. Greater occipital nerve block discussed.",
                    severityOrStatus = "Normal"
                )
            )
            medicalRecordDao.insertAll(records)

            // Lab Results
            val labResults = listOf(
                LabResultRecord(
                    patientId = 1,
                    patientName = "Arthur Pendelton",
                    testName = "Cardiac Troponin-I (High Sensitivity)",
                    category = "Biochemistry",
                    performedBy = "CarePath Diagnostics Lab",
                    specimen = "Venous Blood / Heparin Plasma",
                    parametersSummary = "Troponin-I: 1.82 ng/mL [Ref Range: <0.04 ng/mL]\nCK-MB: 54 U/L [Ref Range: 5-25 U/L]\nBNP: 340 pg/mL [Ref Range: <100 pg/mL]",
                    conclusion = "Significantly elevated myocardial biomarkers consistent with acute myocardial necrosis. Down-trending from initial peak.",
                    status = "Critical High"
                ),
                LabResultRecord(
                    patientId = 1,
                    patientName = "Arthur Pendelton",
                    testName = "Comprehensive Metabolic Panel & Lipids",
                    category = "Biochemistry",
                    performedBy = "CarePath Diagnostics Lab",
                    specimen = "Serum",
                    parametersSummary = "Total Cholesterol: 245 mg/dL (High)\nLDL: 168 mg/dL (High)\nHDL: 38 mg/dL (Low)\nTriglycerides: 195 mg/dL\nSerum Creatinine: 1.1 mg/dL (Normal)\neGFR: 74 mL/min",
                    conclusion = "Mixed dyslipidemia. Normal renal function. Statin intensity titration indicated.",
                    status = "Abnormal"
                ),
                LabResultRecord(
                    patientId = 3,
                    patientName = "Lucas Bennett",
                    testName = "Respiratory Viral Panel & Arterial Blood Gas",
                    category = "Pathology",
                    performedBy = "Pediatric Diagnostic Wing",
                    specimen = "Nasopharyngeal Swab & Capillary Blood",
                    parametersSummary = "RSV (Respiratory Syncytial Virus): POSITIVE\nInfluenza A/B: Negative\npH: 7.37 [Ref: 7.35-7.45]\npCO2: 44 mmHg\npO2: 88 mmHg\nOxygen Saturation: 96%",
                    conclusion = "RSV Bronchiolitis confirmed. Adequate gas exchange maintained on humidified room air / low-flow cannula.",
                    status = "Abnormal"
                ),
                LabResultRecord(
                    patientId = 4,
                    patientName = "Eleanor Wright",
                    testName = "High-Resolution Brain MRI with Contrast",
                    category = "Radiology",
                    performedBy = "Advanced Neuro-Imaging Center",
                    specimen = "3.0 Tesla MRI Scan",
                    parametersSummary = "T1/T2/FLAIR Sequences performed.\nVentricular system symmetric and normal.\nScattered punctate subcortical white matter hyperintensities in frontal lobes.\nNo acute intracranial hemorrhage or territorial infarction.",
                    conclusion = "Benign migraine-associated white matter changes without acute neurological pathology.",
                    status = "Normal"
                )
            )
            labResultDao.insertAll(labResults)

            // Treatment Plans
            val treatmentPlans = listOf(
                TreatmentPlan(
                    patientId = 1,
                    patientName = "Arthur Pendelton",
                    planTitle = "Post-PCI Cardiac Care & Secondary Prevention",
                    attendingDoctor = "Dr. Elena Rostova",
                    clinicalGoals = "1. Maintain blood pressure < 130/80 mmHg.\n2. Target LDL-C < 55 mg/dL.\n3. Prevent in-stent thrombosis.\n4. Complete phase-1 cardiac rehabilitation before discharge.",
                    activeInterventions = "Continuous telemetry, Dual Antiplatelet Therapy (DAPT) for 12 months, High-intensity statin, Daily weight monitoring for heart failure signs.",
                    dietaryRestrictions = "Strict cardiac diet: Sodium < 2000 mg/day, fluid restriction 1.8 L/day, saturated fat < 6% of total calories.",
                    status = "Active"
                ),
                TreatmentPlan(
                    patientId = 2,
                    patientName = "Camila Rodriguez",
                    planTitle = "Post-Op Orthopedic Rehabilitation & Bone Healing",
                    attendingDoctor = "Dr. James Sterling",
                    clinicalGoals = "1. Achieve surgical wound healing without infection.\n2. Non-weight bearing mobilization with crutches.\n3. Active ankle and knee range of motion exercises.",
                    activeInterventions = "Wound dressings check Q48h, DVT prophylaxis with enoxaparin 40mg daily, physiotherapy twice daily.",
                    dietaryRestrictions = "High-protein, Calcium & Vitamin D fortified diet to accelerate bone union.",
                    status = "Active"
                ),
                TreatmentPlan(
                    patientId = 3,
                    patientName = "Lucas Bennett",
                    planTitle = "Pediatric Bronchiolitis Hydration & Airway Clearance",
                    attendingDoctor = "Dr. Priya Patel",
                    clinicalGoals = "1. Maintain SpO2 > 95% on room air.\n2. Ensure oral hydration and normal urine output (>1 mL/kg/hr).\n3. Reduce respiratory distress work of breathing.",
                    activeInterventions = "Nasal suctioning before feeds, humidified oxygen as needed, small frequent feeds, fever management with paracetamol.",
                    dietaryRestrictions = "Pediatric oral rehydration solutions, soft diet as tolerated.",
                    status = "Active"
                )
            )
            treatmentPlanDao.insertAll(treatmentPlans)
        }
    }
}
