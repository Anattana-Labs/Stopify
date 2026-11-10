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
import com.shashwat.stopify.ui.components.RoundAction
import androidx.compose.foundation.border
import androidx.compose.ui.semantics.Role
import com.shashwat.stopify.ui.theme.LocalDynamicColors
import com.shashwat.stopify.ui.theme.PrismTokens
import com.shashwat.stopify.ui.theme.glassEdge
import com.shashwat.stopify.ui.theme.glassFill
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.shashwat.stopify.data.SpotifyItemType
import com.shashwat.stopify.data.SpotifyMediaItem
import com.shashwat.stopify.data.SpotifyTrack
import com.shashwat.stopify.data.SpotifyWebClient
import com.shashwat.stopify.data.SpotifyArtistPage
import com.shashwat.stopify.data.fetchArtistPage
import com.shashwat.stopify.ui.components.SimpArtistCircleCard
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
    onPlayContext: (tracks: List<SpotifyTrack>, startIndex: Int, shuffle: Boolean) -> Unit = { l, i, _ -> l.getOrNull(i)?.let(onPlayTrack) },
    onTogglePlayback: () -> Unit = {},
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
    val scheme = MaterialTheme.colorScheme
    var page by remember(artistUri) { mutableStateOf<SpotifyArtistPage?>(null) }
    var isLoading by remember(artistUri) { mutableStateOf(true) }
    var failed by remember(artistUri) { mutableStateOf(false) }
    var reloadKey by remember(artistUri) { mutableIntStateOf(0) }
    var showAllTracks by remember(artistUri) { mutableStateOf(false) }
    var releaseFilter by remember(artistUri) { mutableStateOf("All") }
    var selectedOptionTrack by remember { mutableStateOf<SpotifyTrack?>(null) }
    var coverTint by remember(artistUri) { mutableStateOf<Color?>(null) }

    LaunchedEffect(artistUri, reloadKey) {
        isLoading = true
        failed = false
        val res = client.fetchArtistPage(artistUri)
        if (res.isSuccess) page = res.getOrThrow() else failed = page == null
        isLoading = false
    }

    val avatar = page?.avatarUrl ?: artistCoverUrl
    val name = page?.name ?: artistName
    val topTracks = page?.topTracks.orEmpty()

    LaunchedEffect(avatar) {
        if (!avatar.isNullOrEmpty()) {
            try {
                val req = ImageRequest.Builder(context).data(avatar).allowHardware(false).build()
                val result = context.imageLoader.execute(req)
                if (result is SuccessResult) {
                    val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                    if (bitmap != null) {
                        val palette = Palette.from(bitmap).generate()
                        val rgb = palette.vibrantSwatch?.rgb ?: palette.dominantSwatch?.rgb ?: palette.mutedSwatch?.rgb
                        if (rgb != null) coverTint = Color(rgb)
                    }
                }
            } catch (e: Exception) { }
        }
    }

    val tint = if (LocalDynamicColors.current) (coverTint ?: scheme.primary) else scheme.primary
    val isThisPlaying = topTracks.any { it.uri == playbackState.currentTrack?.uri } && playbackState.isPlaying

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        // Header image (or tinted wash) fading into the page
        Box(modifier = Modifier.fillMaxWidth().height(380.dp)) {
            val hero = page?.headerUrl
            if (!hero.isNullOrEmpty()) {
                AsyncImage(model = hero, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                tint.copy(alpha = if (hero.isNullOrEmpty()) 0.50f else 0.18f),
                                scheme.background.copy(alpha = 0.55f),
                                scheme.background
                            )
                        )
                    )
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 200.dp)
        ) {
            item {
                Box(Modifier.statusBarsPadding().padding(horizontal = PrismTokens.Space.gutter, vertical = 10.dp)) {
                    RoundAction(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onClick = onBack)
                }
            }

            // Identity
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .size(176.dp)
                            .shadow(24.dp, CircleShape, spotColor = tint.copy(alpha = 0.5f))
                            .clip(CircleShape)
                            .background(scheme.surfaceContainerHighest)
                    ) {
                        if (!avatar.isNullOrEmpty()) {
                            AsyncImage(model = avatar, contentDescription = name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        } else {
                            Icon(Icons.Rounded.Person, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(80.dp).align(Alignment.Center))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (page?.verified == true) "Verified artist" else "Artist",
                        style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = scheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    page?.monthlyListeners?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${"%,d".format(it)} monthly listeners",
                            style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp).clip(CircleShape)
                                .background(glassFill(1.6f)).border(1.dp, glassEdge(), CircleShape)
                                .clickable(enabled = topTracks.isNotEmpty(), role = Role.Button, onClickLabel = "Shuffle") {
                                    onPlayContext(topTracks, 0, true)
                                },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Rounded.Shuffle, "Shuffle", tint = scheme.onSurface, modifier = Modifier.size(22.dp)) }
                        Box(
                            modifier = Modifier
                                .height(48.dp).widthIn(min = 130.dp).clip(CircleShape)
                                .background(scheme.onSurface)
                                .clickable(enabled = topTracks.isNotEmpty()) {
                                    if (isThisPlaying) onTogglePlayback() else onPlayContext(topTracks, 0, false)
                                }
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (isThisPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = scheme.surface, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (isThisPlaying) "Pause" else "Play", color = scheme.surface, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                    Spacer(Modifier.height(28.dp))
                }
            }

            if (failed) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Couldn't load this artist", style = MaterialTheme.typography.titleMedium, color = scheme.onSurface)
                        Spacer(Modifier.height(12.dp))
                        Box(
                            Modifier.height(44.dp).clip(CircleShape).background(scheme.onSurface)
                                .clickable { reloadKey++ }.padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("Retry", color = scheme.surface, style = MaterialTheme.typography.labelLarge) }
                    }
                }
            }

            if (isLoading && page == null) {
                items(5) { Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) { M3ExpressiveShimmerTrack() } }
            }

            // Popular
            if (topTracks.isNotEmpty()) {
                item {
                    Text(
                        "Popular", style = MaterialTheme.typography.titleLarge, color = scheme.onSurface,
                        modifier = Modifier.padding(horizontal = PrismTokens.Space.gutter, vertical = 8.dp)
                    )
                }
                val shown = if (showAllTracks) topTracks else topTracks.take(5)
                itemsIndexed(shown) { index, track ->
                    val isCurrent = playbackState.currentTrack?.uri == track.uri
                    SimpTrackItem(
                        track = track,
                        index = index + 1,
                        showCover = true,
                        isPlaying = isCurrent && playbackState.isPlaying,
                        isCurrentTrack = isCurrent,
                        onClick = { onPlayContext(topTracks, index, false) },
                        onMoreClick = { selectedOptionTrack = track }
                    )
                }
                if (topTracks.size > 5) {
                    item {
                        Text(
                            if (showAllTracks) "Show less" else "Show more",
                            style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(horizontal = PrismTokens.Space.gutter, vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showAllTracks = !showAllTracks }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Discography
            val releases = page?.releases.orEmpty()
            if (releases.isNotEmpty()) {
                val kinds = listOf("Album", "Single", "Ep", "Compilation").filter { f ->
                    releases.any { it.subtitle.contains(f, ignoreCase = true) }
                }
                val filters = listOf("All") + kinds
                item {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Discography", style = MaterialTheme.typography.titleLarge, color = scheme.onSurface,
                        modifier = Modifier.padding(horizontal = PrismTokens.Space.gutter, vertical = 8.dp)
                    )
                    if (kinds.size > 1) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = PrismTokens.Space.gutter)
                        ) {
                            items(filters) { f ->
                                val sel = f == releaseFilter
                                Box(
                                    Modifier.height(36.dp).clip(CircleShape)
                                        .background(if (sel) scheme.onSurface else glassFill(1.6f))
                                        .clickable { releaseFilter = f }
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (f == "All") f else f + "s", style = MaterialTheme.typography.labelLarge,
                                        color = if (sel) scheme.surface else scheme.onSurface
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
                item {
                    val list = if (releaseFilter == "All") releases else releases.filter { it.subtitle.contains(releaseFilter, ignoreCase = true) }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(horizontal = PrismTokens.Space.gutter)
                    ) {
                        items(list, key = { it.uri }) { album -> SimpSquareCard(item = album, onClick = { onViewAlbum(album.uri) }) }
                    }
                }
            }

            val appearsOn = page?.appearsOn.orEmpty()
            if (appearsOn.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Appears on", style = MaterialTheme.typography.titleLarge, color = scheme.onSurface,
                        modifier = Modifier.padding(horizontal = PrismTokens.Space.gutter, vertical = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(horizontal = PrismTokens.Space.gutter)
                    ) {
                        items(appearsOn, key = { it.uri }) { album -> SimpSquareCard(item = album, onClick = { onViewAlbum(album.uri) }) }
                    }
                }
            }

            val related = page?.relatedArtists.orEmpty()
            if (related.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Fans also like", style = MaterialTheme.typography.titleLarge, color = scheme.onSurface,
                        modifier = Modifier.padding(horizontal = PrismTokens.Space.gutter, vertical = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(horizontal = PrismTokens.Space.gutter)
                    ) {
                        items(related, key = { it.uri }) { a -> SimpArtistCircleCard(item = a, onClick = { onViewArtist(a.uri, a.name) }) }
                    }
                }
            }

            page?.biography?.let { bio ->
                item {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "About", style = MaterialTheme.typography.titleLarge, color = scheme.onSurface,
                        modifier = Modifier.padding(horizontal = PrismTokens.Space.gutter, vertical = 8.dp)
                    )
                    var expanded by remember(artistUri) { mutableStateOf(false) }
                    Text(
                        bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        maxLines = if (expanded) Int.MAX_VALUE else 5,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(horizontal = PrismTokens.Space.gutter)
                            .clickable { expanded = !expanded }
                    )
                }
            }
        }

        selectedOptionTrack?.let { optTrack ->
            TrackOptionsMenu(
                expanded = true,
                onDismissRequest = { selectedOptionTrack = null },
                track = optTrack,
                onPlayNext = { onPlayNext(optTrack) },
                onAddToQueue = { onAddToQueue(optTrack) },
                onAddToPlaylist = { onAddToPlaylist(optTrack) },
                onViewAlbum = { optTrack.albumUri?.let { onViewAlbum(it) } },
                onToggleFavorite = { onToggleFavorite(optTrack) },
                isFavorite = isFavorite(optTrack)
            )
        }
    }
}
