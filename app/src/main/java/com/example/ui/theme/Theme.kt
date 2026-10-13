package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CareDarkPrimary,
    onPrimary = Color(0xFF003549),
    primaryContainer = Color(0xFF004D68),
    onPrimaryContainer = Color(0xFFC2E8FF),
    secondary = CareDarkSecondary,
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF005047),
    onSecondaryContainer = Color(0xFF70F7E0),
    tertiary = CareDarkTertiary,
    onTertiary = Color(0xFF452B00),
    background = CareBackgroundDark,
    onBackground = CareOnSurfaceDark,
    surface = CareSurfaceDark,
    onSurface = CareOnSurfaceDark,
    surfaceVariant = CareSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = CareOutlineDark,
    error = StatusCriticalDark,
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = CareTealPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = CareTealSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF115E59),
    tertiary = CareAmberTertiary,
    onTertiary = Color.White,
    background = CareBackgroundLight,
    onBackground = CareOnSurfaceLight,
    surface = CareSurfaceLight,
    onSurface = CareOnSurfaceLight,
    surfaceVariant = CareSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF475569),
    outline = CareOutlineLight,
    error = StatusCritical,
    onError = Color.White
)

@Composable
fun CareSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Retain alias for compatibility if needed
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = CareSyncTheme(darkTheme = darkTheme, content = content)
