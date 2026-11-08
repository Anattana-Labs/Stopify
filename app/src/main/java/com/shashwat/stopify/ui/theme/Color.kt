package com.shashwat.stopify.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

/*
 * Prism palette (Aura × Spectrum). One violet seed, near-neutral surfaces tinted slightly toward it, and the
 * "split light": red to violet, used only for progress, the current tab, rings and the playing mark.
 */

/** Prism's own accent. Every scheme is grown from it unless the artwork supplies another seed. */
val PrismViolet = Color(0xFF7C5CFF)

/** The split light on dark pages (bright) and light pages (deeper, so yellow still reads on white). */
val SpectrumDark = listOf(
    0.00f to Color(0xFFFF4D5E), 0.20f to Color(0xFFFF9F43), 0.38f to Color(0xFFFFD84D),
    0.56f to Color(0xFF3DDC97), 0.76f to Color(0xFF36B8FF), 1.00f to Color(0xFF7C5CFF),
)
val SpectrumLight = listOf(
    0.00f to Color(0xFFE23347), 0.20f to Color(0xFFE57A12), 0.38f to Color(0xFFC79A00),
    0.56f to Color(0xFF15A86C), 0.76f to Color(0xFF1789D6), 1.00f to Color(0xFF6A44F0),
)
/** The same colours as a closed loop, for rings. */
val SpectrumRing = listOf(
    Color(0xFFFF4D5E), Color(0xFFFF9F43), Color(0xFFFFD84D), Color(0xFF3DDC97),
    Color(0xFF36B8FF), Color(0xFF7C5CFF), Color(0xFFFF4D5E),
)

internal fun hsl(h: Float, s: Float, l: Float): Color =
    Color(ColorUtils.HSLToColor(floatArrayOf(((h % 360f) + 360f) % 360f, s.coerceIn(0f, 1f), l.coerceIn(0f, 1f))))

internal fun hueOf(c: Color): Float {
    val out = FloatArray(3)
    ColorUtils.colorToHSL(c.toArgbCompat(), out)
    return out[0]
}

internal fun satOf(c: Color): Float {
    val out = FloatArray(3)
    ColorUtils.colorToHSL(c.toArgbCompat(), out)
    return out[1]
}

internal fun Color.toArgbCompat(): Int = this.toArgb()

/** Greys have no hue to grow a scheme from. */
internal fun isNeutralSeed(c: Color): Boolean {
    val out = FloatArray(3)
    ColorUtils.colorToHSL(c.toArgbCompat(), out)
    return out[1] < 0.08f || out[2] > 0.97f || out[2] < 0.03f
}

internal fun contrastOf(fg: Color, bg: Color): Float =
    ColorUtils.calculateContrast(fg.copy(alpha = 1f).toArgbCompat(), bg.copy(alpha = 1f).toArgbCompat()).toFloat()

/** [c] moved toward white or black, as far as it must, until it reads on [bg] at [min]:1. */
internal fun readableOn(c: Color, bg: Color, min: Float = 4.5f): Color {
    if (contrastOf(c, bg) >= min) return c
    val out = FloatArray(3)
    ColorUtils.colorToHSL(c.toArgbCompat(), out)
    val lighten = ColorUtils.calculateLuminance(bg.toArgbCompat()) < 0.5
    var l = out[2]
    repeat(40) {
        l = (l + if (lighten) 0.02f else -0.02f).coerceIn(0f, 1f)
        val next = hsl(out[0], out[1], l)
        if (contrastOf(next, bg) >= min) return next
    }
    return if (lighten) Color.White else Color(0xFF111114)
}

/** White or near-black ink, whichever reads better on [c]. */
internal fun inkOn(c: Color): Color =
    if (contrastOf(Color.White, c) >= contrastOf(Color(0xFF111114), c)) Color.White else Color(0xFF111114)
