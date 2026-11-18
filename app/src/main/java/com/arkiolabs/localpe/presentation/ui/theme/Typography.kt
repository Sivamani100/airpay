package com.arkiolabs.localpe.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.arkiolabs.localpe.R

/**
 * Arkio Labs LocalPe Typography System.
 *
 * Official typography specifications:
 * Uses Inter and Nunito font families for crisp financial data representation,
 * high legibility on outdoor sunlight displays, and modern geometric styling.
 */
val InterFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_medium, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

val NunitoFont = FontFamily(
    Font(R.font.nunito_sans_10pt_regular, FontWeight.Normal),
    Font(R.font.nunito_sans_10pt_semibold, FontWeight.Medium),
    Font(R.font.nunito_sans_10pt_bold, FontWeight.SemiBold),
    Font(R.font.nunito_sans_10pt_extrabold, FontWeight.Bold)
)

/** Authentic LocalPe Font Family */
val LocalPeFont = InterFont

/** Alias ensuring all existing references instantly use LocalPe's Inter font */
val ClashGrotesk = InterFont

/** Authentic LocalPe Typography System */
object LocalPeType {
    val Display = LocalPeFont
    val Body = LocalPeFont
    val MonoFamily = LocalPeFont

    val DisplayLarge = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        letterSpacing = (-0.02).em,
        lineHeight = 34.sp
    )
    val DisplayMedium = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = (-0.02).em,
        lineHeight = 28.sp
    )
    val DisplaySmall = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = (-0.01).em,
        lineHeight = 24.sp
    )
    val HeadlineLarge = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = (-0.01).em,
        lineHeight = 24.sp
    )
    val TitleLarge = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp
    )
    val TitleMedium = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp
    )
    val BodyLarge = TextStyle(
        fontFamily = Body,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    )
    val BodyMedium = TextStyle(
        fontFamily = Body,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    val BodySmall = TextStyle(
        fontFamily = Body,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
    val LabelLarge = TextStyle(
        fontFamily = Body,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.01.em
    )
    val LabelMedium = TextStyle(
        fontFamily = Body,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.02.em
    )
    val LabelSmall = TextStyle(
        fontFamily = Body,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.02.em
    )
    val Mono = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        fontFeatureSettings = "tnum"
    )
    val MonoLarge = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.02).em,
        fontFeatureSettings = "tnum"
    )

    /** Bold tabular text for financial amounts ("₹ 1,500.00"). */
    val MonoAmount = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        fontFeatureSettings = "tnum"
    )
}

val NeoPopType = LocalPeType

val LocalPeTypography = Typography(
    displayLarge = LocalPeType.DisplayLarge,
    displayMedium = LocalPeType.DisplayMedium,
    displaySmall = LocalPeType.DisplaySmall,
    headlineLarge = LocalPeType.HeadlineLarge,
    titleLarge = LocalPeType.TitleLarge,
    titleMedium = LocalPeType.TitleMedium,
    bodyLarge = LocalPeType.BodyLarge,
    bodyMedium = LocalPeType.BodyMedium,
    bodySmall = LocalPeType.BodySmall,
    labelLarge = LocalPeType.LabelLarge,
    labelMedium = LocalPeType.LabelMedium,
    labelSmall = LocalPeType.LabelSmall
)

val NeoPopTypography = LocalPeTypography
