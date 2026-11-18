package com.arkiolabs.localpe.presentation.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Authentic LocalPe design palette.
 * Signature deep purple, soft purple tints, clean white surfaces,
 * crisp dark typography, and light gray dividers.
 */
object LocalPeColors {
    // Primary Purple
    val Primary = Color(0xFF5F259F)
    val PrimaryDark = Color(0xFF4A148C)
    val PrimaryLight = Color(0xFF7B1FA2)
    val PurpleTint = Color(0xFFF5EDFD)
    val PurpleSurface = Color(0xFFEDE7F6)
    val PurpleBorder = Color(0xFFD1C4E9)

    // Neutral Surfaces & Backgrounds
    val Background = Color(0xFFFFFFFF)
    val SectionDivider = Color(0xFFF4F4F6)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF9FAFB)
    val SurfaceElevated = Color(0xFFFFFFFF)

    // Borders & Dividers
    val Border = Color(0xFFEEEEEE)
    val BorderLight = Color(0xFFF5F5F5)
    val BorderStrong = Color(0xFFE0E0E0)

    // Typography
    val TextPrimary = Color(0xFF1F2024)
    val TextSecondary = Color(0xFF6B7280)
    val TextMuted = Color(0xFF9CA3AF)
    val TextOnPrimary = Color(0xFFFFFFFF)

    // Functional & Accents
    val AccentOrange = Color(0xFFFF6D00)
    val AccentOrangeTint = Color(0xFFFFF3E0)
    val SuccessGreen = Color(0xFF00875A)
    val SuccessTint = Color(0xFFE8F5E9)
    val DangerRed = Color(0xFFD32F2F)
    val Danger = DangerRed
    val DangerTint = Color(0xFFFFEBEE)
    val DarkNavy = Color(0xFF1B1B2F)
    val SkyBlue = Color(0xFF38B6FF)
    val SkyBlueDark = Color(0xFF0288D1)
    val SkyBlueTint = Color(0xFFE1F5FE)

    // Banner & Hero Gradients
    val BannerGradient = Brush.linearGradient(
        listOf(Color(0xFF38B6FF), Color(0xFF673AB7))
    )
    val PurpleGradient = Brush.linearGradient(
        listOf(Color(0xFF673AB7), Color(0xFF5F259F))
    )
    val Overlay = Color(0x99000000)
}

/**
 * Backward compatibility alias mapping NeoPop tokens to the LocalPe design language.
 */
object NeoPopColors {
    val Black = Color(0xFFF6F7FA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceHigh = Color(0xFFFFFFFF)
    val SurfaceHigher = Color(0xFFF5EDFD)

    val Border = Color(0xFFE5E7EB)
    val BorderStrong = Color(0xFFD1D5DB)

    val TextPrimary = Color(0xFF1F2024)
    val TextSecondary = Color(0xFF6B7280)
    val TextMuted = Color(0xFF9CA3AF)

    val Accent = Color(0xFF5F259F)
    val AccentDim = Color(0xFF4A148C)
    val AccentDeep = Color(0xFF310D6B)

    val Danger = Color(0xFFD32F2F)
    val DangerDim = Color(0xFFB71C1C)
    val DangerDeep = Color(0xFFFFEBEE)

    val Success = Color(0xFF00875A)
    val SuccessDim = Color(0xFF00693E)
    val SuccessDeep = Color(0xFFE8F5E9)

    val Warn = Color(0xFFFF6D00)
    val WarnDim = Color(0xFFE65100)

    val Overlay = Color(0x99000000)
}
