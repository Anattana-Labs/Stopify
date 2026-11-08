package com.shashwat.stopify.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Radius scale: 8 thumbs, 12 tiles, 20 sheets, 28 dock. */
val M3Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

val ShapeLargeIncreased = RoundedCornerShape(12.dp)
val ShapeExtraLargeIncreased = RoundedCornerShape(20.dp)
val ShapeExtraExtraLarge = RoundedCornerShape(28.dp)
val ShapeFull = CircleShape

/** Artwork shape: even 12dp corners; the play badge sits on a cut corner. */
val CardShape = RoundedCornerShape(12.dp)

/** Dock: rounded top edge only, it is fused to the screen bottom. */
val DockShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

/** Floating mini player card and floating navigation pill. */
val MiniPlayerShape = RoundedCornerShape(22.dp)
val NavPillShape = RoundedCornerShape(32.dp)
