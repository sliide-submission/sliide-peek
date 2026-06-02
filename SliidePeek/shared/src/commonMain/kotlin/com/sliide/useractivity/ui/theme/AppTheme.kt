package com.sliide.useractivity.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

// "Signal" Material 3 colour schemes. Core tokens map onto ColorScheme slots; semantic extras
// (green status, shimmer, soft accent, inverse) live in SignalColors / MaterialTheme.signal.
private val LightColors = lightColorScheme(
    primary = Color(0xFF2D5BFF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE9EEFF),
    onPrimaryContainer = Color(0xFF2D5BFF),
    secondary = Color(0xFF2D5BFF),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF7F7F5),
    onBackground = Color(0xFF14151A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF14151A),
    surfaceVariant = Color(0xFFEFF0F2),
    onSurfaceVariant = Color(0xFF5D5F66),
    outline = Color(0xFFE6E7EA),
    outlineVariant = Color(0xFFE6E7EA),
    error = Color(0xFFD24B4B),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFBECEC),
    onErrorContainer = Color(0xFFD24B4B),
    inverseSurface = Color(0xFF1C1D22),
    inverseOnSurface = Color(0xFFF4F5F6),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5B82FF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1F2A4D),
    onPrimaryContainer = Color(0xFF5B82FF),
    secondary = Color(0xFF5B82FF),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFF0E0F13),
    onBackground = Color(0xFFF1F2F5),
    surface = Color(0xFF181A20),
    onSurface = Color(0xFFF1F2F5),
    surfaceVariant = Color(0xFF1D2027),
    onSurfaceVariant = Color(0xFF9A9CA4),
    outline = Color(0xFF262932),
    outlineVariant = Color(0xFF262932),
    error = Color(0xFFF0716E),
    onError = Color(0xFF16171C),
    errorContainer = Color(0x24F0716E),
    onErrorContainer = Color(0xFFF0716E),
    inverseSurface = Color(0xFFE7E8EC),
    inverseOnSurface = Color(0xFF16171C),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val spaceGrotesk = rememberSpaceGroteskFamily()
    val jetBrainsMono = rememberJetBrainsMonoFamily()
    val typography = remember(spaceGrotesk) { signalTypography(spaceGrotesk) }
    val textStyles = remember(jetBrainsMono) { signalTextStyles(jetBrainsMono) }
    val signalColors = if (darkTheme) SignalDarkColors else SignalLightColors

    CompositionLocalProvider(
        LocalSignalColors provides signalColors,
        LocalSignalTextStyles provides textStyles,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = typography,
            shapes = SignalShapes,
            content = content,
        )
    }
}

/** Soft accent tint used for selected rows, valid focus, and "on" segments. */
@Composable
fun selectionContainerColor(): Color = MaterialTheme.signal.accentSoft
