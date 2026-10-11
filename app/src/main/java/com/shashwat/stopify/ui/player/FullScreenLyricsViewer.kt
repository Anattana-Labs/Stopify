package com.shashwat.stopify.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shashwat.stopify.data.SpotifyColorLyrics
import com.shashwat.stopify.data.SpotifyTrack

/**
 * Full-screen synced lyrics overlay (Spotui / Spotify design).
 * Displays full lyrics with immersive vertical gradient, auto-scroll centering on active line,
 * clickable jump-to-time seeking, and clear typography.
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
    val bgColor = remember(lyrics?.backgroundColor) {
        lyrics?.let { argbToColor(it.backgroundColor) } ?: Color(0xFF1E3264)
    }
    val highlightColor = remember(lyrics?.highlightTextColor) {
        lyrics?.let { argbToColor(it.highlightTextColor) } ?: Color.White
    }

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
            .background(Color(0xFF121212))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        bgColor,
                        bgColor.copy(alpha = 0.65f),
                        Color(0xFF121212)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Track title, artist, and close button (Spotui style)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        text = "LYRICS",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.name,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Close lyrics",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Body: Loading, Empty, or Lyrics List
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(36.dp))
                    }
                }
                lyrics == null || lyrics.lines.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Couldn't find lyrics for this track",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp
                        )
                    }
                }
                else -> {
                    val isSynced = lyrics.syncType == "LINE_SYNCED"
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        itemsIndexed(lyrics.lines) { index, line ->
                            val text = line.words.ifEmpty { "♪" }
                            val isActive = index == activeIndex
                            val targetColor = when {
                                !isSynced -> Color.White.copy(alpha = 0.9f)
                                isActive -> highlightColor
                                else -> highlightColor.copy(alpha = 0.4f)
                            }
                            val color by animateColorAsState(targetValue = targetColor, label = "lyricTextColor")

                            Text(
                                text = text,
                                color = color,
                                fontSize = if (isActive) 26.sp else 23.sp,
                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                                lineHeight = 34.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        enabled = isSynced
                                    ) {
                                        onSeekTo(line.startTimeMs)
                                    }
                                    .padding(vertical = 4.dp)
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Lyrics provided by ${lyrics.providerDisplayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
