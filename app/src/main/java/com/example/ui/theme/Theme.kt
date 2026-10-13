package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ZaraColorScheme = darkColorScheme(
  primary = ZaraPurple,
  onPrimary = Color.White,
  primaryContainer = ZaraPurpleDark,
  onPrimaryContainer = Color.White,
  secondary = ZaraMagenta,
  onSecondary = Color.White,
  tertiary = ZaraCyan,
  onTertiary = Color.Black,
  background = ZaraDarkBg,
  onBackground = ZaraTextPrimary,
  surface = ZaraSurfaceDark,
  onSurface = ZaraTextPrimary,
  surfaceVariant = ZaraSurfaceElevated,
  onSurfaceVariant = ZaraTextSecondary,
  outline = ZaraSurfaceBorder,
  error = ZaraRedLive
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = ZaraColorScheme,
    typography = Typography,
    content = content
  )
}
