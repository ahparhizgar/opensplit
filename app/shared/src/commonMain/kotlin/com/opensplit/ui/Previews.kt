package com.opensplit.ui

import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Light", uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
annotation class ComponentPreview

@Preview(name = "Mobile Dark", uiMode = AndroidUiModes.UI_MODE_NIGHT_YES, widthDp = 390, heightDp = 844)
@Preview(name = "Desktop Light", uiMode = AndroidUiModes.UI_MODE_NIGHT_NO, widthDp = 1280, heightDp = 800)
annotation class ScreenPreview
