package com.opensplit.features.expense

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key

fun KeyEvent.isCmdEnter(): Boolean = key == Key.Enter && isMetaPressed
