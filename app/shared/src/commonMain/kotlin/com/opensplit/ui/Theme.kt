package com.opensplit.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme =
    lightColorScheme(
        primary = LightPrimary,
        onPrimary = LightOnPrimary,
        primaryContainer = LightPrimaryContainer,
        onPrimaryContainer = LightOnPrimaryContainer,
        secondary = LightSecondary,
        onSecondary = LightOnSecondary,
        secondaryContainer = LightSecondaryContainer,
        onSecondaryContainer = LightOnSecondaryContainer,
        background = LightBackground,
        onBackground = LightOnBackground,
        surface = LightSurface,
        onSurface = LightOnSurface,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = LightOnSurfaceVariant,
        outline = LightOutline,
        outlineVariant = LightOutlineVariant,
        error = LightError,
        onError = LightOnError,
        errorContainer = LightErrorContainer,
        onErrorContainer = LightOnErrorContainer,
        inverseSurface = LightInverseSurface,
        inverseOnSurface = LightInverseOnSurface,
        inversePrimary = LightInversePrimary,
        surfaceTint = LightPrimary,
        surfaceBright = Color(0xFFFAFDFC),
        surfaceDim = Color(0xFFD8E2E0),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF2F6F5),
        surfaceContainer = Color(0xFFECF2F0),
        surfaceContainerHigh = Color(0xFFE6ECEB),
        surfaceContainerHighest = Color(0xFFE0E7E5),
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = DarkPrimary,
        onPrimary = DarkOnPrimary,
        primaryContainer = DarkPrimaryContainer,
        onPrimaryContainer = DarkOnPrimaryContainer,
        secondary = DarkSecondary,
        onSecondary = DarkOnSecondary,
        secondaryContainer = DarkSecondaryContainer,
        onSecondaryContainer = DarkOnSecondaryContainer,
        background = DarkBackground,
        onBackground = DarkOnBackground,
        surface = DarkSurface,
        onSurface = DarkOnSurface,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkOnSurfaceVariant,
        outline = DarkOutline,
        outlineVariant = DarkOutlineVariant,
        error = DarkError,
        onError = DarkOnError,
        errorContainer = DarkErrorContainer,
        onErrorContainer = DarkOnErrorContainer,
        inverseSurface = DarkInverseSurface,
        inverseOnSurface = DarkInverseOnSurface,
        inversePrimary = DarkInversePrimary,
        surfaceTint = DarkPrimary,
        surfaceBright = Color(0xFF3B3F3E),
        surfaceDim = Color(0xFF111414),
        surfaceContainerLowest = Color(0xFF141717),
        surfaceContainerLow = Color(0xFF1D2020),
        surfaceContainer = Color(0xFF212424),
        surfaceContainerHigh = Color(0xFF2B2E2E),
        surfaceContainerHighest = Color(0xFF353938),
    )

@Composable
fun OpenSplitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
      colorScheme = colorScheme,
      typography = OpenSplitTypography,
      content = content,
  )
}
