package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DiagnosticTest
import com.example.data.model.DoctorCheckup
import com.example.data.model.MedicationLog
import com.example.data.model.VitalMeasurement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        DoctorCheckup::class,
        DiagnosticTest::class,
        MedicationLog::class,
        VitalMeasurement::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun healthDao(): HealthDao

    companion object {
        @Volatile
        private var INSTANCE: HealthDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): HealthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HealthDatabase::class.java,
                    "caresync_health.db"
                )
                    .addCallback(HealthDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class HealthDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.healthDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: HealthDao) {
            val now = System.currentTimeMillis()
            val oneDay = 86_400_000L

            // 1. Initial Doctor Checkups
            dao.insertCheckup(
                DoctorCheckup(
                    doctorName = "Dr. Robert Sterling, MD",
                    specialty = "Cardiology & Vascular Medicine",
                    clinicOrHospital = "Metropolitan Heart & Vascular Institute, Suite 310",
                    checkupDateMillis = now - (14 * oneDay),
                    diagnosisReason = "Hypertension management & semi-annual cardiac review",
                    clinicalNotes = "Patient presents in good spirits. Blood pressure stable at 126/82 mmHg under Lisinopril therapy. Heart sounds normal with regular rate and rhythm. Advised 30 minutes daily low-impact walking and keeping dietary sodium below 2,000 mg/day.",
                    followUpDateMillis = now + (76 * oneDay),
                    followUpReminderSet = true,
                    vitalsSummary = "BP: 126/82 mmHg, HR: 68 bpm, SpO2: 98%",
                    syncStatus = "SYNCED"
                )
            )

            dao.insertCheckup(
                DoctorCheckup(
                    doctorName = "Dr. Amanda Chen, MD",
                    specialty = "Primary Care & Internal Medicine",
                    clinicOrHospital = "Valley Community Medical Center",
                    checkupDateMillis = now - (45 * oneDay),
                    diagnosisReason = "Annual Comprehensive Health Assessment & Routine Labs",
                    clinicalNotes = "Routine full checkup. Patient reported occasional mild fatigue in late afternoons. Physical exam unremarkable. Fasting metabolic panel and lipid profile ordered. Refilled Atorvastatin prescription.",
                    followUpDateMillis = now + (14 * oneDay),
                    followUpReminderSet = true,
                    vitalsSummary = "BP: 130/84 mmHg, HR: 72 bpm, Weight: 71.5 kg",
                    syncStatus = "SYNCED"
                )
            )

            dao.insertCheckup(
                DoctorCheckup(
                    doctorName = "Dr. Marcus Vance, MD",
                    specialty = "Endocrinology",
                    clinicOrHospital = "University Health Center, Bldg B",
                    checkupDateMillis = now - (90 * oneDay),
                    diagnosisReason = "Type 2 Pre-Diabetes Monitoring & Dietary Plan Review",
                    clinicalNotes = "HbA1c test completed at 5.7%, which demonstrates fantastic glycemic control compared to last year's 6.1%. Continue current lifestyle modifications and Metformin 500mg daily regimen.",
                    followUpDateMillis = now + (90 * oneDay),
                    followUpReminderSet = false,
                    vitalsSummary = "BP: 122/80 mmHg, HR: 70 bpm, Blood Glucose: 102 mg/dL",
                    syncStatus = "SYNCED"
                )
            )

            // 2. Diagnostic Tests
            dao.insertTest(
                DiagnosticTest(
                    testName = "HbA1c (Hemoglobin A1c)",
                    orderedByDoctor = "Dr. Marcus Vance, MD",
                    testDateMillis = now - (92 * oneDay),
                    testCategory = "Blood Test",
                    resultDisplayValue = "5.7 %",
                    referenceRange = "< 5.7% (Normal), 5.7–6.4% (Prediabetes)",
                    statusFlag = "NORMAL",
                    clinicalNotes = "Glycemic control is well managed. Down from 6.1% six months ago."
                )
            )

            dao.insertTest(
                DiagnosticTest(
                    testName = "Lipid Panel (Fasting)",
                    orderedByDoctor = "Dr. Amanda Chen, MD",
                    testDateMillis = now - (44 * oneDay),
                    testCategory = "Metabolic",
                    resultDisplayValue = "Total: 184 mg/dL, LDL: 98 mg/dL, HDL: 56 mg/dL",
                    referenceRange = "Total < 200, LDL < 100 mg/dL",
                    statusFlag = "NORMAL",
                    clinicalNotes = "Cholesterol targets met with low-dose Statin regimen."
                )
            )

            dao.insertTest(
                DiagnosticTest(
                    testName = "Complete Blood Count (CBC)",
                    orderedByDoctor = "Dr. Amanda Chen, MD",
                    testDateMillis = now - (44 * oneDay),
                    testCategory = "Blood Test",
                    resultDisplayValue = "WBC: 6.2 k/uL, RBC: 4.8 m/uL, Hemoglobin: 14.1 g/dL",
                    referenceRange = "WBC: 4.0 - 11.0, Hemoglobin: 13.5 - 17.5",
                    statusFlag = "NORMAL",
                    clinicalNotes = "All cellular indices within healthy physiological parameters."
                )
            )

            dao.insertTest(
                DiagnosticTest(
                    testName = "Electrocardiogram (12-Lead ECG)",
                    orderedByDoctor = "Dr. Robert Sterling, MD",
                    testDateMillis = now - (14 * oneDay),
                    testCategory = "Cardiac",
                    resultDisplayValue = "Normal Sinus Rhythm (68 bpm)",
                    referenceRange = "Rate: 60 - 100 bpm, No ST-T wave abnormalities",
                    statusFlag = "NORMAL",
                    clinicalNotes = "P waves normal, PR interval 160ms, QRS duration 90ms. No ischemia."
                )
            )

            dao.insertTest(
                DiagnosticTest(
                    testName = "Serum Potassium (Electrolyte)",
                    orderedByDoctor = "Dr. Robert Sterling, MD",
                    testDateMillis = now - (14 * oneDay),
                    testCategory = "Blood Test",
                    resultDisplayValue = "4.4 mmol/L",
                    referenceRange = "3.5 - 5.0 mmol/L",
                    statusFlag = "NORMAL",
                    clinicalNotes = "Monitored due to ACE-inhibitor (Lisinopril). Value perfect."
                )
            )

            // 3. Active Medications
            dao.insertMedication(
                MedicationLog(
                    medicationName = "Lisinopril",
                    dosage = "10 mg",
                    frequency = "Once daily (Morning)",
                    scheduledTimes = "08:00 AM",
                    prescribingDoctor = "Dr. Robert Sterling, MD",
                    instructionsVoiceNotes = "Take with a glass of water every morning after breakfast. Do not skip doses.",
                    isReminderActive = true,
                    lastTakenDateMillis = now - (2 * 3600_000L),
                    adherenceStreakDays = 24,
                    totalDosesTaken = 142,
                    isActive = true
                )
            )

            dao.insertMedication(
                MedicationLog(
                    medicationName = "Metformin XR",
                    dosage = "500 mg",
                    frequency = "Once daily (Evening with Dinner)",
                    scheduledTimes = "07:30 PM",
                    prescribingDoctor = "Dr. Marcus Vance, MD",
                    instructionsVoiceNotes = "Extended release tablet. Swallow whole with evening meal to prevent stomach upset.",
                    isReminderActive = true,
                    lastTakenDateMillis = now - (26 * 3600_000L),
                    adherenceStreakDays = 21,
                    totalDosesTaken = 98,
                    isActive = true
                )
            )

            dao.insertMedication(
                MedicationLog(
                    medicationName = "Atorvastatin",
                    dosage = "20 mg",
                    frequency = "Once daily (Bedtime)",
                    scheduledTimes = "09:30 PM",
                    prescribingDoctor = "Dr. Amanda Chen, MD",
                    instructionsVoiceNotes = "Take right before sleep. Avoid grapefruit juice while on this medication.",
                    isReminderActive = true,
                    lastTakenDateMillis = now - (28 * 3600_000L),
                    adherenceStreakDays = 19,
                    totalDosesTaken = 85,
                    isActive = true
                )
            )

            dao.insertMedication(
                MedicationLog(
                    medicationName = "CoQ10 (Ubiquinol)",
                    dosage = "100 mg",
                    frequency = "Once daily with breakfast",
                    scheduledTimes = "08:00 AM",
                    prescribingDoctor = "Dr. Amanda Chen, MD",
                    instructionsVoiceNotes = "Dietary supplement recommended for cellular energy support.",
                    isReminderActive = false,
                    lastTakenDateMillis = now - (2 * 3600_000L),
                    adherenceStreakDays = 12,
                    totalDosesTaken = 45,
                    isActive = true
                )
            )

            // 4. Wearable Vitals History (for trend chart)
            val initialVitals = mutableListOf<VitalMeasurement>()
            for (i in 14 downTo 0) {
                val time = now - (i * oneDay) + (8 * 3600_000L) // around 8 AM
                val systolic = 122 + (i % 5) - (if (i % 2 == 0) 2 else 0)
                val diastolic = 78 + (i % 4)
                val hr = 68 + (i % 7)
                val glucose = 98.0 + (i % 8) * 1.5
                val spo2 = 97 + (i % 3)
                initialVitals.add(
                    VitalMeasurement(
                        timestampMillis = time,
                        systolicBp = systolic,
                        diastolicBp = diastolic,
                        heartRateBpm = hr,
                        bloodOxygenSpO2 = spo2,
                        bloodGlucoseMgDl = glucose,
                        source = "Wearable Tracker",
                        note = "Morning baseline reading"
                    )
                )
            }
            dao.insertVitals(initialVitals)
        }
    }
}
