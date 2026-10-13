package com.shashwat.stopify.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shashwat.stopify.data.SpotifyPlaylistSummary
import com.shashwat.stopify.data.SpotifyTrack
import com.shashwat.stopify.data.SpotifyUserProfile
import com.shashwat.stopify.data.SpotifyWebClient
import com.shashwat.stopify.player.PlaybackState
import com.shashwat.stopify.ui.components.CreatePlaylistDialog
import com.shashwat.stopify.ui.components.SimpTrackItem
import com.shashwat.stopify.ui.components.TrackOptionsMenu
import kotlinx.coroutines.launch

enum class LibraryFilter(val label: String) {
    ALL("All"),
    PLAYLISTS("Playlists"),
    LIKED("Liked Songs"),
    ARTISTS("Artists"),
    RECENTS("Recently Played")
}

enum class LibrarySort(val label: String) {
    RECENTS("Recently Played"),
    NAME("Alphabetical"),
    CREATOR("Creator")
}

enum class LibraryViewMode {
    LIST,
    GRID
}

data class LibraryArtistItem(
    val uri: String,
    val name: String,
    val coverUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
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
    userProfile: SpotifyUserProfile? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("stopify_prefs", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    var playlists by remember { mutableStateOf<List<SpotifyPlaylistSummary>>(emptyList()) }
    var isLoadingPlaylists by remember { mutableStateOf(false) }
    var selectedOptionTrack by remember { mutableStateOf<SpotifyTrack?>(null) }
    var selectedOptionPlaylist by remember { mutableStateOf<SpotifyPlaylistSummary?>(null) }
    var playlistToDelete by remember { mutableStateOf<SpotifyPlaylistSummary?>(null) }
    var spotifyRecents by remember { mutableStateOf<List<SpotifyTrack>>(emptyList()) }
    var likedTracksCount by remember { mutableIntStateOf(0) }

    // UI state
    var selectedFilter by remember { mutableStateOf(LibraryFilter.ALL) }
    var selectedSort by remember { mutableStateOf(LibrarySort.RECENTS) }
    var viewMode by remember { mutableStateOf(LibraryViewMode.LIST) }
    var showSortMenu by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    val accentGreen = MaterialTheme.colorScheme.primary

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

    // Merge session playback history with Spotify remote recents
    val displayRecents = remember(playbackHistory.toList(), spotifyRecents) {
        (playbackHistory + spotifyRecents).distinctBy { it.uri }
    }

    // Derive artists from recent tracks & playlists
    val displayArtists = remember(displayRecents) {
        displayRecents
            .filter { it.artist.isNotBlank() }
            .distinctBy { it.artist.lowercase() }
            .map {
                LibraryArtistItem(
                    uri = it.artistUri ?: "spotify:artist:${it.id}",
                    name = it.artist,
                    coverUrl = it.coverUrl
                )
            }
    }

    // Filter and sort playlists
    val filteredPlaylists = remember(playlists, searchQuery, selectedSort) {
        val filtered = if (searchQuery.isBlank()) {
            playlists
        } else {
            playlists.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.ownerName.contains(searchQuery, ignoreCase = true)
            }
        }
        when (selectedSort) {
            LibrarySort.NAME -> filtered.sortedBy { it.name.lowercase() }
            LibrarySort.CREATOR -> filtered.sortedBy { it.ownerName.lowercase() }
            LibrarySort.RECENTS -> filtered
        }
    }

    // Filter tracks
    val filteredRecents = remember(displayRecents, searchQuery) {
        if (searchQuery.isBlank()) {
            displayRecents
        } else {
            displayRecents.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true) ||
                        it.album.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Filter artists
    val filteredArtists = remember(displayArtists, searchQuery) {
        if (searchQuery.isBlank()) {
            displayArtists
        } else {
            displayArtists.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── TOP HEADER BAR (Spotui style) ────────────────────────
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "Your Library",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 22.sp,
                        modifier = Modifier.weight(1f),
                    )
                    // Search icon
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search library",
                        tint = if (isSearchExpanded) accentGreen else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(end = 14.dp)
                            .size(22.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                isSearchExpanded = !isSearchExpanded
                                if (!isSearchExpanded) searchQuery = ""
                            },
                    )
                    // Grid / list toggle
                    Icon(
                        imageVector = if (viewMode == LibraryViewMode.LIST) Icons.Rounded.GridView else Icons.Rounded.FormatListBulleted,
                        contentDescription = if (viewMode == LibraryViewMode.LIST) "Show as grid" else "Show as list",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(end = 14.dp)
                            .size(20.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                viewMode = if (viewMode == LibraryViewMode.LIST) LibraryViewMode.GRID else LibraryViewMode.LIST
                            }
                    )
                    // Account avatar
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onOpenSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userProfile?.avatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = userProfile!!.avatarUrl,
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Icon(Icons.Rounded.Person, contentDescription = "Account", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // ── EXPANDABLE IN-LIBRARY SEARCH FIELD ───────────────────
            item {
                AnimatedVisibility(
                    visible = isSearchExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 0.dp, horizontal = 0.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = accentGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Find in Your Library...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(accentGreen),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { searchQuery = "" }
                                )
                            }
                        }
                    }
                }
            }

            // ── SPOTIFY-STYLE FILTER PILL CHIPS ──────────────────────
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(LibraryFilter.values()) { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) accentGreen else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    selectedFilter = if (isSelected && filter != LibraryFilter.ALL) {
                                        LibraryFilter.ALL
                                    } else {
                                        filter
                                    }
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = filter.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected && filter != LibraryFilter.ALL) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Clear filter",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── SORTING ROW ───────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showSortMenu = true }
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SwapVert,
                                contentDescription = "Sort",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedSort.label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            LibrarySort.values().forEach { sortOption ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = sortOption.label,
                                            fontWeight = if (selectedSort == sortOption) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedSort == sortOption) accentGreen else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        selectedSort = sortOption
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ── PINNED LIKED SONGS (Shown when ALL or LIKED) ──────────
            if ((selectedFilter == LibraryFilter.ALL || selectedFilter == LibraryFilter.LIKED) && searchQuery.isBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.2f))
                            .clickable { onPlayCollection("spotify:user:library:tracks", "Liked Songs") }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Purple Gradient Square Heart
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF450AF5),
                                                Color(0xFF8E8EE5),
                                                accentGreen
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Favorite,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Liked Songs",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.PushPin,
                                        contentDescription = "Pinned",
                                        tint = accentGreen,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .rotate(45f)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Playlist • $likedTracksCount songs",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Quick Play button
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(accentGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = "Play Liked Songs",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── PLAYLISTS SECTION ────────────────────────────────────
            if (selectedFilter == LibraryFilter.ALL || selectedFilter == LibraryFilter.PLAYLISTS) {
                if (isLoadingPlaylists) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = accentGreen,
                                strokeWidth = 2.5.dp
                            )
                        }
                    }
                } else if (filteredPlaylists.isEmpty() && selectedFilter == LibraryFilter.PLAYLISTS) {
                    item {
                        EmptyLibraryState(
                            title = if (searchQuery.isNotEmpty()) "No results found" else "No playlists yet",
                            subtitle = if (searchQuery.isNotEmpty()) "Try searching with a different keyword" else "Create your first custom playlist to get started",
                            actionLabel = if (searchQuery.isNotEmpty()) null else "Create Playlist",
                            onAction = { showCreatePlaylistDialog = true }
                        )
                    }
                } else {
                    if (viewMode == LibraryViewMode.LIST) {
                        // ── LIST VIEW MODE ───────────────────────────
                        items(filteredPlaylists) { playlist ->
                            PlaylistListItem(
                                playlist = playlist,
                                accentColor = accentGreen,
                                onClick = { onPlayCollection("spotify:playlist:${playlist.id}", playlist.name) },
                                onMoreClick = { selectedOptionPlaylist = playlist }
                            )
                        }
                    } else {
                        // ── GRID VIEW MODE (2-column layout) ─────────
                        val chunked = filteredPlaylists.chunked(2)
                        items(chunked) { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                pair.forEach { playlist ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        PlaylistGridCard(
                                            playlist = playlist,
                                            accentColor = accentGreen,
                                            onClick = { onPlayCollection("spotify:playlist:${playlist.id}", playlist.name) },
                                            onMoreClick = { selectedOptionPlaylist = playlist }
                                        )
                                    }
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // ── ARTISTS SECTION (Shown when ALL or ARTISTS) ───────────
            if ((selectedFilter == LibraryFilter.ALL || selectedFilter == LibraryFilter.ARTISTS) && filteredArtists.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Artists",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${filteredArtists.size} artists",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(filteredArtists) { artistItem ->
                    ArtistListItem(
                        artist = artistItem,
                        accentColor = accentGreen,
                        onClick = { onViewArtist(artistItem.uri, artistItem.name) }
                    )
                }
            }

            // ── RECENTLY PLAYED SECTION ──────────────────────────────
            if (selectedFilter == LibraryFilter.ALL || selectedFilter == LibraryFilter.RECENTS) {
                if (filteredRecents.isNotEmpty()) {
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
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Text(
                                text = "${filteredRecents.size} tracks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(filteredRecents) { histTrack ->
                        val isCurrent = playbackState.currentTrack?.uri == histTrack.uri
                        SimpTrackItem(
                            track = histTrack,
                            isPlaying = isCurrent && playbackState.isPlaying,
                            isCurrentTrack = isCurrent,
                            onClick = { onPlayTrack(histTrack) },
                            onMoreClick = { selectedOptionTrack = histTrack }
                        )
                    }
                } else if (selectedFilter == LibraryFilter.RECENTS) {
                    item {
                        EmptyLibraryState(
                            title = "No recently played tracks",
                            subtitle = "Songs you listen to will show up here",
                            actionLabel = null,
                            onAction = {}
                        )
                    }
                }
            }
        }

        // ── TRACK OPTIONS SHEET ──────────────────────────────────────
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

        // ── PLAYLIST OPTIONS BOTTOM SHEET ────────────────────────────
        if (selectedOptionPlaylist != null) {
            val pl = selectedOptionPlaylist!!
            ModalBottomSheet(
                onDismissRequest = { selectedOptionPlaylist = null },
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // Playlist Header Preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!pl.coverUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = pl.coverUrl,
                                    contentDescription = pl.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = accentGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pl.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Playlist • ${pl.ownerName} • ${pl.trackCount} songs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Play Collection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onPlayCollection("spotify:playlist:${pl.id}", pl.name)
                                selectedOptionPlaylist = null
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = accentGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Play Playlist",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Delete Playlist Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                playlistToDelete = pl
                                selectedOptionPlaylist = null
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Delete Playlist",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // ── DELETE PLAYLIST CONFIRMATION DIALOG ──────────────────────
        if (playlistToDelete != null) {
            AlertDialog(
                onDismissRequest = { playlistToDelete = null },
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                title = {
                    Text(
                        text = "Delete Playlist?",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete '${playlistToDelete!!.name}'? This cannot be undone.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val target = playlistToDelete!!
                            playlistToDelete = null
                            coroutineScope.launch {
                                client.deletePlaylist(target.id, prefs)
                                refreshData()
                            }
                        }
                    ) {
                        Text(
                            text = "Delete",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { playlistToDelete = null }) {
                        Text(
                            text = "Cancel",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }

        // ── CREATE PLAYLIST DIALOG ───────────────────────────────────
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

// ── COMPOSABLE: PLAYLIST LIST ITEM ───────────────────────────────────
@Composable
fun PlaylistListItem(
    playlist: SpotifyPlaylistSummary,
    accentColor: Color,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Square Artwork with 8dp rounded corners (not over-rounded)
        Box(
            modifier = Modifier
                .size(62.dp)
                .shadow(4.dp, RoundedCornerShape(8.dp), spotColor = Color.Black.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            if (!playlist.coverUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = playlist.coverUrl,
                    contentDescription = playlist.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Playlist • ${playlist.ownerName} • ${playlist.trackCount} tracks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── COMPOSABLE: PLAYLIST GRID CARD (2-COLUMN) ────────────────────────
@Composable
fun PlaylistGridCard(
    playlist: SpotifyPlaylistSummary,
    accentColor: Color,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            if (!playlist.coverUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = playlist.coverUrl,
                    contentDescription = playlist.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Quick Play Corner Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = playlist.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${playlist.trackCount} tracks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onMoreClick() }
            )
        }
    }
}

// ── COMPOSABLE: ARTIST LIST ITEM ─────────────────────────────────────
@Composable
fun ArtistListItem(
    artist: LibraryArtistItem,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular artist portrait
        Box(
            modifier = Modifier
                .size(58.dp)
                .shadow(4.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.2f))
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            if (!artist.coverUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = artist.coverUrl,
                    contentDescription = artist.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Artist",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── COMPOSABLE: EMPTY STATE ──────────────────────────────────────────
@Composable
fun EmptyLibraryState(
    title: String,
    subtitle: String,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (!actionLabel.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = actionLabel,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
