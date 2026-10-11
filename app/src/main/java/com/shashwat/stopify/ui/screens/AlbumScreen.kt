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
import androidx.compose.ui.unit.sp
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.shashwat.stopify.data.SpotifyAlbumDetails
import com.shashwat.stopify.data.SpotifyTrack
import com.shashwat.stopify.data.SpotifyWebClient
import com.shashwat.stopify.player.PlaybackState
import com.shashwat.stopify.ui.components.M3ExpressiveShimmerTrack
import com.shashwat.stopify.ui.components.SimpTrackItem
import com.shashwat.stopify.ui.components.TrackOptionsMenu

@Composable
fun SimpAlbumScreen(
    albumUri: String,
    client: SpotifyWebClient,
    playbackState: PlaybackState,
    onBack: () -> Unit,
    onPlayTrack: (SpotifyTrack) -> Unit,
    onPlayNext: (SpotifyTrack) -> Unit,
    onAddToQueue: (SpotifyTrack) -> Unit,
    onAddToPlaylist: (SpotifyTrack) -> Unit,
    onViewArtist: (artistUri: String, artistName: String) -> Unit,
    onToggleFavorite: (SpotifyTrack) -> Unit = {},
    isFavorite: (SpotifyTrack) -> Boolean = { false },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var albumDetail by remember { mutableStateOf<SpotifyAlbumDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedOptionTrack by remember { mutableStateOf<SpotifyTrack?>(null) }
    var isLiked by remember { mutableStateOf(false) }
    var coverTint by remember(albumUri) { mutableStateOf<Color?>(null) }

    LaunchedEffect(albumUri) {
        isLoading = true
        val res = client.fetchAlbumDetails(albumUri)
        if (res.isSuccess) {
            albumDetail = res.getOrThrow()
        }
        isLoading = false
    }

    LaunchedEffect(albumDetail?.coverUrl) {
        val coverUrl = albumDetail?.coverUrl
        if (!coverUrl.isNullOrEmpty()) {
            try {
                val req = ImageRequest.Builder(context)
                    .data(coverUrl)
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
    val isThisPlaying = albumDetail?.tracks?.any { it.uri == playbackState.currentTrack?.uri } == true && playbackState.isPlaying

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .align(Alignment.CenterHorizontally)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )
                repeat(5) {
                    M3ExpressiveShimmerTrack()
                }
            }
        } else if (albumDetail != null) {
            val album = albumDetail!!

            // Ambient background glow using extracted cover tint
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
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

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isLiked = !isLiked },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isLiked) dynamicTint else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // Hero Artwork & Info Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 240dp Hero Cover
                        Box(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .size(240.dp)
                                .shadow(24.dp, RoundedCornerShape(16.dp), spotColor = dynamicTint.copy(alpha = 0.45f))
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        ) {
                            if (!album.coverUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = album.coverUrl,
                                    contentDescription = album.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Album Title
                        Text(
                            text = album.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Artist Name (Clickable)
                        Text(
                            text = album.artist,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = dynamicTint,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.clickable {
                                onViewArtist("spotify:artist:${album.artist}", album.artist)
                            }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Year & Song Count Metadata
                        val yearText = album.releaseYear ?: ""
                        Text(
                            text = "${if (yearText.isNotBlank()) "$yearText • " else ""}${album.tracks.size} songs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Apple Music / SimpMusic Action Row: [Shuffle Circle] [Play Pill] [Download Circle]
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
                                        album.tracks.shuffled().firstOrNull()?.let { onPlayTrack(it) }
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
                                    .widthIn(min = 125.dp)
                                    .clip(CircleShape)
                                    .background(dynamicTint)
                                    .clickable {
                                        album.tracks.firstOrNull()?.let { onPlayTrack(it) }
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

                            // Share / More Button
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .clickable {
                                        // Optional action
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Track list
                itemsIndexed(album.tracks) { index, track ->
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
                onViewAlbum = null,
                onViewArtist = {
                    val artUri = optTrack.artistUri
                        ?: albumDetail?.tracks?.firstOrNull()?.artistUri
                        ?: if (optTrack.artist.isNotBlank()) "spotify:artist:${optTrack.artist}" else null
                    if (artUri != null) onViewArtist(artUri, optTrack.artist)
                },
                onToggleFavorite = { onToggleFavorite(optTrack) },
                isFavorite = isFavorite(optTrack)
            )
        }
    }
}
