package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.WardBed
import com.example.ui.components.FilterChipsRow
import com.example.ui.theme.StatusIcu
import com.example.ui.theme.StatusIcuContainer
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningContainer
import com.example.ui.viewmodel.HospitalViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WardsBedsScreen(
    viewModel: HospitalViewModel,
    onAllocateBedClick: (WardBed) -> Unit,
    modifier: Modifier = Modifier
) {
    val beds by viewModel.filteredBeds.collectAsStateWithLifecycle()
    val allBeds by viewModel.rawBeds.collectAsStateWithLifecycle()
    val wardFilter by viewModel.wardFilter.collectAsStateWithLifecycle()

    val wardOptions = listOf("All Wards", "ICU", "General Ward A", "Pediatric Ward", "Semi-Private", "Emergency Unit")

    val totalBeds = allBeds.size
    val occupiedBeds = allBeds.count { it.isOccupied }
    val occupancyPercentage = if (totalBeds > 0) (occupiedBeds * 100) / totalBeds else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("wards_beds_screen")
    ) {
        // Ward Capacity Progress Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hospital Bed Capacity",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$occupiedBeds Occupied • ${totalBeds - occupiedBeds} Available Vacancies",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "$occupancyPercentage%",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (occupancyPercentage > 80) StatusWarning else StatusSuccess
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { if (totalBeds > 0) occupiedBeds.toFloat() / totalBeds.toFloat() else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (occupancyPercentage > 80) StatusWarning else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Ward Filter Chips
        FilterChipsRow(
            options = wardOptions,
            selectedOption = wardFilter,
            onOptionSelected = { viewModel.setWardFilter(it) }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Beds List
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(beds, key = { it.id }) { bed ->
                BedCard(
                    bed = bed,
                    onAllocate = { onAllocateBedClick(bed) },
                    onVacate = { viewModel.vacateBed(bed.id) }
                )
            }
        }
    }
}

@Composable
fun BedCard(
    bed: WardBed,
    onAllocate: () -> Unit,
    onVacate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bed_card_${bed.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (bed.isOccupied) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (bed.isOccupied) {
                                if (bed.wardName == "ICU") StatusIcuContainer else MaterialTheme.colorScheme.primaryContainer
                            } else StatusSuccessContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bed,
                        contentDescription = "Bed",
                        tint = if (bed.isOccupied) {
                            if (bed.wardName == "ICU") StatusIcu else MaterialTheme.colorScheme.primary
                        } else StatusSuccess,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${bed.wardName} - ${bed.bedNumber}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Daily Rate: $${bed.dailyRate.toInt()}/day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = if (bed.isOccupied) {
                        if (bed.wardName == "ICU") StatusIcuContainer else StatusWarningContainer
                    } else StatusSuccessContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (bed.isOccupied) "OCCUPIED" else "AVAILABLE",
                        color = if (bed.isOccupied) {
                            if (bed.wardName == "ICU") StatusIcu else StatusWarning
                        } else StatusSuccess,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (bed.isOccupied) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Patient Admitted: ${bed.occupiedPatientName ?: "Unknown"}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (bed.admissionDateMillis != null) {
                            val formattedDate = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(bed.admissionDateMillis))
                            Text(
                                text = "Admitted on: $formattedDate",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onVacate,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("vacate_bed_${bed.id}")
                    ) {
                        Text("Vacate / Discharge Bed")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onAllocate,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("allocate_bed_${bed.id}")
                    ) {
                        Text("Allocate / Admit Patient")
                    }
                }
            }
        }
    }
}
