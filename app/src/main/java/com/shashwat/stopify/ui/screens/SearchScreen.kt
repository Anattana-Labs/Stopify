package com.shashwat.stopify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
    BrowseCategory("Pop", listOf(Color(0xFF8D67AB), Color(0xFFE8115B))),
    BrowseCategory("Hip-Hop", listOf(Color(0xFFBA5D07), Color(0xFFBC5900))),
    BrowseCategory("Rock", listOf(Color(0xFFE91429), Color(0xFF8B0000))),
    BrowseCategory("Indie", listOf(Color(0xFF608108), Color(0xFF388E3C))),
    BrowseCategory("Chill", listOf(Color(0xFF477D95), Color(0xFF1976D2))),
    BrowseCategory("Dance / Electronic", listOf(Color(0xFF006450), Color(0xFF1ED760))),
    BrowseCategory("Workout", listOf(Color(0xFF7358FF), Color(0xFF4A148C))),
    BrowseCategory("Focus", listOf(Color(0xFF503750), Color(0xFF311B92)))
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
            .background(Color(0xFF0B0B0F))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Search",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search text field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    onQueryChange(it)
                    if (it.length >= 2) onSearch(it)
                },
                placeholder = { Text("What do you want to listen to?", color = Color.White.copy(alpha = 0.6f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            onQueryChange("")
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = Color.White)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF242424),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
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
                        val isSelected = selectedFilter == filter
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) searchPrimary else Color.White.copy(alpha = 0.08f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedFilter = filter }
                        ) {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
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
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Browse all",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
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
                                            .height(96.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Brush.linearGradient(cat1.gradientColors))
                                            .clickable {
                                                onQueryChange(cat1.title)
                                                onSearch(cat1.title)
                                            }
                                            .padding(14.dp),
                                        contentAlignment = Alignment.TopStart
                                    ) {
                                        Text(
                                            text = cat1.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    if (i + 1 < BrowseCategories.size) {
                                        val cat2 = BrowseCategories[i + 1]
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(96.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Brush.linearGradient(cat2.gradientColors))
                                                .clickable {
                                                    onQueryChange(cat2.title)
                                                    onSearch(cat2.title)
                                                }
                                                .padding(14.dp),
                                            contentAlignment = Alignment.TopStart
                                        ) {
                                            Text(
                                                text = cat2.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
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
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    contentPadding = PaddingValues(bottom = 140.dp)
                ) {
                    // 1. Artists Section
                    if ((selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.ARTISTS) && artists.isNotEmpty()) {
                        item {
                            Text(
                                text = "Artists",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
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
                                fontWeight = FontWeight.Bold,
                                color = Color.White
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
                                fontWeight = FontWeight.Bold,
                                color = Color.White
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
                                fontWeight = FontWeight.Bold,
                                color = Color.White
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
