package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    private const val MED_CHANNEL_ID = "caresync_medication_reminders"
    private const val APPT_CHANNEL_ID = "caresync_appointment_reminders"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val medChannel = NotificationChannel(
                MED_CHANNEL_ID,
                "Medication Adherence Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Prescription dose alerts to ensure treatment plan compliance"
                enableVibration(true)
            }

            val apptChannel = NotificationChannel(
                APPT_CHANNEL_ID,
                "Doctor Follow-Up Appointments",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for scheduled visits and consultations"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(medChannel)
            notificationManager.createNotificationChannel(apptChannel)
        }
    }

    fun triggerMedicationReminder(
        context: Context,
        medicationName: String,
        dosage: String,
        scheduledTime: String
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            medicationName.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, MED_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💊 Medication Time: $medicationName ($dosage)")
            .setContentText("Scheduled for $scheduledTime. Take with water and mark as completed in CareSync.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
        } catch (_: SecurityException) {
            // Permission POST_NOTIFICATIONS may need runtime grant on API 33+
        }
    }

    fun triggerAppointmentReminder(
        context: Context,
        doctorName: String,
        clinic: String,
        dateFormatted: String
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            doctorName.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, APPT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🗓️ Upcoming Follow-up: $doctorName")
            .setContentText("Scheduled on $dateFormatted at $clinic. Review test results and prep questions.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify((System.currentTimeMillis() % 100000).toInt() + 1, builder.build())
        } catch (_: SecurityException) {
            // Permission POST_NOTIFICATIONS
        }
    }
}
