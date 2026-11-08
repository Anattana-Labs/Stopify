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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.shashwat.stopify.ui.theme.MiniPlayerShape
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
 * background, so the player and the navigation read as one object. A hairline at the very top shows progress.
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
    backgroundColor: Color? = null
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val scheme = MaterialTheme.colorScheme

    // Tint the strip with the track's own color, kept dark/quiet so text contrast holds.
    val tint by animateColorAsState(
        targetValue = if (backgroundColor != null) lerp(scheme.surfaceContainerHigh, backgroundColor, 0.22f)
        else scheme.surfaceContainerHigh,
        animationSpec = tween(400),
        label = "mini_tint"
    )

    val duration = playbackState.durationMs.takeIf { it > 0 } ?: track.durationMs
    val progress = if (duration > 0) (playbackState.currentPositionMs.toFloat() / duration).coerceIn(0f, 1f) else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, MiniPlayerShape)
            .clip(MiniPlayerShape)
            .background(tint)
            .border(1.dp, scheme.outlineVariant, MiniPlayerShape)
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dy ->
                    if (dy < -12f) { change.consume(); onExpand() }
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .clip(RoundedCornerShape(10.dp))
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
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
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
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                }
            }

            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favourites" else "Add to favourites",
                    tint = if (isFavorite) scheme.primary else scheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Play / pause: square-ish coral key, the one accent on the strip
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(scheme.primary)
                    .clickable(onClickLabel = if (playbackState.isPlaying) "Pause" else "Play") { onTogglePlayPause() }
                    .semantics { contentDescription = if (playbackState.isPlaying) "Pause" else "Play" },
                contentAlignment = Alignment.Center
            ) {
                if (playbackState.isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = scheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = scheme.onPrimary,
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
        // Progress hairline along the bottom edge of the card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(scheme.onSurface.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(scheme.primary)
            )
        }

    }
}
