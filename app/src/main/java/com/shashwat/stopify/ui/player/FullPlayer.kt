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
import androidx.compose.ui.draw.blur
import com.shashwat.stopify.ui.theme.ArtworkShape
import com.shashwat.stopify.ui.theme.PaneShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import com.shashwat.stopify.ui.theme.LocalDynamicColors
import com.shashwat.stopify.ui.theme.LocalIsDark
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
import com.shashwat.stopify.ui.components.PrismScrubber
import com.shashwat.stopify.ui.theme.spectrumRing
import com.shashwat.stopify.ui.theme.glassFill
import com.shashwat.stopify.ui.theme.glassEdge
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
    deviceName: String? = null,
    onOpenDevices: () -> Unit = {},
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

    // Immersive look: black room, the cover's colour drifting behind it, pale tint for secondary text.
    val dynamicOn = LocalDynamicColors.current
    val tintBase = remember(colorLyrics, dynamicOn) { if (dynamicOn) colorLyrics?.let { argbToColor(it.backgroundColor) } else null } ?: Color(0xFF3A3A44)
    val ground by animateColorAsState(tintBase, tween(900), label = "player_ground")
    val onVideo = activeCanvasUrl != null
    val fg = Color.White
    val tint = remember(tintBase) { lerp(Color.White, tintBase, 0.22f) }
    val fgMuted = tint.copy(alpha = 0.62f)
    val isFav = isFavorite?.invoke(track) ?: isFavoriteTrack

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
            .background(Color.Black)
            .pointerInput(Unit) { detectTapGestures { } }
    ) {
        if (onVideo) {
            CanvasVideo(url = activeCanvasUrl!!, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))
        } else {
            // Backdrop: cover colours blown up and drifting, darkened toward the bottom
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(ground.copy(alpha = 0.85f), ground.copy(alpha = 0.35f), Color.Black))))
            if (!track.coverUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = track.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().alpha(0.38f).blur(90.dp)
                )
            }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)))))
        }

        val gutter = 30.dp
        val contentWidth = minOf(maxWidth - gutter * 2, 560.dp)
        val artSize = minOf(contentWidth, maxHeight * 0.42f).coerceAtLeast(160.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = gutter)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Handle: tap or drag to close
            Box(
                Modifier.fillMaxWidth().height(32.dp).then(closeDrag).clickable(onClickLabel = "Close player", onClick = onCollapse),
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.width(36.dp).height(5.dp).clip(CircleShape).background(tint.copy(alpha = 0.35f)))
            }

            Spacer(Modifier.height(8.dp))

            // Cover: small corners, steps back on pause. Sideways swipe skips; swipe down closes.
            var dragX by remember { mutableFloatStateOf(0f) }
            val pauseScale by animateFloatAsState(if (playbackState.isPlaying) 1f else 0.86f, tween(380), label = "pause_scale")
            Box(
                modifier = Modifier
                    .width(contentWidth)
                    .height(artSize),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(artSize)
                        .graphicsLayer { scaleX = pauseScale; scaleY = pauseScale; alpha = if (onVideo) 0f else 1f }
                        .shadow(10.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.18f))
                        .pointerInput(track.id) {
                            detectHorizontalDragGestures(
                                onDragStart = { dragX = 0f },
                                onDragEnd = { if (dragX < -120f) onNext() else if (dragX > 120f) onPrevious() },
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
                        Icon(Icons.Rounded.Album, null, tint = fgMuted, modifier = Modifier.size(72.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Credits: title and artist left, round glyphs right
            Row(modifier = Modifier.width(contentWidth), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                    Text(
                        track.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = fg,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                    Text(
                        track.artist,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                        color = fgMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clickable(onClickLabel = "Open artist") {
                                val uri = track.artistUri ?: if (track.artist.isNotBlank()) "spotify:artist:${track.artist}" else null
                                if (uri != null) onViewArtist(uri, track.artist)
                            }
                    )
                }
                ImmGlyph(
                    if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    if (isFav) "Remove from favourites" else "Add to favourites",
                    tint, active = isFav, onClick = { onToggleFavorite(track) }
                )
                Spacer(Modifier.width(8.dp))
                ImmGlyph(Icons.Rounded.MoreVert, "Track options", tint, onClick = { showOptions = true })
            }

            if (playbackState.error != null) {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .width(contentWidth)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClickLabel = "Retry") { onTogglePlayPause() }
                        .heightIn(min = 44.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.ErrorOutline, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Couldn't play this track. Tap to retry.", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(Modifier.width(contentWidth)) {
                PrismScrubber(
                    value = if (isScrubbing) scrubProgress else fraction,
                    onValueChange = { isScrubbing = true; scrubProgress = it },
                    onValueChangeFinished = {
                        onSeekTo((scrubProgress * durationMs).toLong())
                        isScrubbing = false
                    },
                    enabled = durationMs > 0,
                    trackColor = Color.White.copy(alpha = 0.22f),
                    description = "Seek, ${formatTimeMs(positionMs)} of ${formatTimeMs(durationMs)}"
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatTimeMs(positionMs), style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"), color = fgMuted)
                    Text(
                        if (playbackState.isBuffering) "Loading…" else "-" + formatTimeMs((durationMs - positionMs).coerceAtLeast(0L)),
                        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                        color = fgMuted
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Transport: bare white glyphs, no backgrounds
                val label = when {
                    playbackState.error != null -> "Retry"
                    playbackState.isPlaying -> "Pause"
                    else -> "Play"
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ImmBare(Icons.Rounded.SkipPrevious, "Previous", 64.dp, 64.dp, onPrevious)
                    if (playbackState.isBuffering) {
                        Box(Modifier.size(92.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(38.dp), strokeWidth = 3.dp, color = Color.White)
                        }
                    } else {
                        ImmBare(
                            when {
                                playbackState.error != null -> Icons.Rounded.Refresh
                                playbackState.isPlaying -> Icons.Rounded.Pause
                                else -> Icons.Rounded.PlayArrow
                            },
                            label, 88.dp, 88.dp, onTogglePlayPause
                        )
                    }
                    ImmBare(Icons.Rounded.SkipNext, "Next", 64.dp, 64.dp, onNext)
                }

                Spacer(Modifier.height(12.dp))

                // Bottom row: lyrics left, one capsule of shuffle / repeat / devices, queue right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ImmBottom(Icons.Rounded.Lyrics, "Lyrics", tint, false) { showLyrics = true }
                    Row(
                        Modifier.height(44.dp).clip(CircleShape).background(tint.copy(alpha = 0.12f)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ImmSegment(Icons.Rounded.Shuffle, if (isShuffle) "Shuffle on" else "Shuffle off", tint, isShuffle, onToggleShuffle)
                        Box(Modifier.width(1.dp).fillMaxHeight().background(tint.copy(alpha = 0.2f)))
                        ImmSegment(Icons.Rounded.Repeat, if (isRepeat) "Repeat on" else "Repeat off", tint, isRepeat, onToggleRepeat)
                        Box(Modifier.width(1.dp).fillMaxHeight().background(tint.copy(alpha = 0.2f)))
                        ImmSegment(Icons.Rounded.Speaker, deviceName ?: "Devices", tint, deviceName != null, onOpenDevices)
                    }
                    ImmBottom(Icons.Rounded.QueueMusic, "Queue", tint, showQueue) { showQueue = true }
                }
            }
        }

        BackHandler(enabled = showLyrics) { showLyrics = false }
        AnimatedVisibility(
            visible = showLyrics,
            modifier = Modifier.fillMaxSize(),
            enter = slideInVertically(tween(420, easing = EmphasizedDecelerate)) { it / 3 } +
                fadeIn(tween(300)) + scaleIn(tween(420, easing = EmphasizedDecelerate), initialScale = 0.94f),
            exit = slideOutVertically(tween(300, easing = EmphasizedAccelerate)) { it / 2 } +
                fadeOut(tween(240)) + scaleOut(tween(300), targetScale = 0.96f)
        ) {
            FullScreenLyricsViewer(
                track = track,
                lyrics = colorLyrics,
                isLoading = isLyricsLoading,
                currentPositionMs = positionMs,
                onSeekTo = onSeekTo,
                onClose = { showLyrics = false }
            )
        }

        BackHandler(enabled = showQueue) { showQueue = false }
        AnimatedVisibility(
            visible = showQueue,
            modifier = Modifier.fillMaxSize(),
            enter = slideInVertically(tween(400, easing = EmphasizedDecelerate)) { it } + fadeIn(tween(240)),
            exit = slideOutVertically(tween(300, easing = EmphasizedAccelerate)) { it } + fadeOut(tween(220))
        ) {
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
private fun ImmGlyph(icon: ImageVector, description: String, tint: Color, active: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(tint.copy(alpha = if (active) 0.34f else 0.18f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = tint, modifier = Modifier.size(19.dp)) }
    }
}

@Composable
private fun ImmBare(icon: ImageVector, description: String, iconSize: Dp, touch: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(touch + 4.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(iconSize)) }
}

@Composable
private fun ImmBottom(icon: ImageVector, description: String, tint: Color, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = if (active) 0.28f else 0.12f))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp)) }
}

@Composable
private fun ImmSegment(icon: ImageVector, description: String, tint: Color, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(56.dp)
            .fillMaxHeight()
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = if (active) Color.White else tint.copy(alpha = 0.7f), modifier = Modifier.size(24.dp)) }
}

@Composable
private fun PlayerIconButton(
    icon: ImageVector,
    description: String,
    tint: Color,
    onClick: () -> Unit,
    size: Dp = 48.dp,
    iconSize: Dp = 26.dp,
    glass: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .then(if (glass) Modifier.background(glassFill(1.4f)).border(1.dp, glassEdge(), CircleShape) else Modifier)
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
        Text(label, style = MaterialTheme.typography.labelMedium, color = tint, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 4.dp))
    }
}

private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

@Composable
fun SpotifyColorLyricsCard(
    lyrics: SpotifyColorLyrics,
    currentPositionMs: Long,
    onLineClick: (Long) -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDark.current
    val cardScheme = MaterialTheme.colorScheme
    val dynamicOn = LocalDynamicColors.current
    val pal = remember(lyrics.backgroundColor, isDark, dynamicOn, cardScheme) {
        lyricsPalette(if (dynamicOn) argbToColor(lyrics.backgroundColor) else null, isDark, cardScheme)
    }
    val bgColor = pal.top
    val highlightColor = pal.active
    val normalTextColor = pal.inactive

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
                                    .background(cardScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Text(
                            text = line.words.ifEmpty { "♪" },
                            style = if (isCurrent) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
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
