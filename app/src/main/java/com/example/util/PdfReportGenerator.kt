package com.example.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.DiagnosticTest
import com.example.data.model.DoctorCheckup
import com.example.data.model.MedicationLog
import com.example.data.model.VitalMeasurement
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    data class PatientProfile(
        val name: String = "Eleanor Vance",
        val age: Int = 68,
        val bloodType: String = "O-Positive",
        val primaryCareDoctor: String = "Dr. Amanda Chen, MD",
        val emergencyContact: String = "David Vance (Son) • (555) 234-5678",
        val allergies: String = "Penicillin (Mild rash), Sulfa drugs"
    )

    fun generatePcpReport(
        context: Context,
        profile: PatientProfile,
        checkups: List<DoctorCheckup>,
        tests: List<DiagnosticTest>,
        medications: List<MedicationLog>,
        recentVitals: List<VitalMeasurement>
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (595x842 pt)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paintText = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
        }

        val paintSecondary = Paint().apply {
            isAntiAlias = true
            color = Color.DKGRAY
            textSize = 10f
        }

        val paintTeal = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#0E7490") // Medical Teal
        }

        val paintBorder = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.LTGRAY
        }

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val generatedDate = dateFormat.format(Date())

        var y = 40f

        // --- 1. Header & Branding Banner ---
        canvas.drawRect(30f, y, 565f, y + 55f, paintTeal)

        paintText.apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("CARESYNC • COMPREHENSIVE HEALTH SUMMARY", 45f, y + 25f, paintText)

        paintText.apply {
            color = Color.WHITE
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText("Prepared for Primary Care Physician Consultation • Generated on: $generatedDate", 45f, y + 42f, paintText)

        y += 70f

        // --- 2. Patient Demographics Box ---
        paintText.apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("PATIENT PROFILE", 30f, y, paintText)
        y += 12f

        canvas.drawRect(30f, y, 565f, y + 55f, paintBorder)

        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paintText.textSize = 9.5f
        canvas.drawText("Name: ${profile.name} (Age: ${profile.age})", 40f, y + 18f, paintText)
        canvas.drawText("Blood Type: ${profile.bloodType}", 240f, y + 18f, paintText)
        canvas.drawText("Primary Care Physician: ${profile.primaryCareDoctor}", 40f, y + 34f, paintText)
        canvas.drawText("Known Allergies: ${profile.allergies}", 240f, y + 34f, paintText)
        canvas.drawText("Emergency Contact: ${profile.emergencyContact}", 40f, y + 48f, paintText)

        y += 70f

        // --- 3. Vitals Baseline & Wearable Summary ---
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paintText.textSize = 11f
        canvas.drawText("RECENT VITALS & WEARABLE TRACKER AVERAGES", 30f, y, paintText)
        y += 12f

        val latestVital = recentVitals.firstOrNull()
        val avgSystolic = recentVitals.mapNotNull { it.systolicBp }.average().takeIf { !it.isNaN() }?.toInt() ?: 124
        val avgDiastolic = recentVitals.mapNotNull { it.diastolicBp }.average().takeIf { !it.isNaN() }?.toInt() ?: 80
        val avgHr = recentVitals.map { it.heartRateBpm }.average().takeIf { !it.isNaN() }?.toInt() ?: 70
        val avgSpo2 = recentVitals.mapNotNull { it.bloodOxygenSpO2 }.average().takeIf { !it.isNaN() }?.toInt() ?: 98

        canvas.drawRect(30f, y, 565f, y + 35f, paintBorder)
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paintText.textSize = 9.5f
        canvas.drawText("Avg Blood Pressure: $avgSystolic/$avgDiastolic mmHg", 40f, y + 16f, paintText)
        canvas.drawText("Resting Heart Rate: $avgHr bpm", 230f, y + 16f, paintText)
        canvas.drawText("Blood Oxygen (SpO2): $avgSpo2%", 410f, y + 16f, paintText)
        canvas.drawText("Last Wearable Sync: ${latestVital?.let { dateFormat.format(Date(it.timestampMillis)) } ?: generatedDate}", 40f, y + 28f, paintSecondary)

        y += 50f

        // --- 4. Active Medications Table ---
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paintText.textSize = 11f
        canvas.drawText("ACTIVE MEDICATIONS & ADHERENCE REGIMEN", 30f, y, paintText)
        y += 14f

        // Table Header
        val headerPaint = Paint().apply {
            color = Color.parseColor("#E0F2FE")
        }
        canvas.drawRect(30f, y, 565f, y + 16f, headerPaint)
        paintText.textSize = 8.5f
        paintText.color = Color.parseColor("#0369A1")
        canvas.drawText("MEDICATION", 35f, y + 11f, paintText)
        canvas.drawText("DOSAGE", 160f, y + 11f, paintText)
        canvas.drawText("FREQUENCY & TIME", 240f, y + 11f, paintText)
        canvas.drawText("PRESCRIBED BY", 410f, y + 11f, paintText)

        paintText.color = Color.BLACK
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        y += 18f

        for (med in medications.take(5)) {
            canvas.drawText(med.medicationName, 35f, y + 10f, paintText)
            canvas.drawText(med.dosage, 160f, y + 10f, paintText)
            canvas.drawText("${med.frequency} (${med.scheduledTimes})", 240f, y + 10f, paintText)
            canvas.drawText(med.prescribingDoctor, 410f, y + 10f, paintText)
            y += 16f
        }

        y += 12f

        // --- 5. Diagnostic Laboratory & Test Results ---
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paintText.textSize = 11f
        canvas.drawText("DIAGNOSTIC & LAB TEST RESULTS", 30f, y, paintText)
        y += 14f

        canvas.drawRect(30f, y, 565f, y + 16f, headerPaint)
        paintText.textSize = 8.5f
        paintText.color = Color.parseColor("#0369A1")
        canvas.drawText("TEST NAME", 35f, y + 11f, paintText)
        canvas.drawText("DATE", 190f, y + 11f, paintText)
        canvas.drawText("RESULT", 270f, y + 11f, paintText)
        canvas.drawText("REFERENCE RANGE", 400f, y + 11f, paintText)
        canvas.drawText("STATUS", 500f, y + 11f, paintText)

        paintText.color = Color.BLACK
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        y += 18f

        for (test in tests.take(5)) {
            canvas.drawText(test.testName, 35f, y + 10f, paintText)
            canvas.drawText(dateFormat.format(Date(test.testDateMillis)), 190f, y + 10f, paintText)
            canvas.drawText(test.resultDisplayValue.take(24), 270f, y + 10f, paintText)
            canvas.drawText(test.referenceRange.take(20), 400f, y + 10f, paintText)

            val statusPaint = Paint().apply {
                isAntiAlias = true
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = when (test.statusFlag) {
                    "NORMAL" -> Color.parseColor("#15803D")
                    "CRITICAL" -> Color.parseColor("#B91C1C")
                    else -> Color.parseColor("#B45309")
                }
            }
            canvas.drawText(test.statusFlag, 500f, y + 10f, statusPaint)
            y += 16f
        }

        y += 12f

        // --- 6. Recent Medical Checkups & Physician Notes ---
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paintText.textSize = 11f
        canvas.drawText("RECENT DOCTOR CHECKUPS & CLINICAL NOTES", 30f, y, paintText)
        y += 14f

        for (checkup in checkups.take(2)) {
            canvas.drawRect(30f, y, 565f, y + 54f, paintBorder)
            paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paintText.textSize = 9f
            canvas.drawText("${checkup.doctorName} • ${checkup.specialty} (${dateFormat.format(Date(checkup.checkupDateMillis))})", 38f, y + 13f, paintText)

            paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paintText.textSize = 8.5f
            canvas.drawText("Reason: ${checkup.diagnosisReason}", 38f, y + 25f, paintText)

            val notesSnippet = if (checkup.clinicalNotes.length > 95) checkup.clinicalNotes.take(95) + "..." else checkup.clinicalNotes
            canvas.drawText("Notes: $notesSnippet", 38f, y + 37f, paintSecondary)

            val followUpStr = checkup.followUpDateMillis?.let { "Next Follow-up: ${dateFormat.format(Date(it))}" } ?: "Follow-up: None scheduled"
            canvas.drawText(followUpStr, 38f, y + 48f, paintText)

            y += 60f
        }

        // --- 7. Footer & Physician Signature Block ---
        y = 790f
        canvas.drawLine(30f, y, 565f, y, paintBorder)
        paintSecondary.textSize = 8f
        canvas.drawText("Physician Review Signature: _________________________________    Date: _____________", 35f, y + 18f, paintSecondary)
        canvas.drawText("CONFIDENTIAL PATIENT RECORD • GENERATED BY CARESYNC HEALTHCARE PLATFORM", 35f, y + 32f, paintSecondary)

        pdfDocument.finishPage(page)

        // Write to cache file
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val outputFile = File(reportsDir, "CareSync_Medical_Report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    fun getShareableUri(context: Context, file: File) =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
