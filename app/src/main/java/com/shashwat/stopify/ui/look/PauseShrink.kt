package com.shashwat.stopify.ui.look

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

const val PAUSED_ARTWORK_SCALE = 0.86f

@Composable
fun rememberPauseScale(playing: Boolean): State<Float> = animateFloatAsState(
    targetValue = if (playing) 1f else PAUSED_ARTWORK_SCALE,
    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
    label = "pauseScale",
)
