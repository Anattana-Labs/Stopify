package com.shashwat.stopify.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Radius scale: 6 chips/thumbs, 14 tiles, 22 sheets. Cards use a "notched" corner (see CardShape). */
val M3Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

val ShapeLargeIncreased = RoundedCornerShape(14.dp)
val ShapeExtraLargeIncreased = RoundedCornerShape(22.dp)
val ShapeExtraExtraLarge = RoundedCornerShape(28.dp)
val ShapeFull = CircleShape

/** Signature artwork shape: soft corners with one tight corner, like a sleeve edge. */
val CardShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomEnd = 22.dp, bottomStart = 4.dp)
