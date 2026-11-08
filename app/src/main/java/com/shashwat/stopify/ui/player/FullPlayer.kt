package com.shashwat.stopify.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.alpha
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
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp

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

/**
 * Full-screen player. One column, top to bottom: header, artwork, title row, scrubber, transport, tool row.
 * Gestures: swipe down anywhere on the header or artwork to close, swipe the artwork sideways for
 * previous / next. Back also closes it (handled by the caller).
 */
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
    isShuffle: Boolean = false,
    isRepeat: Boolean = false,
    onToggleShuffle: () -> Unit = {},
    onToggleRepeat: () -> Unit = {},
    canvasUrl: String? = null,
    canvasEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableFloatStateOf(0f) }
    var showQueue by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }

    var colorLyrics by remember(track.id) { mutableStateOf<SpotifyColorLyrics?>(null) }
    var isLyricsLoading by remember(track.id) { mutableStateOf(false) }
    var fetchedCanvas by remember(track.id) { mutableStateOf<String?>(null) }
    val activeCanvasUrl = if (canvasEnabled) (canvasUrl ?: fetchedCanvas) else null

    LaunchedEffect(track.id, client, canvasEnabled, canvasUrl) {
        if (canvasEnabled && canvasUrl == null && client != null) {
            fetchedCanvas = try { client.fetchCanvasUrl(track.id) } catch (e: Exception) { null }
        }
    }
    LaunchedEffect(track.id, client) {
        if (client != null) {
            isLyricsLoading = true
            colorLyrics = try { client.fetchColorLyrics(track.id, track.coverUrl).getOrNull() } catch (e: Exception) { null }
            isLyricsLoading = false
        }
    }

    val durationMs = playbackState.durationMs.takeIf { it > 0 } ?: track.durationMs
    val positionMs = if (isScrubbing) (scrubProgress * durationMs).toLong() else playbackState.currentPositionMs
    val fraction = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    // The room takes a quiet tint from the track; text stays on the theme's own foreground for contrast.
    val tintBase = remember(colorLyrics) { colorLyrics?.let { argbToColor(it.backgroundColor) } } ?: scheme.primary
    val ground by animateColorAsState(lerp(scheme.surface, tintBase, 0.22f), tween(500), label = "player_ground")
    val onVideo = activeCanvasUrl != null
    val fg = if (onVideo) Color.White else scheme.onSurface
    val fgMuted = if (onVideo) Color.White.copy(alpha = 0.78f) else scheme.onSurfaceVariant
    val isFav = isFavorite?.invoke(track) ?: isFavoriteTrack
    val activeLine = remember(colorLyrics, positionMs / 500) {
        colorLyrics?.lines?.lastOrNull { it.startTimeMs <= positionMs }?.words?.takeIf { it.isNotBlank() }
    }

    // Swipe down closes the player
    val closeDrag = Modifier.pointerInput(Unit) {
        var total = 0f
        detectVerticalDragGestures(
            onDragStart = { total = 0f },
            onDragEnd = { if (total > 140f) onCollapse() },
            onVerticalDrag = { _, dy -> total += dy }
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ground)
            // Swallow taps so nothing underneath can be pressed through the player
            .pointerInput(Unit) { detectTapGestures { } }
    ) {
        if (onVideo) {
            CanvasVideo(url = activeCanvasUrl!!, modifier = Modifier.fillMaxSize())
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(0.55f), Color.Black.copy(0.15f), Color.Black.copy(0.45f), Color.Black.copy(0.85f))
                        )
                    )
            )
        }

        val artSize = minOf(maxWidth - 48.dp, maxHeight * 0.42f).coerceAtLeast(160.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().then(closeDrag).padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerIconButton(Icons.Rounded.KeyboardArrowDown, "Close player", fg, onCollapse)
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Now playing", style = MaterialTheme.typography.labelMedium, color = fgMuted)
                    Text(
                        track.album.ifBlank { "Stopify" },
                        style = MaterialTheme.typography.titleSmall,
                        color = fg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                PlayerIconButton(Icons.Rounded.MoreVert, "Track options", fg) { showOptions = true }
            }

            Spacer(Modifier.height(20.dp))

            // Artwork: edge, no shadow. Sideways swipe skips.
            var dragX by remember { mutableFloatStateOf(0f) }
            Box(
                modifier = Modifier
                    .size(artSize)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, fg.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                    .background(scheme.surfaceContainerHighest)
                    .pointerInput(track.id) {
                        detectHorizontalDragGestures(
                            onDragStart = { dragX = 0f },
                            onDragEnd = {
                                if (dragX < -120f) onNext() else if (dragX > 120f) onPrevious()
                            },
                            onHorizontalDrag = { _, dx -> dragX += dx }
                        )
                    }
                    .then(closeDrag),
                contentAlignment = Alignment.Center
            ) {
                if (!track.coverUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = track.coverUrl,
                        contentDescription = "Cover of ${track.album.ifBlank { track.name }}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Rounded.Album, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(72.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Title row
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        track.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = fg,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                    Text(
                        track.artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = fgMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClickLabel = "Open artist") {
                                val uri = track.artistUri ?: if (track.artist.isNotBlank()) "spotify:artist:${track.artist}" else null
                                if (uri != null) onViewArtist(uri, track.artist)
                            }
                            .padding(vertical = 4.dp)
                    )
                }
                PlayerIconButton(
                    if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    if (isFav) "Remove from favourites" else "Add to favourites",
                    if (isFav) scheme.primary else fg
                ) { onToggleFavorite(track) }
            }

            // Status line: error with retry, or the current lyric line
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp), contentAlignment = Alignment.CenterStart) {
                when {
                    playbackState.error != null -> Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClickLabel = "Retry") { onTogglePlayPause() }
                            .heightIn(min = 40.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.ErrorOutline, null, tint = scheme.error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Couldn't play this track. Tap to retry.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (onVideo) Color.White else scheme.error
                        )
                    }
                    activeLine != null -> Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClickLabel = "Open lyrics") { showLyrics = true }
                            .heightIn(min = 40.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Lyrics, null, tint = fgMuted, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(activeLine, style = MaterialTheme.typography.bodyMedium, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            // Scrubber
            Slider(
                value = if (isScrubbing) scrubProgress else fraction,
                onValueChange = { isScrubbing = true; scrubProgress = it },
                onValueChangeFinished = {
                    onSeekTo((scrubProgress * durationMs).toLong())
                    isScrubbing = false
                },
                enabled = durationMs > 0,
                colors = SliderDefaults.colors(
                    thumbColor = scheme.primary,
                    activeTrackColor = scheme.primary,
                    inactiveTrackColor = fg.copy(alpha = 0.22f),
                    disabledThumbColor = fg.copy(alpha = 0.3f),
                    disabledActiveTrackColor = fg.copy(alpha = 0.3f),
                    disabledInactiveTrackColor = fg.copy(alpha = 0.15f)
                ),
                modifier = Modifier.fillMaxWidth().semantics {
                    contentDescription = "Seek, ${formatTimeMs(positionMs)} of ${formatTimeMs(durationMs)}"
                }
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTimeMs(positionMs), style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"), color = fgMuted)
                Text(
                    if (playbackState.isBuffering) "Loading…" else "-" + formatTimeMs((durationMs - positionMs).coerceAtLeast(0L)),
                    style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                    color = fgMuted
                )
            }

            Spacer(Modifier.height(12.dp))

            // Transport
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerIconButton(
                    Icons.Rounded.Shuffle, if (isShuffle) "Shuffle on" else "Shuffle off",
                    if (isShuffle) scheme.primary else fgMuted, onToggleShuffle
                )
                PlayerIconButton(Icons.Rounded.SkipPrevious, "Previous", fg, onPrevious, size = 56.dp, iconSize = 34.dp)
                val label = when {
                    playbackState.error != null -> "Retry"
                    playbackState.isPlaying -> "Pause"
                    else -> "Play"
                }
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(scheme.primary)
                        .clickable(onClickLabel = label, role = Role.Button, onClick = onTogglePlayPause)
                        .semantics { contentDescription = label },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        playbackState.isBuffering -> CircularProgressIndicator(
                            modifier = Modifier.size(30.dp), strokeWidth = 3.dp, color = scheme.onPrimary
                        )
                        else -> Icon(
                            when {
                                playbackState.error != null -> Icons.Rounded.Refresh
                                playbackState.isPlaying -> Icons.Rounded.Pause
                                else -> Icons.Rounded.PlayArrow
                            },
                            null, tint = scheme.onPrimary, modifier = Modifier.size(40.dp)
                        )
                    }
                }
                PlayerIconButton(Icons.Rounded.SkipNext, "Next", fg, onNext, size = 56.dp, iconSize = 34.dp)
                PlayerIconButton(
                    Icons.Rounded.Repeat, if (isRepeat) "Repeat on" else "Repeat off",
                    if (isRepeat) scheme.primary else fgMuted, onToggleRepeat
                )
            }

            Spacer(Modifier.height(20.dp))

            // Tools: every one of these does something
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(fg.copy(alpha = if (onVideo) 0.14f else 0.07f)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PlayerTool(Icons.Rounded.Lyrics, "Lyrics", fg, Modifier.weight(1f)) { showLyrics = true }
                PlayerTool(Icons.Rounded.PlaylistAdd, "Save", fg, Modifier.weight(1f)) { onAddToPlaylist(track) }
                PlayerTool(
                    Icons.Rounded.QueueMusic,
                    if (playbackQueue.isEmpty()) "Queue" else "Queue · ${playbackQueue.size}",
                    fg, Modifier.weight(1f)
                ) { showQueue = true }
            }
        }

        if (showLyrics) {
            BackHandler { showLyrics = false }
            FullScreenLyricsViewer(
                track = track,
                lyrics = colorLyrics,
                isLoading = isLyricsLoading,
                currentPositionMs = positionMs,
                onSeekTo = onSeekTo,
                onClose = { showLyrics = false }
            )
        }

        if (showQueue) {
            BackHandler { showQueue = false }
            QueueOverlayContent(
                currentTrack = track,
                playbackQueue = playbackQueue,
                onClose = { showQueue = false },
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

        TrackOptionsMenu(
            expanded = showOptions,
            onDismissRequest = { showOptions = false },
            track = track,
            onPlayNext = { onPlayNext(track) },
            onAddToQueue = { onAddToQueue(track) },
            onAddToPlaylist = { onAddToPlaylist(track) },
            onToggleFavorite = { onToggleFavorite(track) },
            isFavorite = isFav,
            onViewAlbum = {
                showOptions = false
                val albUri = track.albumUri ?: if (track.album.isNotBlank()) "spotify:album:${track.id}" else null
                if (albUri != null) onViewAlbum(albUri)
            },
            onViewArtist = {
                showOptions = false
                val artUri = track.artistUri ?: if (track.artist.isNotBlank()) "spotify:artist:${track.artist}" else null
                if (artUri != null) onViewArtist(artUri, track.artist)
            }
        )
    }
}

@Composable
private fun PlayerIconButton(
    icon: ImageVector,
    description: String,
    tint: Color,
    onClick: () -> Unit,
    size: Dp = 48.dp,
    iconSize: Dp = 26.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
private fun PlayerTool(icon: ImageVector, label: String, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = tint, maxLines = 1)
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
    val themePrimary = MaterialTheme.colorScheme.primary
    val themeSurface = MaterialTheme.colorScheme.surface
    val isDark = isSystemInDarkTheme()

    val bgColor = remember(lyrics.backgroundColor, themePrimary, themeSurface, isDark) {
        val raw = argbToColor(lyrics.backgroundColor)
        blendThemeTint(raw, themePrimary, themeSurface, isDark)
    }

    val lum = 0.299f * bgColor.red + 0.587f * bgColor.green + 0.114f * bgColor.blue
    val isLightBg = lum > 0.5f

    val highlightColor = if (isLightBg) Color(0xFF191C1E) else Color.White
    val normalTextColor = if (isLightBg) Color(0xFF43474E).copy(alpha = 0.65f) else Color.White.copy(alpha = 0.45f)

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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = bgColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(440.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
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
                            shape = RoundedCornerShape(12.dp),
                            color = highlightColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "SYNCED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = highlightColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = highlightColor.copy(alpha = 0.18f),
                    modifier = Modifier.clickable { onExpand() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "MORE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = highlightColor
                        )
                        Icon(
                            imageVector = Icons.Rounded.OpenInFull,
                            contentDescription = "Full screen lyrics",
                            tint = highlightColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLineClick(line.startTimeMs) }
                            .padding(vertical = 4.dp)
                    ) {
                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(themePrimary)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Text(
                            text = line.words.ifEmpty { "♪" },
                            style = if (isCurrent) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) highlightColor else if (isPast) highlightColor.copy(alpha = 0.65f) else normalTextColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
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
