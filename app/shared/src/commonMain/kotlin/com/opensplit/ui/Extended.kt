package com.opensplit.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal object ExtendedTheme {
  val colors: ExtendedColorScheme
    @Composable get() = lightExtendedColorScheme()

  @Immutable class Values(val colorScheme: ExtendedColorScheme = lightExtendedColorScheme())

  val LocalExtendedTheme: CompositionLocal<Values> = staticCompositionLocalOf { Values() }
}

val MaterialTheme.colorSchemeExtended: ExtendedColorScheme
  @Composable get() = ExtendedTheme.colors

data class ExtendedColorScheme(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
)

fun lightExtendedColorScheme(): ExtendedColorScheme =
    ExtendedColorScheme(
        success = Color(0xFF4CAF50),
        onSuccess = Color(0xFFFFFFFF),
        successContainer = Color(0xFFC8E6C9),
        onSuccessContainer = Color(0xFF1B5E20),
        warning = Color(0xFFFFC107),
        onWarning = Color(0xFF000000),
        warningContainer = Color(0xFFFFF8E1),
        onWarningContainer = Color(0xFF3E2723),
        info = Color(0xFF2196F3),
        onInfo = Color(0xFFFFFFFF),
        infoContainer = Color(0xFFE3F2FD),
        onInfoContainer = Color(0xFF0D47A1),
    )
