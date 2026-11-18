package com.arkiolabs.airpay.presentation.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AirpayColorScheme = lightColorScheme(
    primary = AirpayColors.Primary,
    onPrimary = Color.White,
    primaryContainer = AirpayColors.PurpleTint,
    onPrimaryContainer = AirpayColors.PrimaryDark,
    secondary = AirpayColors.PrimaryLight,
    onSecondary = Color.White,
    background = AirpayColors.Background,
    onBackground = AirpayColors.TextPrimary,
    surface = AirpayColors.Surface,
    onSurface = AirpayColors.TextPrimary,
    surfaceVariant = AirpayColors.SurfaceVariant,
    onSurfaceVariant = AirpayColors.TextSecondary,
    error = AirpayColors.DangerRed,
    onError = Color.White,
    outline = AirpayColors.Border,
    outlineVariant = AirpayColors.BorderStrong
)

/**
 * Clean Airpay light theme with signature purple accents and crisp typography.
 */
@Composable
fun AirpayTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AirpayColors.Background.toArgb()
            window.navigationBarColor = AirpayColors.Surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }
    MaterialTheme(
        colorScheme = AirpayColorScheme,
        typography = AirpayTypography,
        shapes = AirpayShapes,
        content = content
    )
}
