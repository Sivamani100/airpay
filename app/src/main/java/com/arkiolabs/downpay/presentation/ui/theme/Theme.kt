package com.arkiolabs.downpay.presentation.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DownpayColorScheme = lightColorScheme(
    primary = DownpayColors.Primary,
    onPrimary = Color.White,
    primaryContainer = DownpayColors.PurpleTint,
    onPrimaryContainer = DownpayColors.PrimaryDark,
    secondary = DownpayColors.PrimaryLight,
    onSecondary = Color.White,
    background = DownpayColors.Background,
    onBackground = DownpayColors.TextPrimary,
    surface = DownpayColors.Surface,
    onSurface = DownpayColors.TextPrimary,
    surfaceVariant = DownpayColors.SurfaceVariant,
    onSurfaceVariant = DownpayColors.TextSecondary,
    error = DownpayColors.DangerRed,
    onError = Color.White,
    outline = DownpayColors.Border,
    outlineVariant = DownpayColors.BorderStrong
)

/**
 * Clean Downpay light theme with signature purple accents and crisp typography.
 */
@Composable
fun DownpayTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DownpayColors.Background.toArgb()
            window.navigationBarColor = DownpayColors.Surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }
    MaterialTheme(
        colorScheme = DownpayColorScheme,
        typography = DownpayTypography,
        shapes = DownpayShapes,
        content = content
    )
}
