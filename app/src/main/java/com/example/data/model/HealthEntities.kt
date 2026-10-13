package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctor_checkups")
data class DoctorCheckup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val doctorName: String,
    val specialty: String,
    val clinicOrHospital: String,
    val checkupDateMillis: Long,
    val diagnosisReason: String,
    val clinicalNotes: String, // Can be voice-dictated
    val followUpDateMillis: Long? = null,
    val followUpReminderSet: Boolean = false,
    val vitalsSummary: String = "",
    val syncStatus: String = "SYNCED", // SYNCED, PENDING, LOCAL_ONLY
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "diagnostic_tests")
data class DiagnosticTest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val testName: String,
    val orderedByDoctor: String,
    val testDateMillis: Long,
    val testCategory: String, // Blood Test, Cardiac, Metabolic, Urinalysis, Imaging
    val resultDisplayValue: String, // e.g. "5.7%", "118 mg/dL", "Normal Sinus"
    val referenceRange: String, // e.g. "4.0 - 5.6 %", "70 - 99 mg/dL"
    val statusFlag: String, // NORMAL, ELEVATED, LOW, CRITICAL
    val clinicalNotes: String = "",
    val syncStatus: String = "SYNCED"
)

@Entity(tableName = "medication_logs")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationName: String,
    val dosage: String,
    val frequency: String,
    val scheduledTimes: String, // e.g. "08:00 AM, 08:00 PM"
    val prescribingDoctor: String,
    val instructionsVoiceNotes: String = "", // Voice dictated notes
    val isReminderActive: Boolean = true,
    val lastTakenDateMillis: Long? = null,
    val adherenceStreakDays: Int = 0,
    val totalDosesTaken: Int = 0,
    val isActive: Boolean = true,
    val syncStatus: String = "SYNCED"
)

@Entity(tableName = "vital_measurements")
data class VitalMeasurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestampMillis: Long,
    val systolicBp: Int? = null,
    val diastolicBp: Int? = null,
    val heartRateBpm: Int = 72,
    val bloodOxygenSpO2: Int? = 98,
    val bloodGlucoseMgDl: Double? = null,
    val source: String = "Wearable Tracker", // Wearable Tracker, Manual, Clinical
    val note: String = ""
)
