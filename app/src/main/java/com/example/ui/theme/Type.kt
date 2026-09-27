package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val CairoFontFamily = FontFamily(
    Font(R.font.cairo, FontWeight.Normal),
    Font(R.font.cairo, FontWeight.Medium),
    Font(R.font.cairo, FontWeight.SemiBold),
    Font(R.font.cairo, FontWeight.Bold)
)

val PlusJakartaFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

fun buildPharbTypography(isArabic: Boolean = true, fontScaleMultiplier: Float = 1.0f): Typography {
    val primaryFont = if (isArabic) CairoFontFamily else PlusJakartaFontFamily
    val displayFont = if (isArabic) CairoFontFamily else PlusJakartaFontFamily
    val scale = fontScaleMultiplier.coerceIn(0.85f, 1.35f)

    return Typography(
        displayLarge = TextStyle(
            fontFamily = displayFont,
            fontWeight = FontWeight.Bold,
            fontSize = (32 * scale).sp,
            lineHeight = (42 * scale).sp
        ),
        displayMedium = TextStyle(
            fontFamily = displayFont,
            fontWeight = FontWeight.Bold,
            fontSize = (26 * scale).sp,
            lineHeight = (34 * scale).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = displayFont,
            fontWeight = FontWeight.Bold,
            fontSize = (22 * scale).sp,
            lineHeight = (30 * scale).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = displayFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = (19 * scale).sp,
            lineHeight = (26 * scale).sp
        ),
        titleLarge = TextStyle(
            fontFamily = primaryFont,
            fontWeight = FontWeight.Bold,
            fontSize = (18 * scale).sp,
            lineHeight = (25 * scale).sp
        ),
        titleMedium = TextStyle(
            fontFamily = primaryFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16 * scale).sp,
            lineHeight = (23 * scale).sp
        ),
        titleSmall = TextStyle(
            fontFamily = primaryFont,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = primaryFont,
            fontWeight = FontWeight.Normal,
            fontSize = (15.5f * scale).sp,
            lineHeight = (24 * scale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = primaryFont,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * scale).sp,
            lineHeight = (21 * scale).sp
        ),
        bodySmall = TextStyle(
            fontFamily = primaryFont,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * scale).sp,
            lineHeight = (18 * scale).sp
        ),
        labelLarge = TextStyle(
            fontFamily = primaryFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = (13.5f * scale).sp,
            lineHeight = (19 * scale).sp
        ),
        labelMedium = TextStyle(
            fontFamily = primaryFont,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * scale).sp,
            lineHeight = (16 * scale).sp
        ),
        labelSmall = TextStyle(
            fontFamily = primaryFont,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * scale).sp,
            lineHeight = (15 * scale).sp
        )
    )
}

val Typography = buildPharbTypography()
