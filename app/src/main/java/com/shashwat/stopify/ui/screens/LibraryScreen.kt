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
import com.shashwat.stopify.ui.components.PrismSheet
import com.shashwat.stopify.ui.components.PrismChip
import com.shashwat.stopify.ui.components.RoundAction
import com.shashwat.stopify.ui.components.ScreenHeader
import com.shashwat.stopify.ui.theme.PaneShape
import com.shashwat.stopify.ui.theme.PrismTokens
import com.shashwat.stopify.ui.theme.glassEdge
import com.shashwat.stopify.ui.theme.glassFill
import com.shashwat.stopify.ui.theme.pane
import com.shashwat.stopify.ui.theme.spectrumBrush
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
import com.shashwat.stopify.ui.components.PrismButton
import coil.compose.AsyncImage
import com.shashwat.stopify.data.SpotifyPlaylistSummary
import com.shashwat.stopify.data.LibraryKind
import com.shashwat.stopify.data.DownloadState
import com.shashwat.stopify.data.SpotifyTrack
import com.shashwat.stopify.data.SpotifyUserProfile
import com.shashwat.stopify.data.SpotifyWebClient
import com.shashwat.stopify.data.StopifyDownloadManager
import com.shashwat.stopify.player.PlaybackState
import com.shashwat.stopify.ui.components.CreatePlaylistDialog
import com.shashwat.stopify.ui.components.SimpTrackItem
import com.shashwat.stopify.ui.components.TrackOptionsMenu
import kotlinx.coroutines.launch

enum class LibraryFilter(val label: String) {
    ALL("All"),
    RECENTS("Recents"),
    PLAYLISTS("Playlists"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    PODCASTS("Podcasts"),
    DOWNLOADED("Downloaded"),
    FOLDERS("Folders"),
    LIKED("Liked Songs")
}

enum class LibrarySort(val label: String) {
    RECENTS("Recently Played"),
    ADDED("Recently Added"),
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
            val res = client.fetchFullLibrary(prefs)
            if (res.isSuccess) {
                playlists = res.getOrThrow()
            }
            isLoadingPlaylists = false
        }
        coroutineScope.launch {
            val likedRes = client.fetchLikedSongs(prefs)
            if (likedRes.isSuccess) {
                likedTracksCount = likedRes.getOrThrow().size
            } else {
                likedTracksCount = client.favoriteTrackIds.size
            }
        }
        coroutineScope.launch {
            val recentsRes = client.fetchSpotifyRecentlyPlayed(prefs)
            if (recentsRes.isSuccess) {
                spotifyRecents = recentsRes.getOrThrow()
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

    // Filter and sort playlists & folders
    val filteredPlaylists = remember(playlists, searchQuery, selectedSort, selectedFilter) {
        val byFilter = when (selectedFilter) {
            LibraryFilter.PLAYLISTS -> playlists.filter { it.kind == LibraryKind.PLAYLIST }
            LibraryFilter.FOLDERS -> playlists.filter { it.kind == LibraryKind.FOLDER }
            LibraryFilter.ALBUMS -> playlists.filter { it.kind == LibraryKind.ALBUM }
            LibraryFilter.ARTISTS -> playlists.filter { it.kind == LibraryKind.ARTIST }
            LibraryFilter.PODCASTS -> playlists.filter { it.kind == LibraryKind.PODCAST || it.kind == LibraryKind.AUDIOBOOK }
            LibraryFilter.ALL -> playlists
            else -> emptyList()
        }
        val filtered = if (searchQuery.isBlank()) {
            byFilter
        } else {
            byFilter.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.ownerName.contains(searchQuery, ignoreCase = true)
            }
        }
        when (selectedSort) {
            LibrarySort.NAME -> filtered.sortedBy { it.name.lowercase() }
            LibrarySort.CREATOR -> filtered.sortedBy { it.ownerName.lowercase() }
            LibrarySort.ADDED -> filtered.sortedByDescending { it.addedAtMs }
            LibrarySort.RECENTS -> filtered.sortedByDescending { it.pinned }
        }
    }

    val recentTracks = remember(spotifyRecents, playbackHistory, searchQuery) {
        val merged = (playbackHistory + spotifyRecents).distinctBy { it.uri }
        if (searchQuery.isBlank()) merged else merged.filter {
            it.name.contains(searchQuery, true) || it.artist.contains(searchQuery, true)
        }
    }
    val downloadManager = remember { StopifyDownloadManager.getInstance(context) }
    val downloadJobs by downloadManager.jobs.collectAsState()
    val downloadedTracks by downloadManager.downloadedTracks.collectAsState()
    val filteredDownloads = remember(downloadedTracks, searchQuery) {
        if (searchQuery.isBlank()) {
            downloadedTracks
        } else {
            downloadedTracks.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true) ||
                        it.album.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = PrismTokens.Space.gutter, end = PrismTokens.Space.gutter, top = 0.dp, bottom = 200.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── TOP HEADER: Prism's light page title with round glass actions ──
            item {
                ScreenHeader(title = "Library", start = 0.dp, end = 0.dp) {
                    RoundAction(
                        Icons.Rounded.Search,
                        if (isSearchExpanded) "Close library search" else "Search library"
                    ) {
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) searchQuery = ""
                    }
                    RoundAction(
                        if (viewMode == LibraryViewMode.LIST) Icons.Rounded.GridView else Icons.Rounded.FormatListBulleted,
                        if (viewMode == LibraryViewMode.LIST) "Show as grid" else "Show as list"
                    ) {
                        viewMode = if (viewMode == LibraryViewMode.LIST) LibraryViewMode.GRID else LibraryViewMode.LIST
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(1.dp, glassEdge(), CircleShape)
                            .clickable(onClickLabel = "Open settings") { onOpenSettings() },
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
                            Icon(Icons.Rounded.Person, contentDescription = "Account", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
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
                            .heightIn(min = 48.dp)
                            .clip(CircleShape)
                            .background(glassFill(1.3f))
                            .border(1.dp, glassEdge(), CircleShape)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
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
                                        fontSize = 16.sp,
                                        fontFamily = com.shashwat.stopify.ui.theme.PrismFont
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
                        PrismChip(
                            selected = isSelected,
                            label = filter.label,
                            icon = if (isSelected && filter != LibraryFilter.ALL) Icons.Rounded.Close else null,
                            onClick = {
                                selectedFilter = if (isSelected && filter != LibraryFilter.ALL) {
                                    LibraryFilter.ALL
                                } else {
                                    filter
                                }
                            }
                        )
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
                                .clip(RoundedCornerShape(6.dp))
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pane(PaneShape)
                            .clickable { onPlayCollection("spotify:user:library:tracks", "Liked Songs") }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Liked songs: Prism's split-light tile
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(spectrumBrush(0f, 220f)),
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = "Play Liked Songs",
                                    tint = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── DOWNLOAD QUEUE with live progress ────────────────────
            if ((selectedFilter == LibraryFilter.ALL || selectedFilter == LibraryFilter.DOWNLOADED) && downloadJobs.isNotEmpty()) {
                item {
                    Text(
                        text = "Downloading · ${downloadJobs.size}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                items(downloadJobs, key = { "dl_" + it.track.id }) { job ->
                    DownloadJobRow(
                        job = job,
                        onCancel = { downloadManager.dismissJob(job.track.id) },
                        onRetry = { downloadManager.enqueue(job.track) }
                    )
                }
            }

            // ── RECENTS (everything played lately) ───────────────────
            if (selectedFilter == LibraryFilter.RECENTS) {
                if (recentTracks.isEmpty()) {
                    item {
                        EmptyLibraryState(
                            title = "Nothing played yet",
                            subtitle = "Songs you listen to will show up here",
                            actionLabel = null,
                            onAction = {}
                        )
                    }
                } else {
                    items(recentTracks, key = { "rc_" + it.uri }) { t ->
                        val isCurrent = playbackState.currentTrack?.uri == t.uri
                        SimpTrackItem(
                            track = t,
                            isPlaying = isCurrent && playbackState.isPlaying,
                            isCurrentTrack = isCurrent,
                            onClick = { onPlayTrack(t) },
                            onMoreClick = { selectedOptionTrack = t },
                            showCover = true
                        )
                    }
                }
            }

            // ── DOWNLOADED SONGS SECTION ─────────────────────────────
            if (selectedFilter == LibraryFilter.ALL || selectedFilter == LibraryFilter.DOWNLOADED) {
                if (filteredDownloads.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowDownward,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Downloaded",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            Text(
                                text = "${filteredDownloads.size} songs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(filteredDownloads) { dlTrack ->
                        val isCurrent = playbackState.currentTrack?.uri == dlTrack.uri
                        SimpTrackItem(
                            track = dlTrack,
                            isPlaying = isCurrent && playbackState.isPlaying,
                            isCurrentTrack = isCurrent,
                            onClick = { onPlayTrack(dlTrack) },
                            onMoreClick = { selectedOptionTrack = dlTrack },
                            showCover = true,
                            isDownloaded = true
                        )
                    }
                } else if (selectedFilter == LibraryFilter.DOWNLOADED) {
                    item {
                        EmptyLibraryState(
                            title = "No downloads yet",
                            subtitle = "Songs you download for offline playback will appear here",
                            actionLabel = null,
                            onAction = {}
                        )
                    }
                }
            }

            // ── PLAYLISTS & FOLDERS SECTION ──────────────────────────
            if (selectedFilter != LibraryFilter.DOWNLOADED && selectedFilter != LibraryFilter.RECENTS && selectedFilter != LibraryFilter.LIKED) {
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
                } else if (filteredPlaylists.isEmpty() && selectedFilter != LibraryFilter.ALL) {
                    item {
                        EmptyLibraryState(
                            title = if (searchQuery.isNotEmpty()) "No results found" else "Nothing here yet",
                            subtitle = if (searchQuery.isNotEmpty()) "Try searching with a different keyword" else "Save ${selectedFilter.label.lowercase()} on Spotify and they will appear here",
                            actionLabel = if (searchQuery.isNotEmpty() || selectedFilter != LibraryFilter.PLAYLISTS) null else "Create Playlist",
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
                                onClick = { onPlayCollection(playlist.uri, playlist.name) },
                                onMoreClick = { if (playlist.kind == LibraryKind.PLAYLIST) selectedOptionPlaylist = playlist else onPlayCollection(playlist.uri, playlist.name) }
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
                                            onClick = { onPlayCollection(playlist.uri, playlist.name) },
                                            onMoreClick = { if (playlist.kind == LibraryKind.PLAYLIST) selectedOptionPlaylist = playlist else onPlayCollection(playlist.uri, playlist.name) }
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
            PrismSheet(onDismiss = { selectedOptionPlaylist = null }) {
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
                                .clip(RoundedCornerShape(6.dp))
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
                            .clip(RoundedCornerShape(6.dp))
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
                            .clip(RoundedCornerShape(6.dp))
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
    val artShape = if (playlist.kind == LibraryKind.ARTIST) CircleShape else RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Square Artwork with 8dp rounded corners (not over-rounded)
        Box(
            modifier = Modifier
                .size(62.dp)
                .shadow(4.dp, artShape, spotColor = Color.Black.copy(alpha = 0.2f))
                .clip(artShape)
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
            } else if (playlist.isFolder) {
                Icon(
                    imageVector = Icons.Rounded.Folder,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(32.dp)
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
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            val sub = if (playlist.isFolder) {
                "Folder • ${if (playlist.trackCount > 0) "${playlist.trackCount} items" else "Spotify"}"
            } else {
                libraryKindSubtitle(playlist)
            }
            Text(
                text = sub,
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
                .clip(RoundedCornerShape(6.dp))
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
            } else if (playlist.isFolder) {
                Icon(
                    imageVector = Icons.Rounded.Folder,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(42.dp)
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
                text = libraryKindSubtitle(playlist),
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
            .clip(RoundedCornerShape(10.dp))
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
                PrismButton(
                    onClick = onAction,
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}


private fun libraryKindSubtitle(p: SpotifyPlaylistSummary): String = when (p.kind) {
    LibraryKind.FOLDER -> "Folder"
    LibraryKind.ARTIST -> "Artist"
    LibraryKind.ALBUM -> "Album" + if (p.ownerName.isNotBlank()) " • ${p.ownerName}" else ""
    LibraryKind.PODCAST -> "Podcast" + if (p.ownerName.isNotBlank()) " • ${p.ownerName}" else ""
    LibraryKind.AUDIOBOOK -> "Audiobook" + if (p.ownerName.isNotBlank()) " • ${p.ownerName}" else ""
    LibraryKind.PLAYLIST -> "Playlist • ${p.ownerName}" + if (p.trackCount > 0) " • ${p.trackCount} tracks" else ""
}

// ── COMPOSABLE: ONE QUEUED / RUNNING / FAILED DOWNLOAD ───────────────
@Composable
fun DownloadJobRow(job: com.shashwat.stopify.data.DownloadJob, onCancel: () -> Unit, onRetry: () -> Unit) {
    val failed = job.state == DownloadState.FAILED
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pane(PaneShape)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            if (!job.track.coverUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = job.track.coverUrl,
                    contentDescription = job.track.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Rounded.MusicNote, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                job.track.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = when (job.state) {
                    DownloadState.QUEUED -> "Waiting to download"
                    DownloadState.DOWNLOADING -> "Downloading ${(job.progress * 100).toInt()}%"
                    DownloadState.FAILED -> "Failed: ${job.error ?: "unknown error"}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!failed) {
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { job.progress },
                    modifier = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            }
        }
        if (failed) {
            IconButton(onClick = onRetry, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Retry download", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
        IconButton(onClick = onCancel, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Rounded.Close, contentDescription = "Cancel download", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
