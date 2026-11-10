package com.shashwat.stopify.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.material3.ColorScheme
import com.shashwat.stopify.ui.theme.LocalDynamicColors
import com.shashwat.stopify.ui.theme.LocalIsDark
import com.shashwat.stopify.ui.theme.hsl
import com.shashwat.stopify.ui.theme.hueOf
import com.shashwat.stopify.ui.theme.satOf
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shashwat.stopify.data.SpotifyColorLyrics
import com.shashwat.stopify.data.SpotifyTrack

internal data class LyricsPalette(
    val top: Color, val bottom: Color,
    val active: Color, val inactive: Color,
    val title: Color, val subtitle: Color
)

/**
 * Turns Spotify's lyrics colour into a gradient that fits the app's mode: deep tones with light text in dark mode,
 * pale tones with dark text in light mode. Without a seed (no colour, or Dynamic color off) the theme's own surfaces are used.
 */
internal fun lyricsPalette(seed: Color?, isDark: Boolean, scheme: ColorScheme): LyricsPalette {
    if (seed == null) {
        val ink = scheme.onSurface
        return LyricsPalette(
            top = scheme.surfaceContainerHigh, bottom = scheme.surface,
            active = ink, inactive = ink.copy(alpha = if (isDark) 0.38f else 0.42f),
            title = ink, subtitle = scheme.onSurfaceVariant
        )
    }
    val h = hueOf(seed)
    val s = satOf(seed).coerceIn(0.28f, 0.85f)
    return if (isDark) {
        val ink = hsl(h, (s * 0.45f).coerceAtMost(0.5f), 0.96f)
        LyricsPalette(
            top = hsl(h, s * 0.62f, 0.24f), bottom = hsl(h, s * 0.5f, 0.07f),
            active = ink, inactive = ink.copy(alpha = 0.38f),
            title = ink, subtitle = ink.copy(alpha = 0.72f)
        )
    } else {
        val ink = hsl(h, (s * 0.7f).coerceAtMost(0.6f), 0.10f)
        LyricsPalette(
            top = hsl(h, (s * 0.95f).coerceAtMost(0.9f), 0.84f), bottom = hsl(h, s * 0.75f, 0.96f),
            active = ink, inactive = ink.copy(alpha = 0.42f),
            title = ink, subtitle = ink.copy(alpha = 0.66f)
        )
    }
}

/**
 * Full-screen synced lyrics overlay with theme color tinting and modern UI.
 */
@Composable
fun FullScreenLyricsViewer(
    track: SpotifyTrack,
    lyrics: SpotifyColorLyrics?,
    isLoading: Boolean,
    currentPositionMs: Long,
    onSeekTo: (Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val themePrimary = scheme.primary
    val isDark = LocalIsDark.current
    val dynamic = LocalDynamicColors.current

    // Spotify's lyrics colour is one mid-tone; it is turned into a dark gradient in dark mode and a light one in light mode.
    val seed = remember(lyrics?.backgroundColor, dynamic) {
        if (dynamic) lyrics?.let { argbToColor(it.backgroundColor) } else null
    }
    val palette = remember(seed, isDark, scheme.surface, scheme.surfaceContainerHigh, scheme.onSurface) {
        lyricsPalette(seed, isDark, scheme)
    }
    val topColor by animateColorAsState(palette.top, tween(600), label = "lyricsTop")
    val bottomColor by animateColorAsState(palette.bottom, tween(600), label = "lyricsBottom")
    val isLightBg = !isDark

    val activeHighlightColor = palette.active
    val inactiveTextColor = palette.inactive
    val titleColor = palette.title
    val subtitleColor = palette.subtitle
    val closeButtonTint = palette.title

    val activeIndex = remember(lyrics?.lines, currentPositionMs) {
        if (lyrics != null && lyrics.lines.isNotEmpty() && lyrics.syncType == "LINE_SYNCED") {
            lyrics.lines.indexOfLast { it.startTimeMs <= currentPositionMs }
        } else {
            -1
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            listState.animateScrollToItem(activeIndex.coerceAtLeast(0), scrollOffset = -260)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors = listOf(topColor, bottomColor))
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Enhanced Header: Badge, title, artist, and stylish circular close button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        text = track.name,
                        color = titleColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Light,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.artist,
                        color = subtitleColor,
                        fontSize = 13.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isLightBg) Color.White.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.14f))
                        .border(1.dp, titleColor.copy(alpha = 0.18f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Close lyrics",
                        tint = closeButtonTint,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Body: Loading, Empty, or Enhanced Lyrics List
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = activeHighlightColor,
                            modifier = Modifier.size(38.dp),
                            strokeWidth = 3.dp
                        )
                    }
                }
                lyrics == null || lyrics.lines.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = "Couldn't find lyrics for this track",
                                color = subtitleColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                else -> {
                    val isSynced = lyrics.syncType == "LINE_SYNCED"
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        itemsIndexed(lyrics.lines) { index, line ->
                            val text = line.words.ifEmpty { "♪" }
                            val isActive = index == activeIndex
                            val targetColor = when {
                                !isSynced -> activeHighlightColor.copy(alpha = 0.9f)
                                isActive -> activeHighlightColor
                                else -> inactiveTextColor
                            }
                            val color by animateColorAsState(targetValue = targetColor, label = "lyricTextColor")
                            val scale by animateFloatAsState(
                                targetValue = if (isActive && isSynced) 1.06f else 1.0f,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
                                label = "lyricScale"
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        transformOrigin = TransformOrigin(0f, 0.5f)
                                    }
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        enabled = isSynced
                                    ) {
                                        onSeekTo(line.startTimeMs)
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = text,
                                    color = color,
                                    fontSize = 27.sp,
                                    fontWeight = if (isActive) FontWeight.Medium else FontWeight.Light,
                                    lineHeight = 38.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(28.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = activeHighlightColor.copy(alpha = 0.08f),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Text(
                                    text = "Lyrics provided by ${lyrics.providerDisplayName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = subtitleColor,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
