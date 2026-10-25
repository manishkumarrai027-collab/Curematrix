package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Doctor
import com.example.data.model.Patient
import com.example.data.model.WardBed
import com.example.ui.dialogs.AddDoctorDialog
import com.example.ui.dialogs.AddLabResultDialog
import com.example.ui.dialogs.AddMedicalRecordDialog
import com.example.ui.dialogs.AddPrescriptionDialog
import com.example.ui.dialogs.AddTreatmentPlanDialog
import com.example.ui.dialogs.AllocateBedDialog
import com.example.ui.dialogs.BookAppointmentDialog
import com.example.ui.dialogs.CreateBillDialog
import com.example.ui.dialogs.PatientDetailDialog
import com.example.ui.dialogs.PatientReportDialog
import com.example.ui.dialogs.PersonnelAuthDialog
import com.example.ui.dialogs.RegisterPatientDialog
import com.example.ui.dialogs.UpdateVitalsDialog
import com.example.ui.screens.AppointmentsScreen
import com.example.ui.screens.BillingScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DoctorsScreen
import com.example.ui.screens.MedicalRecordsScreen
import com.example.ui.screens.PatientsScreen
import com.example.ui.screens.WardsBedsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.ui.viewmodel.HospitalViewModel
import com.example.ui.viewmodel.HospitalViewModelFactory

enum class HospitalScreen(val title: String, val icon: ImageVector) {
    Dashboard("Overview", Icons.Default.Dashboard),
    Patients("Patients", Icons.Default.People),
    MedicalRecords("EMR Records", Icons.Default.Description),
    Appointments("Schedule", Icons.Default.CalendarMonth),
    HospitalOps("Ops & Wards", Icons.Default.Bed)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as HospitalApplication
        val viewModel = ViewModelProvider(
            this,
            HospitalViewModelFactory(app.repository)
        )[HospitalViewModel::class.java]

        setContent {
            MyApplicationTheme {
                HospitalMainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalMainApp(viewModel: HospitalViewModel) {
    var currentScreen by remember { mutableStateOf(HospitalScreen.Dashboard) }
    var opsSubTab by remember { mutableIntStateOf(0) } // 0: Wards, 1: Specialists, 2: Invoices

    val patients by viewModel.rawPatients.collectAsStateWithLifecycle()
    val doctors by viewModel.rawDoctors.collectAsStateWithLifecycle()
    val selectedPatient by viewModel.selectedPatient.collectAsStateWithLifecycle()
    val currentPersonnel by viewModel.currentPersonnel.collectAsStateWithLifecycle()

    val allPrescriptions by viewModel.allPrescriptions.collectAsStateWithLifecycle()
    val allMedicalRecords by viewModel.allMedicalRecords.collectAsStateWithLifecycle()
    val allTreatmentPlans by viewModel.allTreatmentPlans.collectAsStateWithLifecycle()
    val allLabResults by viewModel.allLabResults.collectAsStateWithLifecycle()

    // Dialog Visibility States
    var showRegisterPatientDialog by remember { mutableStateOf(false) }
    var showBookAppointmentDialog by remember { mutableStateOf(false) }
    var selectedDoctorForBooking by remember { mutableStateOf<Doctor?>(null) }
    var showAddDoctorDialog by remember { mutableStateOf(false) }
    var bedToAllocate by remember { mutableStateOf<WardBed?>(null) }
    var showCreateBillDialog by remember { mutableStateOf(false) }
    var showUpdateVitalsDialog by remember { mutableStateOf(false) }
    var showAddDiagnosisDialog by remember { mutableStateOf(false) }
    var showAddTreatmentPlanDialog by remember { mutableStateOf(false) }
    var showAddLabResultDialog by remember { mutableStateOf(false) }
    var showAddPrescriptionDialog by remember { mutableStateOf(false) }
    var showPersonnelAuthDialog by remember { mutableStateOf(false) }
    var patientForReportDialog by remember { mutableStateOf<Patient?>(null) }

    // Back handling: If on secondary screen, go back to Dashboard
    if (currentScreen != HospitalScreen.Dashboard) {
        BackHandler {
            currentScreen = HospitalScreen.Dashboard
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = "CareHospital",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "CareHospital",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Medical Management System",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    // Authorized Personnel Session Indicator & Switcher Button
                    Surface(
                        onClick = { showPersonnelAuthDialog = true },
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("topbar_auth_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Personnel Auth",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentPersonnel.name.split(" ").take(2).joinToString(" "),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                HospitalScreen.entries.forEach { screen ->
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.name}")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                HospitalScreen.Dashboard -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToPatients = { currentScreen = HospitalScreen.Patients },
                        onNavigateToDoctors = {
                            opsSubTab = 1
                            currentScreen = HospitalScreen.HospitalOps
                        },
                        onNavigateToAppointments = { currentScreen = HospitalScreen.Appointments },
                        onNavigateToWards = {
                            opsSubTab = 0
                            currentScreen = HospitalScreen.HospitalOps
                        },
                        onNavigateToBilling = {
                            opsSubTab = 2
                            currentScreen = HospitalScreen.HospitalOps
                        },
                        onOpenRegisterPatient = { showRegisterPatientDialog = true },
                        onOpenBookAppointment = {
                            selectedDoctorForBooking = null
                            showBookAppointmentDialog = true
                        },
                        onOpenCreateBill = { showCreateBillDialog = true },
                        onPatientClick = { patient ->
                            viewModel.selectPatient(patient)
                        }
                    )
                }

                HospitalScreen.Patients -> {
                    PatientsScreen(
                        viewModel = viewModel,
                        onRegisterPatientClick = { showRegisterPatientDialog = true },
                        onPatientClick = { patient ->
                            viewModel.selectPatient(patient)
                        },
                        onExportPatientReport = { patient ->
                            patientForReportDialog = patient
                        }
                    )
                }

                HospitalScreen.MedicalRecords -> {
                    MedicalRecordsScreen(
                        viewModel = viewModel,
                        onOpenAddDiagnosis = { showAddDiagnosisDialog = true },
                        onOpenAddTreatmentPlan = { showAddTreatmentPlanDialog = true },
                        onOpenAddLabResult = { showAddLabResultDialog = true },
                        onOpenAddPrescription = { showAddPrescriptionDialog = true },
                        onOpenAuthSwitcher = { showPersonnelAuthDialog = true },
                        onExportPatientReport = { patient ->
                            patientForReportDialog = patient
                        }
                    )
                }

                HospitalScreen.Appointments -> {
                    AppointmentsScreen(
                        viewModel = viewModel,
                        onBookAppointmentClick = {
                            selectedDoctorForBooking = null
                            showBookAppointmentDialog = true
                        }
                    )
                }

                HospitalScreen.HospitalOps -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ScrollableTabRow(
                            selectedTabIndex = opsSubTab,
                            edgePadding = 16.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Tab(
                                selected = opsSubTab == 0,
                                onClick = { opsSubTab = 0 },
                                text = { Text("Wards & Beds") },
                                modifier = Modifier.testTag("ops_tab_wards")
                            )
                            Tab(
                                selected = opsSubTab == 1,
                                onClick = { opsSubTab = 1 },
                                text = { Text("Medical Specialists") },
                                modifier = Modifier.testTag("ops_tab_doctors")
                            )
                            Tab(
                                selected = opsSubTab == 2,
                                onClick = { opsSubTab = 2 },
                                text = { Text("Invoices & Billing") },
                                modifier = Modifier.testTag("ops_tab_billing")
                            )
                        }

                        when (opsSubTab) {
                            0 -> WardsBedsScreen(
                                viewModel = viewModel,
                                onAllocateBedClick = { bed -> bedToAllocate = bed }
                            )
                            1 -> DoctorsScreen(
                                viewModel = viewModel,
                                onAddDoctorClick = { showAddDoctorDialog = true },
                                onBookDoctorAppointment = { doc ->
                                    selectedDoctorForBooking = doc
                                    showBookAppointmentDialog = true
                                }
                            )
                            2 -> BillingScreen(
                                viewModel = viewModel,
                                onCreateBillClick = { showCreateBillDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- ALL POPUP & ACTION DIALOGS ---

    // 1. Patient Registration Module
    if (showRegisterPatientDialog) {
        RegisterPatientDialog(
            doctors = doctors,
            onDismiss = { showRegisterPatientDialog = false },
            onConfirm = { name, dob, age, gender, bloodGroup, phone, email, address, emergencyContact, history, medications, status, diagnosis, allergies, doctorId, doctorName ->
                viewModel.registerPatient(
                    name = name,
                    dateOfBirth = dob,
                    age = age,
                    gender = gender,
                    bloodGroup = bloodGroup,
                    phone = phone,
                    email = email,
                    address = address,
                    emergencyContact = emergencyContact,
                    primaryMedicalHistory = history,
                    currentMedications = medications,
                    status = status,
                    diagnosis = diagnosis,
                    allergies = allergies,
                    doctorId = doctorId,
                    doctorName = doctorName
                )
                showRegisterPatientDialog = false
            }
        )
    }

    // 2. Patient Profile & Medical Details Dialog
    selectedPatient?.let { patient ->
        PatientDetailDialog(
            patient = patient,
            onDismiss = { viewModel.selectPatient(null) },
            onUpdateVitalsClick = { showUpdateVitalsDialog = true },
            onExportPdfClick = { patientForReportDialog = patient },
            onDischargeClick = { viewModel.dischargePatient(patient.id) },
            onDeleteClick = { viewModel.deletePatient(patient) }
        )
    }

    // 3. Update Vitals Dialog
    if (showUpdateVitalsDialog && selectedPatient != null) {
        UpdateVitalsDialog(
            patient = selectedPatient!!,
            onDismiss = { showUpdateVitalsDialog = false },
            onConfirm = { bp, pulse, spo2, temp ->
                viewModel.updatePatientVitals(selectedPatient!!.id, bp, pulse, spo2, temp)
                showUpdateVitalsDialog = false
            }
        )
    }

    // 4. Book Consultation Appointment Dialog
    if (showBookAppointmentDialog) {
        BookAppointmentDialog(
            patients = patients,
            doctors = doctors,
            initialDoctor = selectedDoctorForBooking,
            onDismiss = { showBookAppointmentDialog = false },
            onConfirm = { patientId, patientName, doctorId, doctorName, specialty, dateMillis, dateString, timeSlot, reason, notes ->
                viewModel.scheduleAppointment(
                    patientId = patientId,
                    patientName = patientName,
                    doctorId = doctorId,
                    doctorName = doctorName,
                    doctorSpecialty = specialty,
                    dateMillis = dateMillis,
                    dateString = dateString,
                    timeSlot = timeSlot,
                    reason = reason,
                    notes = notes
                )
                showBookAppointmentDialog = false
            }
        )
    }

    // 5. Add Specialist Doctor Dialog
    if (showAddDoctorDialog) {
        AddDoctorDialog(
            onDismiss = { showAddDoctorDialog = false },
            onConfirm = { name, specialty, qualification, exp, fee, room, phone, email, hours ->
                viewModel.addDoctor(name, specialty, qualification, exp, fee, room, phone, email, hours)
                showAddDoctorDialog = false
            }
        )
    }

    // 6. Allocate Bed Dialog
    bedToAllocate?.let { bed ->
        AllocateBedDialog(
            bed = bed,
            patients = patients,
            onDismiss = { bedToAllocate = null },
            onConfirm = { patientId, patientName ->
                viewModel.allocateBed(bed.id, patientId, patientName)
                bedToAllocate = null
            }
        )
    }

    // 7. Create Invoice / Bill Dialog
    if (showCreateBillDialog) {
        CreateBillDialog(
            patients = patients,
            onDismiss = { showCreateBillDialog = false },
            onConfirm = { patientId, patientName, cFee, wFee, mFee, lFee, disc, pStatus, pMethod, notes, date ->
                viewModel.generateBill(
                    patientId = patientId,
                    patientName = patientName,
                    consultationFee = cFee,
                    wardCharges = wFee,
                    medicineCharges = mFee,
                    labTestCharges = lFee,
                    discount = disc,
                    paymentStatus = pStatus,
                    paymentMethod = pMethod,
                    notes = notes,
                    date = date
                )
                showCreateBillDialog = false
            }
        )
    }

    // 8. EMR: Add Diagnosis / Progress Note Dialog
    if (showAddDiagnosisDialog) {
        AddMedicalRecordDialog(
            patients = patients,
            currentDoctorName = currentPersonnel.name,
            onDismiss = { showAddDiagnosisDialog = false },
            onConfirm = { patientId, patientName, recordType, title, content, severity, doctorName, department ->
                viewModel.addMedicalRecord(patientId, patientName, recordType, title, content, severity, doctorName, department)
                showAddDiagnosisDialog = false
            }
        )
    }

    // 9. EMR: Add Treatment Plan Dialog
    if (showAddTreatmentPlanDialog) {
        AddTreatmentPlanDialog(
            patients = patients,
            defaultDoctorName = currentPersonnel.name,
            onDismiss = { showAddTreatmentPlanDialog = false },
            onConfirm = { patientId, patientName, title, doctorName, goals, interventions, dietary ->
                viewModel.addTreatmentPlan(patientId, patientName, title, doctorName, goals, interventions, dietary)
                showAddTreatmentPlanDialog = false
            }
        )
    }

    // 10. EMR: Add Lab Result Dialog
    if (showAddLabResultDialog) {
        AddLabResultDialog(
            patients = patients,
            onDismiss = { showAddLabResultDialog = false },
            onConfirm = { patientId, patientName, testName, category, performedBy, specimen, parameters, conclusion, status ->
                viewModel.addLabResult(patientId, patientName, testName, category, performedBy, specimen, parameters, conclusion, status)
                showAddLabResultDialog = false
            }
        )
    }

    // 11. EMR: Add Prescription Dialog
    if (showAddPrescriptionDialog) {
        AddPrescriptionDialog(
            patients = patients,
            defaultDoctorName = currentPersonnel.name,
            onDismiss = { showAddPrescriptionDialog = false },
            onConfirm = { patientId, patientName, doctorId, doctorName, medName, dosage, freq, duration, diagnosis, medicines, instructions ->
                viewModel.addPrescription(
                    patientId = patientId,
                    patientName = patientName,
                    doctorId = doctorId,
                    doctorName = doctorName,
                    medicationName = medName,
                    dosage = dosage,
                    frequency = freq,
                    duration = duration,
                    diagnosis = diagnosis,
                    medicines = medicines,
                    instructions = instructions
                )
                showAddPrescriptionDialog = false
            }
        )
    }

    // 12. Personnel Security & Switcher Dialog
    if (showPersonnelAuthDialog) {
        PersonnelAuthDialog(
            personnelList = viewModel.authorizedStaffList,
            currentPersonnel = currentPersonnel,
            onDismiss = { showPersonnelAuthDialog = false },
            onSelectPersonnel = { personnel ->
                viewModel.switchPersonnel(personnel)
                showPersonnelAuthDialog = false
            }
        )
    }

    // 13. Printable Medical Report & Prescription PDF Export Dialog
    patientForReportDialog?.let { patient ->
        PatientReportDialog(
            patient = patient,
            prescriptions = allPrescriptions.filter { it.patientId == patient.id },
            medicalRecords = allMedicalRecords.filter { it.patientId == patient.id },
            treatmentPlans = allTreatmentPlans.filter { it.patientId == patient.id },
            labResults = allLabResults.filter { it.patientId == patient.id },
            attendingStaffName = currentPersonnel.name,
            onDismiss = { patientForReportDialog = null }
        )
    }
}
