package com.shashwat.stopify.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

val AmbientGlowHeight = 360.dp

@Composable
fun AmbientThemeGlow(
    modifier: Modifier = Modifier,
    tint: Color? = null
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val isLightTheme = backgroundColor.luminance() > 0.5f

    val glow by animateColorAsState(
        targetValue = when {
            tint == null -> backgroundColor
            isLightTheme -> lerp(tint, Color.White, 0.85f)
            else -> tint.copy(alpha = 0.45f)
        },
        animationSpec = tween(500),
        label = "ambientGlow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AmbientGlowHeight)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        glow,
                        glow.copy(alpha = 0.35f),
                        backgroundColor
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            backgroundColor
                        )
                    )
                )
        )
    }
}
