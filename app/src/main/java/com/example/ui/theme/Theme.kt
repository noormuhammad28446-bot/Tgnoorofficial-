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
    primary = QuickoGreen,
    secondary = QuickoLightGreen,
    tertiary = QuickoOrange,
    background = Color(0xFF0B120F), // Midnight dark green-gray
    surface = Color(0xFF131D1A),
    onPrimary = QuickoSurface,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1E2E2A),
    onSurfaceVariant = Color(0xFF94A3B8),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = QuickoGreen,
    secondary = QuickoLightGreen,
    tertiary = QuickoOrange,
    background = QuickoBackground,
    surface = QuickoSurface,
    onPrimary = QuickoSurface,
    onSecondary = QuickoDarkGreen,
    onTertiary = QuickoSurface,
    onBackground = QuickoTextPrimary,
    onSurface = QuickoTextPrimary,
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = QuickoTextSecondary,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Set dynamic colors to false by default so that our green and white theme takes center stage
  dynamicColor: Boolean = false,
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
