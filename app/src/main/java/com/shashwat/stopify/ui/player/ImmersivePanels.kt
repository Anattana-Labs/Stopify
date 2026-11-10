package com.shashwat.stopify.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shashwat.stopify.data.SpotifyLyricLine
import com.shashwat.stopify.data.SpotifyTrack

/** Lyrics in the player's middle: the line being sung is white and large, the rest dimmed. */
@Composable
internal fun ImmLyricsPanel(
    lines: List<SpotifyLyricLine>,
    loading: Boolean,
    positionMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (lines.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                if (loading) "Finding the words…" else "No lyrics for this track.",
                style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.6f)
            )
        }
        return
    }
    val active = remember(lines, positionMs / 300) { lines.indexOfLast { it.startTimeMs <= positionMs } }
    val state = rememberLazyListState()
    LaunchedEffect(active) {
        if (active >= 0) state.animateScrollToItem(active.coerceAtLeast(0), scrollOffset = -120)
    }
    LazyColumn(state = state, modifier = modifier, contentPadding = PaddingValues(vertical = 24.dp)) {
        itemsIndexed(lines) { i, line ->
            Text(
                text = line.words.ifBlank { "♪" },
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
                color = Color.White.copy(alpha = if (i == active) 1f else if (i < active) 0.35f else 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSeek(line.startTimeMs) }
                    .padding(vertical = 8.dp)
            )
        }
    }
}

/** The queue in the player's middle: what plays next, tap to jump, cross to remove. */
@Composable
internal fun ImmQueuePanel(
    queue: List<SpotifyTrack>,
    tint: Color,
    onPlay: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Up next", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White, modifier = Modifier.weight(1f)
            )
            if (queue.isNotEmpty()) Text(
                "Clear", style = MaterialTheme.typography.labelLarge, color = tint,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClear).padding(8.dp)
            )
        }
        if (queue.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Finding songs to play next…", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.6f))
            }
        } else LazyColumn(Modifier.fillMaxSize()) {
            itemsIndexed(queue, key = { i, t -> "${t.uri}#$i" }) { i, t ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onPlay(i) }.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = t.coverUrl, contentDescription = null, contentScale = ContentScale.Crop,
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp))
                    )
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(t.name, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge)
                        Text(t.artist, color = tint.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(
                        Icons.Rounded.Close, "Remove", tint = tint.copy(alpha = 0.7f),
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).clickable { onRemove(i) }.padding(8.dp)
                    )
                }
            }
        }
    }
}
