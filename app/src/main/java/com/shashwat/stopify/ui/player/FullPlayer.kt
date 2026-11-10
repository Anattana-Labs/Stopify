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

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.offset
import com.shashwat.stopify.ui.look.MeshGradientBackground
import com.shashwat.stopify.ui.look.ThinSlider
import com.shashwat.stopify.ui.look.playerTintOf
import com.shashwat.stopify.ui.look.rememberArtworkColors
import com.shashwat.stopify.ui.look.rememberPauseScale
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

    // Immersive look, ported from myspoti: mesh of the cover's colours behind a black room,
    // a pale tint for secondary text, the cover collapsing into a header when a panel opens.
    val palette = rememberArtworkColors(track.coverUrl)
    val tint = remember(palette) { playerTintOf(palette) }
    val onVideo = activeCanvasUrl != null
    val fg = Color.White
    val fgMuted = tint.copy(alpha = 0.55f)
    val isFav = isFavorite?.invoke(track) ?: isFavoriteTrack

    var panel by rememberSaveable { mutableIntStateOf(0) } // 0 none, 1 lyrics, 2 queue
    val collapse by animateFloatAsState(
        targetValue = if (panel == 0) 0f else 1f,
        animationSpec = tween(450, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "playerCollapse"
    )
    val pauseScale by rememberPauseScale(playbackState.isPlaying)
    BackHandler(enabled = panel != 0) { panel = 0 }

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
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f + 0.2f * collapse)))
        } else {
            MeshGradientBackground(palette = palette, trackKey = track.id)
        }

        val gutter = 30.dp
        val contentWidth = minOf(maxWidth - gutter * 2, 560.dp)
        val thumb = 54.dp
        val headerH = 60.dp
        val topPad = 8.dp
        val gap = 20.dp
        var dragX by remember { mutableFloatStateOf(0f) }

        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Handle: tap or drag to close
            Box(
                Modifier.fillMaxWidth().height(32.dp).then(closeDrag).clickable(onClickLabel = "Close player", onClick = onCollapse),
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.width(36.dp).height(5.dp).clip(CircleShape).background(tint.copy(alpha = 0.35f)))
            }

            // Everything that changes between the full player and the panels lives in this one
            // weighted box, so the controls below it never move.
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val left = (maxWidth - contentWidth) / 2
                val fullArt = minOf(contentWidth, maxHeight - topPad - gap - headerH - 16.dp).coerceAtLeast(thumb)
                val spare = (maxHeight - topPad - fullArt - gap - headerH - 16.dp).coerceAtLeast(0.dp)
                val groupTop = spare / 2
                fun p() = collapse
                fun artSize(): Dp = androidx.compose.ui.unit.lerp(fullArt, thumb, p())
                fun artTop(): Dp = topPad + androidx.compose.ui.unit.lerp(groupTop, 0.dp, p())
                fun artStart(): Dp = left + androidx.compose.ui.unit.lerp((contentWidth - fullArt) / 2, 0.dp, p())
                fun titleTop(): Dp = topPad + androidx.compose.ui.unit.lerp(groupTop + fullArt + gap, 0.dp, p())
                fun titleStart(): Dp = androidx.compose.ui.unit.lerp(0.dp, thumb + 12.dp, p())

                // ---- Panel, under the header ----
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(top = topPad + headerH, start = left, end = left)
                        .graphicsLayer { alpha = ((p() - 0.6f) / 0.4f).coerceIn(0f, 1f) }
                ) {
                    when (panel) {
                        1 -> ImmLyricsPanel(colorLyrics?.lines.orEmpty(), isLyricsLoading, positionMs, onSeekTo, Modifier.fillMaxSize())
                        2 -> ImmQueuePanel(
                            playbackQueue, tint,
                            onPlay = onPlayTrackFromQueue, onRemove = onRemoveFromQueue, onClear = onClearQueue,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // ---- Cover: the whole sleeve, then the header's thumbnail ----
                Box(
                    modifier = Modifier
                        .offset { IntOffset(artStart().roundToPx(), artTop().roundToPx()) }
                        .layout { measurable, _ ->
                            val side = artSize().roundToPx()
                            val placeable = measurable.measure(Constraints.fixed(side, side))
                            layout(side, side) { placeable.place(0, 0) }
                        }
                        .graphicsLayer {
                            val s = pauseScale + (1f - pauseScale) * p()
                            scaleX = s
                            scaleY = s
                            alpha = if (onVideo && panel == 0) 0f else 1f
                            shadowElevation = 10.dp.toPx() * (1f - p())
                            shape = RoundedCornerShape(8.dp)
                            clip = true
                        }
                        .background(Color.Black.copy(alpha = 0.18f))
                        .pointerInput(track.id, panel) {
                            if (panel == 0) detectHorizontalDragGestures(
                                onDragStart = { dragX = 0f },
                                onDragEnd = { if (dragX < -120f) onNext() else if (dragX > 120f) onPrevious() },
                                onHorizontalDrag = { _, dx -> dragX += dx }
                            )
                        }
                        .then(closeDrag)
                        .clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) {
                            if (panel != 0) panel = 0
                        },
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
                        Icon(Icons.Rounded.Album, null, tint = fgMuted, modifier = Modifier.size(40.dp))
                    }
                }

                // ---- Credits + round glyphs ----
                Row(
                    modifier = Modifier
                        .offset { IntOffset((left + titleStart()).roundToPx(), titleTop().roundToPx()) }
                        .layout { measurable, _ ->
                            val w = (contentWidth - titleStart()).roundToPx()
                            val h = headerH.roundToPx()
                            val placeable = measurable.measure(Constraints.fixed(w, h))
                            layout(w, h) { placeable.place(0, 0) }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            track.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold),
                            color = fg,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                        Text(
                            track.artist,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
                            color = fgMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable(onClickLabel = "Open artist") {
                                val uri = track.artistUri ?: if (track.artist.isNotBlank()) "spotify:artist:${track.artist}" else null
                                if (uri != null) onViewArtist(uri, track.artist)
                            }
                        )
                    }
                    ImmGlyph(
                        if (isFav) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        if (isFav) "Remove from favourites" else "Add to favourites",
                        tint, active = isFav, onClick = { onToggleFavorite(track) }
                    )
                    Spacer(Modifier.width(4.dp))
                    ImmGlyph(Icons.Rounded.MoreHoriz, "Track options", tint, onClick = { showOptions = true })
                }
            }

            if (playbackState.error != null) {
                Row(
                    modifier = Modifier
                        .width(contentWidth)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClickLabel = "Retry") { onTogglePlayPause() }
                        .heightIn(min = 40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.ErrorOutline, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Couldn't play this track. Tap to retry.", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }
            }

            // ---- Controls: scrubber, transport, bottom row ----
            Column(Modifier.width(contentWidth).padding(bottom = 18.dp)) {
                ThinSlider(
                    value = if (isScrubbing) scrubProgress else fraction,
                    onValueChange = { isScrubbing = true; scrubProgress = it },
                    onValueChangeFinished = {
                        onSeekTo((scrubProgress * durationMs).toLong())
                        isScrubbing = false
                    },
                    loading = playbackState.isBuffering && !isScrubbing,
                )
                Row(Modifier.fillMaxWidth().offset(y = (-9).dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatTimeMs(positionMs), style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"), color = fgMuted)
                    Text(
                        "-" + formatTimeMs((durationMs - positionMs).coerceAtLeast(0L)),
                        style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                        color = fgMuted
                    )
                }

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

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ImmBottom(Icons.Rounded.Lyrics, "Lyrics", tint, panel == 1) { panel = if (panel == 1) 0 else 1 }
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
                    ImmBottom(Icons.Rounded.QueueMusic, "Queue", tint, panel == 2) { panel = if (panel == 2) 0 else 2 }
                }
            }
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
