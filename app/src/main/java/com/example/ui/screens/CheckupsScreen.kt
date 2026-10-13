package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoctorCheckup
import com.example.ui.components.VoiceTextField
import com.example.ui.viewmodel.HealthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CheckupsScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val checkups by viewModel.checkups.collectAsState()
    val isElderlyMode by viewModel.isElderlyMode.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredCheckups = remember(checkups, searchQuery) {
        if (searchQuery.isBlank()) checkups
        else checkups.filter {
            it.doctorName.contains(searchQuery, ignoreCase = true) ||
                    it.specialty.contains(searchQuery, ignoreCase = true) ||
                    it.diagnosisReason.contains(searchQuery, ignoreCase = true)
        }
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header & Search
            Text(
                text = "Medical Checkups & Consultations",
                style = MaterialTheme.typography.titleLarge,
                fontSize = if (isElderlyMode) 22.sp else 19.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Chronological records of doctor visits and clinical recommendations",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by doctor, clinic, or condition...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkups_search_bar")
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredCheckups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No checkups matched \"$searchQuery\"" else "No medical checkups recorded yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredCheckups, key = { it.id }) { checkup ->
                        DoctorCheckupCard(
                            checkup = checkup,
                            isElderlyMode = isElderlyMode,
                            dateFormat = dateFormat,
                            onDelete = { viewModel.deleteCheckup(checkup) },
                            onTriggerReminder = {
                                checkup.followUpDateMillis?.let { fDate ->
                                    com.example.util.NotificationHelper.triggerAppointmentReminder(
                                        context,
                                        checkup.doctorName,
                                        checkup.clinicOrHospital,
                                        dateFormat.format(Date(fDate))
                                    )
                                    Toast.makeText(context, "Notification sent for follow-up with ${checkup.doctorName}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // FAB to Add Checkup
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("add_checkup_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Medical Checkup")
        }
    }

    if (showAddDialog) {
        AddCheckupDialog(
            isElderlyMode = isElderlyMode,
            onDismiss = { showAddDialog = false },
            onConfirm = { newCheckup ->
                viewModel.addCheckup(newCheckup)
                showAddDialog = false
                Toast.makeText(context, "Checkup with ${newCheckup.doctorName} recorded", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun DoctorCheckupCard(
    checkup: DoctorCheckup,
    isElderlyMode: Boolean,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit,
    onTriggerReminder: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("checkup_card_${checkup.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Doctor & Specialty Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(if (isElderlyMode) 46.dp else 40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = checkup.doctorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = if (isElderlyMode) 19.sp else 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = checkup.specialty,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = if (isElderlyMode) 14.sp else 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = dateFormat.format(Date(checkup.checkupDateMillis)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clinic Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = checkup.clinicOrHospital,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = if (isElderlyMode) 13.sp else 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reason for Visit
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Diagnosis / Reason for Visit:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = checkup.diagnosisReason,
                        fontSize = if (isElderlyMode) 15.sp else 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clinical Notes
            Text(
                text = "Doctor's Advice & Clinical Notes:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = checkup.clinicalNotes,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = if (isElderlyMode) 15.sp else 13.sp,
                lineHeight = if (isElderlyMode) 22.sp else 19.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Vitals recorded at visit
            if (checkup.vitalsSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Visit Vitals: ${checkup.vitalsSummary}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Follow-up Appointment & Reminders
            if (checkup.followUpDateMillis != null) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Follow-Up Scheduled",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = dateFormat.format(Date(checkup.followUpDateMillis)),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        IconButton(
                            onClick = onTriggerReminder,
                            modifier = Modifier.size(32.dp).testTag("trigger_reminder_${checkup.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Test Notification Reminder",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Card Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Checkup",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddCheckupDialog(
    isElderlyMode: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (DoctorCheckup) -> Unit
) {
    var doctorName by remember { mutableStateOf("Dr. Sarah Jenkins, MD") }
    var specialty by remember { mutableStateOf("Cardiology") }
    var clinic by remember { mutableStateOf("Heart & Vascular Health Pavilion") }
    var diagnosisReason by remember { mutableStateOf("Blood pressure checkup & medication review") }
    var clinicalNotes by remember { mutableStateOf("Patient instructed to continue walking daily. Low sodium diet emphasized.") }
    var vitalsSummary by remember { mutableStateOf("BP: 124/80 mmHg, HR: 68 bpm") }
    var setReminder by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Doctor Checkup",
                fontWeight = FontWeight.Bold,
                fontSize = if (isElderlyMode) 22.sp else 19.sp
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(
                        value = doctorName,
                        onValueChange = { doctorName = it },
                        label = { Text("Attending Doctor Name") },
                        modifier = Modifier.fillMaxWidth().testTag("input_doctor_name")
                    )
                }

                item {
                    OutlinedTextField(
                        value = specialty,
                        onValueChange = { specialty = it },
                        label = { Text("Medical Specialty") },
                        modifier = Modifier.fillMaxWidth().testTag("input_specialty")
                    )
                }

                item {
                    OutlinedTextField(
                        value = clinic,
                        onValueChange = { clinic = it },
                        label = { Text("Clinic or Hospital Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = diagnosisReason,
                        onValueChange = { diagnosisReason = it },
                        label = { Text("Reason for Visit / Diagnosis") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = vitalsSummary,
                        onValueChange = { vitalsSummary = it },
                        label = { Text("In-Clinic Vitals (BP, HR, Weight)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Voice Dictation Notes
                item {
                    Text(
                        text = "Clinical Notes (Voice Dictation Supported):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    VoiceTextField(
                        value = clinicalNotes,
                        onValueChange = { clinicalNotes = it },
                        label = "Doctor's Advice & Notes",
                        placeholder = "Tap microphone to dictate clinical instructions...",
                        minLines = 3,
                        maxLines = 5,
                        isElderlyMode = isElderlyMode,
                        testTag = "checkup_notes_voice_field"
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Schedule Follow-Up Reminder", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Alert in 30 days for routine review", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = setReminder,
                            onCheckedChange = { setReminder = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (doctorName.isNotBlank()) {
                        val now = System.currentTimeMillis()
                        val followUpMillis = if (setReminder) now + (30 * 86_400_000L) else null
                        onConfirm(
                            DoctorCheckup(
                                doctorName = doctorName,
                                specialty = specialty,
                                clinicOrHospital = clinic,
                                checkupDateMillis = now,
                                diagnosisReason = diagnosisReason,
                                clinicalNotes = clinicalNotes,
                                followUpDateMillis = followUpMillis,
                                followUpReminderSet = setReminder,
                                vitalsSummary = vitalsSummary,
                                syncStatus = "SYNCED"
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("save_checkup_button")
            ) {
                Text("Save Checkup")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
