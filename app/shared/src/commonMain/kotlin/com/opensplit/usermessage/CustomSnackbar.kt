package com.opensplit.usermessage

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import com.opensplit.ui.colorSchemeExtended
import com.opensplit.usermessage.UserMessage.Tone.Error
import com.opensplit.usermessage.UserMessage.Tone.Info
import com.opensplit.usermessage.UserMessage.Tone.Success
import com.opensplit.usermessage.UserMessage.Tone.Warning
import kotlin.math.roundToInt

@Composable
fun CustomSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
) {
  val visuals = snackbarData.visuals as SnackbarMessage
  visuals.actionLabel = visuals.actionLabel2?.content()
  val offsetX = remember { Animatable(0f) }

  if (visuals.shouldShake) {
    LaunchedEffect(Unit) {
      repeat(3) {
        offsetX.animateTo(10f, tween(durationMillis = 80))
        offsetX.animateTo(-10f, tween(durationMillis = 80))
      }
      offsetX.animateTo(0f)
    }
  }

  val colors = getColors(visuals.tone)
  val actionLabel = visuals.actionLabel2?.content()
  val data2 =
      object : SnackbarData {
        override val visuals: SnackbarVisuals
          get() = visuals.copy(actionLabel = actionLabel)

        override fun dismiss() {
          snackbarData.dismiss()
        }

        override fun performAction() {
          snackbarData.performAction()
        }
      }
  Snackbar(
      modifier = modifier.offset { IntOffset(offsetX.value.roundToInt(), 0) },
      snackbarData = snackbarData,
      containerColor = colors.containerColor,
      contentColor = colors.contentColor,
      actionColor = colors.actionColor,
      actionContentColor = colors.actionContentColor,
      dismissActionContentColor = colors.dismissActionContentColor,
  )
}

private data class CustomSnackbarColors(
    val containerColor: Color,
    val contentColor: Color,
    val actionColor: Color,
    val actionContentColor: Color,
    val dismissActionContentColor: Color,
)

@Composable
private fun getColors(tone: UserMessage.Tone): CustomSnackbarColors =
    when (tone) {
      Success -> {
        CustomSnackbarColors(
            containerColor = MaterialTheme.colorSchemeExtended.successContainer,
            contentColor = MaterialTheme.colorSchemeExtended.onSuccessContainer,
            actionColor = MaterialTheme.colorSchemeExtended.onSuccessContainer,
            actionContentColor = MaterialTheme.colorSchemeExtended.onSuccessContainer,
            dismissActionContentColor = MaterialTheme.colorSchemeExtended.onSuccessContainer,
        )
      }

      Info -> {
        CustomSnackbarColors(
            containerColor = MaterialTheme.colorSchemeExtended.infoContainer,
            contentColor = MaterialTheme.colorSchemeExtended.onInfoContainer,
            actionColor = MaterialTheme.colorSchemeExtended.onInfoContainer,
            actionContentColor = MaterialTheme.colorSchemeExtended.onInfoContainer,
            dismissActionContentColor = MaterialTheme.colorSchemeExtended.onInfoContainer,
        )
      }

      Warning -> {
        CustomSnackbarColors(
            containerColor = MaterialTheme.colorSchemeExtended.warningContainer,
            contentColor = MaterialTheme.colorSchemeExtended.onWarningContainer,
            actionColor = MaterialTheme.colorSchemeExtended.onWarningContainer,
            actionContentColor = MaterialTheme.colorSchemeExtended.onWarningContainer,
            dismissActionContentColor = MaterialTheme.colorSchemeExtended.onWarningContainer,
        )
      }

      Error -> {
        CustomSnackbarColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            actionColor = MaterialTheme.colorScheme.onErrorContainer,
            actionContentColor = MaterialTheme.colorScheme.onErrorContainer,
            dismissActionContentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
      }
    }
