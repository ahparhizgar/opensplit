package com.opensplit.usermessage

data class UserMessage(
    val content: UiString,
    val tone: Tone = Tone.Info,
    val dismissMode: DismissMode = DismissMode.AutoAndManual(),
) {
  enum class Tone {
    Info,
    Success,
    Warning,
    Error,
  }

  sealed interface DismissMode {
    data object Manual : DismissMode

    data class AutoOnly(
        val duration: Duration = Duration.Short,
    ) : DismissMode

    data class AutoAndManual(
        val duration: Duration = Duration.Short,
    ) : DismissMode
  }

  enum class Duration {
    Short,
    Long,
  }
}
