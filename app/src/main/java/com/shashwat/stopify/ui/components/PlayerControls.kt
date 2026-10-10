package com.shashwat.stopify.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SimpPlayerControlLayout(
    isPlaying: Boolean,
    isShuffle: Boolean,
    isRepeat: Boolean,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    isLoading: Boolean = false,
    isSmallSize: Boolean = false,
    horizontalPadding: Dp = 20.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val height = if (isSmallSize) 48.dp else 96.dp
    val smallIcon = if (isSmallSize) 20.dp to 28.dp else 26.dp to 44.dp
    val mediumIcon = if (isSmallSize) 28.dp to 38.dp else 36.dp to 54.dp
    val bigIcon = if (isSmallSize) 38.dp to 48.dp else 70.dp to 84.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = horizontalPadding)
    ) {
        // Shuffle
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(smallIcon.second)
                    .clip(CircleShape)
                    .clickable { onToggleShuffle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (isShuffle) activeColor else contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(smallIcon.first)
                )
            }
        }

        // Previous
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(mediumIcon.second)
                    .clip(CircleShape)
                    .clickable { onPrevious() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Previous",
                    tint = contentColor,
                    modifier = Modifier.size(mediumIcon.first)
                )
            }
        }

        // Play/Pause Disc Button
        Box(Modifier.weight(1.3f), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(bigIcon.second)
                    .clip(CircleShape)
                    .background(contentColor)
                    .clickable { onTogglePlayPause() },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(if (isSmallSize) 22.dp else 34.dp),
                        color = MaterialTheme.colorScheme.surface,
                        strokeWidth = 3.dp
                    )
                } else {
                    Crossfade(targetState = isPlaying, label = "play_pause") { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(if (playing) 38.dp else 42.dp)
                        )
                    }
                }
            }
        }

        // Next
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(mediumIcon.second)
                    .clip(CircleShape)
                    .clickable { onNext() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    tint = contentColor,
                    modifier = Modifier.size(mediumIcon.first)
                )
            }
        }

        // Repeat
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(smallIcon.second)
                    .clip(CircleShape)
                    .clickable { onToggleRepeat() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Repeat,
                    contentDescription = "Repeat",
                    tint = if (isRepeat) activeColor else contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(smallIcon.first)
                )
            }
        }
    }
}
