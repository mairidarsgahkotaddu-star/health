package com.example

import com.example.data.model.DiagnosticTest
import com.example.data.model.DoctorCheckup
import com.example.data.model.MedicationLog
import com.example.data.model.VitalMeasurement
import com.example.util.CloudSyncManager
import com.example.util.PdfReportGenerator
import com.example.util.WearableSyncSimulator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CareSyncHealthUnitTest {

    @Test
    fun testPatientProfileDefaultValues() {
        val profile = PdfReportGenerator.PatientProfile()
        assertEquals("Eleanor Vance", profile.name)
        assertEquals(68, profile.age)
        assertEquals("O-Positive", profile.bloodType)
        assertTrue(profile.primaryCareDoctor.contains("Dr."))
    }

    @Test
    fun testWearableVitalsGenerationRanges() = runBlocking {
        val vitals = WearableSyncSimulator.syncVitalsFromTracker()
        assertTrue("Wearable should generate multiple readings", vitals.isNotEmpty())

        vitals.forEach { vital ->
            assertTrue("Heart rate in realistic physiological range", vital.heartRateBpm in 50..120)
            assertTrue("SpO2 in realistic range", (vital.bloodOxygenSpO2 ?: 98) in 90..100)
            assertTrue("Systolic blood pressure in realistic range", (vital.systolicBp ?: 120) in 90..180)
            assertTrue("Diastolic blood pressure in realistic range", (vital.diastolicBp ?: 80) in 50..110)
        }
    }

    @Test
    fun testMedicationAdherenceStreakIncrement() {
        val med = MedicationLog(
            id = 1,
            medicationName = "Lisinopril",
            dosage = "10 mg",
            frequency = "Once Daily",
            scheduledTimes = "08:00 AM",
            prescribingDoctor = "Dr. Robert Sterling",
            adherenceStreakDays = 24,
            totalDosesTaken = 50
        )

        val updated = med.copy(
            adherenceStreakDays = med.adherenceStreakDays + 1,
            totalDosesTaken = med.totalDosesTaken + 1,
            lastTakenDateMillis = System.currentTimeMillis()
        )

        assertEquals(25, updated.adherenceStreakDays)
        assertEquals(51, updated.totalDosesTaken)
        assertNotNull(updated.lastTakenDateMillis)
    }

    @Test
    fun testDiagnosticTestStatusFlags() {
        val normalTest = DiagnosticTest(
            id = 1,
            testName = "HbA1c",
            orderedByDoctor = "Dr. Amanda Chen",
            testDateMillis = System.currentTimeMillis(),
            testCategory = "Blood Test",
            resultDisplayValue = "5.7 %",
            referenceRange = "< 5.7 %",
            statusFlag = "NORMAL"
        )

        assertEquals("NORMAL", normalTest.statusFlag)
        assertEquals("HbA1c", normalTest.testName)
    }

    @Test
    fun testDoctorCheckupModelIntegrity() {
        val checkup = DoctorCheckup(
            id = 10,
            doctorName = "Dr. Amanda Chen, MD",
            specialty = "Primary Care",
            clinicOrHospital = "Valley Medical",
            checkupDateMillis = System.currentTimeMillis(),
            diagnosisReason = "Annual Exam",
            clinicalNotes = "Routine exam, patient doing well.",
            followUpReminderSet = true
        )

        assertEquals("Dr. Amanda Chen, MD", checkup.doctorName)
        assertTrue(checkup.followUpReminderSet)
    }
}
