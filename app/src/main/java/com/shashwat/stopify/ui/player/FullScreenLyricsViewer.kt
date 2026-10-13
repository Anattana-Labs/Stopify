package com.shashwat.stopify.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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

/**
 * Blend base color with theme primary and surface to provide a gorgeous theme tint.
 */
fun blendThemeTint(baseColor: Color, themePrimary: Color, themeSurface: Color, isDark: Boolean): Color {
    val blendedR = (baseColor.red * 0.68f + themePrimary.red * 0.32f).coerceIn(0f, 1f)
    val blendedG = (baseColor.green * 0.68f + themePrimary.green * 0.32f).coerceIn(0f, 1f)
    val blendedB = (baseColor.blue * 0.68f + themePrimary.blue * 0.32f).coerceIn(0f, 1f)
    val blended = Color(blendedR, blendedG, blendedB, 1f)

    return if (!isDark) {
        Color(
            (blended.red * 0.35f + themeSurface.red * 0.65f).coerceIn(0f, 1f),
            (blended.green * 0.35f + themeSurface.green * 0.65f).coerceIn(0f, 1f),
            (blended.blue * 0.35f + themeSurface.blue * 0.65f).coerceIn(0f, 1f),
            1f
        )
    } else {
        blended
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
    val themePrimary = MaterialTheme.colorScheme.primary
    val themeSurface = MaterialTheme.colorScheme.surface
    val defaultSurfaceFallback = MaterialTheme.colorScheme.surfaceContainerHighest
    val isDark = (themeSurface.red * 0.299f + themeSurface.green * 0.587f + themeSurface.blue * 0.114f) < 0.52f

    val defaultBgColor = if (isDark) Color(0xFF1E3264) else defaultSurfaceFallback
    val rawBgColor = remember(lyrics?.backgroundColor, defaultBgColor) {
        lyrics?.let { argbToColor(it.backgroundColor) } ?: defaultBgColor
    }

    val bgColor = remember(rawBgColor, themePrimary, themeSurface, isDark) {
        blendThemeTint(rawBgColor, themePrimary, themeSurface, isDark)
    }

    val isLightBg = (bgColor.red * 0.299f + bgColor.green * 0.587f + bgColor.blue * 0.114f) > 0.52f

    val activeHighlightColor = remember(lyrics?.highlightTextColor, themePrimary, isLightBg) {
        if (isLightBg) {
            themePrimary
        } else {
            val rawHighlight = lyrics?.let { argbToColor(it.highlightTextColor) } ?: Color.White
            Color(
                (rawHighlight.red * 0.55f + themePrimary.red * 0.45f).coerceIn(0f, 1f),
                (rawHighlight.green * 0.55f + themePrimary.green * 0.45f).coerceIn(0f, 1f),
                (rawHighlight.blue * 0.55f + themePrimary.blue * 0.45f).coerceIn(0f, 1f),
                1f
            )
        }
    }

    val inactiveTextColor = if (isLightBg) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    } else {
        activeHighlightColor.copy(alpha = 0.40f)
    }

    val titleColor = if (isLightBg) MaterialTheme.colorScheme.onSurface else Color.White
    val subtitleColor = if (isLightBg) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.75f)
    val closeButtonTint = if (isLightBg) MaterialTheme.colorScheme.onSurface else Color.White

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
                Brush.verticalGradient(
                    colors = listOf(
                        bgColor,
                        bgColor,
                        if (isDark) bgColor.copy(alpha = 0.94f) else bgColor
                    )
                )
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
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = themePrimary.copy(alpha = if (isLightBg) 0.14f else 0.22f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "LYRICS",
                            color = if (isLightBg) themePrimary else activeHighlightColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = track.name,
                        color = titleColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isLightBg) MaterialTheme.colorScheme.surfaceContainerHigh else Color.White.copy(alpha = 0.12f))
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
                            color = themePrimary,
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
                                !isSynced -> if (isLightBg) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.9f)
                                isActive -> activeHighlightColor
                                else -> inactiveTextColor
                            }
                            val color by animateColorAsState(targetValue = targetColor, label = "lyricTextColor")
                            val scale by animateFloatAsState(
                                targetValue = if (isActive && isSynced) 1.03f else 1.0f,
                                label = "lyricScale"
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
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
                                if (isSynced && isActive) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(28.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(themePrimary)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }

                                Text(
                                    text = text,
                                    color = color,
                                    fontSize = if (isActive) 26.sp else 23.sp,
                                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                                    lineHeight = 34.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(28.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = themePrimary.copy(alpha = 0.08f),
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
