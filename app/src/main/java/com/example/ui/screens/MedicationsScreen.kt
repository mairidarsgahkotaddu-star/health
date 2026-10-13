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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.data.model.MedicationLog
import com.example.ui.components.VoiceTextField
import com.example.ui.viewmodel.HealthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MedicationsScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val medications by viewModel.medications.collectAsState()
    val isElderlyMode by viewModel.isElderlyMode.collectAsState()
    val context = LocalContext.current

    var showAddMedDialog by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    val totalDoses = medications.sumOf { it.totalDosesTaken }
    val maxStreak = medications.maxOfOrNull { it.adherenceStreakDays } ?: 0

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header
            Text(
                text = "Medication Logs & Adherence",
                style = MaterialTheme.typography.titleLarge,
                fontSize = if (isElderlyMode) 22.sp else 19.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Prescriptions, daily doses checklist, and adherence push alerts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Adherence Stats Summary Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth().testTag("adherence_summary_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Prescription Adherence",
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isElderlyMode) 16.sp else 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Active regimen: ${medications.count { it.isActive }} prescriptions",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$maxStreak Days",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "Streak",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (medications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No medications logged yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(medications, key = { it.id }) { med ->
                        MedicationCard(
                            med = med,
                            isElderlyMode = isElderlyMode,
                            dateFormat = dateFormat,
                            onTakeDose = {
                                viewModel.markMedicationTaken(med)
                                Toast.makeText(context, "Dose logged: ${med.medicationName} marked taken!", Toast.LENGTH_SHORT).show()
                            },
                            onTriggerAlert = {
                                viewModel.triggerMedicationReminderNow(med)
                                Toast.makeText(context, "Push reminder sent for ${med.medicationName}", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { viewModel.deleteMedication(med) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // FAB to add medication
        FloatingActionButton(
            onClick = { showAddMedDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("add_medication_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Prescription")
        }
    }

    if (showAddMedDialog) {
        AddMedicationDialog(
            isElderlyMode = isElderlyMode,
            onDismiss = { showAddMedDialog = false },
            onConfirm = { newMed ->
                viewModel.addMedication(newMed)
                showAddMedDialog = false
                Toast.makeText(context, "Prescription for ${newMed.medicationName} added", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun MedicationCard(
    med: MedicationLog,
    isElderlyMode: Boolean,
    dateFormat: SimpleDateFormat,
    onTakeDose: () -> Unit,
    onTriggerAlert: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("med_card_${med.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(if (isElderlyMode) 44.dp else 38.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = med.medicationName,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = if (isElderlyMode) 20.sp else 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${med.dosage} • ${med.frequency}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = if (isElderlyMode) 14.sp else 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    color = Color(0xFF16A34A).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${med.adherenceStreakDays}d streak",
                        color = Color(0xFF16A34A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scheduled times
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Times: ${med.scheduledTimes}",
                    fontSize = if (isElderlyMode) 14.sp else 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Prescribed by
            if (med.prescribingDoctor.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Prescribing Physician: ${med.prescribingDoctor}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Voice notes & instructions
            if (med.instructionsVoiceNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Instructions: ${med.instructionsVoiceNotes}",
                        fontSize = if (isElderlyMode) 14.sp else 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Take Dose & Push Reminder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onTakeDose,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .height(if (isElderlyMode) 50.dp else 42.dp)
                        .testTag("take_dose_button_${med.id}")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mark Taken",
                        fontSize = if (isElderlyMode) 15.sp else 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTriggerAlert,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("med_alert_button_${med.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Send push notification reminder",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete medication",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddMedicationDialog(
    isElderlyMode: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (MedicationLog) -> Unit
) {
    var medName by remember { mutableStateOf("Amlodipine") }
    var dosage by remember { mutableStateOf("5 mg") }
    var frequency by remember { mutableStateOf("Once daily with breakfast") }
    var scheduledTimes by remember { mutableStateOf("08:00 AM") }
    var doctor by remember { mutableStateOf("Dr. Robert Sterling, MD") }
    var instructionsVoiceNotes by remember { mutableStateOf("Take in morning. Do not take with grapefruit juice.") }
    var pushReminders by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Prescription",
                fontWeight = FontWeight.Bold,
                fontSize = if (isElderlyMode) 22.sp else 19.sp
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("Medication Name") },
                        modifier = Modifier.fillMaxWidth().testTag("input_med_name")
                    )
                }

                item {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage (e.g. 10 mg, 500 mg)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = frequency,
                        onValueChange = { frequency = it },
                        label = { Text("Frequency & Schedule") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = scheduledTimes,
                        onValueChange = { scheduledTimes = it },
                        label = { Text("Scheduled Time (e.g. 08:00 AM)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = doctor,
                        onValueChange = { doctor = it },
                        label = { Text("Prescribing Physician") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Voice-to-Text field for instructions
                item {
                    Text(
                        text = "Instructions & Notes (Voice Dictation Supported):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    VoiceTextField(
                        value = instructionsVoiceNotes,
                        onValueChange = { instructionsVoiceNotes = it },
                        label = "Instructions / Precautions",
                        placeholder = "Tap mic to dictate special directions...",
                        minLines = 2,
                        maxLines = 4,
                        isElderlyMode = isElderlyMode,
                        testTag = "med_notes_voice_field"
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Push Notification Alerts", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Daily reminder popup to ensure adherence", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = pushReminders,
                            onCheckedChange = { pushReminders = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (medName.isNotBlank()) {
                        onConfirm(
                            MedicationLog(
                                medicationName = medName,
                                dosage = dosage,
                                frequency = frequency,
                                scheduledTimes = scheduledTimes,
                                prescribingDoctor = doctor,
                                instructionsVoiceNotes = instructionsVoiceNotes,
                                isReminderActive = pushReminders,
                                lastTakenDateMillis = null,
                                adherenceStreakDays = 0,
                                totalDosesTaken = 0,
                                isActive = true
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("save_medication_button")
            ) {
                Text("Save Prescription")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
