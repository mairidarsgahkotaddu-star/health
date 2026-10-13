package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VitalTrendCharts
import com.example.ui.viewmodel.HealthTab
import com.example.ui.viewmodel.HealthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VitalsOverviewScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val vitals by viewModel.vitals.collectAsState()
    val isSyncingWearable by viewModel.isSyncingWearable.collectAsState()
    val wearableInfo by viewModel.wearableDeviceInfo.collectAsState()
    val isElderlyMode by viewModel.isElderlyMode.collectAsState()
    val isOfflineMode by viewModel.isOfflineModeActive.collectAsState()
    val lastSyncTime by viewModel.lastCloudSyncTimestamp.collectAsState()

    var showManualLogDialog by remember { mutableStateOf(false) }

    val latestVital = vitals.firstOrNull()
    val dateFormat = SimpleDateFormat("h:mm a, MMM dd", Locale.getDefault())

    val infiniteTransition = rememberInfiniteTransition(label = "syncSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Status & Offline / Cloud Banner
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isOfflineMode) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = if (isOfflineMode) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isOfflineMode) "Offline Mode • Secured Locally" else "Cloud Vault Synced (AES-256)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isOfflineMode) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        IconButton(
                            onClick = { viewModel.lockApp() },
                            modifier = Modifier.size(28.dp).testTag("quick_lock_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock Medical Records",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 2. Wearable Fitness Tracker Integration Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("wearable_sync_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BluetoothConnected,
                                        contentDescription = "Wearable Tracker",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = wearableInfo.deviceName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "${wearableInfo.connectionStatus} • Battery ${wearableInfo.batteryPercentage}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Last synced: ${dateFormat.format(Date(wearableInfo.lastSyncTimestamp))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )

                            Button(
                                onClick = { viewModel.syncWithWearableTracker() },
                                enabled = !isSyncingWearable,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(if (isElderlyMode) 48.dp else 40.dp)
                                    .testTag("sync_wearable_button")
                            ) {
                                if (isSyncingWearable) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.rotate(rotation).size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Syncing Vitals...", fontSize = 13.sp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sync Vitals Now", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Current Live Vitals 4-Grid
            item {
                Text(
                    text = "Current Physiological Metrics",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = if (isElderlyMode) 20.sp else 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Blood Pressure
                    VitalStatCard(
                        title = "Blood Pressure",
                        value = "${latestVital?.systolicBp ?: 124}/${latestVital?.diastolicBp ?: 80}",
                        unit = "mmHg",
                        status = "Normal Target",
                        statusColor = Color(0xFF16A34A),
                        icon = Icons.Default.Speed,
                        iconTint = Color(0xFFEF4444),
                        isElderlyMode = isElderlyMode,
                        modifier = Modifier.weight(1f)
                    )

                    // Heart Rate
                    VitalStatCard(
                        title = "Resting HR",
                        value = "${latestVital?.heartRateBpm ?: 70}",
                        unit = "BPM",
                        status = "Optimal Rhythm",
                        statusColor = Color(0xFF16A34A),
                        icon = Icons.Default.Favorite,
                        iconTint = Color(0xFFE11D48),
                        isElderlyMode = isElderlyMode,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // SpO2
                    VitalStatCard(
                        title = "Blood Oxygen",
                        value = "${latestVital?.bloodOxygenSpO2 ?: 98}",
                        unit = "% SpO2",
                        status = "Healthy Saturation",
                        statusColor = Color(0xFF16A34A),
                        icon = Icons.Default.WaterDrop,
                        iconTint = Color(0xFF0284C7),
                        isElderlyMode = isElderlyMode,
                        modifier = Modifier.weight(1f)
                    )

                    // Glucose
                    VitalStatCard(
                        title = "Fasting Glucose",
                        value = String.format(Locale.US, "%.0f", latestVital?.bloodGlucoseMgDl ?: 98.0),
                        unit = "mg/dL",
                        status = "Normal Glycemic",
                        statusColor = Color(0xFF16A34A),
                        icon = Icons.Default.Speed,
                        iconTint = Color(0xFFD97706),
                        isElderlyMode = isElderlyMode,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Data Visualization Trends Chart
            item {
                VitalTrendCharts(
                    vitals = vitals,
                    isElderlyMode = isElderlyMode,
                    modifier = Modifier.testTag("vital_trends_chart")
                )
            }

            // 5. Quick Nav to PCP PDF Report
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Physician Summary Report",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isElderlyMode) 17.sp else 15.sp
                                )
                                Text(
                                    text = "Ready to export into PDF for doctor visits",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.setTab(HealthTab.REPORTS_SYNC) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("nav_to_report_button")
                        ) {
                            Text("View PDF", fontSize = 12.sp)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // Floating Action Button to Log Manual Vitals
        FloatingActionButton(
            onClick = { showManualLogDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("log_vital_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Log Vital Sign")
        }
    }

    if (showManualLogDialog) {
        ManualVitalDialog(
            onDismiss = { showManualLogDialog = false },
            onConfirm = { sys, dia, hr, glu, spo2, note ->
                viewModel.logManualVital(sys, dia, hr, glu, spo2, note)
                showManualLogDialog = false
            },
            isElderlyMode = isElderlyMode
        )
    }
}

@Composable
fun VitalStatCard(
    title: String,
    value: String,
    unit: String,
    status: String,
    statusColor: Color,
    icon: ImageVector,
    iconTint: Color,
    isElderlyMode: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = if (isElderlyMode) 14.sp else 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = if (isElderlyMode) 26.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = statusColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = status,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun ManualVitalDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int?, Int?, Int, Double?, Int?, String) -> Unit,
    isElderlyMode: Boolean
) {
    var systolicText by remember { mutableStateOf("120") }
    var diastolicText by remember { mutableStateOf("80") }
    var hrText by remember { mutableStateOf("72") }
    var glucoseText by remember { mutableStateOf("95") }
    var spo2Text by remember { mutableStateOf("98") }
    var noteText by remember { mutableStateOf("Home blood pressure cuff check") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Record Vital Signs",
                fontWeight = FontWeight.Bold,
                fontSize = if (isElderlyMode) 20.sp else 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = systolicText,
                        onValueChange = { systolicText = it },
                        label = { Text("Systolic BP") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = diastolicText,
                        onValueChange = { diastolicText = it },
                        label = { Text("Diastolic BP") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hrText,
                        onValueChange = { hrText = it },
                        label = { Text("Heart Rate (bpm)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = spo2Text,
                        onValueChange = { spo2Text = it },
                        label = { Text("SpO2 (%)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = glucoseText,
                    onValueChange = { glucoseText = it },
                    label = { Text("Blood Glucose (mg/dL)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Measurement Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        systolicText.toIntOrNull(),
                        diastolicText.toIntOrNull(),
                        hrText.toIntOrNull() ?: 72,
                        glucoseText.toDoubleOrNull(),
                        spo2Text.toIntOrNull(),
                        noteText
                    )
                }
            ) {
                Text("Save Vitals")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
