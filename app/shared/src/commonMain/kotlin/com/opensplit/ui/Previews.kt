package com.opensplit.ui

import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Light", uiMode = 16)
@Preview(name = "Dark", uiMode = 32)
annotation class ComponentPreview

@Preview(name = "Mobile Light", uiMode = 16, widthDp = 390, heightDp = 844)
@Preview(name = "Mobile Dark", uiMode = 32, widthDp = 390, heightDp = 844)
@Preview(name = "Desktop Light", uiMode = 16, widthDp = 1280, heightDp = 800)
annotation class ScreenPreview
