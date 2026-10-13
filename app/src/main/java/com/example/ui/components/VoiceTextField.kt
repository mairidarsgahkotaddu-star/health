package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.VoiceInputHelper

@Composable
fun VoiceTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "Type or tap the microphone to speak...",
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    maxLines: Int = 4,
    isElderlyMode: Boolean = false,
    testTag: String = "voice_text_field"
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        val speechText = VoiceInputHelper.parseSpeechResult(result.resultCode, result.data)
        if (!speechText.isNullOrBlank()) {
            val updated = if (value.isBlank()) speechText else "$value $speechText"
            onValueChange(updated)
            Toast.makeText(context, "Dictation added: \"$speechText\"", Toast.LENGTH_SHORT).show()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    text = label,
                    fontSize = if (isElderlyMode) 16.sp else 14.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = if (isElderlyMode) 15.sp else 13.sp
                )
            },
            trailingIcon = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    if (isListening) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .scale(pulseScale)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                    CircleShape
                                )
                        )
                    }
                    IconButton(
                        onClick = {
                            if (VoiceInputHelper.isSpeechRecognitionAvailable(context)) {
                                isListening = true
                                speechLauncher.launch(
                                    VoiceInputHelper.createSpeechIntent("Dictate $label for your records")
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    "Voice recognition service initializing on device. Please type.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .size(if (isElderlyMode) 52.dp else 44.dp)
                            .testTag("${testTag}_mic_button")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.Mic,
                            contentDescription = "Voice dictation for $label",
                            tint = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(if (isElderlyMode) 28.dp else 24.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            minLines = minLines,
            maxLines = maxLines,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontSize = if (isElderlyMode) 18.sp else 15.sp,
                lineHeight = if (isElderlyMode) 26.sp else 22.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )

        if (isListening) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Listening... Speak your note clearly",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
