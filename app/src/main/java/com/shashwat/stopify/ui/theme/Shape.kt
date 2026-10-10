package com.shashwat.stopify.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 Expressive Corner Radius Scale
 */
val M3Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(12.dp)
)

// Minimal Spotify-styled Shapes
val ShapeLargeIncreased = RoundedCornerShape(8.dp)
val ShapeExtraLargeIncreased = RoundedCornerShape(8.dp)
val ShapeExtraExtraLarge = RoundedCornerShape(12.dp)
val ShapeFull = CircleShape

