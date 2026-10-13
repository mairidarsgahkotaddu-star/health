package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.DiagnosticTest
import com.example.data.model.DoctorCheckup
import com.example.data.model.MedicationLog
import com.example.data.model.VitalMeasurement
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreHealthSync(context: Context) {

    private val tag = "FirestoreSync"
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.getString(R.string.firestore_database_id)
    )
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUser() = auth.currentUser

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google to sync with Firestore.")
    }

    suspend fun syncLocalDataToFirestore(
        checkups: List<DoctorCheckup>,
        tests: List<DiagnosticTest>,
        medications: List<MedicationLog>,
        vitals: List<VitalMeasurement>
    ): Int {
        val uid = requireUserId()
        var totalSynced = 0

        // 1. Sync Checkups
        val checkupsRef = db.collection("users").document(uid).collection("checkups")
        for (c in checkups) {
            val docId = "checkup_${c.id}"
            val payload = hashMapOf(
                "doctorName" to c.doctorName,
                "specialty" to c.specialty,
                "clinicOrHospital" to c.clinicOrHospital,
                "checkupDateMillis" to c.checkupDateMillis,
                "diagnosisReason" to c.diagnosisReason,
                "clinicalNotes" to c.clinicalNotes,
                "followUpDateMillis" to c.followUpDateMillis,
                "followUpReminderSet" to c.followUpReminderSet,
                "vitalsSummary" to c.vitalsSummary,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            checkupsRef.document(docId).set(payload, SetOptions.merge()).await()
            totalSynced++
        }

        // 2. Sync Diagnostic Tests
        val testsRef = db.collection("users").document(uid).collection("diagnostic_tests")
        for (t in tests) {
            val docId = "test_${t.id}"
            val payload = hashMapOf(
                "testName" to t.testName,
                "orderedByDoctor" to t.orderedByDoctor,
                "testDateMillis" to t.testDateMillis,
                "testCategory" to t.testCategory,
                "resultDisplayValue" to t.resultDisplayValue,
                "referenceRange" to t.referenceRange,
                "statusFlag" to t.statusFlag,
                "clinicalNotes" to t.clinicalNotes,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            testsRef.document(docId).set(payload, SetOptions.merge()).await()
            totalSynced++
        }

        // 3. Sync Medications
        val medsRef = db.collection("users").document(uid).collection("medications")
        for (m in medications) {
            val docId = "med_${m.id}"
            val payload = hashMapOf(
                "medicationName" to m.medicationName,
                "dosage" to m.dosage,
                "frequency" to m.frequency,
                "scheduledTimes" to m.scheduledTimes,
                "prescribingDoctor" to m.prescribingDoctor,
                "instructionsVoiceNotes" to m.instructionsVoiceNotes,
                "isReminderActive" to m.isReminderActive,
                "lastTakenDateMillis" to m.lastTakenDateMillis,
                "adherenceStreakDays" to m.adherenceStreakDays,
                "totalDosesTaken" to m.totalDosesTaken,
                "isActive" to m.isActive,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            medsRef.document(docId).set(payload, SetOptions.merge()).await()
            totalSynced++
        }

        // 4. Sync Vitals
        val vitalsRef = db.collection("users").document(uid).collection("vitals")
        for (v in vitals.take(30)) {
            val docId = "vital_${v.id}"
            val payload = hashMapOf(
                "timestampMillis" to v.timestampMillis,
                "systolicBp" to v.systolicBp,
                "diastolicBp" to v.diastolicBp,
                "heartRateBpm" to v.heartRateBpm,
                "bloodOxygenSpO2" to v.bloodOxygenSpO2,
                "bloodGlucoseMgDl" to v.bloodGlucoseMgDl,
                "source" to v.source,
                "note" to v.note,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            vitalsRef.document(docId).set(payload, SetOptions.merge()).await()
            totalSynced++
        }

        Log.i(tag, "Successfully synced $totalSynced records to Firestore database")
        return totalSynced
    }
}
