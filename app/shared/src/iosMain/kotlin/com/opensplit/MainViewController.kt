package com.opensplit

import androidx.compose.ui.window.ComposeUIViewController
import com.opensplit.root.RootComponent

fun MainViewController(root: RootComponent) = ComposeUIViewController { App(root) }
