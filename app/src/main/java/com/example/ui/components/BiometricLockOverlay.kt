package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BiometricLockOverlay(
    isLocked: Boolean,
    onUnlockWithPin: (String) -> Boolean,
    onUnlockWithBiometric: () -> Unit,
    errorMessage: String? = null,
    isElderlyMode: Boolean = false
) {
    AnimatedVisibility(
        visible = isLocked,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        var enteredPin by remember { mutableStateOf("") }
        var localError by remember { mutableStateOf<String?>(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Shield / Lock Icon
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Medical Data Privacy Lock",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "CareSync Privacy Lock",
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = if (isElderlyMode) 24.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Medical records & prescriptions are locked for patient confidentiality.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = if (isElderlyMode) 16.sp else 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = enteredPin.length > i
                        Box(
                            modifier = Modifier
                                .size(if (isElderlyMode) 20.dp else 16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                        )
                    }
                }

                val activeError = errorMessage ?: localError
                if (activeError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = activeError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // PIN Keypad Grid (3x4)
                val pinKeys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("BIO", "0", "DEL")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in pinKeys) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (key in row) {
                                PinKeyButton(
                                    key = key,
                                    isElderlyMode = isElderlyMode,
                                    onClick = {
                                        when (key) {
                                            "DEL" -> {
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                    localError = null
                                                }
                                            }
                                            "BIO" -> {
                                                onUnlockWithBiometric()
                                            }
                                            else -> {
                                                if (enteredPin.length < 4) {
                                                    val next = enteredPin + key
                                                    enteredPin = next
                                                    localError = null
                                                    if (next.length == 4) {
                                                        val success = onUnlockWithPin(next)
                                                        if (!success) {
                                                            enteredPin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Biometric Bypass Button
                Button(
                    onClick = onUnlockWithBiometric,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(if (isElderlyMode) 54.dp else 46.dp)
                        .testTag("biometric_quick_unlock_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Unlock with Fingerprint",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Touch Fingerprint to Unlock",
                        fontSize = if (isElderlyMode) 16.sp else 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PinKeyButton(
    key: String,
    isElderlyMode: Boolean,
    onClick: () -> Unit
) {
    val size = if (isElderlyMode) 68.dp else 58.dp

    Surface(
        shape = CircleShape,
        color = when (key) {
            "BIO" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            "DEL" -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        },
        modifier = Modifier
            .size(size)
            .clickable { onClick() }
            .testTag("pin_key_$key")
    ) {
        Box(contentAlignment = Alignment.Center) {
            when (key) {
                "BIO" -> Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Fingerprint sensor",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                "DEL" -> Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Delete digit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                else -> Text(
                    text = key,
                    fontSize = if (isElderlyMode) 26.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
