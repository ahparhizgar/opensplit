package com.opensplit.component

import com.arkivanov.decompose.DecomposeSettings

val ProductionDecomposeSettings =
    DecomposeSettings(
        duplicateConfigurationsEnabled = true,
    )

val TestDecomposeSettings =
    DecomposeSettings(
        duplicateConfigurationsEnabled = true,
        mainThreadCheckEnabled = false,
    )
