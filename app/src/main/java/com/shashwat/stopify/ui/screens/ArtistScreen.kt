package com.shashwat.stopify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.shashwat.stopify.data.SpotifyItemType
import com.shashwat.stopify.data.SpotifyMediaItem
import com.shashwat.stopify.data.SpotifyTrack
import com.shashwat.stopify.data.SpotifyWebClient
import com.shashwat.stopify.player.PlaybackState
import com.shashwat.stopify.ui.components.M3ExpressiveShimmerTrack
import com.shashwat.stopify.ui.components.SimpSquareCard
import com.shashwat.stopify.ui.components.SimpTrackItem
import com.shashwat.stopify.ui.components.TrackOptionsMenu

@Composable
fun SimpArtistScreen(
    artistUri: String,
    artistName: String,
    artistCoverUrl: String?,
    client: SpotifyWebClient,
    playbackState: PlaybackState,
    onBack: () -> Unit,
    onPlayTrack: (SpotifyTrack) -> Unit,
    onPlayNext: (SpotifyTrack) -> Unit,
    onAddToQueue: (SpotifyTrack) -> Unit,
    onAddToPlaylist: (SpotifyTrack) -> Unit,
    onViewAlbum: (String) -> Unit,
    onToggleFavorite: (SpotifyTrack) -> Unit = {},
    isFavorite: (SpotifyTrack) -> Boolean = { false },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var topTracks by remember { mutableStateOf<List<SpotifyTrack>>(emptyList()) }
    var albums by remember { mutableStateOf<List<SpotifyMediaItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedOptionTrack by remember { mutableStateOf<SpotifyTrack?>(null) }
    var currentCoverUrl by remember(artistUri, artistCoverUrl) { mutableStateOf(artistCoverUrl) }
    var coverTint by remember(artistUri) { mutableStateOf<Color?>(null) }

    LaunchedEffect(artistUri, artistName) {
        isLoading = true
        // 1. Fetch avatar if not provided
        if (currentCoverUrl.isNullOrEmpty()) {
            val avatarRes = client.fetchArtistAvatar(artistUri, artistName)
            if (avatarRes.isSuccess && !avatarRes.getOrNull().isNullOrEmpty()) {
                currentCoverUrl = avatarRes.getOrNull()
            }
        }

        // 2. Fetch popular tracks
        val tracksRes = client.fetchArtistTopTracks(artistUri, artistName)
        if (tracksRes.isSuccess) {
            topTracks = tracksRes.getOrThrow()
            if (currentCoverUrl.isNullOrEmpty() && topTracks.isNotEmpty()) {
                currentCoverUrl = topTracks.firstOrNull()?.coverUrl
            }
        }

        // 3. Fetch albums
        val albumsRes = client.searchMulti(artistName, SpotifyItemType.ALBUM)
        if (albumsRes.isSuccess) {
            albums = albumsRes.getOrThrow().filter { it.type == SpotifyItemType.ALBUM }
        }
        isLoading = false
    }

    // Palette extraction for tint
    LaunchedEffect(currentCoverUrl) {
        if (!currentCoverUrl.isNullOrEmpty()) {
            try {
                val req = ImageRequest.Builder(context)
                    .data(currentCoverUrl)
                    .allowHardware(false)
                    .build()
                val result = context.imageLoader.execute(req)
                if (result is SuccessResult) {
                    val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                    if (bitmap != null) {
                        val palette = Palette.from(bitmap).generate()
                        val rgb = palette.vibrantSwatch?.rgb
                            ?: palette.dominantSwatch?.rgb
                            ?: palette.mutedSwatch?.rgb
                        if (rgb != null) {
                            coverTint = Color(rgb)
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback to theme primary
            }
        }
    }

    val dynamicTint = coverTint ?: MaterialTheme.colorScheme.primary
    val isThisPlaying = topTracks.any { it.uri == playbackState.currentTrack?.uri } && playbackState.isPlaying

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Ambient background glow using cover tint
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            dynamicTint.copy(alpha = 0.45f),
                            dynamicTint.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Top App Bar
            item {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
            }

            // Hero Circular Avatar & Info Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 180dp Circular Avatar
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .size(180.dp)
                            .shadow(24.dp, CircleShape, spotColor = dynamicTint.copy(alpha = 0.45f))
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        if (!currentCoverUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = currentCoverUrl,
                                contentDescription = artistName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(80.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Verified Badge + Name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Verified",
                            tint = dynamicTint,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Verified Artist",
                            style = MaterialTheme.typography.labelMedium,
                            color = dynamicTint
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = artistName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // SimpMusic Action Row: [Shuffle Circle] [Play Pill]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shuffle Button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .clickable {
                                    topTracks.shuffled().firstOrNull()?.let { onPlayTrack(it) }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = "Shuffle",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Play Pill Button
                        Box(
                            modifier = Modifier
                                 .height(48.dp)
                                 .widthIn(min = 130.dp)
                                 .clip(CircleShape)
                                 .background(dynamicTint)
                                 .clickable {
                                     topTracks.firstOrNull()?.let { onPlayTrack(it) }
                                 }
                                 .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isThisPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isThisPlaying) "Pause" else "Play",
                                    color = Color.Black,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }

            // Popular Tracks Header
            item {
                Text(
                    text = "Popular Tracks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            if (isLoading) {
                items(5) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        M3ExpressiveShimmerTrack()
                    }
                }
            } else {
                itemsIndexed(topTracks) { index, track ->
                    val isCurrent = playbackState.currentTrack?.uri == track.uri
                    SimpTrackItem(
                        track = track,
                        index = index + 1,
                        isPlaying = isCurrent && playbackState.isPlaying,
                        isCurrentTrack = isCurrent,
                        onClick = { onPlayTrack(track) },
                        onMoreClick = { selectedOptionTrack = track }
                    )
                }

                // Albums & Discography
                if (albums.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Albums & Singles",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp)
                        ) {
                            items(albums) { album ->
                                SimpSquareCard(
                                    item = album,
                                    onClick = { onViewAlbum(album.uri) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3-dot Options Menu
        selectedOptionTrack?.let { optTrack ->
            TrackOptionsMenu(
                expanded = true,
                onDismissRequest = { selectedOptionTrack = null },
                track = optTrack,
                onPlayNext = { onPlayNext(optTrack) },
                onAddToQueue = { onAddToQueue(optTrack) },
                onAddToPlaylist = { onAddToPlaylist(optTrack) },
                onViewAlbum = {
                    val alb = optTrack.albumUri ?: if (optTrack.album.isNotBlank()) "spotify:album:${optTrack.id}" else null
                    if (alb != null) onViewAlbum(alb)
                },
                onToggleFavorite = { onToggleFavorite(optTrack) },
                isFavorite = isFavorite(optTrack)
            )
        }
    }
}
