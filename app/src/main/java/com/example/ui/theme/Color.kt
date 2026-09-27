package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// PHARB Official Brand Palette — Less Clutter, More Experience
val PharbDeepNavy = Color(0xFF0B192C)
val PharbRoyalBlue = Color(0xFF1E56A0)
val PharbVibrantBlue = Color(0xFF2563EB)
val PharbSoftBlue = Color(0xFF38BDF8)
val PharbIceBlue = Color(0xFFE0F2FE)
val PharbMetallicSilver = Color(0xFFCBD5E1)
val PharbSilverHighlight = Color(0xFFE2E8F0)

// Neutrals - Light Mode
val PharbWhite = Color(0xFFFFFFFF)
val PharbVeryLightGray = Color(0xFFF4F7FB)
val PharbSurfaceLight = Color(0xFFFFFFFF)
val PharbCardLight = Color(0xFFF8FAFC)
val PharbBorderLight = Color(0xFFE2E8F0)
val PharbTextPrimaryLight = Color(0xFF0F172A)
val PharbTextSecondaryLight = Color(0xFF475569)

// Neutrals - Dark Mode (Graphite, Charcoal, Deep Navy)
val PharbCharcoal = Color(0xFF0D1117)
val PharbGraphite = Color(0xFF161B26)
val PharbElevatedDark = Color(0xFF1E2638)
val PharbBorderDark = Color(0xFF2A344B)
val PharbTextPrimaryDark = Color(0xFFF1F5F9)
val PharbTextSecondaryDark = Color(0xFF94A3B8)

// AMOLED Mode
val PharbAmoledBlack = Color(0xFF000000)
val PharbAmoledCard = Color(0xFF0A0E17)

// Customizable Accent Presets
enum class PharbAccentPreset(
    val id: String,
    val labelAr: String,
    val labelEn: String,
    val primary: Color,
    val secondary: Color
) {
    ROYAL_BLUE("royal_blue", "الأزرق الملكي (الافتراضي)", "Royal Blue", Color(0xFF2563EB), Color(0xFF38BDF8)),
    EMERALD_OASIS("emerald", "أخضر الواحة", "Emerald Oasis", Color(0xFF059669), Color(0xFF34D399)),
    AMBER_HORIZON("amber", "العنبر الدافئ", "Amber Horizon", Color(0xFFD97706), Color(0xFFFBBF24)),
    VIOLET_PRISM("violet", "البنفسجي الهندسي", "Violet Prism", Color(0xFF7C3AED), Color(0xFFA78BFA))
}

// Semantic & Reaction Colors
val ReactionLikeColor = Color(0xFF2563EB)
val ReactionLoveColor = Color(0xFFE11D48)
val ReactionLaughColor = Color(0xFFF59E0B)
val ReactionWowColor = Color(0xFF8B5CF6)
val ReactionSadColor = Color(0xFF0284C7)
val ReactionAngryColor = Color(0xFFDC2626)

val PharbVerifiedBlue = Color(0xFF0EA5E9)
val PharbEducationEmerald = Color(0xFF10B981)
val PharbBusinessGold = Color(0xFFF59E0B)
val PharbCreatorPurple = Color(0xFF8B5CF6)
val PharbLiveRed = Color(0xFFEF4444)
val PharbOnlineGreen = Color(0xFF22C55E)

val PharbBrandGradient = Brush.linearGradient(
    colors = listOf(PharbDeepNavy, PharbRoyalBlue, PharbSoftBlue)
)

val PharbAccentGradient = Brush.horizontalGradient(
    colors = listOf(PharbVibrantBlue, PharbSoftBlue)
)
