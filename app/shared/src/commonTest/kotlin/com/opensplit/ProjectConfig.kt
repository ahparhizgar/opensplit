package com.opensplit

import com.arkivanov.decompose.DecomposeSettings
import io.kotest.core.config.AbstractProjectConfig

// The name matters for Kotest
object ProjectConfig : AbstractProjectConfig() {
  init {
    DecomposeSettings.settings =
        DecomposeSettings(
            mainThreadCheckEnabled = false,
            duplicateConfigurationsEnabled = true,
        )
  }
}
