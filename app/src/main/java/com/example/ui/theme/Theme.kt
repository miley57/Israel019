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
    primary = ProfDarkPrimary,
    secondary = ProfDarkSecondary,
    tertiary = ProfDarkTertiary,
    background = ProfDarkBackground,
    surface = ProfDarkSurface,
    surfaceVariant = ProfDarkSurfaceVariant,
    onPrimary = ProfDarkOnPrimary,
    onSecondary = ProfDarkOnSecondary,
    onBackground = ProfDarkOnBackground,
    onSurface = ProfDarkOnSurface,
    onSurfaceVariant = ProfDarkOnSurfaceVariant,
    outline = ProfDarkOutline,
    secondaryContainer = ProfDarkSecondaryContainer,
    onSecondaryContainer = ProfDarkOnSecondaryContainer,
    tertiaryContainer = ProfDarkTertiaryContainer,
    onTertiaryContainer = ProfDarkOnTertiaryContainer
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ProfPrimary,
    secondary = ProfSecondary,
    tertiary = ProfTertiary,
    background = ProfBackground,
    surface = ProfSurface,
    surfaceVariant = ProfSurfaceVariant,
    onPrimary = ProfOnPrimary,
    onSecondary = ProfOnSecondary,
    onBackground = ProfOnBackground,
    onSurface = ProfOnSurface,
    onSurfaceVariant = ProfOnSurfaceVariant,
    outline = ProfOutline,
    secondaryContainer = ProfSecondaryContainer,
    onSecondaryContainer = ProfOnSecondaryContainer,
    tertiaryContainer = ProfTertiaryContainer,
    onTertiaryContainer = ProfOnTertiaryContainer
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color can be used on Android 12+ if desired, but we prefer our custom theme
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
