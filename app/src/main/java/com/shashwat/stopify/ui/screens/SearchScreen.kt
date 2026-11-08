package com.shashwat.stopify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import com.shashwat.stopify.ui.components.PrismChip
import com.shashwat.stopify.ui.components.ScreenHeader
import com.shashwat.stopify.ui.theme.PrismTokens
import com.shashwat.stopify.ui.theme.glassFill
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shashwat.stopify.data.SpotifyItemType
import com.shashwat.stopify.data.SpotifyMediaItem
import com.shashwat.stopify.data.SpotifyTrack
import com.shashwat.stopify.player.PlaybackState
import com.shashwat.stopify.ui.components.M3ExpressiveShimmerTrack
import com.shashwat.stopify.ui.components.SimpArtistCircleCard
import com.shashwat.stopify.ui.components.SimpSquareCard
import com.shashwat.stopify.ui.components.SimpTrackItem
import com.shashwat.stopify.ui.components.TrackOptionsMenu

enum class SearchFilter(val label: String) {
    ALL("All"),
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    PLAYLISTS("Playlists")
}

data class BrowseCategory(val title: String, val gradientColors: List<Color>)

private val BrowseCategories = listOf(
    BrowseCategory("Pop", listOf(Color(0xFFB52A40), Color(0xFF76182B))),
    BrowseCategory("Hip-Hop", listOf(Color(0xFFB3560E), Color(0xFF78380A))),
    BrowseCategory("Rock", listOf(Color(0xFF8A6A00), Color(0xFF5C4600))),
    BrowseCategory("Indie", listOf(Color(0xFF12875A), Color(0xFF0B5A3C))),
    BrowseCategory("Chill", listOf(Color(0xFF1673B0), Color(0xFF0D4B78))),
    BrowseCategory("Dance / Electronic", listOf(Color(0xFF0E8A8A), Color(0xFF075A66))),
    BrowseCategory("Workout", listOf(Color(0xFF5B3FD6), Color(0xFF3A2699))),
    BrowseCategory("Focus", listOf(Color(0xFF7A3FB5), Color(0xFF4C2377)))
)

@Composable
fun SimpSearchScreen(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    searchResults: List<SpotifyMediaItem>,
    isSearching: Boolean,
    playbackState: PlaybackState,
    onMediaItemClick: (SpotifyMediaItem) -> Unit,
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
    val focusManager = LocalFocusManager.current
    var selectedOptionTrack by remember { mutableStateOf<SpotifyTrack?>(null) }
    var selectedFilter by remember { mutableStateOf(SearchFilter.ALL) }

    val artists = remember(searchResults) { searchResults.filter { it.type == SpotifyItemType.ARTIST } }
    val albums = remember(searchResults) { searchResults.filter { it.type == SpotifyItemType.ALBUM } }
    val playlists = remember(searchResults) { searchResults.filter { it.type == SpotifyItemType.PLAYLIST } }
    val songs = remember(searchResults) { searchResults.filter { it.type == SpotifyItemType.TRACK || it.uri.contains(":track:") } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PrismTokens.Space.gutter)
        ) {
            ScreenHeader(title = "Search", start = 0.dp, end = 0.dp)

            Spacer(modifier = Modifier.height(6.dp))

            // Search text field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    onQueryChange(it)
                    if (it.length >= 2) onSearch(it)
                },
                placeholder = { Text("What do you want to listen to?", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            onQueryChange("")
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                },
                singleLine = true,
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = glassFill(2f),
                    unfocusedContainerColor = glassFill(1.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Filter Chips Row (when results available)
            if (searchResults.isNotEmpty()) {
                val searchPrimary = MaterialTheme.colorScheme.primary
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    items(SearchFilter.values()) { filter ->
                        PrismChip(
                            selected = selectedFilter == filter,
                            label = filter.label,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (isSearching) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(6) {
                        M3ExpressiveShimmerTrack()
                    }
                }
            } else if (searchResults.isEmpty()) {
                // Spotify "Browse all" Categories
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Browse all",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (i in BrowseCategories.indices step 2) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    val cat1 = BrowseCategories[i]
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(76.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Brush.linearGradient(cat1.gradientColors))
                                            .clickable {
                                                onQueryChange(cat1.title)
                                                onSearch(cat1.title)
                                            }
                                            .padding(12.dp),
                                        contentAlignment = Alignment.TopStart
                                    ) {
                                        Text(
                                            text = cat1.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White
                                        )
                                    }

                                    if (i + 1 < BrowseCategories.size) {
                                        val cat2 = BrowseCategories[i + 1]
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(76.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Brush.linearGradient(cat2.gradientColors))
                                                .clickable {
                                                    onQueryChange(cat2.title)
                                                    onSearch(cat2.title)
                                                }
                                                .padding(12.dp),
                                            contentAlignment = Alignment.TopStart
                                        ) {
                                            Text(
                                                text = cat2.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = Color.White
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 180.dp)
                ) {
                    // 1. Artists Section
                    if ((selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.ARTISTS) && artists.isNotEmpty()) {
                        item {
                            Text(
                                text = "Artists",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                items(artists) { artist ->
                                    SimpArtistCircleCard(
                                        item = artist,
                                        onClick = { onMediaItemClick(artist) }
                                    )
                                }
                            }
                        }
                    }

                    // 2. Albums Section
                    if ((selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.ALBUMS) && albums.isNotEmpty()) {
                        item {
                            Text(
                                text = "Albums",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                items(albums) { album ->
                                    SimpSquareCard(
                                        item = album,
                                        onClick = { onMediaItemClick(album) }
                                    )
                                }
                            }
                        }
                    }

                    // 3. Playlists Section
                    if ((selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.PLAYLISTS) && playlists.isNotEmpty()) {
                        item {
                            Text(
                                text = "Playlists",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                items(playlists) { playlist ->
                                    SimpSquareCard(
                                        item = playlist,
                                        onClick = { onMediaItemClick(playlist) }
                                    )
                                }
                            }
                        }
                    }

                    // 4. Songs Section
                    if ((selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.SONGS) && songs.isNotEmpty()) {
                        item {
                            Text(
                                text = "Songs",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        items(songs) { songItem ->
                            val track = songItem.toTrack()
                            val isCurrent = playbackState.currentTrack?.uri == track.uri
                            SimpTrackItem(
                                track = track,
                                isPlaying = isCurrent && playbackState.isPlaying,
                                isCurrentTrack = isCurrent,
                                onClick = { onPlayTrack(track) },
                                onMoreClick = { selectedOptionTrack = track }
                            )
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
                onViewArtist = {
                    val art = optTrack.artistUri ?: if (optTrack.artist.isNotBlank()) "spotify:artist:${optTrack.artist}" else null
                    if (art != null) onViewArtist(art, optTrack.artist)
                },
                onToggleFavorite = { onToggleFavorite(optTrack) },
                isFavorite = isFavorite(optTrack)
            )
        }
    }
}
