package com.shashwat.stopify.ui.screens

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shashwat.stopify.data.SpotifyPlaylistDetails
import com.shashwat.stopify.data.SpotifyTrack
import com.shashwat.stopify.data.SpotifyWebClient
import com.shashwat.stopify.player.PlaybackState
import com.shashwat.stopify.ui.components.LiquidGlassIconButton
import com.shashwat.stopify.ui.components.M3ExpressiveShimmerTrack
import com.shashwat.stopify.ui.components.SearchAndAddTracksDialog
import com.shashwat.stopify.ui.components.SimpTrackItem
import com.shashwat.stopify.ui.components.TrackOptionsMenu
import kotlinx.coroutines.launch

@Composable
fun SimpPlaylistScreen(
    playlistUri: String,
    client: SpotifyWebClient,
    playbackState: PlaybackState,
    onBack: () -> Unit,
    onPlayTrack: (SpotifyTrack) -> Unit,
    onPlayNext: (SpotifyTrack) -> Unit,
    onAddToQueue: (SpotifyTrack) -> Unit,
    onAddToPlaylist: (SpotifyTrack) -> Unit,
    onViewAlbum: (String) -> Unit,
    onViewArtist: (artistUri: String, artistName: String) -> Unit = { _, _ -> },
    onToggleFavorite: (SpotifyTrack) -> Unit = {},
    isFavorite: (SpotifyTrack) -> Boolean = { false },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("stopify_prefs", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    var playlistDetail by remember { mutableStateOf<SpotifyPlaylistDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedOptionTrack by remember { mutableStateOf<SpotifyTrack?>(null) }
    var showAddSongsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val isCustomPlaylist = remember(playlistUri) { playlistUri.contains("local_") }

    fun reloadDetails() {
        coroutineScope.launch {
            val res = client.fetchPlaylistDetails(playlistUri, prefs)
            if (res.isSuccess) {
                playlistDetail = res.getOrThrow()
            }
        }
    }

    LaunchedEffect(playlistUri) {
        isLoading = true
        val res = client.fetchPlaylistDetails(playlistUri, prefs)
        if (res.isSuccess) {
            playlistDetail = res.getOrThrow()
        }
        isLoading = false
    }

    val isThisPlaying = playlistDetail?.tracks?.any { it.uri == playbackState.currentTrack?.uri } == true && playbackState.isPlaying

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .statusBarsPadding()
    ) {
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LiquidGlassIconButton(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack
                )
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .align(Alignment.CenterHorizontally)
                        .background(Color(0xFF282828))
                )
                repeat(5) {
                    M3ExpressiveShimmerTrack()
                }
            }
        } else if (playlistDetail != null) {
            val playlist = playlistDetail!!

            // Ambient background glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1ED760).copy(alpha = 0.25f),
                                Color(0xFF181818).copy(alpha = 0.5f),
                                Color(0xFF121212)
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
                        LiquidGlassIconButton(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            onClick = onBack
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCustomPlaylist) {
                                IconButton(
                                    onClick = { showDeleteConfirmDialog = true },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.1f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Delete Playlist",
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
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
                        // Hero Cover
                        Box(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .size(230.dp)
                                .shadow(24.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.6f))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF242424))
                        ) {
                            if (!playlist.coverUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = playlist.coverUrl,
                                    contentDescription = playlist.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MusicNote,
                                        contentDescription = null,
                                        tint = Color(0xFF1ED760),
                                        modifier = Modifier.size(64.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Playlist Title
                        Text(
                            text = playlist.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Owner / Description
                        val ownerText = playlist.ownerName
                        val descText = playlist.description ?: ""
                        Text(
                            text = if (descText.isNotBlank()) descText else "By $ownerText",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Song Count Metadata
                        Text(
                            text = "${playlist.tracks.size} songs",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF1ED760),
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Row: [Shuffle] [Play] [+ Add Songs]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Shuffle Button
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .clickable {
                                        playlist.tracks.shuffled().firstOrNull()?.let { onPlayTrack(it) }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Big Spotify Green Play Button
                            Box(
                                modifier = Modifier
                                    .height(48.dp)
                                    .widthIn(min = 130.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1ED760))
                                    .clickable {
                                        playlist.tracks.firstOrNull()?.let { onPlayTrack(it) }
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

                            // Add Songs to playlist button
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .clickable { showAddSongsDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Add Songs",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                if (playlist.tracks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "This playlist is empty",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showAddSongsDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1ED760)),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Songs", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Track list
                    itemsIndexed(playlist.tracks) { index, track ->
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
                onViewArtist = {
                    val art = optTrack.artistUri ?: if (optTrack.artist.isNotBlank()) "spotify:artist:${optTrack.artist}" else null
                    if (art != null) onViewArtist(art, optTrack.artist)
                },
                onToggleFavorite = { onToggleFavorite(optTrack) },
                isFavorite = isFavorite(optTrack)
            )
        }

        // Add Songs Dialog
        if (showAddSongsDialog && playlistDetail != null) {
            SearchAndAddTracksDialog(
                playlistId = playlistDetail!!.id,
                playlistName = playlistDetail!!.name,
                client = client,
                prefs = prefs,
                onDismiss = { showAddSongsDialog = false },
                onTrackAdded = {
                    reloadDetails()
                }
            )
        }

        // Delete Playlist Confirmation Dialog
        if (showDeleteConfirmDialog && playlistDetail != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Playlist") },
                text = { Text("Are you sure you want to delete '${playlistDetail!!.name}'?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                client.deletePlaylist(playlistDetail!!.id, prefs)
                                showDeleteConfirmDialog = false
                                onBack()
                            }
                        }
                    ) {
                        Text("Delete", color = Color(0xFFFF5252))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
