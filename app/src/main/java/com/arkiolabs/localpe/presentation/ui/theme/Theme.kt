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

private val LocalPeColorScheme = lightColorScheme(
    primary = LocalPeColors.Primary,
    onPrimary = Color.White,
    primaryContainer = LocalPeColors.PurpleTint,
    onPrimaryContainer = LocalPeColors.PrimaryDark,
    secondary = LocalPeColors.PrimaryLight,
    onSecondary = Color.White,
    background = LocalPeColors.Background,
    onBackground = LocalPeColors.TextPrimary,
    surface = LocalPeColors.Surface,
    onSurface = LocalPeColors.TextPrimary,
    surfaceVariant = LocalPeColors.SurfaceVariant,
    onSurfaceVariant = LocalPeColors.TextSecondary,
    error = LocalPeColors.DangerRed,
    onError = Color.White,
    outline = LocalPeColors.Border,
    outlineVariant = LocalPeColors.BorderStrong
)

/**
 * Clean LocalPe light theme with signature purple accents and crisp typography.
 */
@Composable
fun LocalPeTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = LocalPeColors.Background.toArgb()
            window.navigationBarColor = LocalPeColors.Surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }
    MaterialTheme(
        colorScheme = LocalPeColorScheme,
        typography = NeoPopTypography,
        shapes = NeoPopShapes,
        content = content
    )
}
