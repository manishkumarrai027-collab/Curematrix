package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MedicalPersonnel
import com.example.data.model.Patient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicalRecordDialog(
    patients: List<Patient>,
    currentDoctorName: String,
    onDismiss: () -> Unit,
    onConfirm: (
        patientId: Long,
        patientName: String,
        recordType: String,
        title: String,
        content: String,
        severity: String,
        doctorOrStaff: String,
        department: String
    ) -> Unit
) {
    var selectedPatient by remember { mutableStateOf(patients.firstOrNull()) }
    var patientExpanded by remember { mutableStateOf(false) }

    val recordTypes = listOf("Diagnosis", "Progress Note", "Clinical Consultation", "Discharge Summary")
    var selectedRecordType by remember { mutableStateOf("Diagnosis") }
    var recordTypeExpanded by remember { mutableStateOf(false) }

    val severities = listOf("Normal", "Attention Required", "Critical", "Resolved")
    var selectedSeverity by remember { mutableStateOf("Normal") }
    var severityExpanded by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Internal Medicine") }
    var content by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Clinical Diagnosis / Note",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Patient dropdown
                ExposedDropdownMenuBox(
                    expanded = patientExpanded,
                    onExpandedChange = { patientExpanded = !patientExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedPatient?.let { "${it.fullName} (${it.mrn})" } ?: "Select Patient",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Patient *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patientExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = patientExpanded,
                        onDismissRequest = { patientExpanded = false }
                    ) {
                        patients.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.fullName} • ${p.mrn}") },
                                onClick = {
                                    selectedPatient = p
                                    patientExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = recordTypeExpanded,
                        onExpandedChange = { recordTypeExpanded = !recordTypeExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedRecordType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Record Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recordTypeExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = recordTypeExpanded,
                            onDismissRequest = { recordTypeExpanded = false }
                        ) {
                            recordTypes.forEach {
                                DropdownMenuItem(text = { Text(it) }, onClick = {
                                    selectedRecordType = it
                                    recordTypeExpanded = false
                                })
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = severityExpanded,
                        onExpandedChange = { severityExpanded = !severityExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedSeverity,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Severity / Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = severityExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = severityExpanded,
                            onDismissRequest = { severityExpanded = false }
                        ) {
                            severities.forEach {
                                DropdownMenuItem(text = { Text(it) }, onClick = {
                                    selectedSeverity = it
                                    severityExpanded = false
                                })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Diagnosis / Subject Title *") },
                    placeholder = { Text("e.g. Acute Myocardial Infarction LAD Stenting") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("medical_record_title_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department / Clinical Wing") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("medical_record_dept_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Detailed Clinical Narrative & Findings *") },
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("medical_record_content_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val patient = selectedPatient
                            if (patient == null) {
                                error = "Please select a patient"
                                return@Button
                            }
                            if (title.isBlank() || content.isBlank()) {
                                error = "Title and clinical narrative are required"
                                return@Button
                            }
                            onConfirm(
                                patient.id,
                                patient.fullName,
                                selectedRecordType,
                                title.trim(),
                                content.trim(),
                                selectedSeverity,
                                currentDoctorName,
                                department.trim()
                            )
                        },
                        modifier = Modifier.testTag("save_medical_record_button")
                    ) {
                        Text("Save Record")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLabResultDialog(
    patients: List<Patient>,
    onDismiss: () -> Unit,
    onConfirm: (
        patientId: Long,
        patientName: String,
        testName: String,
        category: String,
        performedBy: String,
        specimen: String,
        parameters: String,
        conclusion: String,
        status: String
    ) -> Unit
) {
    var selectedPatient by remember { mutableStateOf(patients.firstOrNull()) }
    var patientExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Hematology", "Biochemistry", "Pathology", "Radiology", "Cardiology", "Microbiology")
    var selectedCategory by remember { mutableStateOf("Biochemistry") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val statuses = listOf("Normal", "Abnormal", "Critical High", "Critical Low")
    var selectedStatus by remember { mutableStateOf("Normal") }
    var statusExpanded by remember { mutableStateOf(false) }

    var testName by remember { mutableStateOf("") }
    var performedBy by remember { mutableStateOf("CarePath Diagnostics Lab") }
    var specimen by remember { mutableStateOf("Venous Blood / Serum") }
    var parameters by remember { mutableStateOf("") }
    var conclusion by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Diagnostic / Lab Result",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Patient dropdown
                ExposedDropdownMenuBox(
                    expanded = patientExpanded,
                    onExpandedChange = { patientExpanded = !patientExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedPatient?.let { "${it.fullName} (${it.mrn})" } ?: "Select Patient",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Patient *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patientExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = patientExpanded,
                        onDismissRequest = { patientExpanded = false }
                    ) {
                        patients.forEach { p ->
                            DropdownMenuItem(text = { Text("${p.fullName} (${p.mrn})") }, onClick = {
                                selectedPatient = p
                                patientExpanded = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = testName,
                    onValueChange = { testName = it },
                    label = { Text("Test Name * (e.g. Troponin-I, CBC, CT Chest)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("lab_test_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                            categories.forEach {
                                DropdownMenuItem(text = { Text(it) }, onClick = {
                                    selectedCategory = it
                                    categoryExpanded = false
                                })
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedStatus,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status / Result") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                            statuses.forEach {
                                DropdownMenuItem(text = { Text(it) }, onClick = {
                                    selectedStatus = it
                                    statusExpanded = false
                                })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = specimen,
                    onValueChange = { specimen = it },
                    label = { Text("Specimen / Modality (e.g. Venous Blood, MRI 3T)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = parameters,
                    onValueChange = { parameters = it },
                    label = { Text("Observed Values & Reference Range *") },
                    placeholder = { Text("e.g. Troponin-I: 1.82 ng/mL [Ref: <0.04 ng/mL]") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("lab_parameters_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = conclusion,
                    onValueChange = { conclusion = it },
                    label = { Text("Pathologist / Radiologist Conclusion *") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("lab_conclusion_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val patient = selectedPatient
                            if (patient == null) {
                                error = "Please select a patient"
                                return@Button
                            }
                            if (testName.isBlank() || parameters.isBlank() || conclusion.isBlank()) {
                                error = "Test name, parameters, and conclusion are required"
                                return@Button
                            }
                            onConfirm(
                                patient.id,
                                patient.fullName,
                                testName.trim(),
                                selectedCategory,
                                performedBy.trim(),
                                specimen.trim(),
                                parameters.trim(),
                                conclusion.trim(),
                                selectedStatus
                            )
                        },
                        modifier = Modifier.testTag("save_lab_result_button")
                    ) {
                        Text("Record Result")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTreatmentPlanDialog(
    patients: List<Patient>,
    defaultDoctorName: String,
    onDismiss: () -> Unit,
    onConfirm: (
        patientId: Long,
        patientName: String,
        title: String,
        attendingDoctor: String,
        goals: String,
        interventions: String,
        dietary: String
    ) -> Unit
) {
    var selectedPatient by remember { mutableStateOf(patients.firstOrNull()) }
    var patientExpanded by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var doctorName by remember { mutableStateOf(defaultDoctorName) }
    var goals by remember { mutableStateOf("") }
    var interventions by remember { mutableStateOf("") }
    var dietary by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Treatment Plan",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Patient dropdown
                ExposedDropdownMenuBox(
                    expanded = patientExpanded,
                    onExpandedChange = { patientExpanded = !patientExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedPatient?.let { "${it.fullName} (${it.mrn})" } ?: "Select Patient",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Patient *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patientExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = patientExpanded,
                        onDismissRequest = { patientExpanded = false }
                    ) {
                        patients.forEach { p ->
                            DropdownMenuItem(text = { Text("${p.fullName} (${p.mrn})") }, onClick = {
                                selectedPatient = p
                                patientExpanded = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Treatment Plan Title *") },
                    placeholder = { Text("e.g. Post-PCI Cardiac Rehabilitation Protocol") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("treatment_plan_title_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text("Attending Physician *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = goals,
                    onValueChange = { goals = it },
                    label = { Text("Clinical Goals * (Numbered list)") },
                    placeholder = { Text("1. Target BP < 130/80\n2. Prevent in-stent restenosis") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("treatment_goals_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = interventions,
                    onValueChange = { interventions = it },
                    label = { Text("Active Medical Interventions & Nursing Care *") },
                    placeholder = { Text("Daily telemetry, DAPT medication adherence, physical therapy") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("treatment_interventions_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dietary,
                    onValueChange = { dietary = it },
                    label = { Text("Dietary & Lifestyle Restrictions") },
                    placeholder = { Text("Low sodium < 2g/day, fluid limit 1.8L/day") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("treatment_dietary_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val patient = selectedPatient
                            if (patient == null) {
                                error = "Please select a patient"
                                return@Button
                            }
                            if (title.isBlank() || goals.isBlank() || interventions.isBlank()) {
                                error = "Title, goals, and active interventions are required"
                                return@Button
                            }
                            onConfirm(
                                patient.id,
                                patient.fullName,
                                title.trim(),
                                doctorName.trim(),
                                goals.trim(),
                                interventions.trim(),
                                dietary.trim()
                            )
                        },
                        modifier = Modifier.testTag("save_treatment_plan_button")
                    ) {
                        Text("Create Plan")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPrescriptionDialog(
    patients: List<Patient>,
    defaultDoctorName: String,
    onDismiss: () -> Unit,
    onConfirm: (
        patientId: Long,
        patientName: String,
        doctorId: Long,
        doctorName: String,
        medicationName: String,
        dosage: String,
        frequency: String,
        duration: String,
        diagnosis: String,
        medicines: String,
        instructions: String
    ) -> Unit
) {
    var selectedPatient by remember { mutableStateOf(patients.firstOrNull()) }
    var patientExpanded by remember { mutableStateOf(false) }

    var doctorName by remember { mutableStateOf(defaultDoctorName) }
    var diagnosis by remember { mutableStateOf(selectedPatient?.diagnosis ?: "") }
    var medicationName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("500 mg") }
    var frequency by remember { mutableStateOf("Twice daily (BID)") }
    var duration by remember { mutableStateOf("7 days") }
    var medicines by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("Take with warm water after meals.") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Write New Prescription",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Patient dropdown
                ExposedDropdownMenuBox(
                    expanded = patientExpanded,
                    onExpandedChange = { patientExpanded = !patientExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedPatient?.let { "${it.fullName} (${it.mrn})" } ?: "Select Patient",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Patient *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patientExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = patientExpanded,
                        onDismissRequest = { patientExpanded = false }
                    ) {
                        patients.forEach { p ->
                            DropdownMenuItem(text = { Text("${p.fullName} (${p.mrn})") }, onClick = {
                                selectedPatient = p
                                if (p.diagnosis.isNotEmpty()) {
                                    diagnosis = p.diagnosis
                                }
                                patientExpanded = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text("Prescribing Physician *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    label = { Text("Clinical Diagnosis *") },
                    placeholder = { Text("e.g. Essential Hypertension, Bronchitis") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("prescription_diagnosis_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Medication, Dosage & Frequency",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = medicationName,
                    onValueChange = {
                        medicationName = it
                        if (medicines.isBlank() || medicines.startsWith(it)) {
                            medicines = "$it $dosage - $frequency ($duration)"
                        }
                    },
                    label = { Text("Medication Name *") },
                    placeholder = { Text("e.g. Amoxicillin, Atorvastatin, Metformin") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("prescription_medication_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage *") },
                        placeholder = { Text("e.g. 500 mg, 1 tab") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("prescription_dosage_input")
                    )

                    OutlinedTextField(
                        value = frequency,
                        onValueChange = { frequency = it },
                        label = { Text("Frequency *") },
                        placeholder = { Text("e.g. Twice daily (BID)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("prescription_frequency_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration *") },
                    placeholder = { Text("e.g. 7 days, 30 days, Ongoing") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("prescription_duration_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = medicines,
                    onValueChange = { medicines = it },
                    label = { Text("Full Prescribed Medication Summary *") },
                    placeholder = { Text("1. Amoxicillin 500mg - 1 capsule TID (7 days)\n2. Paracetamol 500mg - 1 tab PRN") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("prescription_medicines_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Special Patient Instructions") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("prescription_instructions_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val patient = selectedPatient
                            if (patient == null) {
                                error = "Please select a patient"
                                return@Button
                            }
                            val medSummary = medicines.ifBlank {
                                if (medicationName.isNotBlank()) "$medicationName $dosage - $frequency ($duration)" else ""
                            }
                            if (diagnosis.isBlank() || (medicationName.isBlank() && medSummary.isBlank())) {
                                error = "Diagnosis and medication details are required"
                                return@Button
                            }
                            onConfirm(
                                patient.id,
                                patient.fullName,
                                patient.assignedDoctorId ?: 1L,
                                doctorName.trim(),
                                medicationName.trim().ifBlank { medSummary.take(30) },
                                dosage.trim(),
                                frequency.trim(),
                                duration.trim(),
                                diagnosis.trim(),
                                medSummary.trim(),
                                instructions.trim()
                            )
                        },
                        modifier = Modifier.testTag("save_prescription_button")
                    ) {
                        Text("Issue Prescription")
                    }
                }
            }
        }
    }
}

@Composable
fun PersonnelAuthDialog(
    personnelList: List<MedicalPersonnel>,
    currentPersonnel: MedicalPersonnel,
    onDismiss: () -> Unit,
    onSelectPersonnel: (MedicalPersonnel) -> Unit
) {
    var selected by remember { mutableStateOf(currentPersonnel) }
    var enteredPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = "Security", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Authorized Personnel Switcher")
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Select your medical staff credentials to access patient confidential records:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                personnelList.forEach { p ->
                    val isChosen = p.staffId == selected.staffId
                    Surface(
                        onClick = { selected = p },
                        color = if (isChosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "${p.name} • ${p.role}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isChosen) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${p.department} • Level: ${p.accessLevel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = { enteredPin = it },
                    label = { Text("Enter Staff Security PIN (Default: 1234)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("personnel_pin_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (enteredPin != selected.pin && enteredPin != "1234") {
                        error = "Incorrect security PIN. Default is 1234."
                        return@Button
                    }
                    onSelectPersonnel(selected)
                },
                modifier = Modifier.testTag("confirm_personnel_switch_button")
            ) {
                Text("Authorize Access")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
