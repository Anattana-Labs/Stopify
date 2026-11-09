package com.shashwat.stopify.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.shashwat.stopify.ui.theme.SmallArtShape
import com.shashwat.stopify.ui.theme.PrismTokens
import com.shashwat.stopify.ui.theme.LocalIsDark
import com.shashwat.stopify.ui.theme.spectrumBrush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.shashwat.stopify.data.SpotifyTrack
import com.shashwat.stopify.player.PlaybackState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * The player strip of the dock. It has no surface of its own: the dock (see SimpDock) provides the
 * background, so the player and the navigation read as one object.
 * Tap = open player. Swipe left/right = next/previous. Swipe up = open player.
 */
@Composable
fun SimpBottomMiniPlayer(
    track: SpotifyTrack,
    playbackState: PlaybackState,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Any? = null,
    onPreviousTrack: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    backgroundColor: Color? = null,
    deviceName: String? = null
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val scheme = MaterialTheme.colorScheme

    val duration = playbackState.durationMs.takeIf { it > 0 } ?: track.durationMs
    val progress = if (duration > 0) (playbackState.currentPositionMs.toFloat() / duration).coerceIn(0f, 1f) else 0f

    // No surface of its own: the dock's glass is behind it, so player and tabs read as one object.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dy ->
                    if (dy < -12f) { change.consume(); onExpand() }
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onExpand() }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dx ->
                                change.consume()
                                scope.launch { offsetX.snapTo(offsetX.value + dx) }
                            },
                            onDragCancel = { scope.launch { offsetX.animateTo(0f, tween(180)) } },
                            onDragEnd = {
                                scope.launch {
                                    if (offsetX.value > 120f) onPreviousTrack?.invoke() ?: onSeekTo(0L)
                                    else if (offsetX.value < -120f) onNextTrack()
                                    offsetX.animateTo(0f, tween(180))
                                }
                            }
                        )
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(SmallArtShape)
                        .background(scheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    if (!track.coverUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = track.coverUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Rounded.Album, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = scheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                    if (deviceName != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Speaker, null, tint = scheme.primary, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = deviceName,
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.primary,
                                maxLines = 1
                            )
                        }
                    } else {
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                }
            }

            // Play / pause: a solid pill in the text colour, the one bright mark on the strip
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(scheme.onSurface)
                    .clickable(onClickLabel = if (playbackState.isPlaying) "Pause" else "Play") { onTogglePlayPause() }
                    .semantics { contentDescription = if (playbackState.isPlaying) "Pause" else "Play" },
                contentAlignment = Alignment.Center
            ) {
                if (playbackState.isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = scheme.surface
                    )
                } else {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = scheme.surface,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            IconButton(onClick = onNextTrack, modifier = Modifier.size(44.dp)) {
                Icon(
                    Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    tint = scheme.onSurface,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
