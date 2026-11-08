package com.shashwat.stopify.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.shashwat.stopify.R

@OptIn(ExperimentalTextApi::class)
private fun vf(w: Int) = Font(
    R.font.outfit, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w))
)

/** Outfit, Prism's own typeface: one variable file at every weight the scale uses. */
val PrismFont = FontFamily((200..800 step 100).map { vf(it) })

// Kept for call sites that name a role; both roles are Outfit now.
val DisplayFont = PrismFont
val BodyFont = PrismFont

private fun style(size: Float, line: Float, weight: Int, tracking: Float = 0f) = TextStyle(
    fontFamily = PrismFont, fontSize = size.sp, lineHeight = line.sp,
    fontWeight = FontWeight(weight), letterSpacing = tracking.em
)

/**
 * Prism's 1.7 scale: thin and large at the top, calm in the middle, firm only for small labels.
 * ExtraLight (200) only from 28sp up, Light (300) from 21sp, nothing under 400 below that,
 * so thin strokes never blur at row size or fall below contrast.
 */
val Typography = Typography(
    displayLarge = style(56f, 60f, 200, -0.03f),
    displayMedium = style(44f, 50f, 200, -0.025f),
    displaySmall = style(36f, 42f, 200, -0.02f),
    // Page titles: Home's greeting, Library, Settings.
    headlineLarge = style(36f, 42f, 200, -0.02f),
    // Hero titles: album, playlist, artist.
    headlineMedium = style(28f, 34f, 300, -0.015f),
    headlineSmall = style(24f, 30f, 300, -0.01f),
    // Section headings.
    titleLarge = style(22f, 28f, 300, -0.01f),
    // Row titles.
    titleMedium = style(16f, 22f, 500, -0.003f),
    titleSmall = style(14f, 20f, 500),
    bodyLarge = style(16f, 22f, 400),
    bodyMedium = style(14.5f, 20f, 400),
    bodySmall = style(13f, 18f, 400, 0.003f),
    // Buttons, tabs, chips; labelSmall also for eyebrows.
    labelLarge = style(14f, 18f, 500, 0.01f),
    labelMedium = style(12.5f, 16f, 500, 0.02f),
    labelSmall = style(11.5f, 14f, 500, 0.05f)
)
