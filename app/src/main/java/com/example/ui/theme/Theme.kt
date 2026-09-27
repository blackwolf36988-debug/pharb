package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

enum class PharbThemeMode(val labelAr: String, val labelEn: String) {
    LIGHT("الوضع الفاتح", "Light Mode"),
    DARK("الوضع الداكن الاحترافي (Admin Dark)", "Professional Dark Mode"),
    AMOLED("وضع AMOLED الأسود", "AMOLED Pure Black"),
    SYSTEM("تلقائي حسب النظام", "System Default")
}

val PharbShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Custom Material3 Dark ColorScheme matching the dark, executive aesthetic
 * of the PHARB Admin & Analytics Panel:
 * - Deep Charcoal background (#0D1117)
 * - Graphite & Elevated Slate surfaces (#161B26 / #1E2638)
 * - Deep Navy containers (#0B192C) with Vibrant Royal Blue (#2563EB) & Soft Cyan (#38BDF8) accents
 * - Emerald (#10B981) tertiary highlights and Live Red (#EF4444) moderation alerts
 */
val PharbAdminDarkColorScheme: ColorScheme = darkColorScheme(
    primary = PharbVibrantBlue,
    onPrimary = Color.White,
    primaryContainer = PharbDeepNavy,
    onPrimaryContainer = PharbSoftBlue,
    inversePrimary = PharbRoyalBlue,
    secondary = PharbSoftBlue,
    onSecondary = PharbDeepNavy,
    secondaryContainer = PharbElevatedDark,
    onSecondaryContainer = PharbTextPrimaryDark,
    tertiary = PharbEducationEmerald,
    onTertiary = Color(0xFF052E16),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = PharbCharcoal,
    onBackground = PharbTextPrimaryDark,
    surface = PharbGraphite,
    onSurface = PharbTextPrimaryDark,
    surfaceVariant = PharbElevatedDark,
    onSurfaceVariant = PharbTextSecondaryDark,
    surfaceTint = PharbVibrantBlue,
    inverseSurface = PharbVeryLightGray,
    inverseOnSurface = PharbTextPrimaryLight,
    error = PharbLiveRed,
    onError = Color.White,
    errorContainer = Color(0xFF450A0A),
    onErrorContainer = Color(0xFFFECACA),
    outline = PharbBorderDark,
    outlineVariant = Color(0xFF1E293B),
    scrim = Color(0xCC000000),
    surfaceBright = Color(0xFF263046),
    surfaceDim = Color(0xFF0A0E14),
    surfaceContainerLowest = Color(0xFF080B10),
    surfaceContainerLow = Color(0xFF111620),
    surfaceContainer = PharbGraphite,
    surfaceContainerHigh = PharbElevatedDark,
    surfaceContainerHighest = Color(0xFF252F45)
)

val PharbAmoledDarkColorScheme: ColorScheme = PharbAdminDarkColorScheme.copy(
    background = PharbAmoledBlack,
    surface = PharbAmoledBlack,
    surfaceVariant = PharbAmoledCard,
    surfaceContainerLowest = PharbAmoledBlack,
    surfaceContainerLow = Color(0xFF05070C),
    surfaceContainer = PharbAmoledCard,
    surfaceContainerHigh = Color(0xFF121826)
)

val PharbProfessionalLightColorScheme: ColorScheme = lightColorScheme(
    primary = PharbVibrantBlue,
    onPrimary = PharbWhite,
    primaryContainer = PharbIceBlue,
    onPrimaryContainer = PharbDeepNavy,
    inversePrimary = PharbSoftBlue,
    secondary = PharbRoyalBlue,
    onSecondary = PharbWhite,
    secondaryContainer = PharbVeryLightGray,
    onSecondaryContainer = PharbDeepNavy,
    tertiary = PharbEducationEmerald,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF064E3B),
    background = PharbVeryLightGray,
    onBackground = PharbTextPrimaryLight,
    surface = PharbWhite,
    onSurface = PharbTextPrimaryLight,
    surfaceVariant = PharbCardLight,
    onSurfaceVariant = PharbTextSecondaryLight,
    surfaceTint = PharbVibrantBlue,
    inverseSurface = PharbGraphite,
    inverseOnSurface = PharbTextPrimaryDark,
    error = PharbLiveRed,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    outline = PharbBorderLight,
    outlineVariant = Color(0xFFCBD5E1),
    scrim = Color(0x99000000)
)

fun createPharbColorScheme(
    themeMode: PharbThemeMode = PharbThemeMode.DARK,
    accentPreset: PharbAccentPreset = PharbAccentPreset.ROYAL_BLUE,
    systemDark: Boolean = true
): ColorScheme {
    val effectiveDark = when (themeMode) {
        PharbThemeMode.LIGHT -> false
        PharbThemeMode.DARK, PharbThemeMode.AMOLED -> true
        PharbThemeMode.SYSTEM -> systemDark
    }

    val baseScheme = when {
        !effectiveDark -> PharbProfessionalLightColorScheme
        themeMode == PharbThemeMode.AMOLED -> PharbAmoledDarkColorScheme
        else -> PharbAdminDarkColorScheme
    }

    return if (effectiveDark) {
        baseScheme.copy(
            primary = accentPreset.primary,
            onPrimaryContainer = accentPreset.secondary,
            secondary = accentPreset.secondary,
            surfaceTint = accentPreset.primary
        )
    } else {
        baseScheme.copy(
            primary = accentPreset.primary,
            surfaceTint = accentPreset.primary
        )
    }
}

/**
 * Main MaterialTheme wrapper for PHARB Social Network.
 * Applies the custom dark, professional Material3 ColorScheme (`PharbAdminDarkColorScheme`) by default.
 */
@Composable
fun PharbTheme(
    themeMode: PharbThemeMode = PharbThemeMode.DARK,
    accentPreset: PharbAccentPreset = PharbAccentPreset.ROYAL_BLUE,
    isArabic: Boolean = true,
    fontScaleMultiplier: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val effectiveDark = when (themeMode) {
        PharbThemeMode.LIGHT -> false
        PharbThemeMode.DARK, PharbThemeMode.AMOLED -> true
        PharbThemeMode.SYSTEM -> systemDark
    }

    val colorScheme = createPharbColorScheme(
        themeMode = themeMode,
        accentPreset = accentPreset,
        systemDark = systemDark
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !effectiveDark
            insetsController.isAppearanceLightNavigationBars = !effectiveDark
        }
    }

    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    val typography = buildPharbTypography(
        isArabic = isArabic,
        fontScaleMultiplier = fontScaleMultiplier
    )

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = PharbShapes,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    PharbTheme(
        themeMode = if (darkTheme) PharbThemeMode.DARK else PharbThemeMode.LIGHT,
        content = content
    )
}
