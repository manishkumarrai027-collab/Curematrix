package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Appointment
import com.example.data.model.Patient
import com.example.data.model.WardBed
import com.example.ui.components.BloodGroupBadge
import com.example.ui.components.HospitalAnalyticsCard
import com.example.ui.components.PatientStatusBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.theme.StatusIcu
import com.example.ui.theme.StatusIpd
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.HospitalViewModel

@Composable
fun DashboardScreen(
    viewModel: HospitalViewModel,
    onNavigateToPatients: () -> Unit,
    onNavigateToDoctors: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToWards: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onOpenRegisterPatient: () -> Unit,
    onOpenBookAppointment: () -> Unit,
    onOpenCreateBill: () -> Unit,
    onPatientClick: (Patient) -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val patients by viewModel.rawPatients.collectAsStateWithLifecycle()
    val appointments by viewModel.rawAppointments.collectAsStateWithLifecycle()
    val beds by viewModel.rawBeds.collectAsStateWithLifecycle()
    val bills by viewModel.rawBills.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Hospital Readiness Status Banner
        item {
            HospitalReadinessBanner()
        }

        // Live KPI Grid
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Total Patients",
                        value = "${metrics.totalPatients}",
                        subtitle = "${metrics.admittedPatients} Admitted / ICU",
                        icon = Icons.Default.People,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToPatients() }
                            .testTag("stat_card_patients")
                    )
                    StatCard(
                        title = "Bed Occupancy",
                        value = "${metrics.occupiedBeds}/${metrics.totalBeds}",
                        subtitle = if (metrics.totalBeds > 0) "${(metrics.occupiedBeds * 100) / metrics.totalBeds}% Occupied" else "0%",
                        icon = Icons.Default.Bed,
                        accentColor = if (metrics.totalBeds > 0 && (metrics.occupiedBeds * 100 / metrics.totalBeds) > 80) StatusWarning else StatusSuccess,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToWards() }
                            .testTag("stat_card_beds")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Active Doctors",
                        value = "${metrics.activeDoctors}",
                        subtitle = "On Duty & Available",
                        icon = Icons.Default.MedicalServices,
                        accentColor = Color(0xFF00897B),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToDoctors() }
                            .testTag("stat_card_doctors")
                    )
                    StatCard(
                        title = "Appointments",
                        value = "${metrics.activeAppointments}",
                        subtitle = "Scheduled / In-Prog",
                        icon = Icons.Default.CalendarMonth,
                        accentColor = Color(0xFF1E88E5),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToAppointments() }
                            .testTag("stat_card_appointments")
                    )
                }
            }
        }

        // Quick Actions Row
        item {
            SectionHeader(title = "Quick Hospital Actions")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    QuickActionButton(
                        label = "New Patient",
                        icon = Icons.Default.People,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        onClick = onOpenRegisterPatient,
                        testTag = "quick_action_register_patient"
                    )
                }
                item {
                    QuickActionButton(
                        label = "Consultation",
                        icon = Icons.Default.CalendarMonth,
                        containerColor = Color(0xFF0288D1),
                        contentColor = Color.White,
                        onClick = onOpenBookAppointment,
                        testTag = "quick_action_book_appointment"
                    )
                }
                item {
                    QuickActionButton(
                        label = "Wards & Beds",
                        icon = Icons.Default.Bed,
                        containerColor = Color(0xFF43A047),
                        contentColor = Color.White,
                        onClick = onNavigateToWards,
                        testTag = "quick_action_wards"
                    )
                }
                item {
                    QuickActionButton(
                        label = "New Invoice",
                        icon = Icons.Default.Receipt,
                        containerColor = Color(0xFF7E57C2),
                        contentColor = Color.White,
                        onClick = onOpenCreateBill,
                        testTag = "quick_action_create_bill"
                    )
                }
            }
        }

        // Hospital Analytics & Trend Visualizations (Patient Admission Trends & Monthly Billing)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            HospitalAnalyticsCard(
                patients = patients,
                bills = bills
            )
        }

        // Live OPD Token Queue
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SectionHeader(
                title = "Live OPD Token Queue",
                actionText = "View All",
                onActionClick = onNavigateToAppointments
            )
            OpdQueueCard(appointments = appointments)
        }

        // In-Patients Under Intensive & Admitted Care
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SectionHeader(
                title = "Critical & Admitted Patients",
                actionText = "All Patients",
                onActionClick = onNavigateToPatients
            )
        }

        val admittedPatients = patients.filter { it.status == "ICU" || it.status == "IPD (Admitted)" }
        if (admittedPatients.isEmpty()) {
            item {
                Text(
                    text = "No in-patients currently admitted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        } else {
            items(admittedPatients) { patient ->
                DashboardPatientItem(
                    patient = patient,
                    onClick = { onPatientClick(patient) }
                )
            }
        }
    }
}

@Composable
fun HospitalReadinessBanner(modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = "Hospital",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CareHospital System Active",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "24/7 Level 1 Trauma Center • ER & ICU Operational",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Surface(
                color = StatusSuccessContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(StatusSuccess)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ONLINE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                        color = StatusSuccess
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
        }
    }
}

@Composable
fun OpdQueueCard(appointments: List<Appointment>, modifier: Modifier = Modifier) {
    val activeAppointments = appointments.filter { it.status == "In Progress" || it.status == "Scheduled" }
    val currentServing = activeAppointments.firstOrNull { it.status == "In Progress" } ?: activeAppointments.firstOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (currentServing != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NOW SERVING",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = currentServing.patientName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${currentServing.doctorName} • ${currentServing.doctorSpecialty}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TOKEN",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "#${currentServing.tokenNumber}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                val nextInQueue = activeAppointments.filter { it.id != currentServing.id }.take(3)
                if (nextInQueue.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Next in Queue: " + nextInQueue.joinToString(separator = ", ") { "Token #${it.tokenNumber} (${it.patientName})" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "No consultations currently waiting in the OPD queue.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DashboardPatientItem(
    patient: Patient,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
            .testTag("dashboard_patient_${patient.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (patient.status == "ICU") StatusIcu.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = patient.fullName.take(2).uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (patient.status == "ICU") StatusIcu else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = patient.fullName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    BloodGroupBadge(bloodGroup = patient.bloodGroup)
                }
                Text(
                    text = "${patient.assignedWard ?: "Admitted"} • Bed: ${patient.assignedBedNumber ?: "Pending"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (patient.diagnosis.isNotEmpty()) {
                    Text(
                        text = patient.diagnosis,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            PatientStatusBadge(status = patient.status)
        }
    }
}
