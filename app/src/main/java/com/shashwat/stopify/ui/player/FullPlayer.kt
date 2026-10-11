package com.shashwat.stopify.ui.player

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shashwat.stopify.data.SpotifyColorLyrics
import com.shashwat.stopify.data.SpotifyTrack
import com.shashwat.stopify.data.SpotifyWebClient
import com.shashwat.stopify.player.PlaybackState
import com.shashwat.stopify.ui.components.SimpPlayerControlLayout
import com.shashwat.stopify.ui.components.TrackOptionsMenu
import kotlinx.coroutines.launch

fun argbToColor(argb: Int): Color {
    val r = ((argb shr 16) and 0xFF) / 255f
    val g = ((argb shr 8) and 0xFF) / 255f
    val b = (argb and 0xFF) / 255f
    return Color(red = r, green = g, blue = b, alpha = 1f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpFullPlayer(
    track: SpotifyTrack,
    playbackState: PlaybackState,
    client: SpotifyWebClient? = null,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onCollapse: () -> Unit,
    playbackQueue: List<SpotifyTrack> = emptyList(),
    onClearQueue: () -> Unit = {},
    onPlayTrackFromQueue: (Int) -> Unit = {},
    onMoveQueueItem: (from: Int, to: Int) -> Unit = { _, _ -> },
    onRemoveFromQueue: (Int) -> Unit = {},
    onAddToPlaylist: (SpotifyTrack) -> Unit = {},
    onViewAlbum: (String) -> Unit = {},
    onPlayNext: (SpotifyTrack) -> Unit = {},
    onAddToQueue: (SpotifyTrack) -> Unit = {},
    onViewArtist: (String, String) -> Unit = { _, _ -> },
    onToggleFavorite: (SpotifyTrack) -> Unit = {},
    isFavoriteTrack: Boolean = false,
    isFavorite: ((SpotifyTrack) -> Boolean)? = null,
    modifier: Modifier = Modifier
) {
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableFloatStateOf(0f) }
    var showQueueOverlay by remember { mutableStateOf(false) }
    var showTrackOptionsMenu by remember { mutableStateOf(false) }
    var showFullScreenLyrics by remember { mutableStateOf(false) }
    var isShuffle by remember { mutableStateOf(false) }
    var isRepeat by remember { mutableStateOf(false) }

    var colorLyrics by remember { mutableStateOf<SpotifyColorLyrics?>(null) }
    var isLyricsLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Fetch Spotify Color Lyrics
    LaunchedEffect(track.id, client) {
        if (client != null) {
            isLyricsLoading = true
            colorLyrics = null
            val res = client.fetchColorLyrics(track.id, track.coverUrl)
            colorLyrics = res.getOrNull()
            isLyricsLoading = false
        }
    }

    val durationMs = playbackState.durationMs
    val currentPositionMs = if (isScrubbing) {
        (scrubProgress * durationMs).toLong()
    } else {
        playbackState.currentPositionMs
    }

    val progressFraction = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val lyricsBgColor = remember(colorLyrics) {
        colorLyrics?.let { argbToColor(it.backgroundColor) }
    }

    val lyricsHighlightColor = remember(colorLyrics) {
        colorLyrics?.let { argbToColor(it.highlightTextColor) } ?: Color.White
    }

    val playerBackdropColor = Color(0xFF121212)
    val gradientTopColor = lyricsBgColor ?: Color(0xFF282828)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(playerBackdropColor)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        gradientTopColor,
                        playerBackdropColor
                    ),
                    startY = 0f,
                    endY = 1600f
                )
            )
    ) {
        val screenHeight = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
        ) {
            // Main Player (Screen 1 - SimpMusic classic Spotify player)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = screenHeight),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Top App Bar (SimpMusic Spotify layout)
                CenterAlignedTopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "PLAYING FROM " + if (track.album.isNotBlank()) "ALBUM" else "PLAYLIST",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f),
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = track.album.ifEmpty { "Stopify" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.basicMarquee()
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onCollapse,
                            modifier = Modifier.padding(start = 12.dp).size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Collapse",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showTrackOptionsMenu = true },
                            modifier = Modifier.padding(end = 12.dp).size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "Options",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                )

                // 2. Large Cover Artwork (SimpMusic Classic: 8dp subtle corners, big square)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .aspectRatio(1f)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(8.dp),
                            spotColor = Color.Black.copy(alpha = 0.5f),
                            ambientColor = Color.Transparent
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.DarkGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!track.coverUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = track.coverUrl,
                            contentDescription = track.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Album,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(96.dp)
                        )
                    }
                }

                // Inline Lyric Line (Between artwork and metadata - SimpMusic style)
                val activeLine = remember(colorLyrics, currentPositionMs) {
                    colorLyrics?.lines?.lastOrNull { it.startTimeMs <= currentPositionMs }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (colorLyrics != null && activeLine != null && activeLine.words.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    showFullScreenLyrics = true
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lyrics,
                                contentDescription = null,
                                tint = lyricsHighlightColor.copy(alpha = 0.85f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = activeLine.words,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = lyricsHighlightColor,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                        }
                    }
                }

                // 3. Track Info + Heart (NowPlayingTrackInfoRow)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val currentIsFav = isFavorite?.invoke(track) ?: isFavoriteTrack
                    IconButton(
                        onClick = { onToggleFavorite(track) },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (currentIsFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (currentIsFav) Color(0xFF1DB954) else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // 4. Scrubber Slider & Timestamps (SpotifyPlaybackControls)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Slider(
                        value = if (isScrubbing) scrubProgress else progressFraction,
                        onValueChange = {
                            isScrubbing = true
                            scrubProgress = it
                        },
                        onValueChangeFinished = {
                            isScrubbing = false
                            onSeekTo((scrubProgress * durationMs).toLong())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTimeMs(currentPositionMs),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatTimeMs(durationMs),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                // 5. Classic Player Transport Row (SimpPlayerControlLayout)
                SimpPlayerControlLayout(
                    isPlaying = playbackState.isPlaying,
                    isShuffle = isShuffle,
                    isRepeat = isRepeat,
                    onTogglePlayPause = onTogglePlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onToggleShuffle = { isShuffle = !isShuffle },
                    onToggleRepeat = { isRepeat = !isRepeat },
                    isLoading = playbackState.isBuffering,
                    activeColor = Color(0xFF1DB954),
                    contentColor = Color.White,
                    horizontalPadding = 20.dp
                )

                // 6. Bottom Buttons (Info, Output, Add to playlist, Queue)
                Row(
                    modifier = Modifier
                        .height(36.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            modifier = Modifier.size(26.dp),
                            onClick = {
                                val albUri = track.albumUri ?: if (track.album.isNotBlank()) "spotify:album:${track.id}" else null
                                if (albUri != null) onViewAlbum(albUri)
                            }
                        ) {
                            Icon(imageVector = Icons.Rounded.Info, tint = Color.White, contentDescription = "Info")
                        }
                        IconButton(
                            modifier = Modifier.size(26.dp),
                            onClick = {}
                        ) {
                            Icon(imageVector = Icons.Rounded.Headphones, tint = Color.White, contentDescription = "Output")
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            modifier = Modifier.size(26.dp),
                            onClick = { onAddToPlaylist(track) }
                        ) {
                            Icon(imageVector = Icons.Rounded.PlaylistAdd, tint = Color.White, contentDescription = "Add to playlist")
                        }
                        IconButton(
                            modifier = Modifier.size(26.dp),
                            onClick = { showQueueOverlay = true }
                        ) {
                            Icon(imageVector = Icons.Rounded.QueueMusic, tint = Color.White, contentDescription = "Queue")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Screen 2: Lyrics Section (Scroll down to view full lyrics - SimpMusic style)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 48.dp)
            ) {
                if (colorLyrics != null && colorLyrics!!.lines.isNotEmpty()) {
                    SpotifyColorLyricsCard(
                        lyrics = colorLyrics!!,
                        currentPositionMs = currentPositionMs,
                        onLineClick = { onSeekTo(it) },
                        onExpand = { showFullScreenLyrics = true }
                    )
                } else if (isLyricsLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }

        // Full Screen Lyrics Viewer (Spotui style)
        if (showFullScreenLyrics) {
            FullScreenLyricsViewer(
                track = track,
                lyrics = colorLyrics,
                isLoading = isLyricsLoading,
                currentPositionMs = currentPositionMs,
                onSeekTo = onSeekTo,
                onClose = { showFullScreenLyrics = false }
            )
        }

        // Full-screen Queue Overlay Modal Sheet
        if (showQueueOverlay) {
            QueueOverlayContent(
                currentTrack = track,
                playbackQueue = playbackQueue,
                onClose = { showQueueOverlay = false },
                onClearQueue = onClearQueue,
                onPlayTrackFromQueue = onPlayTrackFromQueue,
                onMoveQueueItem = onMoveQueueItem,
                onRemoveFromQueue = onRemoveFromQueue,
                onAddToPlaylist = onAddToPlaylist,
                onViewAlbum = onViewAlbum,
                onViewArtist = onViewArtist,
                onToggleFavorite = { onToggleFavorite(it) },
                isFavorite = { isFavorite?.invoke(it) ?: (it.id == track.id && isFavoriteTrack) }
            )
        }

        // Track Options Bottom Sheet
        val currentIsFav = isFavorite?.invoke(track) ?: isFavoriteTrack
        TrackOptionsMenu(
            expanded = showTrackOptionsMenu,
            onDismissRequest = { showTrackOptionsMenu = false },
            track = track,
            onPlayNext = { onPlayNext(track) },
            onAddToQueue = { onAddToQueue(track) },
            onAddToPlaylist = { onAddToPlaylist(track) },
            onToggleFavorite = { onToggleFavorite(track) },
            isFavorite = currentIsFav,
            onViewAlbum = {
                showTrackOptionsMenu = false
                val albUri = track.albumUri ?: if (track.album.isNotBlank()) "spotify:album:${track.id}" else null
                if (albUri != null) onViewAlbum(albUri)
            },
            onViewArtist = {
                showTrackOptionsMenu = false
                val artUri = track.artistUri ?: if (track.artist.isNotBlank()) "spotify:artist:${track.artist}" else null
                if (artUri != null) onViewArtist(artUri, track.artist)
            }
        )
    }
}

@Composable
fun SpotifyColorLyricsCard(
    lyrics: SpotifyColorLyrics,
    currentPositionMs: Long,
    onLineClick: (Long) -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = remember(lyrics.backgroundColor) { argbToColor(lyrics.backgroundColor) }
    val highlightColor = remember(lyrics.highlightTextColor) { argbToColor(lyrics.highlightTextColor) }
    val normalTextColor = remember(lyrics.textColor) {
        val c = argbToColor(lyrics.textColor)
        if (c.red < 0.2f && c.green < 0.2f && c.blue < 0.2f) {
            Color.White.copy(alpha = 0.45f)
        } else {
            c.copy(alpha = 0.7f)
        }
    }

    val activeIndex = remember(lyrics.lines, currentPositionMs) {
        lyrics.lines.indexOfLast { it.startTimeMs <= currentPositionMs }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            listState.animateScrollToItem((activeIndex - 1).coerceAtLeast(0))
        }
    }

    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = bgColor),
        modifier = modifier
            .fillMaxWidth()
            .height(440.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Lyrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = highlightColor
                    )
                    if (lyrics.syncType == "LINE_SYNCED") {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = highlightColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "SYNCED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = highlightColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onExpand,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Fullscreen,
                        contentDescription = "Full screen lyrics",
                        tint = highlightColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(lyrics.lines) { index, line ->
                    val isCurrent = index == activeIndex
                    val isPast = index < activeIndex

                    Text(
                        text = line.words.ifEmpty { "♪" },
                        style = if (isCurrent) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = if (isCurrent) highlightColor else if (isPast) highlightColor.copy(alpha = 0.65f) else normalTextColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLineClick(line.startTimeMs) }
                            .padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Lyrics provided by ${lyrics.providerDisplayName}",
                style = MaterialTheme.typography.labelSmall,
                color = highlightColor.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

private fun formatTimeMs(ms: Long): String {
    val totalSec = ms / 1000
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    return String.format("%02d:%02d", minutes, seconds)
}
