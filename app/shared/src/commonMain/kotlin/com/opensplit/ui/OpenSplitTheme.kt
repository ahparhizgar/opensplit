package com.opensplit.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.opensplit.ui.theme.AppTypography
import com.opensplit.ui.theme.ExtendedColorScheme
import com.opensplit.ui.theme.darkScheme
import com.opensplit.ui.theme.extendedDark
import com.opensplit.ui.theme.extendedLight
import com.opensplit.ui.theme.lightScheme

internal object ExtendedTheme {
  val colors: ExtendedColorScheme
    @Composable @ReadOnlyComposable get() = LocalExtendedTheme.current

  val LocalExtendedTheme: ProvidableCompositionLocal<ExtendedColorScheme> =
      staticCompositionLocalOf {
        extendedLight
      }
}

val MaterialTheme.colorSchemeExtended: ExtendedColorScheme
  @Composable @ReadOnlyComposable get() = ExtendedTheme.colors

@Composable
fun OpenSplitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) darkScheme else lightScheme
  val extendedColorScheme = if (darkTheme) extendedDark else extendedLight

  CompositionLocalProvider(
      ExtendedTheme.LocalExtendedTheme provides extendedColorScheme,
  ) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
  }
}
