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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiagnosticTest
import com.example.ui.viewmodel.HealthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LabTestsScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val tests by viewModel.tests.collectAsState()
    val isElderlyMode by viewModel.isElderlyMode.collectAsState()
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf("All") }
    var showAddTestDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Blood Test", "Metabolic", "Cardiac", "Imaging")

    val filteredTests = remember(tests, selectedCategory) {
        if (selectedCategory == "All") tests
        else tests.filter { it.testCategory.equals(selectedCategory, ignoreCase = true) }
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header
            Text(
                text = "Diagnostic Lab Tests",
                style = MaterialTheme.typography.titleLarge,
                fontSize = if (isElderlyMode) 22.sp else 19.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Pathology, blood panels, ECG, and clinical lab values",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 13.sp) },
                        modifier = Modifier.testTag("filter_chip_$cat")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredTests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No diagnostic tests found in $selectedCategory",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredTests, key = { it.id }) { test ->
                        DiagnosticTestCard(
                            test = test,
                            isElderlyMode = isElderlyMode,
                            dateFormat = dateFormat,
                            onDelete = {
                                viewModel.deleteTest(test)
                                Toast.makeText(context, "${test.testName} removed", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // FAB to add diagnostic test
        FloatingActionButton(
            onClick = { showAddTestDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("add_test_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Lab Test")
        }
    }

    if (showAddTestDialog) {
        AddDiagnosticTestDialog(
            isElderlyMode = isElderlyMode,
            onDismiss = { showAddTestDialog = false },
            onConfirm = { newTest ->
                viewModel.addTest(newTest)
                showAddTestDialog = false
                Toast.makeText(context, "${newTest.testName} added to records", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun DiagnosticTestCard(
    test: DiagnosticTest,
    isElderlyMode: Boolean,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit
) {
    val (statusBg, statusFg) = when (test.statusFlag.uppercase(Locale.ROOT)) {
        "NORMAL" -> Pair(Color(0xFF16A34A).copy(alpha = 0.12f), Color(0xFF16A34A))
        "CRITICAL" -> Pair(Color(0xFFDC2626).copy(alpha = 0.15f), Color(0xFFDC2626))
        "LOW" -> Pair(Color(0xFF0284C7).copy(alpha = 0.12f), Color(0xFF0284C7))
        else -> Pair(Color(0xFFD97706).copy(alpha = 0.12f), Color(0xFFD97706)) // ELEVATED
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("test_card_${test.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(if (isElderlyMode) 44.dp else 38.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Biotech,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = test.testName,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = if (isElderlyMode) 19.sp else 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${test.testCategory} • Ordered by ${test.orderedByDoctor}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = if (isElderlyMode) 13.sp else 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = test.statusFlag,
                        color = statusFg,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Result Display Box
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Result Value:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = test.resultDisplayValue,
                            fontSize = if (isElderlyMode) 18.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target / Normal Range:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = test.referenceRange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Clinical Notes
            if (test.clinicalNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Clinical Interpretation: ${test.clinicalNotes}",
                    fontSize = if (isElderlyMode) 14.sp else 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Date: ${dateFormat.format(Date(test.testDateMillis))}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Test",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddDiagnosticTestDialog(
    isElderlyMode: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (DiagnosticTest) -> Unit
) {
    var testName by remember { mutableStateOf("Fasting Blood Glucose") }
    var category by remember { mutableStateOf("Blood Test") }
    var orderedBy by remember { mutableStateOf("Dr. Amanda Chen, MD") }
    var resultValue by remember { mutableStateOf("94 mg/dL") }
    var referenceRange by remember { mutableStateOf("70 - 99 mg/dL") }
    var statusFlag by remember { mutableStateOf("NORMAL") }
    var notes by remember { mutableStateOf("Normal fasting blood sugar") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Record Diagnostic Lab Test",
                fontWeight = FontWeight.Bold,
                fontSize = if (isElderlyMode) 22.sp else 19.sp
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = testName,
                        onValueChange = { testName = it },
                        label = { Text("Test Name (e.g. HbA1c, CBC, ECG)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_test_name")
                    )
                }

                item {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (Blood Test, Cardiac, etc.)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = orderedBy,
                        onValueChange = { orderedBy = it },
                        label = { Text("Doctor / Ordering Lab") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = resultValue,
                        onValueChange = { resultValue = it },
                        label = { Text("Result (e.g. 5.7 %, 120/80)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = referenceRange,
                        onValueChange = { referenceRange = it },
                        label = { Text("Normal Reference Range") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = statusFlag,
                        onValueChange = { statusFlag = it },
                        label = { Text("Status Flag (NORMAL, ELEVATED, CRITICAL)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Physician Notes / Interpretation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (testName.isNotBlank()) {
                        onConfirm(
                            DiagnosticTest(
                                testName = testName,
                                testCategory = category,
                                orderedByDoctor = orderedBy,
                                testDateMillis = System.currentTimeMillis(),
                                resultDisplayValue = resultValue,
                                referenceRange = referenceRange,
                                statusFlag = statusFlag.uppercase(Locale.ROOT),
                                clinicalNotes = notes,
                                syncStatus = "SYNCED"
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("save_test_button")
            ) {
                Text("Save Test")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
