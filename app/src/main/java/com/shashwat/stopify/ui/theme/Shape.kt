package com.shashwat.stopify.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Prism's soft corners: 8 / 12 / 16 / 22 / 28. */
val M3Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

val ShapeLargeIncreased = RoundedCornerShape(16.dp)
val ShapeExtraLargeIncreased = RoundedCornerShape(22.dp)
val ShapeExtraExtraLarge = RoundedCornerShape(28.dp)
val ShapeFull = CircleShape

/** A tile (settings pane inner rows, list highlights). */
val TileShape = RoundedCornerShape(16.dp)
/** An album, playlist or song cover on a shelf. */
val CardShape = RoundedCornerShape(14.dp)
/** A song's cover inside a list row. */
val SmallArtShape = RoundedCornerShape(10.dp)
/** A pane of grouped content (settings groups, the resume card). */
val PaneShape = RoundedCornerShape(22.dp)

/** The docked tab bar keeps a rounded top edge only. */
val DockShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

/** Floating mini player card (joined into the bar's glass) and the navigation capsule. */
val MiniPlayerShape = RoundedCornerShape(22.dp)
val NavPillShape = RoundedCornerShape(28.dp)

/** The big cover in the full player and on album / playlist / artist heroes. */
val ArtworkShape = RoundedCornerShape(28.dp)
