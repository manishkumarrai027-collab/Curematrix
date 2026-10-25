package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Doctor
import com.example.data.model.Patient
import com.example.ui.components.BloodGroupBadge
import com.example.ui.components.PatientStatusBadge
import com.example.ui.components.VitalsRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPatientDialog(
    doctors: List<Doctor>,
    onDismiss: () -> Unit,
    onConfirm: (
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
        currentMedications: String,
        status: String,
        diagnosis: String,
        allergies: String,
        doctorId: Long?,
        doctorName: String?
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var bloodGroup by remember { mutableStateOf("O+") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
    var primaryMedicalHistory by remember { mutableStateOf("") }
    var currentMedications by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("OPD") }
    var diagnosis by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var selectedDoctor by remember { mutableStateOf(doctors.firstOrNull()) }

    var genderExpanded by remember { mutableStateOf(false) }
    var bloodGroupExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var doctorExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Patient Registration",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Collect demographics, contact & complete medical history",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Section 1: Demographics & DOB
                Text(
                    text = "1. Identity & Date of Birth",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Patient Full Name *") },
                    placeholder = { Text("e.g. Johnathan Doe") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("patient_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { input ->
                            dob = input
                            // Auto calculate age if formatted YYYY-MM-DD or year provided
                            val yearMatch = Regex("^(\\d{4})").find(input)
                            if (yearMatch != null) {
                                val year = yearMatch.value.toIntOrNull()
                                if (year != null && year in 1900..2026) {
                                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                                    val calcAge = currentYear - year
                                    if (calcAge >= 0) {
                                        ageText = calcAge.toString()
                                    }
                                }
                            }
                        },
                        label = { Text("Date of Birth (YYYY-MM-DD)") },
                        placeholder = { Text("1992-06-24") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f).testTag("patient_dob_input")
                    )

                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) ageText = it },
                        label = { Text("Age (Years) *") },
                        placeholder = { Text("34") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.8f).testTag("patient_age_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = !genderExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gender") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                            modifier = Modifier.menuAnchor().testTag("patient_gender_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = { genderExpanded = false }
                        ) {
                            listOf("Male", "Female", "Other").forEach {
                                DropdownMenuItem(
                                    text = { Text(it) },
                                    onClick = {
                                        gender = it
                                        genderExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = bloodGroupExpanded,
                        onExpandedChange = { bloodGroupExpanded = !bloodGroupExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = bloodGroup,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Blood Group") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodGroupExpanded) },
                            modifier = Modifier.menuAnchor().testTag("patient_blood_group_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = bloodGroupExpanded,
                            onDismissRequest = { bloodGroupExpanded = false }
                        ) {
                            listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-").forEach {
                                DropdownMenuItem(
                                    text = { Text(it) },
                                    onClick = {
                                        bloodGroup = it
                                        bloodGroupExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Contact Information
                Text(
                    text = "2. Contact & Emergency Information",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Primary Phone *") },
                        placeholder = { Text("+1 (555) 000-0000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("patient_phone_input")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("john@example.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("patient_email_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Residential Address") },
                    placeholder = { Text("123 Health Ave, Apt 4") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("patient_address_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    label = { Text("Emergency Contact (Name, Relation & Phone) *") },
                    placeholder = { Text("e.g. Mary Doe (Sister): +1 (555) 123-4567") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("patient_emergency_contact_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Section 3: Comprehensive Medical History
                Text(
                    text = "3. Medical History & Current Care",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = primaryMedicalHistory,
                    onValueChange = { primaryMedicalHistory = it },
                    label = { Text("Primary Medical History (Surgeries, Chronic Diseases, Family History)") },
                    placeholder = { Text("e.g. Type-2 Diabetes (7 yrs), Hypertension, Appendectomy (2018), Family history of CAD") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("patient_history_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = currentMedications,
                    onValueChange = { currentMedications = it },
                    label = { Text("Current Daily Medications & Dosages") },
                    placeholder = { Text("e.g. Metformin 500mg BID, Lisinopril 10mg daily, Aspirin 81mg") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("patient_medications_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Known Drug & Food Allergies") },
                    placeholder = { Text("e.g. Penicillin, Sulfa, Peanuts, Latex (or None Known)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("patient_allergies_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    label = { Text("Chief Complaint / Presenting Condition") },
                    placeholder = { Text("e.g. Acute chest discomfort, Annual wellness exam, Follow-up") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("patient_diagnosis_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Section 4: Admission & Specialist
                Text(
                    text = "4. Triage & Specialist Assignment",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Admission Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier.menuAnchor().testTag("patient_status_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            listOf("OPD", "IPD (Admitted)", "ICU").forEach {
                                DropdownMenuItem(
                                    text = { Text(it) },
                                    onClick = {
                                        status = it
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Doctor selection
                    ExposedDropdownMenuBox(
                        expanded = doctorExpanded,
                        onExpandedChange = { doctorExpanded = !doctorExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedDoctor?.fullName ?: "Select Doctor",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assigned Doctor") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = doctorExpanded) },
                            modifier = Modifier.menuAnchor().testTag("patient_assigned_doctor_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = doctorExpanded,
                            onDismissRequest = { doctorExpanded = false }
                        ) {
                            doctors.forEach { doc ->
                                DropdownMenuItem(
                                    text = { Text("${doc.fullName} (${doc.specialty})") },
                                    onClick = {
                                        selectedDoctor = doc
                                        doctorExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Patient name is required"
                                return@Button
                            }
                            val age = ageText.toIntOrNull()
                            if (age == null || age <= 0) {
                                errorMessage = "Please enter a valid age"
                                return@Button
                            }
                            if (phone.isBlank()) {
                                errorMessage = "Phone number is required"
                                return@Button
                            }
                            if (emergencyContact.isBlank()) {
                                errorMessage = "Emergency contact details are required"
                                return@Button
                            }
                            onConfirm(
                                name.trim(),
                                dob.trim().ifBlank { "N/A" },
                                age,
                                gender,
                                bloodGroup,
                                phone.trim(),
                                email.trim(),
                                address.trim(),
                                emergencyContact.trim(),
                                primaryMedicalHistory.trim(),
                                currentMedications.trim(),
                                status,
                                diagnosis.trim(),
                                allergies.trim().ifBlank { "None Known" },
                                selectedDoctor?.id,
                                selectedDoctor?.fullName
                            )
                        },
                        modifier = Modifier.testTag("save_patient_button")
                    ) {
                        Text("Save Patient Record")
                    }
                }
            }
        }
    }
}

@Composable
fun PatientDetailDialog(
    patient: Patient,
    onDismiss: () -> Unit,
    onUpdateVitalsClick: () -> Unit,
    onExportPdfClick: () -> Unit = {},
    onDischargeClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = patient.fullName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = patient.mrn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BloodGroupBadge(bloodGroup = patient.bloodGroup)
                            Spacer(modifier = Modifier.width(8.dp))
                            PatientStatusBadge(status = patient.status)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Vitals Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current Vitals",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            IconButton(
                                onClick = onUpdateVitalsClick,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("update_vitals_button")
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Vitals",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        VitalsRow(
                            bloodPressure = patient.bloodPressure,
                            pulseRate = patient.pulseRate,
                            oxygenLevel = patient.oxygenLevel,
                            temperature = patient.temperature
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Personal & Contact Info
                Text(
                    text = "Demographics & Contact",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                DetailItem(
                    label = "Date of Birth & Age",
                    value = "${if (patient.dateOfBirth.isNotEmpty()) "${patient.dateOfBirth} • " else ""}${patient.age} Years (${patient.gender})"
                )
                DetailItem(label = "Phone Number", value = patient.contactPhone)
                if (patient.email.isNotEmpty()) {
                    DetailItem(label = "Email Address", value = patient.email)
                }
                if (patient.address.isNotEmpty()) {
                    DetailItem(label = "Address", value = patient.address)
                }
                if (patient.emergencyContact.isNotEmpty()) {
                    DetailItem(label = "Emergency Contact", value = patient.emergencyContact)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Primary Medical History
                Text(
                    text = "Primary Medical History",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                DetailItem(
                    label = "Past Conditions & Surgeries",
                    value = patient.primaryMedicalHistory.ifBlank { "No prior chronic medical history reported." }
                )
                if (patient.currentMedications.isNotBlank()) {
                    DetailItem(
                        label = "Active Daily Medications",
                        value = patient.currentMedications
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Clinical care
                if (!patient.assignedDoctorName.isNullOrEmpty()) {
                    DetailItem(label = "Attending Specialist", value = patient.assignedDoctorName)
                }
                if (!patient.assignedWard.isNullOrEmpty()) {
                    DetailItem(
                        label = "Ward & Bed Allocation",
                        value = "${patient.assignedWard} - ${patient.assignedBedNumber ?: "N/A"}"
                    )
                }
                if (patient.diagnosis.isNotEmpty()) {
                    DetailItem(label = "Current Diagnosis", value = patient.diagnosis)
                }
                DetailItem(label = "Known Allergies", value = patient.allergies)

                Spacer(modifier = Modifier.height(18.dp))

                // Export & Print PDF Report Action
                Button(
                    onClick = onExportPdfClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_pdf_report_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "PDF Report",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export & Print Medical Report (PDF)", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (patient.status != "Discharged") {
                        Button(
                            onClick = onDischargeClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("discharge_patient_button")
                        ) {
                            Text("Discharge")
                        }
                    }

                    OutlinedButton(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("delete_patient_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun UpdateVitalsDialog(
    patient: Patient,
    onDismiss: () -> Unit,
    onConfirm: (bp: String, pulse: Int, spo2: Int, temp: Float) -> Unit
) {
    var bp by remember { mutableStateOf(patient.bloodPressure) }
    var pulseText by remember { mutableStateOf(patient.pulseRate.toString()) }
    var spo2Text by remember { mutableStateOf(patient.oxygenLevel.toString()) }
    var tempText by remember { mutableStateOf(patient.temperature.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Update Vitals: ${patient.fullName}")
        },
        text = {
            Column {
                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                OutlinedTextField(
                    value = bp,
                    onValueChange = { bp = it },
                    label = { Text("Blood Pressure (e.g. 120/80)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("vitals_bp_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = pulseText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) pulseText = it },
                    label = { Text("Pulse Rate (bpm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("vitals_pulse_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = spo2Text,
                    onValueChange = { if (it.all { c -> c.isDigit() }) spo2Text = it },
                    label = { Text("SpO2 Oxygen (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("vitals_spo2_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tempText,
                    onValueChange = { tempText = it },
                    label = { Text("Temperature (°F)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("vitals_temp_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pulse = pulseText.toIntOrNull()
                    val spo2 = spo2Text.toIntOrNull()
                    val temp = tempText.toFloatOrNull()
                    if (bp.isBlank() || pulse == null || spo2 == null || temp == null) {
                        error = "Please enter valid numeric vitals"
                        return@Button
                    }
                    onConfirm(bp.trim(), pulse, spo2, temp)
                },
                modifier = Modifier.testTag("save_vitals_confirm_button")
            ) {
                Text("Save Vitals")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
