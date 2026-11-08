package com.shashwat.stopify.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.shashwat.stopify.ui.theme.PaneShape
import com.shashwat.stopify.ui.theme.PrismTokens
import com.shashwat.stopify.ui.theme.glassEdge
import com.shashwat.stopify.ui.theme.glassFill
import com.shashwat.stopify.ui.theme.ink3
import com.shashwat.stopify.ui.theme.pane
import com.shashwat.stopify.ui.theme.spectrumBrush
import com.shashwat.stopify.ui.theme.spectrumRing

/*
 * Prism's controls, ported to Stopify. Buttons are pills; the primary action is a solid pill in the text colour;
 * secondary actions and chips are glass; what is picked or playing is lit with the split light.
 */

private val ButtonPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp)

/** The primary action: a solid pill in the text colour (white in dark mode), its label in the ground colour. */
@Composable
fun PrismButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    contentPadding: PaddingValues = ButtonPadding,
    content: @Composable RowScope.() -> Unit
) {
    val s = MaterialTheme.colorScheme
    Button(
        onClick, modifier.heightIn(min = 46.dp), enabled, shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = s.onSurface, contentColor = s.surface,
            disabledContainerColor = s.onSurface.copy(alpha = 0.12f), disabledContentColor = s.onSurface.copy(alpha = 0.38f)
        ),
        elevation = null, contentPadding = contentPadding, content = content
    )
}

/** A secondary action: a glass pill with a lit edge. */
@Composable
fun PrismOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    contentPadding: PaddingValues = ButtonPadding,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable RowScope.() -> Unit
) {
    val s = MaterialTheme.colorScheme
    OutlinedButton(
        onClick, modifier.heightIn(min = 46.dp), enabled, shape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = glassFill(1.3f), contentColor = contentColor, disabledContentColor = s.onSurface.copy(alpha = 0.38f)
        ),
        border = BorderStroke(1.dp, glassEdge()),
        contentPadding = contentPadding, content = content
    )
}

/** A quieter filled action: a stronger glass pill. */
@Composable
fun PrismTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    contentPadding: PaddingValues = ButtonPadding,
    content: @Composable RowScope.() -> Unit
) {
    val s = MaterialTheme.colorScheme
    Button(
        onClick, modifier.heightIn(min = 46.dp), enabled, shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = glassFill(2.4f), contentColor = s.onSurface,
            disabledContainerColor = glassFill(1f), disabledContentColor = s.onSurface.copy(alpha = 0.38f)
        ),
        border = BorderStroke(1.dp, glassEdge()),
        elevation = null, contentPadding = contentPadding, content = content
    )
}

/** A switch lit with the split light when on: a 44×26 capsule with a round knob. */
@Composable
fun PrismSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val s = MaterialTheme.colorScheme
    val dim = if (enabled) 1f else 0.4f
    val lit by animateFloatAsState(if (checked) 1f else 0f, tween(PrismTokens.Motion.STANDARD), label = "lit")
    val knob by animateColorAsState(
        if (checked) Color.White else s.onSurface.copy(alpha = 0.6f), tween(PrismTokens.Motion.QUICK), label = "knob"
    )
    val x by animateDpAsState(if (checked) 19.dp else 3.dp, tween(PrismTokens.Motion.STANDARD), label = "x")
    Box(
        modifier
            .heightIn(min = 48.dp)
            .then(
                if (onCheckedChange != null) Modifier.toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(width = 44.dp, height = 26.dp)
                .background(glassFill(2.2f), CircleShape)
                .background(spectrumBrush(), CircleShape, alpha = lit * dim)
                .border(1.dp, glassEdge(), CircleShape),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                Modifier.offset(x = x).size(20.dp)
                    .shadow(if (checked) 2.dp else 0.dp, CircleShape)
                    .background(knob.copy(alpha = knob.alpha * dim), CircleShape)
            )
        }
    }
}

/** A choice: a glass pill, lifted and ringed in split light when picked. */
@Composable
fun PrismChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val scheme = MaterialTheme.colorScheme
    val quiet = glassFill(1.2f)
    val lifted = glassFill(2.6f)
    val bg by animateColorAsState(if (selected) lifted else quiet, tween(180), label = "chipBg")
    val fg by animateColorAsState(if (selected) scheme.onSurface else scheme.onSurfaceVariant, tween(180), label = "chipFg")
    Row(
        modifier
            .heightIn(min = 40.dp)
            .clip(CircleShape).background(bg)
            .then(if (selected) Modifier.spectrumRing(CircleShape) else Modifier.border(1.dp, glassEdge(), CircleShape))
            .semantics { this.selected = selected }
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(18.dp), tint = fg)
            Spacer(Modifier.width(6.dp))
        }
        Text(label, style = MaterialTheme.typography.labelLarge, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Two to four options side by side in one glass capsule; the picked one is lifted out and ringed in split light. */
@Composable
fun <T> Segmented(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    onSelect: (T) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val lifted = glassFill(3f)
    Row(
        modifier.fillMaxWidth().height(48.dp).clip(CircleShape)
            .background(glassFill(1.2f)).border(1.dp, glassEdge(), CircleShape).padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { o ->
            val on = o == selected
            val bg by animateColorAsState(if (on) lifted else Color.Transparent, tween(180), label = "seg")
            val fg by animateColorAsState(if (on) scheme.onSurface else scheme.onSurfaceVariant, tween(180), label = "segInk")
            Box(
                Modifier.weight(1f).fillMaxHeight().clip(CircleShape).background(bg)
                    .then(if (on) Modifier.spectrumRing(CircleShape, 1.2.dp) else Modifier)
                    .semantics { this.selected = on }
                    .clickable(role = Role.RadioButton) { onSelect(o) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label(o), style = MaterialTheme.typography.labelLarge, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, color = fg, modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
        }
    }
}

/** A header action: a round of glass, 44 dp. */
@Composable
fun RoundAction(
    icon: ImageVector,
    description: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.38f)
    Box(
        modifier.size(44.dp).clip(CircleShape).background(glassFill(1.4f)).border(1.dp, glassEdge(), CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, description, Modifier.size(22.dp), tint = tint) }
}

/** A top-level page's heading: a big light title, an optional quiet line under it, round actions on the right. */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    start: androidx.compose.ui.unit.Dp = 20.dp,
    end: androidx.compose.ui.unit.Dp = 14.dp,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier.fillMaxWidth().statusBarsPadding().padding(start = start, end = end, top = 14.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!subtitle.isNullOrBlank()) Text(
                subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

/** A secondary page's bar: a glass back button, then a big light title that scrolls with the page. */
@Composable
fun SubPageHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            RoundAction(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onClick = onBack)
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, content = actions)
        }
        Text(
            title, Modifier.padding(start = 8.dp, top = 12.dp, bottom = 10.dp),
            style = MaterialTheme.typography.headlineLarge, maxLines = 2, overflow = TextOverflow.Ellipsis
        )
    }
}

/** A small uppercase label over a group: widely spaced, quiet. */
@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(
        text.uppercase(), modifier, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.1.em),
        color = color, maxLines = 1
    )
}

/** A shelf's heading: a quiet strapline over a light title; with [onMore] the whole row opens the shelf. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    strapline: String? = null,
    onMore: (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .then(if (onMore != null) Modifier.clickable(onClickLabel = "See all", onClick = onMore) else Modifier)
            .padding(vertical = 6.dp)
    ) {
        if (!strapline.isNullOrBlank()) Text(
            strapline, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            if (onMore != null) Icon(Icons.Rounded.ChevronRight, "See all", Modifier.size(26.dp), tint = scheme.onSurfaceVariant)
        }
    }
}

/** A titled pane of settings rows: a small label, then the rows on a lifted pane. */
@Composable
fun SettingsGroup(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier.fillMaxWidth()) {
        if (title.isNotBlank()) Eyebrow(title, Modifier.padding(start = 12.dp, bottom = 8.dp))
        Column(Modifier.fillMaxWidth().pane(PaneShape).padding(vertical = 6.dp)) { content() }
    }
}

/** A thin progress line in split light along a track, [fraction] 0..1. */
@Composable
fun SpectrumLine(fraction: Float, modifier: Modifier = Modifier, height: androidx.compose.ui.unit.Dp = 3.dp) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier.fillMaxWidth().height(height).clip(CircleShape).background(scheme.onSurface.copy(alpha = 0.12f))) {
        Box(
            Modifier.fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f, 1f)).clip(CircleShape)
                .background(spectrumBrush(0f, 600f))
        )
    }
}

/** A hairline divider in the ink's 9 %. */
@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(0.75.dp).background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.09f)))
}

/** Keeps [ink3] reachable from components that only import this kit. */
@Composable
fun quietInk(): Color = MaterialTheme.colorScheme.ink3

@Composable
fun GlassBox(modifier: Modifier = Modifier, shape: Shape = PaneShape, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.clip(shape).background(glassFill(1.6f)).border(1.dp, glassEdge(), shape), content = content)
}

/**
 * Prism's scrubber: a thin track that thickens under the finger, filled with the split light, a small round knob.
 * Tap or drag anywhere on it; [onValueChange] reports while dragging, [onValueChangeFinished] on release.
 */
@Composable
fun PrismScrubber(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
    description: String = "Seek"
) {
    var width by androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    var active by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val thick by animateDpAsState(if (active) 8.dp else 4.dp, tween(PrismTokens.Motion.QUICK), label = "scrubThick")
    val v = value.coerceIn(0f, 1f)
    val density = androidx.compose.ui.platform.LocalDensity.current
    val knob = 14.dp
    Box(
        modifier
            .fillMaxWidth()
            .height(48.dp)
            .onSizeChanged { width = it.width.toFloat().coerceAtLeast(1f) }
            .semantics {
                contentDescription = description
                progressBarRangeInfo = androidx.compose.ui.semantics.ProgressBarRangeInfo(v, 0f..1f)
            }
            .then(
                if (!enabled) Modifier else Modifier
                    .pointerScrub(
                        onPress = { x -> active = true; onValueChange((x / width).coerceIn(0f, 1f)) },
                        onDrag = { x -> onValueChange((x / width).coerceIn(0f, 1f)) },
                        onEnd = { active = false; onValueChangeFinished() }
                    )
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(Modifier.fillMaxWidth().height(thick).clip(CircleShape).background(trackColor))
        Box(
            Modifier.fillMaxWidth(v).height(thick).clip(CircleShape)
                .background(if (enabled) spectrumBrush(0f, with(density) { 360.dp.toPx() }) else Brush.linearGradient(listOf(trackColor, trackColor)))
        )
        if (enabled) Box(
            Modifier
                .offset { androidx.compose.ui.unit.IntOffset((width * v - with(density) { (knob / 2).toPx() }).toInt(), 0) }
                .size(knob)
                .shadow(2.dp, CircleShape)
                .background(Color.White, CircleShape)
        )
    }
}

private fun Modifier.pointerScrub(
    onPress: (Float) -> Unit,
    onDrag: (Float) -> Unit,
    onEnd: () -> Unit
): Modifier = this.pointerInput(Unit) {
    awaitEachGesture {
        val down = awaitFirstDown()
        down.consume()
        onPress(down.position.x)
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) break
            change.consume()
            onDrag(change.position.x)
        }
        onEnd()
    }
}
