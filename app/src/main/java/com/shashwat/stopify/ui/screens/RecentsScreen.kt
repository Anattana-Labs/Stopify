package com.shashwat.stopify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shashwat.stopify.data.SpotifyTrack
import com.shashwat.stopify.player.PlaybackState
import com.shashwat.stopify.ui.components.RoundAction
import com.shashwat.stopify.ui.components.SimpTrackItem
import com.shashwat.stopify.ui.components.SubPageHeader
import com.shashwat.stopify.ui.components.TrackOptionsMenu

/** Everything played lately, newest first: the page behind the arrow on Home's "Recently played". */
@Composable
fun SimpRecentsScreen(
    tracks: List<SpotifyTrack>,
    playbackState: PlaybackState,
    onBack: () -> Unit,
    onPlayTrack: (SpotifyTrack) -> Unit,
    onPlayAll: (List<SpotifyTrack>) -> Unit,
    onPlayNext: (SpotifyTrack) -> Unit,
    onAddToQueue: (SpotifyTrack) -> Unit,
    onAddToPlaylist: (SpotifyTrack) -> Unit,
    onViewAlbum: (String) -> Unit,
    onViewArtist: (artistUri: String, artistName: String) -> Unit,
    onToggleFavorite: (SpotifyTrack) -> Unit,
    isFavorite: (SpotifyTrack) -> Boolean,
    modifier: Modifier = Modifier
) {
    var optionTrack by remember { mutableStateOf<SpotifyTrack?>(null) }
    val now = remember(tracks) { System.currentTimeMillis() }
    val rows = remember(tracks, now) {
        val out = mutableListOf<RecentRow>()
        var last: String? = null
        tracks.forEach { t ->
            val label = dayLabel(t.playedAt, now)
            if (label != last) { out.add(RecentRow.Header(label)); last = label }
            out.add(RecentRow.Song(t))
        }
        out
    }
    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 200.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item(key = "header") {
                SubPageHeader(title = "Recently played", onBack = onBack) {
                    if (tracks.isNotEmpty()) RoundAction(Icons.Rounded.PlayArrow, "Play all recently played") { onPlayAll(tracks) }
                }
            }
            if (tracks.isEmpty()) {
                item(key = "empty") {
                    Text(
                        "Songs you play will be listed here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
            items(rows, key = { r -> when (r) { is RecentRow.Header -> "h_" + r.label; is RecentRow.Song -> "r_" + r.track.uri } }) { row ->
                if (row is RecentRow.Header) {
                    Text(
                        row.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.animateItem().padding(start = 10.dp, top = 18.dp, bottom = 6.dp)
                    )
                    return@items
                }
                val t = (row as RecentRow.Song).track
                val current = playbackState.currentTrack?.uri == t.uri
                SimpTrackItem(
                    modifier = Modifier.animateItem(),
                    track = t,
                    isPlaying = current && playbackState.isPlaying,
                    isCurrentTrack = current,
                    onClick = { onPlayTrack(t) },
                    onMoreClick = { optionTrack = t },
                    showCover = true
                )
            }
        }
        optionTrack?.let { t ->
            TrackOptionsMenu(
                expanded = true,
                onDismissRequest = { optionTrack = null },
                track = t,
                onPlayNext = { onPlayNext(t) },
                onAddToQueue = { onAddToQueue(t) },
                onAddToPlaylist = { onAddToPlaylist(t) },
                onViewAlbum = t.albumUri?.let { a -> { onViewAlbum(a) } },
                onViewArtist = t.artistUri?.let { a -> { onViewArtist(a, t.artist) } },
                onToggleFavorite = { onToggleFavorite(t) },
                isFavorite = isFavorite(t)
            )
        }
    }
}

private sealed interface RecentRow {
    data class Header(val label: String) : RecentRow
    data class Song(val track: SpotifyTrack) : RecentRow
}

/** "Today", "Yesterday", a weekday for the last week, then "Mar 4" / "Mar 4, 2024"; "Earlier" when the time is unknown. */
internal fun dayLabel(playedAt: Long, now: Long): String {
    if (playedAt <= 0L) return "Earlier"
    fun startOfDay(t: Long): Long = java.util.Calendar.getInstance().apply {
        timeInMillis = t
        set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis
    val today = startOfDay(now)
    val day = startOfDay(playedAt)
    val diffDays = java.util.Calendar.getInstance().let { c ->
        c.timeInMillis = today
        var n = 0
        while (c.timeInMillis > day && n < 4000) { c.add(java.util.Calendar.DAY_OF_YEAR, -1); n++ }
        n
    }
    return when {
        diffDays <= 0 -> "Today"
        diffDays == 1 -> "Yesterday"
        diffDays < 7 -> java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault()).format(java.util.Date(playedAt))
        else -> {
            val sameYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) ==
                java.util.Calendar.getInstance().apply { timeInMillis = playedAt }.get(java.util.Calendar.YEAR)
            java.text.SimpleDateFormat(if (sameYear) "MMM d" else "MMM d, yyyy", java.util.Locale.getDefault()).format(java.util.Date(playedAt))
        }
    }
}
