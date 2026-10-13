package com.example.util

import com.example.data.model.VitalMeasurement
import kotlinx.coroutines.delay
import kotlin.random.Random

object WearableSyncSimulator {

    data class WearableDeviceInfo(
        val deviceName: String = "Galaxy Watch 6 Classic",
        val connectionStatus: String = "Connected (Bluetooth LE)",
        val batteryPercentage: Int = 86,
        val lastSyncTimestamp: Long = System.currentTimeMillis() - 3600_000L,
        val isAutoSyncActive: Boolean = true
    )

    suspend fun syncVitalsFromTracker(): List<VitalMeasurement> {
        // Realistic sync delay representing BLE data exchange
        delay(1200)

        val now = System.currentTimeMillis()
        val random = Random(now)

        // Generate current reading + 2 recent hourly readings from wearable memory
        val readings = mutableListOf<VitalMeasurement>()

        for (offsetHours in listOf(0L, 2L, 4L)) {
            val timestamp = now - (offsetHours * 3600_000L)
            val systolic = 120 + random.nextInt(-4, 7)
            val diastolic = 78 + random.nextInt(-3, 5)
            val hr = 70 + random.nextInt(-6, 9)
            val spo2 = 98 + random.nextInt(-1, 2).coerceIn(0, 1) // 98 or 99
            val glucose = 98.0 + random.nextInt(-5, 12)

            readings.add(
                VitalMeasurement(
                    timestampMillis = timestamp,
                    systolicBp = systolic,
                    diastolicBp = diastolic,
                    heartRateBpm = hr,
                    bloodOxygenSpO2 = spo2,
                    bloodGlucoseMgDl = glucose,
                    source = "Wearable Tracker (Smartwatch)",
                    note = "Continuous optical PPG sensor capture"
                )
            )
        }

        return readings
    }
}
