package com.opensplit.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// From Penpot M3/Typography tokens (Roboto, lineHeight computed from ratio × fontSize)
val OpenSplitTypography = Typography(
    titleLarge = TextStyle(
        fontWeight = FontWeight.Normal,   // 400
        fontSize = 22.sp,
        lineHeight = 28.sp,               // 1.27 × 22 ≈ 28
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,   // 500
        fontSize = 16.sp,
        lineHeight = 24.sp,               // 1.50 × 16 = 24
        letterSpacing = 0.15.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,   // 400
        fontSize = 16.sp,
        lineHeight = 24.sp,               // 1.50 × 16 = 24
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,   // 400
        fontSize = 14.sp,
        lineHeight = 20.sp,               // 1.43 × 14 ≈ 20
        letterSpacing = 0.25.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,   // 500
        fontSize = 14.sp,
        lineHeight = 20.sp,               // 1.43 × 14 ≈ 20
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,   // 500
        fontSize = 12.sp,
        lineHeight = 16.sp,               // 1.33 × 12 ≈ 16
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,   // 500
        fontSize = 11.sp,
        lineHeight = 16.sp,               // 1.45 × 11 ≈ 16
        letterSpacing = 0.5.sp,
    ),
)

