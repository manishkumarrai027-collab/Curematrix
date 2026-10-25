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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.data.model.Billing
import com.example.data.model.BillingRecord
import com.example.ui.components.FilterChipsRow
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningContainer
import com.example.ui.viewmodel.HospitalViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BillingScreen(
    viewModel: HospitalViewModel,
    onCreateBillClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bills by viewModel.filteredBills.collectAsStateWithLifecycle()
    val rawBills by viewModel.rawBills.collectAsStateWithLifecycle()
    val statusFilter by viewModel.billingStatusFilter.collectAsStateWithLifecycle()

    val filterOptions = listOf("All", "Paid", "Pending", "Insurance Claim")

    val totalPaid = rawBills.filter { it.paymentStatus.equals("Paid", ignoreCase = true) }
        .sumOf { if (it.amount > 0) it.amount else it.totalAmount }
    val totalPending = rawBills.filter { it.paymentStatus.equals("Pending", ignoreCase = true) }
        .sumOf { if (it.amount > 0) it.amount else it.totalAmount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateBillClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("create_bill_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Invoice")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("billing_screen")
        ) {
            // Revenue Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(StatusSuccessContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Paid", tint = StatusSuccess, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Collected", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$${String.format("%.2f", totalPaid)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = StatusSuccess
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(StatusWarningContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PendingActions, contentDescription = "Pending", tint = StatusWarning, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pending Dues", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$${String.format("%.2f", totalPending)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = StatusWarning
                        )
                    }
                }
            }

            // Filter Chips
            FilterChipsRow(
                options = filterOptions,
                selectedOption = statusFilter,
                onOptionSelected = { viewModel.setBillingStatusFilter(it) }
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (bills.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = "No Bills",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No invoices found for this filter",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(bills, key = { it.id }) { bill ->
                        InvoiceCard(
                            bill = bill,
                            onMarkPaid = { viewModel.markBillPaid(bill.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceCard(
    bill: Billing,
    onMarkPaid: () -> Unit,
    modifier: Modifier = Modifier
) {
    val billAmount = if (bill.amount > 0.0) bill.amount else bill.totalAmount
    val dateText = bill.date.ifBlank {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(bill.dateMillis))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("invoice_card_${bill.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Invoice #, Patient Name, Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = bill.billNumber.ifBlank { "INV-#${bill.id}" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Patient: ${bill.patientName}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Billing Date: $dateText",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = when (bill.paymentStatus) {
                        "Paid" -> StatusSuccessContainer
                        "Pending" -> StatusWarningContainer
                        else -> MaterialTheme.colorScheme.tertiaryContainer
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = bill.paymentStatus.uppercase(),
                        color = when (bill.paymentStatus) {
                            "Paid" -> StatusSuccess
                            "Pending" -> StatusWarning
                            else -> MaterialTheme.colorScheme.onTertiaryContainer
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Itemized Breakdown Table
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Doctor Consultation:", style = MaterialTheme.typography.bodySmall)
                        Text("$${bill.consultationFee.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                    }
                    if (bill.wardCharges > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ward / Bed Charges:", style = MaterialTheme.typography.bodySmall)
                            Text("$${bill.wardCharges.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                        }
                    }
                    if (bill.medicineCharges > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pharmacy & Meds:", style = MaterialTheme.typography.bodySmall)
                            Text("$${bill.medicineCharges.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                        }
                    }
                    if (bill.labTestCharges > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Diagnostics & Lab Tests:", style = MaterialTheme.typography.bodySmall)
                            Text("$${bill.labTestCharges.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                        }
                    }
                    if (bill.discount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Discount / Subsidy:", style = MaterialTheme.typography.bodySmall, color = StatusSuccess)
                            Text("-$${bill.discount.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = StatusSuccess)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Total and payment method row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Medical Cost: $${String.format(Locale.US, "%.2f", billAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Method: ${bill.paymentMethod} • Status: ${bill.paymentStatus}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (bill.paymentStatus.equals("Pending", ignoreCase = true)) {
                    Button(
                        onClick = onMarkPaid,
                        modifier = Modifier.testTag("mark_paid_button_${bill.id}")
                    ) {
                        Text("Mark Paid")
                    }
                }
            }
        }
    }
}
