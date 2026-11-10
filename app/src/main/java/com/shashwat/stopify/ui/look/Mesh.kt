package com.shashwat.stopify.ui.look

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val FallbackColors = listOf(
    Color(0xFF3A1C71),
    Color(0xFFD76D77),
    Color(0xFF2B5876),
    Color(0xFFFFAF7B),
)

/** The four mesh colours, wrapped so the backdrop can skip recomposition. */
@Immutable
data class MeshPalette(val colors: List<Color>)

/** [from] blended into [to] by [fraction], colour by colour (a crossfade between two songs). */
fun lerp(from: MeshPalette, to: MeshPalette, fraction: Float): MeshPalette {
    fun four(p: MeshPalette) = (p.colors.ifEmpty { FallbackColors } + FallbackColors).take(4)
    return MeshPalette(four(from).zip(four(to)) { a, b -> androidx.compose.ui.graphics.lerp(a, b, fraction) })
}

/**
 * The Immersive player's backdrop: four luminous colour blobs sampled from
 * the album art, drawn as soft radial gradients and blurred into a mesh.
 * Colour changes on track skip crossfade over ~1.4s instead of snapping.
 *
 * The blobs drift when there is a reason to — the player opening, or
 * [trackKey] changing — and then come to rest. Orbiting forever would mean
 * re-blurring a full-screen layer at display refresh rate for as long as the
 * player is up: the most expensive thing on screen, for motion that reads as
 * ambient at best. The settled frame looks the same; only the battery drain is gone.
 */
@Composable
fun MeshGradientBackground(
    palette: MeshPalette,
    modifier: Modifier = Modifier,
    trackKey: Any? = null,
    driftMillis: Int = 8_000,
    /**
     * Keep the blobs orbiting instead of letting them settle. Off wherever the
     * mesh fills a screen, for the reason in the note above.
     */
    continuous: Boolean = false,
    /**
     * How far the blobs are smeared. The default is sized for a full screen;
     * a small surface needs proportionally less, or the four colours blend into
     * one wash before they reach its edges.
     */
    blurRadius: Dp = 64.dp,
    /**
     * Off for a surface that should read as a still image: colours snap
     * straight to target instead of crossfading, and the blobs never drift
     * on a [trackKey] change, only settling once on first composition.
     */
    animated: Boolean = true,
    /**
     * How long a palette change takes to crossfade. A track skip wants it
     * brisk; a surface that changes colour on a timer wants it long enough
     * that one fade runs into the next and the colour never sits still.
     */
    colorFadeMillis: Int = 1400,
) {
    val reduceAnimation = rememberReduceMotion()

    val tuned = (palette.colors.ifEmpty { FallbackColors } + FallbackColors)
        .take(4)
        .map { it.tuned() }

    // Each colour slot crossfades independently when the track (palette) changes,
    // unless animations are off, in which case colours snap straight to target.
    val colorSpec: AnimationSpec<Color> = if (reduceAnimation || !animated) snap() else tween(colorFadeMillis)
    val animatedColors = tuned.mapIndexed { index, color ->
        animateColorAsState(color, colorSpec, label = "meshColor$index").value
    }
    val baseColor by animateColorAsState(tuned.first().dimmed(), colorSpec, label = "meshBase")

    // Read in the draw lambda, not here: an Animatable read during draw
    // invalidates only the drawing, leaving composition out of the loop.
    val phase = remember { Animatable(0f) }
    LaunchedEffect(trackKey, reduceAnimation, continuous, animated) {
        when {
            !animated || reduceAnimation -> phase.snapTo(0f)
            // A full turn at a time, restarted rather than looped with an
            // infinite spec: the blobs' speeds are irrational multiples of each
            // other, so the pattern never repeats, and a linear phase keeps the
            // orbit even instead of easing to a halt each lap.
            continuous -> while (isActive) {
                phase.animateTo(
                    targetValue = phase.value + (2 * PI).toFloat(),
                    animationSpec = tween(driftMillis * 4, easing = LinearEasing),
                )
            }
            else -> phase.animateTo(
                targetValue = phase.value + DRIFT_RADIANS,
                animationSpec = tween(driftMillis, easing = FastOutSlowInEasing),
            )
        }
    }

    // Scale up slightly so the blur's clamped edges never show, then blur the
    // whole layer (RenderEffect, API 31+; a no-op below — the radial falloff
    // already reads soft there).
    //
    // Clipped on the way out, and from a layer of its own rather than by setting
    // `clip` on the one below: that one clips what is drawn *into* it, in its own
    // coordinates, and the scale is applied after — so the overhang the scale
    // creates survives it. This has to sit outside the scale to contain it.
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .graphicsLayer {
                scaleX = 1.3f
                scaleY = 1.3f
            }
            .background(baseColor)
            .blur(blurRadius),
    ) {
        val anchors = listOf(
            Offset(0.20f, 0.25f),
            Offset(0.80f, 0.20f),
            Offset(0.75f, 0.80f),
            Offset(0.25f, 0.75f),
        )
        val speeds = listOf(1f, -0.7f, 0.85f, -1.15f)
        val drift = phase.value

        animatedColors.forEachIndexed { index, color ->
            val anchor = anchors[index]
            val center = Offset(
                x = (anchor.x + 0.16f * cos(drift * speeds[index] + index * 1.7f)) * size.width,
                y = (anchor.y + 0.16f * sin(drift * speeds[index] * 0.9f + index * 2.3f)) * size.height,
            )
            val radius = size.maxDimension * 0.62f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.85f), color.copy(alpha = 0f)),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )
        }

        // Gentle scrim so white text stays legible over bright art.
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.10f),
                    Color.Black.copy(alpha = 0.38f),
                ),
            ),
        )
    }
}

@Composable
internal fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        android.provider.Settings.Global.getFloat(context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

/** The four mesh colours of a cover, taken once it has loaded (Coil + Palette). */
@Composable
fun rememberArtworkColors(imageUrl: String?): MeshPalette {
    val context = LocalContext.current
    var palette by remember { mutableStateOf(MeshPalette(FallbackColors)) }
    LaunchedEffect(imageUrl) {
        if (imageUrl.isNullOrBlank()) { palette = MeshPalette(FallbackColors); return@LaunchedEffect }
        try {
            val req = coil.request.ImageRequest.Builder(context).data(imageUrl).size(PALETTE_PX).allowHardware(false).build()
            val res = coil.Coil.imageLoader(context).execute(req)
            val bmp = (res as? coil.request.SuccessResult)?.drawable?.let { (it as? android.graphics.drawable.BitmapDrawable)?.bitmap }
            if (bmp != null) {
                val colors = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                    artworkMeshColors(Bitmap.createScaledBitmap(bmp, PALETTE_PX, PALETTE_PX, true))
                }
                palette = MeshPalette(colors)
            }
        } catch (e: Exception) { }
    }
    return palette
}

private const val PALETTE_PX = 112

private const val DRIFT_RADIANS = (PI * 0.45f).toFloat()

/**
 * Four mesh colours drawn from the artwork.
 *
 * The named swatches — vibrant, muted and friends — are a convenience over the
 * full set, and on dark or desaturated sleeves every vibrant slot comes back
 * null. Topping the rest up from [FallbackColors] is what left those covers
 * sitting under the stock purple. So the whole swatch list is read instead, and
 * any shortfall is derived from the art's own colours rather than borrowed.
 */
fun artworkMeshColors(bitmap: Bitmap): List<Color> {
    fun swatchesOf(clearFilters: Boolean): List<Color> =
        Palette.from(bitmap).maximumColorCount(24)
            .apply { if (clearFilters) clearFilters() }
            .generate()
            .swatches
            .sortedByDescending { it.population }
            .map { Color(it.rgb) }

    val found = swatchesOf(clearFilters = false).ifEmpty {
        // The default filter discards near-black and near-white, which on a
        // monochrome sleeve can be everything there is.
        swatchesOf(clearFilters = true)
    }

    val distinct = found.distinctEnough()
    return when {
        distinct.isEmpty() -> FallbackColors
        distinct.size >= 4 -> distinct.take(4)
        else -> distinct.expandedToFour()
    }
}

/** Drop near-duplicates, so the four blobs don't collapse into one wash. */
private fun List<Color>.distinctEnough(): List<Color> {
    val kept = mutableListOf<Color>()
    forEach { color -> if (kept.none { it.isCloseTo(color) }) kept += color }
    return kept
}

private fun Color.isCloseTo(other: Color): Boolean {
    val a = hsl()
    val b = other.hsl()
    val hueGap = abs(a[0] - b[0]).let { min(it, 360f - it) }
    return hueGap < 15f && abs(a[2] - b[2]) < 0.12f
}

/** Fill the empty slots off the art itself, fanning hue and lightness out. */
private fun List<Color>.expandedToFour(): List<Color> {
    val out = toMutableList()
    var step = 1
    while (out.size < 4) {
        out += this[(out.size - size) % size].shifted(24f * step, 0.12f * step)
        step++
    }
    return out
}

private fun Color.shifted(hue: Float, lightness: Float): Color {
    val hsl = hsl()
    hsl[0] = (hsl[0] + hue) % 360f
    hsl[2] = (hsl[2] + lightness).coerceIn(0.2f, 0.7f)
    return Color(ColorUtils.HSLToColor(hsl))
}

private fun Color.hsl(): FloatArray =
    FloatArray(3).also { ColorUtils.colorToHSL(toArgb(), it) }

/** Boost saturation and clamp lightness so any artwork yields a rich, non-muddy mesh. */
private fun Color.tuned(): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(toArgb(), hsl)
    hsl[1] = (hsl[1] * 1.35f).coerceAtMost(1f)
    hsl[2] = hsl[2].coerceIn(0.28f, 0.58f)
    return Color(ColorUtils.HSLToColor(hsl))
}

private fun Color.dimmed(): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(toArgb(), hsl)
    hsl[2] = 0.12f
    return Color(ColorUtils.HSLToColor(hsl))
}

/** The pale tint of the cover's first visibly coloured swatch, or white. Used for secondary text and glyphs. */
fun playerTintOf(palette: MeshPalette): Color {
    val source = palette.colors.firstOrNull { maxOf(it.red, it.green, it.blue) - minOf(it.red, it.green, it.blue) >= 0.08f }
        ?: return Color.White
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(source.toArgb(), hsl)
    hsl[1] = hsl[1].coerceIn(0.18f, 0.40f)
    hsl[2] = 0.88f
    return Color(ColorUtils.HSLToColor(hsl))
}
