package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Appointment
import com.example.data.model.Doctor
import com.example.data.model.Patient
import com.example.ui.components.FilterChipsRow
import com.example.ui.dialogs.AppointmentDetailDialog
import com.example.ui.dialogs.BookAppointmentDialog
import com.example.ui.dialogs.RescheduleAppointmentDialog
import com.example.ui.theme.StatusIcu
import com.example.ui.theme.StatusIcuContainer
import com.example.ui.theme.StatusIpd
import com.example.ui.theme.StatusIpdContainer
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningContainer
import com.example.ui.viewmodel.HospitalViewModel
import com.example.util.CalendarDay
import com.example.util.DateUtils
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppointmentsViewMode(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CALENDAR("Calendar View", Icons.Default.CalendarMonth),
    LIST("List / Agenda", Icons.Default.ViewAgenda)
}

@Composable
fun AppointmentsScreen(
    viewModel: HospitalViewModel,
    onBookAppointmentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allAppointments by viewModel.rawAppointments.collectAsStateWithLifecycle()
    val filteredAppointments by viewModel.filteredAppointments.collectAsStateWithLifecycle()
    val statusFilter by viewModel.appointmentStatusFilter.collectAsStateWithLifecycle()
    val doctorFilter by viewModel.appointmentDoctorFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.appointmentSearchQuery.collectAsStateWithLifecycle()
    val doctors by viewModel.rawDoctors.collectAsStateWithLifecycle()
    val patients by viewModel.rawPatients.collectAsStateWithLifecycle()

    var viewMode by remember { mutableStateOf(AppointmentsViewMode.CALENDAR) }

    // Calendar state: Year and Month (0-indexed)
    val initialCal = remember { Calendar.getInstance() }
    var currentYear by remember { mutableIntStateOf(initialCal.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableIntStateOf(initialCal.get(Calendar.MONTH)) }
    var selectedDateMillis by remember { mutableLongStateOf(DateUtils.getStartOfDay(System.currentTimeMillis())) }
    var isMonthExpanded by remember { mutableStateOf(true) }

    // Dialogs state
    var appointmentToReschedule by remember { mutableStateOf<Appointment?>(null) }
    var appointmentToViewDetails by remember { mutableStateOf<Appointment?>(null) }
    var showInternalBookDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showInternalBookDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("book_appointment_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Book Appointment")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("appointments_screen")
        ) {
            // View Mode Segmented Tab Row
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Medical Appointments",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "OPD schedule, token queues & specialist consults",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // View Mode Switcher
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(modifier = Modifier.padding(4.dp)) {
                                Surface(
                                    onClick = { viewMode = AppointmentsViewMode.CALENDAR },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (viewMode == AppointmentsViewMode.CALENDAR) MaterialTheme.colorScheme.primary else Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = if (viewMode == AppointmentsViewMode.CALENDAR) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Calendar",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (viewMode == AppointmentsViewMode.CALENDAR) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    onClick = { viewMode = AppointmentsViewMode.LIST },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (viewMode == AppointmentsViewMode.LIST) MaterialTheme.colorScheme.primary else Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.ViewAgenda,
                                            contentDescription = null,
                                            tint = if (viewMode == AppointmentsViewMode.LIST) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Agenda List",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (viewMode == AppointmentsViewMode.LIST) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Appointment Stats Bar
                    AppointmentStatsHeader(allAppointments = allAppointments)
                }
            }

            // Main Content Area: Calendar View or List View
            when (viewMode) {
                AppointmentsViewMode.CALENDAR -> {
                    CalendarViewContent(
                        year = currentYear,
                        month = currentMonth,
                        selectedDateMillis = selectedDateMillis,
                        allAppointments = allAppointments,
                        isMonthExpanded = isMonthExpanded,
                        onToggleMonthExpanded = { isMonthExpanded = !isMonthExpanded },
                        onPrevMonth = {
                            if (currentMonth == 0) {
                                currentMonth = 11
                                currentYear -= 1
                            } else {
                                currentMonth -= 1
                            }
                        },
                        onNextMonth = {
                            if (currentMonth == 11) {
                                currentMonth = 0
                                currentYear += 1
                            } else {
                                currentMonth += 1
                            }
                        },
                        onJumpToToday = {
                            val today = Calendar.getInstance()
                            currentYear = today.get(Calendar.YEAR)
                            currentMonth = today.get(Calendar.MONTH)
                            selectedDateMillis = DateUtils.getStartOfDay(today.timeInMillis)
                        },
                        onSelectDate = { selectedDateMillis = it },
                        onBookAppointmentForDate = { dateMillis ->
                            selectedDateMillis = dateMillis
                            showInternalBookDialog = true
                        },
                        onSendReminder = { appt ->
                            viewModel.sendAppointmentReminder(appt)
                            scope.launch {
                                snackbarHostState.showSnackbar("Reminder sent to ${appt.patientName} via SMS & Email!")
                            }
                        },
                        onConfirm = { appt ->
                            viewModel.confirmAppointment(appt)
                            scope.launch {
                                snackbarHostState.showSnackbar("Appointment confirmed for Token #${appt.tokenNumber}")
                            }
                        },
                        onStatusChange = { appt, newStatus ->
                            viewModel.updateAppointmentStatus(appt, newStatus)
                        },
                        onReschedule = { appt ->
                            appointmentToReschedule = appt
                        },
                        onViewDetails = { appt ->
                            appointmentToViewDetails = appt
                        },
                        onCancel = { appt ->
                            viewModel.cancelAppointment(appt)
                            scope.launch {
                                snackbarHostState.showSnackbar("Token #${appt.tokenNumber} marked Cancelled")
                            }
                        }
                    )
                }

                AppointmentsViewMode.LIST -> {
                    ListViewContent(
                        appointments = filteredAppointments,
                        doctors = doctors,
                        statusFilter = statusFilter,
                        doctorFilter = doctorFilter,
                        searchQuery = searchQuery,
                        onStatusFilterChange = { viewModel.setAppointmentStatusFilter(it) },
                        onDoctorFilterChange = { viewModel.setAppointmentDoctorFilter(it) },
                        onSearchQueryChange = { viewModel.setAppointmentSearchQuery(it) },
                        onSendReminder = { appt ->
                            viewModel.sendAppointmentReminder(appt)
                            scope.launch {
                                snackbarHostState.showSnackbar("Reminder sent to ${appt.patientName} via SMS & Email!")
                            }
                        },
                        onConfirm = { appt ->
                            viewModel.confirmAppointment(appt)
                            scope.launch {
                                snackbarHostState.showSnackbar("Appointment confirmed for Token #${appt.tokenNumber}")
                            }
                        },
                        onStatusChange = { appt, newStatus ->
                            viewModel.updateAppointmentStatus(appt, newStatus)
                        },
                        onReschedule = { appt ->
                            appointmentToReschedule = appt
                        },
                        onViewDetails = { appt ->
                            appointmentToViewDetails = appt
                        },
                        onCancel = { appt ->
                            viewModel.cancelAppointment(appt)
                            scope.launch {
                                snackbarHostState.showSnackbar("Appointment Token #${appt.tokenNumber} marked Cancelled")
                            }
                        }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showInternalBookDialog) {
        BookAppointmentDialog(
            patients = patients,
            doctors = doctors,
            initialDateMillis = selectedDateMillis,
            onDismiss = { showInternalBookDialog = false },
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
                showInternalBookDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("Consultation scheduled for $patientName on $dateString at $timeSlot")
                }
            }
        )
    }

    appointmentToReschedule?.let { appt ->
        RescheduleAppointmentDialog(
            appointment = appt,
            onDismiss = { appointmentToReschedule = null },
            onConfirm = { newMillis, newLabel, newSlot ->
                viewModel.rescheduleAppointment(appt, newMillis, newLabel, newSlot)
                appointmentToReschedule = null
                scope.launch {
                    snackbarHostState.showSnackbar("Rescheduled Token #${appt.tokenNumber} to $newLabel at $newSlot")
                }
            }
        )
    }

    appointmentToViewDetails?.let { appt ->
        AppointmentDetailDialog(
            appointment = appt,
            onDismiss = { appointmentToViewDetails = null },
            onStatusChange = { newStatus ->
                viewModel.updateAppointmentStatus(appt, newStatus)
                appointmentToViewDetails = null
                scope.launch {
                    snackbarHostState.showSnackbar("Token #${appt.tokenNumber} updated to $newStatus")
                }
            },
            onRescheduleClick = {
                val target = appt
                appointmentToViewDetails = null
                appointmentToReschedule = target
            },
            onSendReminder = {
                viewModel.sendAppointmentReminder(appt)
                scope.launch {
                    snackbarHostState.showSnackbar("Reminder sent to ${appt.patientName} via SMS & Email!")
                }
            },
            onCancelClick = {
                viewModel.cancelAppointment(appt)
                appointmentToViewDetails = null
                scope.launch {
                    snackbarHostState.showSnackbar("Token #${appt.tokenNumber} marked Cancelled")
                }
            },
            onDeleteClick = {
                viewModel.deleteAppointment(appt)
                appointmentToViewDetails = null
                scope.launch {
                    snackbarHostState.showSnackbar("Token #${appt.tokenNumber} record removed")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// APPOINTMENT STATS HEADER
// -------------------------------------------------------------
@Composable
fun AppointmentStatsHeader(allAppointments: List<Appointment>) {
    val totalCount = allAppointments.size
    val todayStart = DateUtils.getStartOfDay(System.currentTimeMillis())
    val todayAppointments = allAppointments.filter { DateUtils.isSameDay(it.dateMillis, todayStart) }
    val inProgressCount = allAppointments.count { it.status == "In Progress" }
    val completedCount = allAppointments.count { it.status == "Completed" }
    val scheduledCount = allAppointments.count { it.status == "Scheduled" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatBadge(label = "Today's Consults", count = todayAppointments.size, containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
        StatBadge(label = "In Progress", count = inProgressCount, containerColor = StatusWarningContainer, contentColor = StatusWarning)
        StatBadge(label = "Scheduled", count = scheduledCount, containerColor = StatusIpdContainer, contentColor = StatusIpd)
        StatBadge(label = "Completed", count = completedCount, containerColor = StatusSuccessContainer, contentColor = StatusSuccess)
        StatBadge(label = "Total Records", count = totalCount, containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun StatBadge(
    label: String,
    count: Int,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

// -------------------------------------------------------------
// CALENDAR VIEW CONTENT
// -------------------------------------------------------------
@Composable
fun CalendarViewContent(
    year: Int,
    month: Int,
    selectedDateMillis: Long,
    allAppointments: List<Appointment>,
    isMonthExpanded: Boolean,
    onToggleMonthExpanded: () -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onJumpToToday: () -> Unit,
    onSelectDate: (Long) -> Unit,
    onBookAppointmentForDate: (Long) -> Unit,
    onSendReminder: (Appointment) -> Unit,
    onConfirm: (Appointment) -> Unit,
    onStatusChange: (Appointment, String) -> Unit,
    onReschedule: (Appointment) -> Unit,
    onViewDetails: (Appointment) -> Unit,
    onCancel: (Appointment) -> Unit
) {
    val monthDays = remember(year, month) { DateUtils.getMonthMatrix(year, month) }
    val weekStripDays = remember(selectedDateMillis) { DateUtils.getWeekStripDays(selectedDateMillis, daysBefore = 3, daysAfter = 10) }

    // Filter appointments for the selected date
    val dayAppointments = remember(selectedDateMillis, allAppointments) {
        allAppointments.filter { DateUtils.isSameDay(it.dateMillis, selectedDateMillis) }
            .sortedBy { it.timeSlot }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // Month Navigation Header
        item {
            CalendarNavigationHeader(
                year = year,
                month = month,
                isMonthExpanded = isMonthExpanded,
                onPrevMonth = onPrevMonth,
                onNextMonth = onNextMonth,
                onJumpToToday = onJumpToToday,
                onToggleExpand = onToggleMonthExpanded
            )
        }

        // Calendar Grid or Week Strip
        item {
            AnimatedVisibility(
                visible = isMonthExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                MonthCalendarGrid(
                    days = monthDays,
                    currentMonth = month,
                    selectedDateMillis = selectedDateMillis,
                    appointments = allAppointments,
                    onSelectDate = onSelectDate
                )
            }

            AnimatedVisibility(
                visible = !isMonthExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                WeekHorizontalStrip(
                    days = weekStripDays,
                    selectedDateMillis = selectedDateMillis,
                    appointments = allAppointments,
                    onSelectDate = onSelectDate
                )
            }
        }

        // Day Agenda Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SelectedDateAgendaHeader(
                selectedDateMillis = selectedDateMillis,
                appointmentCount = dayAppointments.size,
                onScheduleClick = { onBookAppointmentForDate(selectedDateMillis) }
            )
        }

        // Daily Schedule Items
        if (dayAppointments.isEmpty()) {
            item {
                EmptyDayScheduleCard(
                    dateMillis = selectedDateMillis,
                    onScheduleClick = { onBookAppointmentForDate(selectedDateMillis) }
                )
            }
        } else {
            items(dayAppointments, key = { it.id }) { appointment ->
                AppointmentTimelineItem(
                    appointment = appointment,
                    onSendReminder = { onSendReminder(appointment) },
                    onConfirm = { onConfirm(appointment) },
                    onStatusChange = { newStatus -> onStatusChange(appointment, newStatus) },
                    onReschedule = { onReschedule(appointment) },
                    onViewDetails = { onViewDetails(appointment) },
                    onCancel = { onCancel(appointment) }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// CALENDAR NAVIGATION HEADER
// -------------------------------------------------------------
@Composable
fun CalendarNavigationHeader(
    year: Int,
    month: Int,
    isMonthExpanded: Boolean,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onJumpToToday: () -> Unit,
    onToggleExpand: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevMonth, modifier = Modifier.size(36.dp).testTag("cal_prev_month")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                }

                Text(
                    text = DateUtils.formatMonthYear(year, month),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(onClick = onNextMonth, modifier = Modifier.size(36.dp).testTag("cal_next_month")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = onJumpToToday,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp).testTag("cal_jump_today")
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Today", style = MaterialTheme.typography.labelSmall)
                }

                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(36.dp).testTag("cal_toggle_expand")
                ) {
                    Icon(
                        imageVector = if (isMonthExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isMonthExpanded) "Collapse Month" else "Expand Month"
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MONTH CALENDAR GRID
// -------------------------------------------------------------
@Composable
fun MonthCalendarGrid(
    days: List<CalendarDay>,
    currentMonth: Int,
    selectedDateMillis: Long,
    appointments: List<Appointment>,
    onSelectDate: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag("month_calendar_grid"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Day of week header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DateUtils.dayHeaders.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 6 rows x 7 days grid
            val weeks = days.chunked(7)
            weeks.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    week.forEach { day ->
                        val isSelected = DateUtils.isSameDay(day.millis, selectedDateMillis)
                        val dayAppointments = appointments.filter { DateUtils.isSameDay(it.dateMillis, day.millis) }

                        CalendarDayCell(
                            day = day,
                            isCurrentMonth = day.month == currentMonth,
                            isSelected = isSelected,
                            appointments = dayAppointments,
                            onClick = { onSelectDate(day.millis) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    day: CalendarDay,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    appointments: List<Appointment>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha = if (isCurrentMonth) 1.0f else 0.35f
    val hasAppointments = appointments.isNotEmpty()

    Box(
        modifier = modifier
            .padding(2.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    day.isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (day.isToday && !isSelected) 1.5.dp else 0.dp,
                color = if (day.isToday && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .testTag("cal_day_${day.dayOfMonth}_${day.month}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected || day.isToday) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                ),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    day.isToday -> MaterialTheme.colorScheme.primary
                    isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                }
            )

            // Indicators
            if (hasAppointments) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    appointments.take(3).forEach { appt ->
                        val dotColor = when (appt.status) {
                            "Completed" -> StatusSuccess
                            "In Progress" -> StatusWarning
                            "Cancelled" -> StatusIcu
                            else -> MaterialTheme.colorScheme.primary
                        }
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 1.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                    if (appointments.size > 3) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(7.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// HORIZONTAL WEEK STRIP
// -------------------------------------------------------------
@Composable
fun WeekHorizontalStrip(
    days: List<CalendarDay>,
    selectedDateMillis: Long,
    appointments: List<Appointment>,
    onSelectDate: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(days) { day ->
                val isSelected = DateUtils.isSameDay(day.millis, selectedDateMillis)
                val dayAppointments = appointments.filter { DateUtils.isSameDay(it.dateMillis, day.millis) }

                Surface(
                    onClick = { onSelectDate(day.millis) },
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        day.isToday -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    },
                    modifier = Modifier.width(52.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day.dayOfWeekShort.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = day.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        if (dayAppointments.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(6.dp))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SELECTED DATE AGENDA HEADER
// -------------------------------------------------------------
@Composable
fun SelectedDateAgendaHeader(
    selectedDateMillis: Long,
    appointmentCount: Int,
    onScheduleClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = DateUtils.formatFullDate(selectedDateMillis),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (appointmentCount == 0) "No consultations scheduled" else "$appointmentCount scheduled consultation${if (appointmentCount > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onScheduleClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("schedule_for_date_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Book OPD", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

// -------------------------------------------------------------
// EMPTY DAY CARD
// -------------------------------------------------------------
@Composable
fun EmptyDayScheduleCard(
    dateMillis: Long,
    onScheduleClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "No Appointments on this Date",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Specialist schedules are open for ${DateUtils.formatDayMonth(dateMillis)}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onScheduleClick,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("empty_schedule_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Schedule OPD Token")
            }
        }
    }
}

// -------------------------------------------------------------
// TIMELINE ITEM FOR CALENDAR VIEW
// -------------------------------------------------------------
@Composable
fun AppointmentTimelineItem(
    appointment: Appointment,
    onSendReminder: () -> Unit,
    onConfirm: () -> Unit,
    onStatusChange: (String) -> Unit,
    onReschedule: () -> Unit,
    onViewDetails: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Time Indicator Column
        Column(
            modifier = Modifier.width(72.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = appointment.timeSlot,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Token #${appointment.tokenNumber}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        val statusInfo = getAppointmentStatusInfo(appointment.status)

        // Card
        Card(
            modifier = Modifier
                .weight(1f)
                .testTag("appointment_card_${appointment.id}")
                .clickable(onClick = onViewDetails),
            colors = CardDefaults.cardColors(
                containerColor = if (appointment.status == "Cancelled") {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            border = BorderStroke(1.5.dp, statusInfo.borderColor.copy(alpha = 0.8f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                // Colored left status accent bar
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                        .background(statusInfo.contentColor)
                )

                Column(modifier = Modifier.weight(1f).padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = appointment.patientName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${appointment.doctorName} • ${appointment.doctorSpecialty}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        AppointmentStatusBadge(
                            status = appointment.status,
                            onStatusToggle = onStatusChange
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = appointment.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Toggle Control Row
                    AppointmentStatusToggleRow(
                        currentStatus = appointment.status,
                        onStatusChange = onStatusChange,
                        appointmentId = appointment.id
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onSendReminder,
                            modifier = Modifier.size(32.dp).testTag("send_reminder_button_${appointment.id}")
                        ) {
                            Icon(
                                imageVector = if (appointment.reminderSent) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "Reminder",
                                tint = if (appointment.reminderSent) StatusSuccess else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = onReschedule,
                            modifier = Modifier.size(32.dp).testTag("reschedule_icon_${appointment.id}")
                        ) {
                            Icon(
                                Icons.Default.EditCalendar,
                                contentDescription = "Reschedule",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (appointment.status == "Scheduled") {
                            Button(
                                onClick = { onStatusChange("In Progress") },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp).testTag("start_consultation_${appointment.id}")
                            ) {
                                Text("Start", style = MaterialTheme.typography.labelSmall)
                            }
                        } else if (appointment.status == "In Progress") {
                            Button(
                                onClick = { onStatusChange("Completed") },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp).testTag("complete_consultation_${appointment.id}")
                            ) {
                                Text("Complete", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        OutlinedButton(
                            onClick = onViewDetails,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp).testTag("details_button_${appointment.id}")
                        ) {
                            Text("Details", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
}

// -------------------------------------------------------------
// LIST / AGENDA VIEW CONTENT
// -------------------------------------------------------------
@Composable
fun ListViewContent(
    appointments: List<Appointment>,
    doctors: List<Doctor>,
    statusFilter: String,
    doctorFilter: String,
    searchQuery: String,
    onStatusFilterChange: (String) -> Unit,
    onDoctorFilterChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSendReminder: (Appointment) -> Unit,
    onConfirm: (Appointment) -> Unit,
    onStatusChange: (Appointment, String) -> Unit,
    onReschedule: (Appointment) -> Unit,
    onViewDetails: (Appointment) -> Unit,
    onCancel: (Appointment) -> Unit
) {
    val statusOptions = listOf("All", "Scheduled", "In Progress", "Completed", "Cancelled")
    var doctorMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search by patient, doctor, reason, token...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("appointment_search_input")
            )
        }

        // Filter chips and Doctor filter row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Status Chips
            FilterChipsRow(
                options = statusOptions,
                selectedOption = statusFilter,
                onOptionSelected = onStatusFilterChange
            )
        }

        // Doctor Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                OutlinedButton(
                    onClick = { doctorMenuExpanded = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("doctor_filter_button")
                ) {
                    Text(
                        text = "Doctor: $doctorFilter",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp))
                }

                DropdownMenu(
                    expanded = doctorMenuExpanded,
                    onDismissRequest = { doctorMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Doctors") },
                        onClick = {
                            onDoctorFilterChange("All Doctors")
                            doctorMenuExpanded = false
                        }
                    )
                    doctors.forEach { doc ->
                        DropdownMenuItem(
                            text = { Text("${doc.fullName} (${doc.specialty})") },
                            onClick = {
                                onDoctorFilterChange(doc.fullName)
                                doctorMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${appointments.size} appointments",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (appointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "No Appointments",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No appointments match your filters",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try adjusting the status or doctor filter, or tap + to book",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(appointments, key = { it.id }) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onSendReminder = { onSendReminder(appointment) },
                        onConfirm = { onConfirm(appointment) },
                        onStatusChange = { newStatus -> onStatusChange(appointment, newStatus) },
                        onReschedule = { onReschedule(appointment) },
                        onViewDetails = { onViewDetails(appointment) },
                        onCancel = { onCancel(appointment) }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// FULL APPOINTMENT CARD (LIST VIEW)
// -------------------------------------------------------------
@Composable
fun AppointmentCard(
    appointment: Appointment,
    onSendReminder: () -> Unit,
    onConfirm: () -> Unit,
    onStatusChange: (String) -> Unit,
    onReschedule: () -> Unit = {},
    onViewDetails: () -> Unit = {},
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusInfo = getAppointmentStatusInfo(appointment.status)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("appointment_card_${appointment.id}")
            .clickable(onClick = onViewDetails),
        colors = CardDefaults.cardColors(
            containerColor = if (appointment.status == "Cancelled") {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(1.5.dp, statusInfo.borderColor.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Colored left status accent bar
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(statusInfo.contentColor)
            )

            Column(modifier = Modifier.weight(1f).padding(14.dp)) {
                // Header: Token Badge, Patient Name, Interactive Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
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
                                text = "#${appointment.tokenNumber}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appointment.patientName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${appointment.doctorName} • ${appointment.doctorSpecialty}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    AppointmentStatusBadge(
                        status = appointment.status,
                        onStatusToggle = onStatusChange
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Time & Date Slot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Time",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${DateUtils.formatDayMonth(appointment.dateMillis)} • ${appointment.timeSlot}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Confirmation chip
                    Surface(
                        color = if (appointment.isConfirmed) StatusSuccessContainer else StatusWarningContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (appointment.isConfirmed) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = "Confirmation",
                                tint = if (appointment.isConfirmed) StatusSuccess else StatusWarning,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (appointment.isConfirmed) "Confirmed" else "Pending Confirmation",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = if (appointment.isConfirmed) StatusSuccess else StatusWarning
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Reason & Clinical Notes
                Text(
                    text = "Reason: ${appointment.reason}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (appointment.notes.isNotEmpty()) {
                    Text(
                        text = "Notes: ${appointment.notes}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Status Toggle Row with Color-Coded Chips
                AppointmentStatusToggleRow(
                    currentStatus = appointment.status,
                    onStatusChange = onStatusChange,
                    appointmentId = appointment.id
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reminder Trigger Button
                    OutlinedButton(
                        onClick = onSendReminder,
                        modifier = Modifier.testTag("send_reminder_button_${appointment.id}"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (appointment.reminderSent) StatusSuccess else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (appointment.reminderSent) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = "Reminder",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (appointment.reminderSent) "Reminder Sent" else "Send Reminder",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = onReschedule,
                            modifier = Modifier.testTag("reschedule_button_${appointment.id}")
                        ) {
                            Text("Reschedule", style = MaterialTheme.typography.labelSmall)
                        }

                        if (appointment.status == "Scheduled") {
                            Button(
                                onClick = { onStatusChange("In Progress") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("start_consultation_${appointment.id}")
                            ) {
                                Text("Start", style = MaterialTheme.typography.labelSmall)
                            }
                        } else if (appointment.status == "In Progress") {
                            Button(
                                onClick = { onStatusChange("Completed") },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                modifier = Modifier.testTag("complete_consultation_${appointment.id}")
                            ) {
                                Text("Complete", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        if (appointment.status != "Completed" && appointment.status != "Cancelled") {
                            OutlinedButton(
                                onClick = onCancel,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.testTag("cancel_appointment_${appointment.id}")
                            ) {
                                Text("Cancel", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STATUS STYLING, BADGES, AND TOGGLE CONTROLS
// -------------------------------------------------------------
data class AppointmentStatusStyle(
    val label: String,
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val icon: ImageVector
)

fun getAppointmentStatusInfo(status: String): AppointmentStatusStyle {
    return when (status) {
        "In Progress" -> AppointmentStatusStyle(
            label = "In Progress",
            containerColor = StatusWarningContainer,
            contentColor = StatusWarning,
            borderColor = StatusWarning,
            icon = Icons.Default.PlayArrow
        )
        "Completed" -> AppointmentStatusStyle(
            label = "Completed",
            containerColor = StatusSuccessContainer,
            contentColor = StatusSuccess,
            borderColor = StatusSuccess,
            icon = Icons.Default.CheckCircle
        )
        "Cancelled" -> AppointmentStatusStyle(
            label = "Cancelled",
            containerColor = StatusIcuContainer,
            contentColor = StatusIcu,
            borderColor = StatusIcu,
            icon = Icons.Default.Clear
        )
        else -> AppointmentStatusStyle(
            label = "Scheduled",
            containerColor = StatusIpdContainer,
            contentColor = StatusIpd,
            borderColor = StatusIpd,
            icon = Icons.Default.Schedule
        )
    }
}

@Composable
fun AppointmentStatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    onStatusToggle: ((String) -> Unit)? = null
) {
    val style = getAppointmentStatusInfo(status)
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            color = style.containerColor,
            border = BorderStroke(1.dp, style.borderColor.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(enabled = onStatusToggle != null) {
                    showMenu = true
                }
                .testTag("appointment_status_badge_${status.lowercase().replace(" ", "_")}")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = style.contentColor,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = style.label,
                    color = style.contentColor,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                if (onStatusToggle != null) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle status",
                        tint = style.contentColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        if (onStatusToggle != null) {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                val availableStatuses = listOf("Scheduled", "In Progress", "Completed", "Cancelled")
                availableStatuses.forEach { targetStatus ->
                    val targetStyle = getAppointmentStatusInfo(targetStatus)
                    val isSelected = targetStatus == status
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(targetStyle.contentColor)
                                )
                                Text(
                                    text = targetStatus,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) targetStyle.contentColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = targetStyle.icon,
                                contentDescription = null,
                                tint = targetStyle.contentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Current status",
                                    tint = targetStyle.contentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        onClick = {
                            showMenu = false
                            if (!isSelected) {
                                onStatusToggle(targetStatus)
                            }
                        },
                        modifier = Modifier.testTag("menu_status_${targetStatus.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }
    }
}

@Composable
fun AppointmentStatusToggleRow(
    currentStatus: String,
    onStatusChange: (String) -> Unit,
    appointmentId: Long,
    modifier: Modifier = Modifier
) {
    val statuses = listOf("Scheduled", "In Progress", "Completed", "Cancelled")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "STATUS TOGGLE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Tap to change",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            statuses.forEach { st ->
                val isSelected = st == currentStatus
                val style = getAppointmentStatusInfo(st)

                Surface(
                    color = if (isSelected) style.contentColor else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) style.contentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (!isSelected) {
                                onStatusChange(st)
                            }
                        }
                        .testTag("status_toggle_${st.lowercase().replace(" ", "_")}_$appointmentId")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = style.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(style.contentColor)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                        Text(
                            text = when (st) {
                                "In Progress" -> "Active"
                                else -> st
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
