package com.arkiolabs.localpe.presentation.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Arkio Labs LocalPe Vector Icon System.
 * Clean, modern linear icon set crafted specifically for LocalPe financial and navigation flows.
 */
object LocalPeIcons {

    val Search: ImageVector by lazy {
        ImageVector.Builder(
            name = "LocalPeIcons.Search",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Circle lens
                moveTo(11.5f, 21f)
                arcTo(9.5f, 9.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 11.5f, 2f)
                arcTo(9.5f, 9.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11.5f, 21f)
                close()
                // Handle
                moveTo(22f, 22f)
                lineTo(20f, 20f)
            }
        }.build()
    }

    val ArrowLeft: ImageVector by lazy {
        ImageVector.Builder(
            name = "Iconsax.ArrowLeft",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9.57f, 5.93f)
                lineTo(3.5f, 12f)
                lineTo(9.57f, 18.07f)
                moveTo(20.5f, 12f)
                lineTo(3.67f, 12f)
            }
        }.build()
    }

    val ArrowRight: ImageVector by lazy {
        ImageVector.Builder(
            name = "Iconsax.ArrowRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(8.91f, 19.92f)
                lineTo(15.43f, 13.4f)
                curveTo(16.2f, 12.63f, 16.2f, 11.37f, 15.43f, 10.6f)
                lineTo(8.91f, 4.08f)
            }
        }.build()
    }

    val Close: ImageVector by lazy {
        ImageVector.Builder(
            name = "Iconsax.Close",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(18f, 6f)
                lineTo(6f, 18f)
                moveTo(6f, 6f)
                lineTo(18f, 18f)
            }
        }.build()
    }

    val Lock: ImageVector by lazy {
        ImageVector.Builder(
            name = "Iconsax.Lock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Shackle
                moveTo(6f, 10f)
                lineTo(6f, 8f)
                curveTo(6f, 4.69f, 7.5f, 2f, 12f, 2f)
                curveTo(16.5f, 2f, 18f, 4.69f, 18f, 8f)
                lineTo(18f, 10f)
                // Body
                moveTo(17f, 22f)
                lineTo(7f, 22f)
                curveTo(3f, 22f, 2f, 21f, 2f, 17f)
                lineTo(2f, 15f)
                curveTo(2f, 11f, 3f, 10f, 7f, 10f)
                lineTo(17f, 10f)
                curveTo(21f, 10f, 22f, 11f, 22f, 15f)
                lineTo(22f, 17f)
                curveTo(22f, 21f, 21f, 22f, 17f, 22f)
                close()
                // Keyhole
                moveTo(12f, 15f)
                lineTo(12f, 17.5f)
            }
        }.build()
    }

    val Fingerprint: ImageVector by lazy {
        ImageVector.Builder(
            name = "Iconsax.Fingerprint",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 13f)
                curveTo(10.9f, 13f, 10f, 12.1f, 10f, 11f)
                curveTo(10f, 9.9f, 10.9f, 9f, 12f, 9f)
                curveTo(13.1f, 9f, 14f, 9.9f, 14f, 11f)
                lineTo(14f, 15.5f)
                curveTo(14f, 16.9f, 12.9f, 18f, 11.5f, 18f)
                curveTo(10.1f, 18f, 9f, 16.9f, 9f, 15.5f)
                lineTo(9f, 9.5f)
                curveTo(9f, 7.6f, 10.3f, 6f, 12f, 6f)
                curveTo(13.7f, 6f, 15f, 7.6f, 15f, 9.5f)
                lineTo(15f, 14f)
                moveTo(6.5f, 16.5f)
                curveTo(6.2f, 15.3f, 6f, 13.9f, 6f, 12f)
                curveTo(6f, 8.7f, 8.7f, 6f, 12f, 6f)
                moveTo(18f, 12f)
                curveTo(18f, 14.8f, 17.5f, 17.2f, 16.8f, 19.5f)
                moveTo(3.5f, 12f)
                curveTo(3.5f, 7.3f, 7.3f, 3.5f, 12f, 3.5f)
                curveTo(16.7f, 3.5f, 20.5f, 7.3f, 20.5f, 12f)
                curveTo(20.5f, 14.5f, 19.9f, 17.5f, 18.5f, 20.5f)
            }
        }.build()
    }

    val Call: ImageVector by lazy {
        ImageVector.Builder(
            name = "Iconsax.Call",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(21.97f, 18.33f)
                curveTo(21.97f, 18.69f, 21.89f, 19.06f, 21.72f, 19.42f)
                curveTo(21.55f, 19.78f, 21.33f, 20.12f, 21.04f, 20.44f)
                curveTo(20.55f, 20.98f, 20.01f, 21.37f, 19.4f, 21.62f)
                curveTo(18.8f, 21.87f, 18.15f, 22f, 17.45f, 22f)
                curveTo(16.43f, 22f, 15.34f, 21.76f, 14.19f, 21.27f)
                curveTo(13.04f, 20.78f, 11.89f, 20.12f, 10.75f, 19.28f)
                curveTo(9.6f, 18.43f, 8.51f, 17.47f, 7.47f, 16.39f)
                curveTo(6.44f, 15.31f, 5.54f, 14.16f, 4.77f, 12.95f)
                curveTo(4f, 11.74f, 3.43f, 10.53f, 3.04f, 9.34f)
                curveTo(2.65f, 8.14f, 2.45f, 7f, 2.45f, 5.92f)
                curveTo(2.45f, 5.25f, 2.57f, 4.62f, 2.81f, 4.04f)
                curveTo(3.05f, 3.45f, 3.42f, 2.92f, 3.93f, 2.44f)
                curveTo(4.58f, 1.81f, 5.29f, 1.5f, 6.04f, 1.5f)
                curveTo(6.36f, 1.5f, 6.68f, 1.57f, 6.98f, 1.72f)
                curveTo(7.28f, 1.87f, 7.54f, 2.09f, 7.75f, 2.4f)
                lineTo(9.95f, 5.51f)
                curveTo(10.35f, 6.07f, 10.45f, 6.6f, 10.25f, 7.1f)
                lineTo(9.44f, 8.15f)
                curveTo(9.31f, 8.32f, 9.25f, 8.51f, 9.25f, 8.73f)
                curveTo(9.25f, 8.9f, 9.28f, 9.06f, 9.35f, 9.22f)
                curveTo(9.5f, 9.54f, 9.8f, 10f, 10.27f, 10.58f)
                curveTo(10.74f, 11.16f, 11.27f, 11.75f, 11.87f, 12.35f)
                curveTo(12.48f, 12.96f, 13.07f, 13.51f, 13.66f, 13.99f)
                curveTo(14.25f, 14.47f, 14.71f, 14.77f, 15.03f, 14.92f)
                curveTo(15.19f, 14.99f, 15.35f, 15.03f, 15.52f, 15.03f)
                curveTo(15.75f, 15.03f, 15.95f, 14.95f, 16.12f, 14.8f)
                lineTo(17.13f, 13.95f)
                curveTo(17.65f, 13.52f, 18.2f, 13.43f, 18.79f, 13.68f)
                lineTo(21.75f, 15.42f)
                curveTo(22.06f, 15.61f, 22.3f, 15.86f, 22.46f, 16.16f)
                curveTo(22.61f, 21.72f, 21.97f, 17.5f, 21.97f, 18.33f)
                close()
            }
        }.build()
    }

    val Bank: ImageVector by lazy {
        ImageVector.Builder(
            name = "Iconsax.Bank",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Roof
                moveTo(2f, 9.5f)
                lineTo(12f, 3.5f)
                lineTo(22f, 9.5f)
                moveTo(3.5f, 9.5f)
                lineTo(20.5f, 9.5f)
                // Pillars
                moveTo(5f, 10f)
                lineTo(5f, 18f)
                moveTo(9.5f, 10f)
                lineTo(9.5f, 18f)
                moveTo(14.5f, 10f)
                lineTo(14.5f, 18f)
                moveTo(19f, 10f)
                lineTo(19f, 18f)
                // Floor
                moveTo(2f, 18f)
                lineTo(22f, 18f)
                moveTo(1f, 21.5f)
                lineTo(23f, 21.5f)
            }
        }.build()
    }

    val TickCircle: ImageVector by lazy {
        ImageVector.Builder(
            name = "Iconsax.TickCircle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 22f)
                curveTo(17.5f, 22f, 22f, 17.5f, 22f, 12f)
                curveTo(22f, 6.5f, 17.5f, 2f, 12f, 2f)
                curveTo(6.5f, 2f, 2f, 6.5f, 2f, 12f)
                curveTo(2f, 17.5f, 6.5f, 22f, 12f, 22f)
                close()
                moveTo(7.75f, 12f)
                lineTo(10.58f, 14.83f)
                lineTo(16.25f, 9.17f)
            }
        }.build()
    }

    val VolumeHigh: ImageVector by lazy {
        ImageVector.Builder(
            name = "Iconsax.VolumeHigh",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Speaker body
                moveTo(2f, 10f)
                lineTo(2f, 14f)
                curveTo(2f, 15f, 2.5f, 15.5f, 3.5f, 15.5f)
                lineTo(6f, 15.5f)
                lineTo(10.5f, 19.5f)
                curveTo(11.8f, 20.6f, 13f, 20f, 13f, 18.5f)
                lineTo(13f, 5.5f)
                curveTo(13f, 4f, 11.8f, 3.4f, 10.5f, 4.5f)
                lineTo(6f, 8.5f)
                lineTo(3.5f, 8.5f)
                curveTo(2.5f, 8.5f, 2f, 9f, 2f, 10f)
                close()
                // Soundwaves
                moveTo(16.5f, 8.5f)
                curveTo(17.8f, 10.5f, 17.8f, 13.5f, 16.5f, 15.5f)
                moveTo(19.5f, 6f)
                curveTo(21.8f, 9.5f, 21.8f, 14.5f, 19.5f, 18f)
            }
        }.build()
    }

    val Music: ImageVector by lazy {
        ImageVector.Builder(
            name = "Iconsax.Music",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9f, 17f)
                curveTo(9f, 19f, 7.5f, 20f, 5.5f, 20f)
                curveTo(3.5f, 20f, 2f, 19f, 2f, 17f)
                curveTo(2f, 15f, 3.5f, 14f, 5.5f, 14f)
                curveTo(7.5f, 14f, 9f, 15f, 9f, 17f)
                close()
                moveTo(9f, 17f)
                lineTo(9f, 6f)
                lineTo(22f, 3f)
                lineTo(22f, 14f)
                moveTo(22f, 14f)
                curveTo(22f, 16f, 20.5f, 17f, 18.5f, 17f)
                curveTo(16.5f, 17f, 15f, 16f, 15f, 14f)
                curveTo(15f, 12f, 16.5f, 11f, 18.5f, 11f)
                curveTo(20.5f, 11f, 22f, 12f, 22f, 14f)
                close()
                moveTo(9f, 9f)
                lineTo(22f, 6f)
            }
        }.build()
    }

    val Trash: ImageVector by lazy {
        ImageVector.Builder(
            name = "Iconsax.Trash",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(21f, 5.98f)
                curveTo(17.67f, 5.65f, 14.32f, 5.48f, 10.98f, 5.48f)
                curveTo(9f, 5.48f, 7.02f, 5.58f, 5.04f, 5.78f)
                lineTo(3f, 5.98f)
                moveTo(8.5f, 4.97f)
                lineTo(8.72f, 3.66f)
                curveTo(8.88f, 2.71f, 9f, 2f, 10.69f, 2f)
                lineTo(13.31f, 2f)
                curveTo(15f, 2f, 15.13f, 2.75f, 15.28f, 3.67f)
                lineTo(15.5f, 4.97f)
                moveTo(18.85f, 9.14f)
                lineTo(18.2f, 19.21f)
                curveTo(18.09f, 20.78f, 18f, 22f, 15.21f, 22f)
                lineTo(8.79f, 22f)
                curveTo(6f, 22f, 5.91f, 20.78f, 5.8f, 19.21f)
                lineTo(5.15f, 9.14f)
                moveTo(10.33f, 16.5f)
                lineTo(13.66f, 16.5f)
                moveTo(9.5f, 12.5f)
                lineTo(14.5f, 12.5f)
            }
        }.build()
    }

    val InfoCircle: ImageVector by lazy {
        ImageVector.Builder(
            name = "Iconsax.InfoCircle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 22f)
                curveTo(17.5f, 22f, 22f, 17.5f, 22f, 12f)
                curveTo(22f, 6.5f, 17.5f, 2f, 12f, 2f)
                curveTo(6.5f, 2f, 2f, 6.5f, 2f, 12f)
                curveTo(2f, 17.5f, 6.5f, 22f, 12f, 22f)
                close()
                moveTo(12f, 8f)
                lineTo(12f, 8.1f)
                moveTo(12f, 11.5f)
                lineTo(12f, 16.5f)
            }
        }.build()
    }

    val Profile: ImageVector by lazy {
        ImageVector.Builder(
            name = "Iconsax.Profile",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Head
                moveTo(12f, 12f)
                curveTo(14.5f, 12f, 16.5f, 10f, 16.5f, 7.5f)
                curveTo(16.5f, 5f, 14.5f, 3f, 12f, 3f)
                curveTo(9.5f, 3f, 7.5f, 5f, 7.5f, 7.5f)
                curveTo(7.5f, 10f, 9.5f, 12f, 12f, 12f)
                close()
                // Shoulders
                moveTo(20.5f, 21.5f)
                curveTo(20.5f, 18f, 17f, 15f, 12f, 15f)
                curveTo(7f, 15f, 3.5f, 18f, 3.5f, 21.5f)
            }
        }.build()
    }

    val Notification: ImageVector by lazy {
        ImageVector.Builder(
            name = "Iconsax.Notification",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 2.5f)
                curveTo(8.5f, 2.5f, 5.5f, 5.5f, 5.5f, 9f)
                lineTo(5.5f, 14.5f)
                lineTo(3.5f, 16.5f)
                lineTo(20.5f, 16.5f)
                lineTo(18.5f, 14.5f)
                lineTo(18.5f, 9f)
                curveTo(18.5f, 5.5f, 15.5f, 2.5f, 12f, 2.5f)
                close()
                moveTo(10f, 19.5f)
                curveTo(10f, 20.6f, 10.9f, 21.5f, 12f, 21.5f)
                curveTo(13.1f, 21.5f, 14f, 20.6f, 14f, 19.5f)
            }
        }.build()
    }

    val Refresh: ImageVector by lazy {
        ImageVector.Builder(
            name = "Iconsax.Refresh",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(22f, 12f)
                curveTo(22f, 17.52f, 17.52f, 22f, 12f, 22f)
                curveTo(6.48f, 22f, 2f, 17.52f, 2f, 12f)
                curveTo(2f, 6.48f, 6.48f, 2f, 12f, 2f)
                curveTo(16.5f, 2f, 20.25f, 4.97f, 21.5f, 9f)
                moveTo(22f, 4f)
                lineTo(22f, 9.5f)
                lineTo(16.5f, 9.5f)
            }
        }.build()
    }

    val ShieldSecurity: ImageVector by lazy {
        ImageVector.Builder(
            name = "Iconsax.ShieldSecurity",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 2f)
                lineTo(3.5f, 5.5f)
                curveTo(3.5f, 13f, 6.5f, 19.5f, 12f, 22f)
                curveTo(17.5f, 19.5f, 20.5f, 13f, 20.5f, 5.5f)
                lineTo(12f, 2f)
                close()
                // Keyhole in shield
                moveTo(12f, 8.5f)
                curveTo(10.9f, 8.5f, 10f, 9.4f, 10f, 10.5f)
                curveTo(10f, 11.3f, 10.5f, 12f, 11.2f, 12.3f)
                lineTo(11f, 15f)
                lineTo(13f, 15f)
                lineTo(12.8f, 12.3f)
                curveTo(13.5f, 12f, 14f, 11.3f, 14f, 10.5f)
                curveTo(14f, 9.4f, 13.1f, 8.5f, 12f, 8.5f)
                close()
            }
        }.build()
    }

    val Setting: ImageVector by lazy {
        ImageVector.Builder(
            name = "Iconsax.Setting",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 15f)
                curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
                curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
                curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
                curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
                close()
                moveTo(2f, 12f)
                curveTo(2f, 10.7f, 2.5f, 9.5f, 3.3f, 8.5f)
                lineTo(4.7f, 9.3f)
                moveTo(22f, 12f)
                curveTo(22f, 13.3f, 21.5f, 14.5f, 20.7f, 15.5f)
                lineTo(19.3f, 14.7f)
            }
        }.build()
    }

    val Copy: ImageVector by lazy {
        ImageVector.Builder(
            name = "Iconsax.Copy",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(16f, 5f)
                lineTo(9f, 5f)
                curveTo(6f, 5f, 4.5f, 6.5f, 4.5f, 9.5f)
                lineTo(4.5f, 16.5f)
                curveTo(4.5f, 19.5f, 6f, 21f, 9f, 21f)
                lineTo(16f, 21f)
                curveTo(19f, 21f, 20.5f, 19.5f, 20.5f, 16.5f)
                lineTo(20.5f, 9.5f)
                curveTo(20.5f, 6.5f, 19f, 5f, 16f, 5f)
                close()
                moveTo(8.5f, 3.5f)
                curveTo(9f, 3f, 9.8f, 2.8f, 11f, 2.8f)
                lineTo(17.5f, 2.8f)
                curveTo(20.2f, 2.8f, 21.5f, 4.1f, 21.5f, 6.8f)
                lineTo(21.5f, 13.5f)
            }
        }.build()
    }
}

val Iconsax = LocalPeIcons
