package com.example.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import java.util.Locale

object VoiceInputHelper {

    fun createSpeechIntent(promptText: String = "Speak clearly to dictate notes..."): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, promptText)
        }
    }

    fun parseSpeechResult(resultCode: Int, data: Intent?): String? {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                return results[0]
            }
        }
        return null
    }

    fun isSpeechRecognitionAvailable(context: Context): Boolean {
        val pm = context.packageManager
        val activities = pm.queryIntentActivities(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),
            0
        )
        return activities.isNotEmpty()
    }
}
