package com.shashwat.stopify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shashwat.stopify.ui.theme.LocalIsDark
import com.shashwat.stopify.ui.theme.glassEdge
import com.shashwat.stopify.ui.theme.glassFill
import com.shashwat.stopify.ui.theme.hairline
import kotlin.random.Random

/** How floating surfaces (dock, mini player, round controls) are made. */
enum class GlassStyle(val label: String) {
    /** Plain tinted surface, no backdrop work. */
    OFF("Solid"),
    /** The content behind is blurred and veiled. */
    BLUR("Blurred"),
    /** Blur plus a refracting lens at the rim: liquid glass. */
    LIQUID("Liquid")
}

val LocalGlassStyle = compositionLocalOf { GlassStyle.BLUR }

/**
 * The one glass recipe for floating surfaces. Picks solid, blurred or liquid from [LocalGlassStyle]; falls
 * back to solid when no backdrop is available (dialogs, previews).
 */
@Composable
fun Modifier.glassSurface(shape: Shape, elevation: Dp = 10.dp): Modifier {
    val style = LocalGlassStyle.current
    val backdrop = LocalBackdrop.current
    val dark = LocalIsDark.current
    val scheme = MaterialTheme.colorScheme
    val shadowed = this.shadow(
        elevation, shape,
        ambientColor = Color.Black.copy(alpha = 0.3f), spotColor = Color.Black.copy(alpha = 0.3f)
    )
    if (style == GlassStyle.OFF || backdrop == null) {
        return shadowed
            .clip(shape)
            .background(scheme.surfaceContainerHigh)
            .background(glassFill(0.8f))
            .border(0.8.dp, glassEdge(), shape)
    }
    val liquid = style == GlassStyle.LIQUID
    return shadowed
        .drawInteractiveGlass(
            backdrop = backdrop,
            layer = null,
            luminance = { 0.5f },
            shape = shape,
            interaction = null,
            liquid = liquid,
            blurDp = if (liquid) 10f else 26f,
            scrimColor = if (dark) Color.Black else Color.White,
            darkenScrim = if (dark) (if (liquid) 0.30f else 0.42f) else (if (liquid) 0.38f else 0.55f)
        )
        .border(0.8.dp, glassEdge(), shape)
}

/** A fine, fixed grain over a surface, so a big flat colour reads as a material. */
fun Modifier.grain(alpha: Float = 0.05f): Modifier = drawWithCache {
    val rnd = Random(7)
    val count = ((size.width * size.height) / 900f).toInt().coerceIn(200, 2200)
    val pts = List(count) { Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height) }
    val dots = List(count) { rnd.nextFloat() }
    onDrawWithContent {
        drawContent()
        pts.forEachIndexed { i, p ->
            drawCircle(
                color = (if (dots[i] > 0.5f) Color.White else Color.Black).copy(alpha = alpha * (0.4f + dots[i])),
                radius = 0.8f,
                center = p
            )
        }
    }
}

val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

/**
 * Prism's textured bottom sheet: a tinted pane with a soft light falling from the top edge, a fine grain, a
 * lit rim and a short handle. Use instead of ModalBottomSheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrismSheet(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalIsDark.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.Transparent,
        contentColor = scheme.onSurface,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = if (dark) 0.55f else 0.35f),
        dragHandle = null,
        shape = SheetShape
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(SheetShape)
                .background(scheme.surfaceContainer)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = if (dark) 0.09f else 0.60f), Color.Transparent),
                        endY = 520f
                    )
                )
                .grain(if (dark) 0.05f else 0.035f)
                .border(1.dp, glassEdge(), SheetShape)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Box(
                Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp, bottom = 8.dp)
                    .width(36.dp).height(4.dp).clip(CircleShape).background(scheme.onSurface.copy(alpha = 0.28f))
            )
            content()
        }
    }
}
