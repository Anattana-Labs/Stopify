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
            items(tracks, key = { "r_" + it.uri }) { t ->
                val current = playbackState.currentTrack?.uri == t.uri
                SimpTrackItem(
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
