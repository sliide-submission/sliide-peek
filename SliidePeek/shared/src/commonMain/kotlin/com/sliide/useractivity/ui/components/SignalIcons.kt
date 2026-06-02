package com.sliide.useractivity.ui.components

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Lightweight stroked icon set transcribed from the Signal hi-fi reference (`design/ums-screens.jsx`).
 * Built by hand from SVG path data so we avoid pulling in `material-icons-extended`. Drawn as strokes;
 * `Icon(tint = …)` recolours them.
 */
object SignalIcons {
    val Plus = stroke("plus", listOf("M12 5v14M5 12h14"), width = 2.2f)
    val Refresh = stroke("refresh", listOf("M21 12a9 9 0 1 1-2.64-6.36", "M21 3v6h-6"))
    val ChevronLeft = stroke("chevronLeft", listOf("M15 18l-6-6 6-6"), width = 2f)
    val Close = stroke("close", listOf("M18 6 6 18M6 6l12 12"))
    val Check = stroke("check", listOf("M20 6 9 17l-5-5"), width = 2.4f)
    val Trash = stroke(
        "trash",
        listOf("M3 6h18M8 6V4a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1v2M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"),
    )
    val Edit = stroke("edit", listOf("M12 20h9", "M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4Z"))
    val Users = stroke(
        "users",
        listOf(
            "M16 19v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
            "M22 19v-2a4 4 0 0 0-3-3.87M16 3.13A4 4 0 0 1 16 11",
        ),
        circles = listOf(Triple(9f, 7f, 4f)),
        fillCircles = false,
        width = 1.7f,
    )
    val WifiOff = stroke(
        "wifiOff",
        listOf("M5 12.5a10 10 0 0 1 14 0M8.5 16a5 5 0 0 1 7 0", "M2 2l20 20"),
        dots = listOf(Pair(12f, 19.5f)),
    )
    val CloudOff = stroke(
        "cloudOff",
        listOf("M17.5 19a4.5 4.5 0 0 0 .5-9 6 6 0 0 0-11.6-1.5A4 4 0 0 0 6.5 19Z", "M2 2l20 20"),
    )
    val AlertTriangle = stroke(
        "alertTriangle",
        listOf("M10.3 3.8 2.4 18a1.9 1.9 0 0 0 1.7 2.9h15.8a1.9 1.9 0 0 0 1.7-2.9L13.7 3.8a1.9 1.9 0 0 0-3.4 0Z", "M12 9v4"),
        dots = listOf(Pair(12f, 16.5f)),
    )
    val AlertCircle = stroke(
        "alertCircle",
        listOf("M12 8v4"),
        circles = listOf(Triple(12f, 12f, 9f)),
        fillCircles = false,
        dots = listOf(Pair(12f, 16f)),
    )

    private fun stroke(
        name: String,
        paths: List<String>,
        circles: List<Triple<Float, Float, Float>> = emptyList(),
        fillCircles: Boolean = false,
        dots: List<Pair<Float, Float>> = emptyList(),
        width: Float = 1.9f,
    ): ImageVector {
        val builder = ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        )
        val black = SolidColor(Color.Black)
        paths.forEach { d ->
            builder.addPath(
                pathData = PathParser().parsePathString(d).toNodes(),
                stroke = black,
                strokeLineWidth = width,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
        circles.forEach { (cx, cy, r) ->
            builder.addPath(
                pathData = PathParser().parsePathString(circlePath(cx, cy, r)).toNodes(),
                stroke = if (fillCircles) null else black,
                fill = if (fillCircles) black else null,
                strokeLineWidth = width,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
        dots.forEach { (cx, cy) ->
            builder.addPath(
                pathData = PathParser().parsePathString(circlePath(cx, cy, 0.9f)).toNodes(),
                fill = black,
            )
        }
        return builder.build()
    }

    private fun circlePath(cx: Float, cy: Float, r: Float): String =
        "M${cx - r},$cy a$r,$r 0 1,0 ${r * 2},0 a$r,$r 0 1,0 ${-r * 2},0 z"
}
