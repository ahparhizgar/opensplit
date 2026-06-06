package com.opensplit.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AdaptiveLayoutSizeClass {
    Compact,
    Medium,
    Expanded;

    val isMedium: Boolean
        get() = this == Medium

    val isExpanded: Boolean
        get() = this == Expanded
}

fun adaptiveLayoutSizeClass(maxWidth: Dp): AdaptiveLayoutSizeClass = when {
    maxWidth < 600.dp -> AdaptiveLayoutSizeClass.Compact
    maxWidth < 840.dp -> AdaptiveLayoutSizeClass.Medium
    else -> AdaptiveLayoutSizeClass.Expanded
}

