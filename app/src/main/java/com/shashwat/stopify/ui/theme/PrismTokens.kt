package com.shashwat.stopify.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Prism's design tokens ("Aura × Spectrum"). Screens read these instead of literal values:
 * spacing, motion, ink levels, the split light, glass fills and edges.
 */
object PrismTokens {
    object Space {
        /** The screen's side margin. */
        val gutter = 22.dp
        val row = 64.dp
        val rowArt = 48.dp
        val section = 32.dp
    }

    object Radius {
        val smallArt = 10.dp
        val art = 14.dp
        val tile = 16.dp
        val large = 22.dp
    }

    /** Durations in ms. */
    object Motion {
        const val QUICK = 120
        const val STANDARD = 240
        const val EMPHASISED = 380
    }

    /** The split light across a width, red to violet (deeper on light pages). */
    fun spectrum(dark: Boolean, startX: Float = 0f, endX: Float = Float.POSITIVE_INFINITY): Brush =
        Brush.horizontalGradient(*(if (dark) SpectrumDark else SpectrumLight).toTypedArray(), startX = startX, endX = endX)

    /** The split light round a circle (the play ring, a current cover). */
    fun ring(): Brush = Brush.sweepGradient(SpectrumRing)
}

/** The split light across a width, on the page now showing. */
@Composable
fun spectrumBrush(startX: Float = 0f, endX: Float = Float.POSITIVE_INFINITY): Brush {
    val dark = LocalIsDark.current
    return remember(dark, startX, endX) { PrismTokens.spectrum(dark, startX, endX) }
}

/** Secondary text: 60 % ink. */
val ColorScheme.ink2: Color get() = onBackground.copy(alpha = 0.60f)
/** Tertiary text and quiet icons: 38 % ink. */
val ColorScheme.ink3: Color get() = onBackground.copy(alpha = 0.38f)
/** Hairlines. */
val ColorScheme.hairline: Color get() = onBackground.copy(alpha = 0.09f)

/** A see-through fill for chips, round buttons and panes: white at 10 % on dark pages, 55 % on light ones. */
@Composable
fun glassFill(strength: Float = 1f): Color =
    if (LocalIsDark.current) Color.White.copy(alpha = (0.10f * strength).coerceAtMost(0.9f))
    else Color.White.copy(alpha = (0.55f * strength).coerceAtMost(0.95f))

/** The lit edge of glass: brightest at the top, fading down the sides. */
@Composable
fun glassEdge(): Brush {
    val dark = LocalIsDark.current
    return Brush.verticalGradient(
        listOf(Color.White.copy(alpha = if (dark) 0.22f else 0.85f), Color.White.copy(alpha = if (dark) 0.05f else 0.30f))
    )
}

/** A thin ring of split light round [shape]: the mark of what is picked or playing. */
@Composable
fun Modifier.spectrumRing(shape: Shape, width: Dp = 1.5.dp): Modifier = border(width, spectrumBrush(), shape)

/**
 * A pane of grouped content (a settings group, a feature card): a quiet container with the glass's light top edge.
 * Dark pages lift it with the container colour; light pages put a white veil on the tinted ground.
 */
@Composable
fun Modifier.pane(shape: Shape = PaneShape): Modifier {
    val scheme = MaterialTheme.colorScheme
    return clip(shape).background(scheme.surfaceContainerHigh).border(1.dp, glassEdge(), shape)
}

/** A round glass disc fill, for icon buttons. */
@Composable
fun Modifier.glassDisc(): Modifier = clip(CircleShape).background(glassFill(1.4f)).border(1.dp, glassEdge(), CircleShape)
