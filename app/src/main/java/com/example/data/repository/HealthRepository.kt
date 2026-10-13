package com.example.data.repository

import com.example.data.local.HealthDao
import com.example.data.model.DiagnosticTest
import com.example.data.model.DoctorCheckup
import com.example.data.model.MedicationLog
import com.example.data.model.VitalMeasurement
import kotlinx.coroutines.flow.Flow

class HealthRepository(private val healthDao: HealthDao) {

    val allCheckups: Flow<List<DoctorCheckup>> = healthDao.getAllCheckups()
    val allTests: Flow<List<DiagnosticTest>> = healthDao.getAllTests()
    val allMedications: Flow<List<MedicationLog>> = healthDao.getAllMedications()
    val allVitals: Flow<List<VitalMeasurement>> = healthDao.getAllVitals()
    fun getRecentVitals(limit: Int = 30): Flow<List<VitalMeasurement>> = healthDao.getRecentVitals(limit)

    // Checkup operations
    suspend fun addCheckup(checkup: DoctorCheckup): Long = healthDao.insertCheckup(checkup)
    suspend fun updateCheckup(checkup: DoctorCheckup) = healthDao.updateCheckup(checkup)
    suspend fun deleteCheckup(checkup: DoctorCheckup) = healthDao.deleteCheckup(checkup)

    // Test operations
    suspend fun addTest(test: DiagnosticTest): Long = healthDao.insertTest(test)
    suspend fun updateTest(test: DiagnosticTest) = healthDao.updateTest(test)
    suspend fun deleteTest(test: DiagnosticTest) = healthDao.deleteTest(test)

    // Medication operations
    suspend fun addMedication(medication: MedicationLog): Long = healthDao.insertMedication(medication)
    suspend fun updateMedication(medication: MedicationLog) = healthDao.updateMedication(medication)
    suspend fun deleteMedication(medication: MedicationLog) = healthDao.deleteMedication(medication)

    suspend fun markMedicationTaken(medication: MedicationLog) {
        val updated = medication.copy(
            lastTakenDateMillis = System.currentTimeMillis(),
            adherenceStreakDays = medication.adherenceStreakDays + 1,
            totalDosesTaken = medication.totalDosesTaken + 1
        )
        healthDao.updateMedication(updated)
    }

    // Vital operations
    suspend fun logVital(vital: VitalMeasurement): Long = healthDao.insertVital(vital)
    suspend fun deleteVital(vital: VitalMeasurement) = healthDao.deleteVital(vital)
    suspend fun insertBatchVitals(vitals: List<VitalMeasurement>) = healthDao.insertVitals(vitals)
}
