package com.arkiolabs.airpay.presentation.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Authentic Airpay design palette strictly derived from the official logo:
 * - Deep Dark Navy: #08101D (Logo background & card base)
 * - Saffron Orange: #FEA301 (Indian tricolor sky)
 * - Pure White: #FFFFFF (Flowing wave, clean surfaces)
 * - Slashed Wi-Fi Green: #00C853 (Vibrant success accent)
 */
object AirpayColors {
    // Primary Dark Navy (Logo Base)
    val Primary = Color(0xFF08101D)
    val PrimaryDark = Color(0xFF03070E)
    val PrimaryLight = Color(0xFF142033)
    val Navy = Color(0xFF08101D)
    val NavySurface = Color(0xFF0F1B2E)

    // Saffron Orange (Logo Top Sky)
    val Saffron = Color(0xFFFEA301)
    val SaffronDark = Color(0xFFE68A00)
    val SaffronLight = Color(0xFFFFB74D)
    val SaffronTint = Color(0xFFFFF4E5)
    val AccentOrange = Saffron
    val AccentOrangeTint = SaffronTint

    // Slashed Wi-Fi Emerald Green (Logo Accent)
    val SuccessGreen = Color(0xFF00C853)
    val Success = SuccessGreen
    val SuccessTint = Color(0xFFE8F8EE)

    // Soft Tint Aliases for backwards compatibility
    val PurpleTint = Color(0xFFF0F4F8)
    val PurpleSurface = Color(0xFFE8EEF5)
    val PurpleBorder = Color(0xFFD4E0EE)

    // Neutral Surfaces & Backgrounds
    val Background = Color(0xFFFFFFFF)
    val SectionDivider = Color(0xFFF4F4F6)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF8F9FA)
    val SurfaceElevated = Color(0xFFFFFFFF)

    // Borders & Dividers
    val Border = Color(0xFFEEEEEE)
    val BorderLight = Color(0xFFF5F5F5)
    val BorderStrong = Color(0xFFE0E0E0)

    // Typography
    val TextPrimary = Color(0xFF08101D)
    val TextSecondary = Color(0xFF6B7280)
    val TextMuted = Color(0xFF9CA3AF)
    val TextOnPrimary = Color(0xFFFFFFFF)

    // Functional & Accents
    val DangerRed = Color(0xFFD32F2F)
    val Danger = DangerRed
    val DangerTint = Color(0xFFFFEBEE)
    val DarkNavy = Color(0xFF08101D)
    val SkyBlue = Color(0xFF08101D)
    val SkyBlueDark = Color(0xFF03070E)
    val SkyBlueTint = Color(0xFFF0F4F8)

    // Banner & Hero Gradients
    val BannerGradient = Brush.verticalGradient(
        listOf(Color(0xFF08101D), Color(0xFF142033))
    )
    val SaffronGradient = Brush.horizontalGradient(
        listOf(Color(0xFFFEA301), Color(0xFFFFA000))
    )
    val PurpleGradient = BannerGradient
    val Overlay = Color(0x99000000)
}

/**
 * Backward compatibility alias mapping NeoPop tokens to the Airpay design language.
 */
object NeoPopColors {
    val Black = Color(0xFFF6F7FA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceHigh = Color(0xFFFFFFFF)
    val SurfaceHigher = Color(0xFFF0F4F8)

    val Border = Color(0xFFE5E7EB)
    val BorderStrong = Color(0xFFD1D5DB)

    val TextPrimary = Color(0xFF08101D)
    val TextSecondary = Color(0xFF6B7280)
    val TextMuted = Color(0xFF9CA3AF)

    val Accent = Color(0xFF08101D)
    val AccentDim = Color(0xFF142033)
    val AccentDeep = Color(0xFF03070E)

    val Danger = Color(0xFFD32F2F)
    val DangerDim = Color(0xFFB71C1C)
    val DangerDeep = Color(0xFFFFEBEE)

    val Success = Color(0xFF00C853)
    val SuccessDim = Color(0xFF00A844)
    val SuccessDeep = Color(0xFFE8F8EE)

    val Warn = Color(0xFFFEA301)
    val WarnDim = Color(0xFFE68A00)

    val Overlay = Color(0x99000000)
}
