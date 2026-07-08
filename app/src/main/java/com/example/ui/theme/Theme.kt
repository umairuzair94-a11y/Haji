package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ElegantAccentBlue,
    onPrimary = ElegantOnAccentBlue,
    secondary = ElegantBorderOutline,
    onSecondary = ElegantTextPrimary,
    background = ElegantBackground,
    onBackground = ElegantTextPrimary,
    surface = ElegantSurface,
    onSurface = ElegantTextPrimary,
    surfaceVariant = ElegantSurface,
    onSurfaceVariant = ElegantTextMuted,
    outline = ElegantBorderOutline,
    outlineVariant = ElegantBorder,
    error = ElegantSoftRed
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF005FAF),
    onPrimary = Color.White,
    secondary = Color(0xFF545F71),
    onSecondary = Color.White,
    background = Color(0xFFF9F9FC),
    onBackground = Color(0xFF111318),
    surface = Color.White,
    onSurface = Color(0xFF111318),
    surfaceVariant = Color(0xFFE2E2E9),
    onSurfaceVariant = Color(0xFF545F71),
    outline = Color(0xFF8C9199),
    outlineVariant = Color(0xFFC2C7D0),
    error = Color(0xFFBA1A1A)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
