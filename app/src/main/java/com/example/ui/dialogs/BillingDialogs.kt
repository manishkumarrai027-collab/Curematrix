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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Patient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBillDialog(
    patients: List<Patient>,
    onDismiss: () -> Unit,
    onConfirm: (
        patientId: Long,
        patientName: String,
        consultationFee: Double,
        wardCharges: Double,
        medicineCharges: Double,
        labCharges: Double,
        discount: Double,
        paymentStatus: String,
        paymentMethod: String,
        notes: String,
        date: String
    ) -> Unit
) {
    var selectedPatient by remember { mutableStateOf(patients.firstOrNull()) }
    var patientExpanded by remember { mutableStateOf(false) }

    var billDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }

    var consultText by remember { mutableStateOf("150") }
    var wardText by remember { mutableStateOf("0") }
    var medText by remember { mutableStateOf("50") }
    var labText by remember { mutableStateOf("75") }
    var discountText by remember { mutableStateOf("0") }

    val paymentStatuses = listOf("Pending", "Paid", "Insurance Claim")
    var selectedStatus by remember { mutableStateOf("Pending") }
    var statusExpanded by remember { mutableStateOf(false) }

    val paymentMethods = listOf("Cash", "Credit Card", "Health Insurance", "UPI / Bank Transfer")
    var selectedMethod by remember { mutableStateOf("Credit Card") }
    var methodExpanded by remember { mutableStateOf(false) }

    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val calculatedTotal by remember {
        derivedStateOf {
            val c = consultText.toDoubleOrNull() ?: 0.0
            val w = wardText.toDoubleOrNull() ?: 0.0
            val m = medText.toDoubleOrNull() ?: 0.0
            val l = labText.toDoubleOrNull() ?: 0.0
            val d = discountText.toDoubleOrNull() ?: 0.0
            (c + w + m + l - d).coerceAtLeast(0.0)
        }
    }

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
                    Text(
                        text = "Generate Invoice",
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

                ExposedDropdownMenuBox(
                    expanded = patientExpanded,
                    onExpandedChange = { patientExpanded = !patientExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedPatient?.let { "${it.fullName} • ${it.mrn}" } ?: "Select Patient",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Patient *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patientExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("bill_patient_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = patientExpanded,
                        onDismissRequest = { patientExpanded = false }
                    ) {
                        patients.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.fullName} (${p.mrn})") },
                                onClick = {
                                    selectedPatient = p
                                    patientExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = billDate,
                    onValueChange = { billDate = it },
                    label = { Text("Billing Date (YYYY-MM-DD) *") },
                    placeholder = { Text("2026-10-02") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("bill_date_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = consultText,
                        onValueChange = { consultText = it },
                        label = { Text("Consult ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("bill_consult_input")
                    )
                    OutlinedTextField(
                        value = wardText,
                        onValueChange = { wardText = it },
                        label = { Text("Ward/Bed ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("bill_ward_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = medText,
                        onValueChange = { medText = it },
                        label = { Text("Pharmacy ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("bill_med_input")
                    )
                    OutlinedTextField(
                        value = labText,
                        onValueChange = { labText = it },
                        label = { Text("Lab/Diag ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("bill_lab_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it },
                    label = { Text("Discount / Waiver ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("bill_discount_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                            value = selectedStatus,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Payment Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            paymentStatuses.forEach {
                                DropdownMenuItem(
                                    text = { Text(it) },
                                    onClick = {
                                        selectedStatus = it
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = methodExpanded,
                        onExpandedChange = { methodExpanded = !methodExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Method") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = methodExpanded,
                            onDismissRequest = { methodExpanded = false }
                        ) {
                            paymentMethods.forEach {
                                DropdownMenuItem(
                                    text = { Text(it) },
                                    onClick = {
                                        selectedMethod = it
                                        methodExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Billing Notes (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("bill_notes_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Calculated Total Box
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Net Payable Amount:",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$${String.format("%.2f", calculatedTotal)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            val patient = selectedPatient
                            if (patient == null) {
                                error = "Please select a patient"
                                return@Button
                            }
                            val c = consultText.toDoubleOrNull() ?: 0.0
                            val w = wardText.toDoubleOrNull() ?: 0.0
                            val m = medText.toDoubleOrNull() ?: 0.0
                            val l = labText.toDoubleOrNull() ?: 0.0
                            val d = discountText.toDoubleOrNull() ?: 0.0
                            onConfirm(
                                patient.id,
                                patient.fullName,
                                c, w, m, l, d,
                                selectedStatus,
                                selectedMethod,
                                notes.trim(),
                                billDate.trim().ifBlank {
                                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                }
                            )
                        },
                        modifier = Modifier.testTag("confirm_create_bill_button")
                    ) {
                        Text("Create Invoice")
                    }
                }
            }
        }
    }
}
