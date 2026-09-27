package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * PHARB Design System Tokens (Section 44, 45, 47)
 * Less Clutter — More Experience
 */
object PharbTokens {
    val SpacingXS = 4.dp
    val SpacingSM = 8.dp
    val SpacingMD = 12.dp
    val SpacingLG = 16.dp
    val SpacingXL = 24.dp
    val SpacingXXL = 32.dp

    val RadiusSM = 10.dp
    val RadiusMD = 16.dp
    val RadiusLG = 22.dp
    val RadiusPill = 999.dp
}

/**
 * 8 Official Logo Variants requested in Section 4:
 * 1. MAIN_LOGO
 * 2. ICON_MARK
 * 3. APP_ICON
 * 4. MONOCHROME_LOGO
 * 5. WHITE_LOGO
 * 6. DARK_LOGO
 * 7. FAVICON
 * 8. SPLASH_LOGO
 */
enum class PharbLogoVariant(val titleAr: String, val titleEn: String) {
    MAIN_LOGO("الشعار الرئيسي", "Main Logo"),
    ICON_MARK("الرمز الهندسي", "Icon Mark"),
    APP_ICON("أيقونة التطبيق", "App Icon"),
    MONOCHROME_LOGO("أحادي اللون", "Monochrome Logo"),
    WHITE_LOGO("الشعار الأبيض", "White Logo"),
    DARK_LOGO("الشعار الداكن", "Dark Logo"),
    FAVICON("أيقونة الويب Favicon", "Favicon"),
    SPLASH_LOGO("شعار البداية المتحرك", "Splash Animated Logo")
}

@Composable
fun PharbGeometricLogo(
    variant: PharbLogoVariant = PharbLogoVariant.MAIN_LOGO,
    markSize: Dp = 38.dp,
    showTagline: Boolean = false,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pharb_logo_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val showText = variant in listOf(
        PharbLogoVariant.MAIN_LOGO,
        PharbLogoVariant.MONOCHROME_LOGO,
        PharbLogoVariant.WHITE_LOGO,
        PharbLogoVariant.DARK_LOGO,
        PharbLogoVariant.SPLASH_LOGO
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier.size(markSize)
        ) {
            val w = size.width
            val h = size.height

            // Background container for APP_ICON, FAVICON, or SPLASH_LOGO
            when (variant) {
                PharbLogoVariant.APP_ICON, PharbLogoVariant.SPLASH_LOGO -> {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(PharbDeepNavy, Color(0xFF132A4A))
                        ),
                        cornerRadius = CornerRadius(w * 0.28f, h * 0.28f),
                        size = Size(w, h)
                    )
                }
                PharbLogoVariant.FAVICON -> {
                    drawRoundRect(
                        color = PharbVibrantBlue,
                        cornerRadius = CornerRadius(w * 0.22f, h * 0.22f),
                        size = Size(w, h)
                    )
                }
                else -> {}
            }

            val primaryStrokeBrush = when (variant) {
                PharbLogoVariant.MONOCHROME_LOGO -> Brush.linearGradient(listOf(Color.Gray, Color.LightGray))
                PharbLogoVariant.WHITE_LOGO, PharbLogoVariant.FAVICON -> Brush.linearGradient(listOf(Color.White, Color(0xFFE0F2FE)))
                PharbLogoVariant.DARK_LOGO -> Brush.linearGradient(listOf(PharbDeepNavy, PharbRoyalBlue))
                else -> Brush.linearGradient(listOf(PharbVibrantBlue, PharbSoftBlue))
            }

            val accentNodeColor = when (variant) {
                PharbLogoVariant.MONOCHROME_LOGO -> Color.White
                PharbLogoVariant.WHITE_LOGO, PharbLogoVariant.FAVICON -> Color.White
                PharbLogoVariant.DARK_LOGO -> PharbRoyalBlue
                else -> PharbSoftBlue
            }

            // Geometric interlocking P + H prism ribbon
            val pad = w * 0.20f
            val strokeW = w * 0.13f

            // Left vertical pillar of P & H
            drawLine(
                brush = primaryStrokeBrush,
                start = Offset(pad, pad),
                end = Offset(pad, h - pad),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )

            // Right lower pillar of H
            drawLine(
                brush = primaryStrokeBrush,
                start = Offset(w - pad, h * 0.48f),
                end = Offset(w - pad, h - pad),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )

            // Geometric P loop + H crossbar
            val pPath = Path().apply {
                moveTo(pad, pad)
                lineTo(w * 0.62f, pad)
                cubicTo(
                    w * 0.86f, pad,
                    w * 0.86f, h * 0.52f,
                    w * 0.62f, h * 0.52f
                )
                lineTo(pad, h * 0.52f)
            }
            drawPath(
                path = pPath,
                brush = primaryStrokeBrush,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )

            // Glowing connection node at top-right apex with metallic silver ring
            val nodeRadius = if (variant == PharbLogoVariant.SPLASH_LOGO) {
                (w * 0.085f) * pulseScale
            } else {
                w * 0.08f
            }
            drawCircle(
                color = PharbMetallicSilver.copy(alpha = 0.55f),
                radius = nodeRadius * 1.35f,
                center = Offset(w - pad, pad * 1.15f)
            )
            drawCircle(
                color = accentNodeColor,
                radius = nodeRadius,
                center = Offset(w - pad, pad * 1.15f)
            )
        }

        if (showText) {
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                val textColor = when (variant) {
                    PharbLogoVariant.WHITE_LOGO -> Color.White
                    PharbLogoVariant.DARK_LOGO -> PharbDeepNavy
                    PharbLogoVariant.MONOCHROME_LOGO -> Color.Gray
                    else -> MaterialTheme.colorScheme.onSurface
                }
                Text(
                    text = "PHARB",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.6.sp
                    ),
                    color = textColor
                )
                if (showTagline) {
                    Text(
                        text = if (isArabic) "CONNECT • CREATE • SHARE" else "CONNECT • CREATE • SHARE",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun PharbVerifiedBadge(
    isVerified: Boolean,
    accountType: String = "PERSONAL",
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        if (isVerified) {
            Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = "PHARB Verified",
                tint = PharbVerifiedBlue,
                modifier = Modifier.size(16.dp)
            )
        }
        when (accountType) {
            "EDUCATION" -> {
                Surface(
                    color = PharbEducationEmerald.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.School,
                            contentDescription = "Education Account",
                            tint = PharbEducationEmerald,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "تعليمي",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = PharbEducationEmerald
                        )
                    }
                }
            }
            "BUSINESS" -> {
                Surface(
                    color = PharbBusinessGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BusinessCenter,
                            contentDescription = "Business Account",
                            tint = PharbBusinessGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "أعمال",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = PharbBusinessGold
                        )
                    }
                }
            }
            "CREATOR" -> {
                Surface(
                    color = PharbCreatorPurple.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Creator Account",
                            tint = PharbCreatorPurple,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "صانع محتوى",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = PharbCreatorPurple
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PharbAvatar(
    name: String,
    avatarColorHex: Long = 0xFF2563EB,
    size: Dp = 44.dp,
    isOnline: Boolean = false,
    hasStoryRing: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val initials = name.trim().split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1) }
        .ifEmpty { "P" }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .then(
                if (onClick != null) Modifier.clip(CircleShape).clickable { onClick() }
                else Modifier
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .then(
                    if (hasStoryRing) {
                        Modifier
                            .border(
                                width = 2.2.dp,
                                brush = Brush.linearGradient(
                                    listOf(PharbVibrantBlue, PharbSoftBlue, PharbEducationEmerald)
                                ),
                                shape = CircleShape
                            )
                            .padding(3.dp)
                    } else Modifier
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(avatarColorHex),
                             PharbDeepNavy
                        )
                    )
                )
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.36f).sp
                ),
                color = Color.White
            )
        }

        if (isOnline) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.28f)
                    .clip(CircleShape)
                    .background(PharbOnlineGreen)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}
