package com.arkiolabs.localpe.presentation.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PhonePeColorScheme = lightColorScheme(
    primary = PhonePeColors.Primary,
    onPrimary = Color.White,
    primaryContainer = PhonePeColors.PurpleTint,
    onPrimaryContainer = PhonePeColors.PrimaryDark,
    secondary = PhonePeColors.PrimaryLight,
    onSecondary = Color.White,
    background = PhonePeColors.Background,
    onBackground = PhonePeColors.TextPrimary,
    surface = PhonePeColors.Surface,
    onSurface = PhonePeColors.TextPrimary,
    surfaceVariant = PhonePeColors.SurfaceVariant,
    onSurfaceVariant = PhonePeColors.TextSecondary,
    error = PhonePeColors.DangerRed,
    onError = Color.White,
    outline = PhonePeColors.Border,
    outlineVariant = PhonePeColors.BorderStrong
)

/**
 * Clean PhonePe light theme with signature purple accents and crisp typography.
 */
@Composable
fun LocalPeTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PhonePeColors.Background.toArgb()
            window.navigationBarColor = PhonePeColors.Surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }
    MaterialTheme(
        colorScheme = PhonePeColorScheme,
        typography = NeoPopTypography,
        shapes = NeoPopShapes,
        content = content
    )
}
