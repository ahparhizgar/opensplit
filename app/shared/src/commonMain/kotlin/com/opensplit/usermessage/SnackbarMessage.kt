package com.opensplit.usermessage

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import com.opensplit.usermessage.UserMessage.DismissMode
import com.opensplit.usermessage.UserMessage.DismissMode.AutoAndManual
import com.opensplit.usermessage.UserMessage.DismissMode.AutoOnly
import com.opensplit.usermessage.UserMessage.DismissMode.Manual

data class SnackbarMessage(
    val content: String,
    val tone: UserMessage.Tone = UserMessage.Tone.Info,
    val dismissMode: DismissMode = AutoAndManual(),
    val actionLabel2: UiString? = null,
    var shouldShake: Boolean = false,
    override var actionLabel: String? = null,
) : SnackbarVisuals {
  override val message: String
    get() = content

  override val withDismissAction: Boolean
    get() =
        when (dismissMode) {
          is Manual -> true
          is AutoAndManual -> true
          is AutoOnly -> false
        }

  override val duration: SnackbarDuration
    get() =
        when (dismissMode) {
          is Manual -> SnackbarDuration.Indefinite
          is AutoAndManual -> dismissMode.duration.toSnackbarDuration()
          is AutoOnly -> dismissMode.duration.toSnackbarDuration()
        }
}

private fun UserMessage.Duration.toSnackbarDuration(): SnackbarDuration =
    when (this) {
      UserMessage.Duration.Short -> SnackbarDuration.Short
      UserMessage.Duration.Long -> SnackbarDuration.Long
    }
