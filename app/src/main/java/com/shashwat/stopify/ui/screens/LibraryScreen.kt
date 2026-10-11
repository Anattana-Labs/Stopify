package com.shashwat.stopify.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shashwat.stopify.data.SpotifyItemType
import com.shashwat.stopify.data.SpotifyMediaItem
import com.shashwat.stopify.data.SpotifyPlaylistSummary
import com.shashwat.stopify.data.SpotifyTrack
import com.shashwat.stopify.data.SpotifyWebClient
import com.shashwat.stopify.player.PlaybackState
import com.shashwat.stopify.ui.components.CreatePlaylistDialog
import com.shashwat.stopify.ui.components.SimpSquareCard
import com.shashwat.stopify.ui.components.SimpTrackItem
import com.shashwat.stopify.ui.components.TrackOptionsMenu
import kotlinx.coroutines.launch

enum class LibraryFilter(val label: String) {
    ALL("All"),
    PLAYLISTS("Playlists"),
    LIKED("Liked Songs"),
    RECENTS("Recently Played")
}

@Composable
fun SimpLibraryScreen(
    client: SpotifyWebClient,
    playbackHistory: List<SpotifyTrack>,
    playbackState: PlaybackState,
    onOpenSettings: () -> Unit,
    onPlayTrack: (SpotifyTrack) -> Unit,
    onPlayCollection: (uri: String, name: String) -> Unit,
    onPlayNext: (SpotifyTrack) -> Unit,
    onAddToQueue: (SpotifyTrack) -> Unit,
    onAddToPlaylist: (SpotifyTrack) -> Unit,
    onViewAlbum: (String) -> Unit,
    onViewArtist: (artistUri: String, artistName: String) -> Unit = { _, _ -> },
    onToggleFavorite: (SpotifyTrack) -> Unit = {},
    isFavorite: (SpotifyTrack) -> Boolean = { false },
    userProfile: com.shashwat.stopify.data.SpotifyUserProfile? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("stopify_prefs", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    var playlists by remember { mutableStateOf<List<SpotifyPlaylistSummary>>(emptyList()) }
    var isLoadingPlaylists by remember { mutableStateOf(false) }
    var selectedOptionTrack by remember { mutableStateOf<SpotifyTrack?>(null) }
    var spotifyRecents by remember { mutableStateOf<List<SpotifyTrack>>(emptyList()) }
    var likedTracksCount by remember { mutableIntStateOf(0) }
    var selectedFilter by remember { mutableStateOf(LibraryFilter.ALL) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var playlistToDelete by remember { mutableStateOf<SpotifyPlaylistSummary?>(null) }

    fun refreshData() {
        isLoadingPlaylists = true
        coroutineScope.launch {
            val res = client.fetchUserPlaylists(prefs)
            if (res.isSuccess) {
                playlists = res.getOrThrow()
            }
            isLoadingPlaylists = false
        }
        coroutineScope.launch {
            val recentsRes = client.fetchSpotifyRecentlyPlayed(prefs)
            if (recentsRes.isSuccess) {
                spotifyRecents = recentsRes.getOrThrow()
            }
        }
        coroutineScope.launch {
            val likedRes = client.fetchLikedSongs(prefs)
            if (likedRes.isSuccess) {
                likedTracksCount = likedRes.getOrThrow().size
            } else {
                likedTracksCount = client.favoriteTrackIds.size
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    LaunchedEffect(playbackState.currentTrack) {
        if (playbackState.currentTrack != null) {
            val recentsRes = client.fetchSpotifyRecentlyPlayed(prefs)
            if (recentsRes.isSuccess) {
                spotifyRecents = recentsRes.getOrThrow()
            }
        }
    }

    // Always merge session history with remote recents to guarantee immediate update
    val displayRecents = remember(playbackHistory.toList(), spotifyRecents) {
        (playbackHistory + spotifyRecents).distinctBy { it.uri }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .shadow(6.dp, CircleShape, spotColor = Color(0xFF1ED760).copy(alpha = 0.4f))
                                .clip(CircleShape)
                                .background(Color(0xFF242424))
                                .border(
                                    1.2.dp,
                                    Brush.linearGradient(listOf(Color(0xFF1ED760), Color(0xFF1DB954))),
                                    CircleShape
                                )
                                .clickable { onOpenSettings() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!userProfile?.avatarUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = userProfile!!.avatarUrl,
                                    contentDescription = "Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = (userProfile?.displayName?.firstOrNull() ?: 'S').uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1ED760),
                                    fontSize = 16.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Your Library",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Create Playlist Button
                        IconButton(
                            onClick = { showCreatePlaylistDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Create Playlist",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Spotify Filter Chips
            item {
                val libPrimary = MaterialTheme.colorScheme.primary
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(LibraryFilter.values()) { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) libPrimary else Color.White.copy(alpha = 0.1f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedFilter = filter }
                        ) {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Liked Songs Banner (Show if ALL or LIKED)
            if (selectedFilter == LibraryFilter.ALL || selectedFilter == LibraryFilter.LIKED) {
                item {
                    val libPrimary = MaterialTheme.colorScheme.primary
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E1E1E),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.4f))
                            .border(0.7.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .clickable { onPlayCollection("spotify:user:library:tracks", "Liked Songs") }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF450AF5),
                                                Color(0xFF8E8EE5),
                                                libPrimary
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Favorite,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Liked Songs",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (likedTracksCount > 0) "$likedTracksCount songs • Playlist" else "Favorite music",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(libPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Playlists Section
            if (selectedFilter == LibraryFilter.ALL || selectedFilter == LibraryFilter.PLAYLISTS) {
                item {
                    val libPrimary = MaterialTheme.colorScheme.primary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Playlists",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        TextButton(onClick = { showCreatePlaylistDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = libPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "New",
                                color = libPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (isLoadingPlaylists) {
                    item {
                        val libPrimary = MaterialTheme.colorScheme.primary
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = libPrimary
                            )
                        }
                    }
                } else if (playlists.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E1E1E),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCreatePlaylistDialog = true }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = null,
                                        tint = Color(0xFF1ED760),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Create your first playlist",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Tap here to build a custom mix",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.65f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            // "Create" card in row
                            item {
                                Column(
                                    modifier = Modifier
                                        .width(130.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showCreatePlaylistDialog = true }
                                        .padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(122.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF242424))
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = "New Playlist",
                                            tint = Color(0xFF1ED760),
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "New Playlist",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Custom",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            items(playlists) { pl ->
                                Column(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onPlayCollection("spotify:playlist:${pl.id}", pl.name) }
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(132.dp)
                                            .shadow(8.dp, RoundedCornerShape(10.dp), spotColor = Color.Black.copy(alpha = 0.4f))
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF242424))
                                    ) {
                                        val plPrimary = MaterialTheme.colorScheme.primary
                                        if (!pl.coverUrl.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = pl.coverUrl,
                                                contentDescription = pl.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.MusicNote,
                                                    contentDescription = null,
                                                    tint = plPrimary,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                            }
                                        }

                                        // Play circle button on card
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(6.dp)
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(plPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.PlayArrow,
                                                contentDescription = "Play",
                                                tint = Color.Black,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = pl.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "${pl.trackCount} tracks • ${pl.ownerName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.65f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recently Played Songs Section
            if (selectedFilter == LibraryFilter.ALL || selectedFilter == LibraryFilter.RECENTS) {
                if (displayRecents.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Recently Played",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = "${displayRecents.size} tracks",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }

                    items(displayRecents) { histTrack ->
                        val isCurrent = playbackState.currentTrack?.uri == histTrack.uri
                        SimpTrackItem(
                            track = histTrack,
                            isPlaying = isCurrent && playbackState.isPlaying,
                            isCurrentTrack = isCurrent,
                            onClick = { onPlayTrack(histTrack) },
                            onMoreClick = { selectedOptionTrack = histTrack }
                        )
                    }
                }
            }
        }

        // Track Options Bottom Sheet
        if (selectedOptionTrack != null) {
            TrackOptionsMenu(
                expanded = true,
                onDismissRequest = { selectedOptionTrack = null },
                track = selectedOptionTrack!!,
                onPlayNext = { onPlayNext(selectedOptionTrack!!) },
                onAddToQueue = { onAddToQueue(selectedOptionTrack!!) },
                onAddToPlaylist = { onAddToPlaylist(selectedOptionTrack!!) },
                onViewAlbum = selectedOptionTrack!!.albumUri?.let { albUri ->
                    { onViewAlbum(albUri) }
                },
                onViewArtist = selectedOptionTrack!!.artistUri?.let { artUri ->
                    { onViewArtist(artUri, selectedOptionTrack!!.artist) }
                },
                onToggleFavorite = { onToggleFavorite(selectedOptionTrack!!) },
                isFavorite = isFavorite(selectedOptionTrack!!)
            )
        }

        // Create Playlist Dialog
        if (showCreatePlaylistDialog) {
            CreatePlaylistDialog(
                client = client,
                prefs = prefs,
                onDismiss = { showCreatePlaylistDialog = false },
                onPlaylistCreated = { newPl ->
                    refreshData()
                    showCreatePlaylistDialog = false
                    onPlayCollection("spotify:playlist:${newPl.id}", newPl.name)
                }
            )
        }
    }
}
