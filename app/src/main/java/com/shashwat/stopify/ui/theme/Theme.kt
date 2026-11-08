package com.shashwat.stopify.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
    /** Prism's violet, or the playing cover's colour when there is one. */
    DYNAMIC,
    /** "White light": greys and ink only. */
    MONOCHROME,
    /** Pure black ground. */
    AMOLED
}

enum class ThemeColorMode(val label: String) {
    SYSTEM("Automatic (Device)"),
    DARK("Dark Mode"),
    LIGHT("Light Mode")
}

/** Whether the app is drawing its dark scheme (not necessarily the phone's). */
val LocalIsDark = staticCompositionLocalOf { true }

/** Kept for older call sites: the accent a screen may use as "the brand". */
val PremiumOrange = PrismViolet

/** Near-neutral greys for "White light": no tint anywhere. */
private fun neutralScheme(dark: Boolean): ColorScheme {
    fun g(l: Float) = hsl(0f, 0f, l)
    return if (dark) {
        val bg = g(0.055f)
        darkColorScheme(
            primary = Color.White, onPrimary = g(0.08f),
            primaryContainer = g(0.26f), onPrimaryContainer = Color.White, inversePrimary = g(0.20f),
            secondary = g(0.82f), onSecondary = g(0.10f),
            secondaryContainer = g(0.22f), onSecondaryContainer = g(0.95f),
            tertiary = g(0.70f), onTertiary = g(0.10f),
            tertiaryContainer = g(0.24f), onTertiaryContainer = g(0.95f),
            background = bg, onBackground = g(0.95f), surface = bg, onSurface = g(0.95f),
            surfaceVariant = g(0.20f), onSurfaceVariant = g(0.74f), surfaceTint = Color.White,
            inverseSurface = g(0.93f), inverseOnSurface = g(0.12f),
            outline = g(0.45f), outlineVariant = g(0.26f),
            surfaceBright = g(0.20f), surfaceDim = bg,
            surfaceContainerLowest = g(0.04f), surfaceContainerLow = g(0.085f),
            surfaceContainer = g(0.105f), surfaceContainerHigh = g(0.135f), surfaceContainerHighest = g(0.165f),
        )
    } else {
        val bg = g(0.985f)
        lightColorScheme(
            primary = g(0.12f), onPrimary = Color.White,
            primaryContainer = g(0.88f), onPrimaryContainer = g(0.08f), inversePrimary = Color.White,
            secondary = g(0.34f), onSecondary = Color.White,
            secondaryContainer = g(0.90f), onSecondaryContainer = g(0.10f),
            tertiary = g(0.45f), onTertiary = Color.White,
            tertiaryContainer = g(0.90f), onTertiaryContainer = g(0.10f),
            background = bg, onBackground = g(0.08f), surface = bg, onSurface = g(0.08f),
            surfaceVariant = g(0.91f), onSurfaceVariant = g(0.34f), surfaceTint = g(0.12f),
            inverseSurface = g(0.16f), inverseOnSurface = g(0.95f),
            outline = g(0.52f), outlineVariant = g(0.82f),
            surfaceBright = bg, surfaceDim = g(0.88f),
            surfaceContainerLowest = Color.White, surfaceContainerLow = g(0.965f),
            surfaceContainer = g(0.95f), surfaceContainerHigh = g(0.93f), surfaceContainerHighest = g(0.905f),
        )
    }
}

/**
 * A full Material 3 scheme grown from one [seed] hue, as Prism does it: a ground at 5.5 % lightness (dark) or 98.5 %
 * (light) with a hint of the seed's hue, containers stepping up by small amounts, and an accent that is checked
 * against the ground ([readableOn]) so a pale yellow or neon cover can never give an accent you cannot read.
 */
fun schemeFromSeed(seed: Color, dark: Boolean, pureBlack: Boolean = false): ColorScheme {
    if (isNeutralSeed(seed)) return neutralScheme(dark)
    val h = hueOf(seed)
    val s = satOf(seed).coerceIn(0.30f, 0.92f)
    val n = 0.10f
    val c = if (dark) {
        val bg = if (pureBlack) Color.Black else hsl(h, n * 1.4f, 0.055f)
        darkColorScheme(
            primary = hsl(h, s, 0.76f), onPrimary = hsl(h, s, 0.14f),
            primaryContainer = hsl(h, s * 0.75f, 0.30f), onPrimaryContainer = hsl(h, s, 0.92f),
            inversePrimary = hsl(h, s, 0.42f),
            secondary = hsl(h, s * 0.35f, 0.76f), onSecondary = hsl(h, s * 0.35f, 0.16f),
            secondaryContainer = hsl(h, s * 0.30f, 0.26f), onSecondaryContainer = hsl(h, s * 0.35f, 0.92f),
            tertiary = hsl(h + 45f, s * 0.6f, 0.78f), onTertiary = hsl(h + 45f, s * 0.6f, 0.16f),
            tertiaryContainer = hsl(h + 45f, s * 0.5f, 0.28f), onTertiaryContainer = hsl(h + 45f, s * 0.6f, 0.92f),
            background = bg, onBackground = hsl(h, n, 0.93f), surface = bg, onSurface = hsl(h, n, 0.93f),
            surfaceVariant = hsl(h, n * 1.5f, 0.20f), onSurfaceVariant = hsl(h, n * 1.2f, 0.74f),
            surfaceTint = hsl(h, s, 0.76f),
            inverseSurface = hsl(h, n, 0.92f), inverseOnSurface = hsl(h, n, 0.12f),
            outline = hsl(h, n, 0.45f), outlineVariant = hsl(h, n, 0.26f),
            surfaceBright = hsl(h, n * 1.4f, if (pureBlack) 0.16f else 0.20f), surfaceDim = bg,
            surfaceContainerLowest = if (pureBlack) Color.Black else hsl(h, n * 1.4f, 0.04f),
            surfaceContainerLow = hsl(h, n * 1.4f, if (pureBlack) 0.05f else 0.085f),
            surfaceContainer = hsl(h, n * 1.4f, if (pureBlack) 0.07f else 0.105f),
            surfaceContainerHigh = hsl(h, n * 1.4f, if (pureBlack) 0.10f else 0.135f),
            surfaceContainerHighest = hsl(h, n * 1.4f, if (pureBlack) 0.13f else 0.165f),
        )
    } else {
        val bg = hsl(h, 0.30f, 0.985f)
        lightColorScheme(
            primary = hsl(h, s, 0.42f), onPrimary = Color.White,
            primaryContainer = hsl(h, s, 0.90f), onPrimaryContainer = hsl(h, s, 0.14f),
            inversePrimary = hsl(h, s, 0.78f),
            secondary = hsl(h, s * 0.30f, 0.40f), onSecondary = Color.White,
            secondaryContainer = hsl(h, s * 0.40f, 0.90f), onSecondaryContainer = hsl(h, s * 0.35f, 0.14f),
            tertiary = hsl(h + 45f, s * 0.6f, 0.40f), onTertiary = Color.White,
            tertiaryContainer = hsl(h + 45f, s * 0.6f, 0.90f), onTertiaryContainer = hsl(h + 45f, s * 0.6f, 0.14f),
            background = bg, onBackground = hsl(h, n, 0.10f), surface = bg, onSurface = hsl(h, n, 0.10f),
            surfaceVariant = hsl(h, 0.18f, 0.91f), onSurfaceVariant = hsl(h, n, 0.34f),
            surfaceTint = hsl(h, s, 0.42f),
            inverseSurface = hsl(h, n, 0.18f), inverseOnSurface = hsl(h, n, 0.95f),
            outline = hsl(h, n, 0.52f), outlineVariant = hsl(h, n, 0.82f),
            surfaceBright = bg, surfaceDim = hsl(h, 0.15f, 0.88f),
            surfaceContainerLowest = Color.White, surfaceContainerLow = hsl(h, 0.30f, 0.965f),
            surfaceContainer = hsl(h, 0.28f, 0.95f), surfaceContainerHigh = hsl(h, 0.25f, 0.93f),
            surfaceContainerHighest = hsl(h, 0.22f, 0.905f),
        )
    }
    val primary = readableOn(c.primary, c.background)
    return c.copy(
        primary = primary, surfaceTint = primary, onPrimary = readableOn(c.onPrimary, primary),
        onPrimaryContainer = readableOn(c.onPrimaryContainer, c.primaryContainer),
        secondary = readableOn(c.secondary, c.background),
        onSecondaryContainer = readableOn(c.onSecondaryContainer, c.secondaryContainer),
        tertiary = readableOn(c.tertiary, c.background),
        onSurfaceVariant = readableOn(c.onSurfaceVariant, c.background),
        outline = readableOn(c.outline, c.background, 3f),
    )
}

@Composable
fun StopifyTheme(
    themeMode: AppThemeMode = AppThemeMode.DYNAMIC,
    colorMode: ThemeColorMode = ThemeColorMode.SYSTEM,
    seedColor: Color? = null,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (colorMode) {
        ThemeColorMode.SYSTEM -> isSystemDark
        ThemeColorMode.DARK -> true
        ThemeColorMode.LIGHT -> false
    }

    val colorScheme = when (themeMode) {
        AppThemeMode.AMOLED -> schemeFromSeed(seedColor ?: PrismViolet, dark = true, pureBlack = true)
        AppThemeMode.MONOCHROME -> neutralScheme(isDark)
        AppThemeMode.DYNAMIC -> schemeFromSeed(seedColor ?: PrismViolet, isDark)
    }
    val dark = isDark || themeMode == AppThemeMode.AMOLED

    CompositionLocalProvider(LocalIsDark provides dark, LocalContentColor provides colorScheme.onBackground) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = M3Shapes,
            content = content
        )
    }
}
