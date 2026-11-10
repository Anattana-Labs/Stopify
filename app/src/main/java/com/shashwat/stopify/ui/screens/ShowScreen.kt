package com.shashwat.stopify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.shashwat.stopify.data.SpotifyEpisodePage
import com.shashwat.stopify.data.SpotifyShowDetails
import com.shashwat.stopify.data.SpotifyTrack
import com.shashwat.stopify.data.SpotifyWebClient
import com.shashwat.stopify.data.fetchShowDetails
import com.shashwat.stopify.data.fetchShowEpisodes
import com.shashwat.stopify.player.PlaybackState
import com.shashwat.stopify.ui.components.M3ExpressiveShimmerTrack
import com.shashwat.stopify.ui.components.RoundAction
import com.shashwat.stopify.ui.components.SimpTrackItem
import com.shashwat.stopify.ui.components.TrackOptionsMenu
import com.shashwat.stopify.ui.theme.PrismTokens

/** A podcast: cover, publisher, description and its episodes (tap one to play it, the rest follow). */
@Composable
fun SimpShowScreen(
    showUri: String,
    showName: String,
    showCoverUrl: String?,
    client: SpotifyWebClient,
    playbackState: PlaybackState,
    onBack: () -> Unit,
    onPlayContext: (tracks: List<SpotifyTrack>, startIndex: Int, shuffle: Boolean) -> Unit,
    onTogglePlayback: () -> Unit,
    onPlayNext: (SpotifyTrack) -> Unit,
    onAddToQueue: (SpotifyTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    var details by remember(showUri) { mutableStateOf<SpotifyShowDetails?>(null) }
    var episodes by remember(showUri) { mutableStateOf<List<SpotifyTrack>>(emptyList()) }
    var total by remember(showUri) { mutableIntStateOf(0) }
    var loading by remember(showUri) { mutableStateOf(true) }
    var loadingMore by remember(showUri) { mutableStateOf(false) }
    var failed by remember(showUri) { mutableStateOf(false) }
    var reload by remember(showUri) { mutableIntStateOf(0) }
    var expandedDesc by remember(showUri) { mutableStateOf(false) }
    var selected by remember { mutableStateOf<SpotifyTrack?>(null) }

    LaunchedEffect(showUri, reload) {
        loading = true
        failed = false
        val d = client.fetchShowDetails(showUri).getOrNull()
        details = d
        val page = client.fetchShowEpisodes(showUri, d?.name ?: showName, d?.coverUrl ?: showCoverUrl, 0, 50).getOrNull()
        if (page != null) { episodes = page.episodes; total = page.total } else failed = d == null
        loading = false
    }

    val cover = details?.coverUrl ?: showCoverUrl
    val isThisPlaying = episodes.any { it.uri == playbackState.currentTrack?.uri } && playbackState.isPlaying

    Box(modifier = modifier.fillMaxSize().background(scheme.background)) {
        Box(
            Modifier.fillMaxWidth().height(360.dp).background(
                Brush.verticalGradient(listOf(scheme.primary.copy(alpha = 0.30f), scheme.background))
            )
        )
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 200.dp)) {
            item {
                Box(Modifier.statusBarsPadding().padding(horizontal = PrismTokens.Space.gutter, vertical = 10.dp)) {
                    RoundAction(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onClick = onBack)
                }
            }
            item {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier
                            .size(196.dp)
                            .shadow(24.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(scheme.surfaceContainerHighest)
                    ) {
                        if (!cover.isNullOrEmpty()) {
                            AsyncImage(cover, details?.name ?: showName, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Podcast", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        details?.name ?: showName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = scheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    details?.publisher?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                    }
                    if (total > 0) {
                        Text("$total episodes", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(18.dp))
                    Box(
                        Modifier
                            .height(48.dp).widthIn(min = 150.dp).clip(CircleShape)
                            .background(scheme.onSurface)
                            .clickable(enabled = episodes.isNotEmpty()) {
                                if (isThisPlaying) onTogglePlayback() else onPlayContext(episodes, 0, false)
                            }
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (isThisPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = scheme.surface, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isThisPlaying) "Pause" else "Play latest", color = scheme.surface, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    details?.description?.let { desc ->
                        Spacer(Modifier.height(20.dp))
                        Text(
                            desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                            maxLines = if (expandedDesc) Int.MAX_VALUE else 4,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { expandedDesc = !expandedDesc }
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }

            item {
                Text(
                    "Episodes", style = MaterialTheme.typography.titleLarge, color = scheme.onSurface,
                    modifier = Modifier.padding(horizontal = PrismTokens.Space.gutter, vertical = 8.dp)
                )
            }
            if (loading && episodes.isEmpty()) {
                items4()
            }
            if (failed) {
                item {
                    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Couldn't load this podcast", style = MaterialTheme.typography.titleMedium, color = scheme.onSurface)
                        Spacer(Modifier.height(12.dp))
                        Box(
                            Modifier.height(44.dp).clip(CircleShape).background(scheme.onSurface)
                                .clickable { reload++ }.padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("Retry", color = scheme.surface, style = MaterialTheme.typography.labelLarge) }
                    }
                }
            }
            itemsIndexed(episodes, key = { _, e -> e.uri }) { index, ep ->
                val isCurrent = playbackState.currentTrack?.uri == ep.uri
                SimpTrackItem(
                    track = ep,
                    isPlaying = isCurrent && playbackState.isPlaying,
                    isCurrentTrack = isCurrent,
                    onClick = { onPlayContext(episodes, index, false) },
                    onMoreClick = { selected = ep }
                )
            }
            if (episodes.isNotEmpty() && episodes.size < total) {
                item {
                    LaunchedEffect(episodes.size) {
                        if (!loadingMore) {
                            loadingMore = true
                            val more = client.fetchShowEpisodes(
                                showUri, details?.name ?: showName, cover, episodes.size, 50
                            ).getOrNull()
                            if (more != null && more.episodes.isNotEmpty()) {
                                val known = episodes.map { it.uri }.toSet()
                                episodes = episodes + more.episodes.filter { it.uri !in known }
                            } else total = episodes.size
                            loadingMore = false
                        }
                    }
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        M3ExpressiveShimmerTrack()
                    }
                }
            }
        }

        selected?.let { ep ->
            TrackOptionsMenu(
                expanded = true,
                onDismissRequest = { selected = null },
                track = ep,
                onPlayNext = { onPlayNext(ep) },
                onAddToQueue = { onAddToQueue(ep) },
                onAddToPlaylist = { },
                onViewAlbum = null,
                onToggleFavorite = null,
                isFavorite = false
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.items4() {
    items(4) { Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) { M3ExpressiveShimmerTrack() } }
}
