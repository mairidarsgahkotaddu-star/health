package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DiagnosticTest
import com.example.data.model.DoctorCheckup
import com.example.data.model.MedicationLog
import com.example.data.model.VitalMeasurement
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    // Checkups
    @Query("SELECT * FROM doctor_checkups ORDER BY checkupDateMillis DESC")
    fun getAllCheckups(): Flow<List<DoctorCheckup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckup(checkup: DoctorCheckup): Long

    @Update
    suspend fun updateCheckup(checkup: DoctorCheckup)

    @Delete
    suspend fun deleteCheckup(checkup: DoctorCheckup)

    // Tests
    @Query("SELECT * FROM diagnostic_tests ORDER BY testDateMillis DESC")
    fun getAllTests(): Flow<List<DiagnosticTest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: DiagnosticTest): Long

    @Update
    suspend fun updateTest(test: DiagnosticTest)

    @Delete
    suspend fun deleteTest(test: DiagnosticTest)

    // Medications
    @Query("SELECT * FROM medication_logs ORDER BY isActive DESC, medicationName ASC")
    fun getAllMedications(): Flow<List<MedicationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationLog): Long

    @Update
    suspend fun updateMedication(medication: MedicationLog)

    @Delete
    suspend fun deleteMedication(medication: MedicationLog)

    // Vitals
    @Query("SELECT * FROM vital_measurements ORDER BY timestampMillis DESC")
    fun getAllVitals(): Flow<List<VitalMeasurement>>

    @Query("SELECT * FROM vital_measurements ORDER BY timestampMillis DESC LIMIT :limit")
    fun getRecentVitals(limit: Int): Flow<List<VitalMeasurement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVital(vital: VitalMeasurement): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitals(vitals: List<VitalMeasurement>)

    @Delete
    suspend fun deleteVital(vital: VitalMeasurement)
}
