package com.sliide.useractivity.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AppSelectionColor = Color(0xFFE7C64D)
val AppSelectionSoftColor = Color(0xFFF6ECC4)

private val LightColors = lightColorScheme(
    primary = AppSelectionColor,
    onPrimary = Color(0xFF2B2B2B),
    background = Color(0xFFF4F3EF),
    onBackground = Color(0xFF2B2B2B),
    surface = Color(0xFFFBFBF9),
    onSurface = Color(0xFF2B2B2B),
    surfaceVariant = Color(0xFFF0F0EC),
    onSurfaceVariant = Color(0xFF6A6A66),
    outline = Color(0xFFB9B9B1),
    outlineVariant = Color(0xFFD2D2CB),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE1C45A),
    onPrimary = Color(0xFF302600),
    background = Color(0xFF181816),
    onBackground = Color(0xFFE8E2D6),
    surface = Color(0xFF22221F),
    onSurface = Color(0xFFE8E2D6),
    surfaceVariant = Color(0xFF2D2D29),
    onSurfaceVariant = Color(0xFFC9C3B8),
    outline = Color(0xFF8F897E),
    outlineVariant = Color(0xFF47433E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}

@Composable
fun selectionContainerColor(): Color = if (MaterialTheme.colorScheme.isLight) {
    AppSelectionSoftColor
} else {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
}

private val ColorScheme.isLight: Boolean
    get() = background.luminance() > 0.5f

private fun Color.luminance(): Float =
    (0.299f * red) + (0.587f * green) + (0.114f * blue)
