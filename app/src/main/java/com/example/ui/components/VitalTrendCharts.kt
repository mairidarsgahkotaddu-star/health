package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VitalMeasurement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ChartMetricType {
    BLOOD_PRESSURE,
    HEART_RATE_AND_SPO2,
    BLOOD_GLUCOSE
}

@Composable
fun VitalTrendCharts(
    vitals: List<VitalMeasurement>,
    modifier: Modifier = Modifier,
    isElderlyMode: Boolean = false
) {
    var selectedMetric by remember { mutableStateOf(ChartMetricType.BLOOD_PRESSURE) }
    var selectedRangeDays by remember { mutableStateOf(14) }

    val filteredVitals = remember(vitals, selectedRangeDays) {
        val cutoff = System.currentTimeMillis() - (selectedRangeDays * 86_400_000L)
        vitals.filter { it.timestampMillis >= cutoff }
            .sortedBy { it.timestampMillis }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vitals Long-Term Trends",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = if (isElderlyMode) 19.sp else 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Wearable sensor & clinical logs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Range pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(7, 14, 30).forEach { days ->
                        val isSelected = selectedRangeDays == days
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clickable { selectedRangeDays = days }
                        ) {
                            Text(
                                text = "${days}D",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metric Selector Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedMetric == ChartMetricType.BLOOD_PRESSURE,
                    onClick = { selectedMetric = ChartMetricType.BLOOD_PRESSURE },
                    label = { Text("Blood Pressure", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                FilterChip(
                    selected = selectedMetric == ChartMetricType.HEART_RATE_AND_SPO2,
                    onClick = { selectedMetric = ChartMetricType.HEART_RATE_AND_SPO2 },
                    label = { Text("HR & SpO2", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedMetric == ChartMetricType.BLOOD_GLUCOSE,
                    onClick = { selectedMetric = ChartMetricType.BLOOD_GLUCOSE },
                    label = { Text("Glucose", fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend
            MetricLegend(selectedMetric)

            Spacer(modifier = Modifier.height(8.dp))

            // Custom Compose Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (filteredVitals.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "No vitals recorded in this period",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    when (selectedMetric) {
                        ChartMetricType.BLOOD_PRESSURE -> BloodPressureCanvas(filteredVitals)
                        ChartMetricType.HEART_RATE_AND_SPO2 -> HeartRateCanvas(filteredVitals)
                        ChartMetricType.BLOOD_GLUCOSE -> GlucoseCanvas(filteredVitals)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricLegend(metric: ChartMetricType) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (metric) {
            ChartMetricType.BLOOD_PRESSURE -> {
                LegendItem(color = Color(0xFFEF4444), label = "Systolic (mmHg)")
                LegendItem(color = Color(0xFF3B82F6), label = "Diastolic (mmHg)")
                LegendItem(color = Color(0x3310B981), label = "Normal Range (120/80)")
            }
            ChartMetricType.HEART_RATE_AND_SPO2 -> {
                LegendItem(color = Color(0xFFE11D48), label = "Heart Rate (bpm)")
                LegendItem(color = Color(0xFF0D9488), label = "SpO2 (%)")
            }
            ChartMetricType.BLOOD_GLUCOSE -> {
                LegendItem(color = Color(0xFFF59E0B), label = "Fasting Glucose (mg/dL)")
                LegendItem(color = Color(0x3310B981), label = "Target (<100)")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BloodPressureCanvas(vitals: List<VitalMeasurement>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val minVal = 60f
        val maxVal = 160f
        val range = maxVal - minVal

        // Healthy Target Shaded Zone (120 max systolic, 80 target diastolic)
        val targetTopY = height - ((125f - minVal) / range * height)
        val targetBottomY = height - ((75f - minVal) / range * height)
        drawRect(
            color = Color(0x1A10B981),
            topLeft = Offset(0f, targetTopY),
            size = androidx.compose.ui.geometry.Size(width, targetBottomY - targetTopY)
        )

        // Baseline guideline (120 line)
        val line120Y = height - ((120f - minVal) / range * height)
        drawLine(
            color = Color.LightGray.copy(alpha = 0.5f),
            start = Offset(0f, line120Y),
            end = Offset(width, line120Y),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )

        if (vitals.size >= 2) {
            val stepX = width / (vitals.size - 1)

            val systolicPath = Path()
            val diastolicPath = Path()

            vitals.forEachIndexed { i, v ->
                val x = i * stepX
                val sys = (v.systolicBp ?: 120).toFloat().coerceIn(minVal, maxVal)
                val dia = (v.diastolicBp ?: 80).toFloat().coerceIn(minVal, maxVal)

                val sysY = height - ((sys - minVal) / range * height)
                val diaY = height - ((dia - minVal) / range * height)

                if (i == 0) {
                    systolicPath.moveTo(x, sysY)
                    diastolicPath.moveTo(x, diaY)
                } else {
                    systolicPath.lineTo(x, sysY)
                    diastolicPath.lineTo(x, diaY)
                }

                // Draw points
                drawCircle(color = Color(0xFFEF4444), radius = 4f, center = Offset(x, sysY))
                drawCircle(color = Color(0xFF3B82F6), radius = 4f, center = Offset(x, diaY))
            }

            drawPath(path = systolicPath, color = Color(0xFFEF4444), style = Stroke(width = 3f))
            drawPath(path = diastolicPath, color = Color(0xFF3B82F6), style = Stroke(width = 3f))
        }
    }
}

@Composable
private fun HeartRateCanvas(vitals: List<VitalMeasurement>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val minVal = 50f
        val maxVal = 110f
        val range = maxVal - minVal

        if (vitals.size >= 2) {
            val stepX = width / (vitals.size - 1)
            val hrPath = Path()

            vitals.forEachIndexed { i, v ->
                val x = i * stepX
                val hr = v.heartRateBpm.toFloat().coerceIn(minVal, maxVal)
                val hrY = height - ((hr - minVal) / range * height)

                if (i == 0) hrPath.moveTo(x, hrY) else hrPath.lineTo(x, hrY)
                drawCircle(color = Color(0xFFE11D48), radius = 4f, center = Offset(x, hrY))
            }
            drawPath(path = hrPath, color = Color(0xFFE11D48), style = Stroke(width = 3.5f))
        }
    }
}

@Composable
private fun GlucoseCanvas(vitals: List<VitalMeasurement>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val minVal = 70f
        val maxVal = 160f
        val range = maxVal - minVal

        // Target zone (<100 mg/dL)
        val targetY = height - ((100f - minVal) / range * height)
        drawRect(
            color = Color(0x1A10B981),
            topLeft = Offset(0f, targetY),
            size = androidx.compose.ui.geometry.Size(width, height - targetY)
        )

        if (vitals.size >= 2) {
            val stepX = width / (vitals.size - 1)
            val glucosePath = Path()

            vitals.forEachIndexed { i, v ->
                val x = i * stepX
                val glu = (v.bloodGlucoseMgDl ?: 100.0).toFloat().coerceIn(minVal, maxVal)
                val y = height - ((glu - minVal) / range * height)

                if (i == 0) glucosePath.moveTo(x, y) else glucosePath.lineTo(x, y)
                drawCircle(color = Color(0xFFF59E0B), radius = 4f, center = Offset(x, y))
            }
            drawPath(path = glucosePath, color = Color(0xFFF59E0B), style = Stroke(width = 3.5f))
        }
    }
}
