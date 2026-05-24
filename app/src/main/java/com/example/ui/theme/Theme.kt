package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
  primary = PrimaryColor,
  onPrimary = OnPrimaryColor,
  primaryContainer = PrimaryContainer,
  onPrimaryContainer = OnPrimaryContainer,
  secondary = SecondaryColor,
  onSecondary = OnSecondaryColor,
  secondaryContainer = SecondaryContainer,
  onSecondaryContainer = OnSecondaryContainer,
  tertiary = TertiaryColor,
  onTertiary = OnTertiaryColor,
  tertiaryContainer = TertiaryContainer,
  onTertiaryContainer = OnTertiaryContainer,
  background = SurfaceBg,
  onBackground = OnSurfaceColor,
  surface = SurfaceContainer,
  onSurface = OnSurfaceColor,
  surfaceVariant = SurfaceContainerHighest,
  onSurfaceVariant = OnSurfaceVariant,
  error = ErrorColor,
  onError = OnErrorColor,
  errorContainer = ErrorContainer
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark-first cinematic palette
  dynamicColor: Boolean = false, // Disable to preserve custom brand design
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
