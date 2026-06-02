package com.sliide.useractivity.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sliide.useractivity.resources.Res
import com.sliide.useractivity.resources.jetbrains_mono_medium
import com.sliide.useractivity.resources.jetbrains_mono_regular
import com.sliide.useractivity.resources.jetbrains_mono_semibold
import com.sliide.useractivity.resources.space_grotesk_bold
import com.sliide.useractivity.resources.space_grotesk_medium
import com.sliide.useractivity.resources.space_grotesk_regular
import org.jetbrains.compose.resources.Font

/**
 * Signal type scale (from `design/design_tokens_and_handoff.html`).
 *
 * Space Grotesk drives UI/display text; JetBrains Mono carries "meta" text (emails, timestamps, IDs,
 * counts, eyebrows). Space Grotesk ships no static SemiBold (600), so `body-strong` maps to Medium (500)
 * and titles/headings use Bold (700).
 */
data class SignalTextStyles(
    /** Emails, breadcrumb-style paths. (`label` · mono 11/500) */
    val email: TextStyle,
    /** Dates, counts, status meta, relative timestamps. (`meta` · mono 10.5/600) */
    val meta: TextStyle,
    /** Section labels, chip text, kicker. (`eyebrow` · mono 10/600 +0.1em UPPER) */
    val eyebrow: TextStyle,
    /** Inline error/diagnostic codes. (mono 10) */
    val code: TextStyle,
)

@Composable
fun rememberSpaceGroteskFamily(): FontFamily = FontFamily(
    Font(Res.font.space_grotesk_regular, FontWeight.Normal),
    Font(Res.font.space_grotesk_medium, FontWeight.Medium),
    // No static SemiBold ships for Space Grotesk; reuse Medium for 600 requests.
    Font(Res.font.space_grotesk_medium, FontWeight.SemiBold),
    Font(Res.font.space_grotesk_bold, FontWeight.Bold),
)

@Composable
fun rememberJetBrainsMonoFamily(): FontFamily = FontFamily(
    Font(Res.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(Res.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(Res.font.jetbrains_mono_semibold, FontWeight.SemiBold),
)

fun signalTypography(spaceGrotesk: FontFamily): Typography {
    fun sg(weight: FontWeight, size: Int, letter: Double = 0.0, line: Int = 0): TextStyle =
        TextStyle(
            fontFamily = spaceGrotesk,
            fontWeight = weight,
            fontSize = size.sp,
            letterSpacing = letter.sp,
            lineHeight = if (line > 0) line.sp else androidx.compose.ui.unit.TextUnit.Unspecified,
        )

    val base = Typography()
    return base.copy(
        // display / headline — used sparingly (kept readable, not oversized)
        headlineSmall = sg(FontWeight.Bold, 23, -0.46),
        // title scale
        titleLarge = sg(FontWeight.Bold, 23, -0.46),
        titleMedium = sg(FontWeight.Bold, 19, -0.38),
        titleSmall = sg(FontWeight.Bold, 17, -0.2),
        // body scale
        bodyLarge = sg(FontWeight.Normal, 15, line = 22),
        bodyMedium = sg(FontWeight.Medium, 14), // body-strong (row name) → Medium (no static 600)
        bodySmall = sg(FontWeight.Normal, 13, line = 21),
        // labels / buttons
        labelLarge = sg(FontWeight.Medium, 13),
        labelMedium = sg(FontWeight.SemiBold, 12),
        labelSmall = sg(FontWeight.Medium, 11),
    )
}

fun signalTextStyles(jetBrainsMono: FontFamily): SignalTextStyles = SignalTextStyles(
    email = TextStyle(fontFamily = jetBrainsMono, fontWeight = FontWeight.Medium, fontSize = 11.sp),
    meta = TextStyle(fontFamily = jetBrainsMono, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp),
    eyebrow = TextStyle(
        fontFamily = jetBrainsMono,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        letterSpacing = 1.0.sp,
    ),
    code = TextStyle(fontFamily = jetBrainsMono, fontWeight = FontWeight.Medium, fontSize = 10.sp, letterSpacing = 0.5.sp),
)

private val FallbackTextStyles = SignalTextStyles(
    email = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 11.sp),
    meta = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp),
    eyebrow = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.0.sp),
    code = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 10.sp),
)

val LocalSignalTextStyles = staticCompositionLocalOf { FallbackTextStyles }

/** Signal mono text styles for the current theme. */
val MaterialTheme.signalType: SignalTextStyles
    @Composable
    @ReadOnlyComposable
    get() = LocalSignalTextStyles.current
