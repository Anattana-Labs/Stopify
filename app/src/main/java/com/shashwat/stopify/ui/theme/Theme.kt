package com.shashwat.stopify.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
    DYNAMIC,
    MONOCHROME,
    AMOLED
}

enum class ThemeColorMode(val label: String) {
    SYSTEM("Automatic (Device)"),
    DARK("Dark Mode"),
    LIGHT("Light Mode")
}

// ── LUXURY PREMIUM ACOUSTIC ORANGE ────────────────────────────────────
val PremiumOrange = Color(0xFFFF6422) // High-end Sunset Amber Orange

val PremiumOrangeDarkScheme = darkColorScheme(
    primary = PremiumOrange,
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF5A1E00),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFFFF8B53),
    onSecondary = Color(0xFF381000),
    secondaryContainer = Color(0xFF3D1B0D),
    onSecondaryContainer = Color(0xFFFFD6C2),
    tertiary = Color(0xFFFFA066),
    onTertiary = Color(0xFF381000),
    surface = Color(0xFF131110),
    onSurface = Color(0xFFF3EEEE),
    surfaceVariant = Color(0xFF221C1A),
    onSurfaceVariant = Color(0xFFC7BDB8),
    surfaceContainerLowest = Color(0xFF0C0A09),
    surfaceContainerLow = Color(0xFF161311),
    surfaceContainer = Color(0xFF1D1917),
    surfaceContainerHigh = Color(0xFF272220),
    surfaceContainerHighest = Color(0xFF342E2B),
    background = Color(0xFF0F0D0C),
    onBackground = Color(0xFFF3EEEE),
    outline = Color(0xFF554A45),
    outlineVariant = Color(0xFF302926)
)

val PremiumOrangeLightScheme = lightColorScheme(
    primary = Color(0xFFE54E0B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBCF),
    onPrimaryContainer = Color(0xFF380C00),
    secondary = Color(0xFF7B5240),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF2E1508),
    tertiary = Color(0xFF8B4D20),
    onTertiary = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F1A18),
    surfaceVariant = Color(0xFFF5EBE6),
    onSurfaceVariant = Color(0xFF51443E),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAF2ED),
    surfaceContainer = Color(0xFFF4ECE6),
    surfaceContainerHigh = Color(0xFFEEE6E0),
    surfaceContainerHighest = Color(0xFFE8E0DA),
    background = Color(0xFFFDF8F5),
    onBackground = Color(0xFF1F1A18),
    outline = Color(0xFF84746D),
    outlineVariant = Color(0xFFD7C2B9)
)

// ── MONOCHROME SCHEMES ────────────────────────────────────────────────
private val MonochromeColorScheme = darkColorScheme(
    primary = Color(0xFFE2E2E2),
    onPrimary = Color(0xFF141414),
    primaryContainer = Color(0xFF383838),
    onPrimaryContainer = Color(0xFFF2F2F2),
    secondary = Color(0xFFC4C4C4),
    onSecondary = Color(0xFF1E1E1E),
    secondaryContainer = Color(0xFF3E3E3E),
    onSecondaryContainer = Color(0xFFE5E5E5),
    tertiary = Color(0xFFAAAAAA),
    onTertiary = Color(0xFF1A1A1A),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFEAEAEA),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFA8A8A8),
    surfaceContainerLowest = Color(0xFF0B0B0B),
    surfaceContainerLow = Color(0xFF161616),
    surfaceContainer = Color(0xFF1C1C1C),
    surfaceContainerHigh = Color(0xFF262626),
    surfaceContainerHighest = Color(0xFF303030),
    background = Color(0xFF0F0F10),
    onBackground = Color(0xFFEAEAEA),
    outline = Color(0xFF4C4C4C),
    outlineVariant = Color(0xFF2E2E2E)
)

private val MonochromeLightScheme = lightColorScheme(
    primary = Color(0xFF202020),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0E0E0),
    onPrimaryContainer = Color(0xFF101010),
    secondary = Color(0xFF505050),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEEEEEE),
    onSecondaryContainer = Color(0xFF222222),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF141414),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF555555),
    background = Color(0xFFF8F8F8),
    onBackground = Color(0xFF141414),
    outline = Color(0xFF888888),
    outlineVariant = Color(0xFFCCCCCC)
)

// ── AMOLED PURE BLACK ─────────────────────────────────────────────────
private val AmoledColorScheme = darkColorScheme(
    primary = PremiumOrange,
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF5A1E00),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFFFF8B53),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF381506),
    onSecondaryContainer = Color(0xFFFFD6C2),
    tertiary = Color(0xFFFFA066),
    onTertiary = Color(0xFF000000),
    surface = Color(0xFF000000), // Pure AMOLED Black
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF121212),
    onSurfaceVariant = Color(0xFFB3B3B3),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF080808),
    surfaceContainer = Color(0xFF101010),
    surfaceContainerHigh = Color(0xFF181818),
    surfaceContainerHighest = Color(0xFF222222),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    outline = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF202020)
)

// ── DYNAMIC ARTWORK PALETTE EXTRACTION ────────────────────────────────
fun generateArtworkColorScheme(seedColor: Color, darkTheme: Boolean = true): ColorScheme {
    val r = seedColor.red
    val g = seedColor.green
    val b = seedColor.blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val d = max - min
    val h = when {
        d == 0f -> 0f
        max == r -> {
            val segment = (g - b) / d
            if (segment < 0f) (segment + 6f) * 60f else segment * 60f
        }
        max == g -> (((b - r) / d) + 2f) * 60f
        else -> (((r - g) / d) + 4f) * 60f
    }
    val l = (max + min) / 2f
    val s = if (d == 0f) 0f else d / (1f - kotlin.math.abs(2f * l - 1f))

    fun fromHsl(hue: Float, sat: Float, lum: Float): Color {
        val c = (1f - kotlin.math.abs(2f * lum - 1f)) * sat
        val x = c * (1f - kotlin.math.abs((hue / 60f) % 2f - 1f))
        val m = lum - c / 2f
        val (rp, gp, bp) = when (((hue / 60f).toInt() % 6 + 6) % 6) {
            0 -> Triple(c, x, 0f)
            1 -> Triple(x, c, 0f)
            2 -> Triple(0f, c, x)
            3 -> Triple(0f, x, c)
            4 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        return Color(
            (rp + m).coerceIn(0f, 1f),
            (gp + m).coerceIn(0f, 1f),
            (bp + m).coerceIn(0f, 1f),
            1f
        )
    }

    if (!darkTheme) {
        val primary = fromHsl(h, 0.90f.coerceAtMost(s.coerceAtLeast(0.6f)), 0.38f)
        val onPrimary = Color.White
        val primaryContainer = fromHsl(h, 0.75f, 0.90f)
        val onPrimaryContainer = fromHsl(h, 0.90f, 0.12f)
        val secondary = fromHsl((h + 30f) % 360f, 0.50f, 0.42f)
        val onSecondary = Color.White
        val secondaryContainer = fromHsl((h + 30f) % 360f, 0.60f, 0.88f)
        val onSecondaryContainer = fromHsl((h + 30f) % 360f, 0.80f, 0.12f)
        val surface = fromHsl(h, 0.12f, 0.98f)
        val onSurface = fromHsl(h, 0.18f, 0.10f)
        val surfaceVariant = fromHsl(h, 0.15f, 0.92f)
        val onSurfaceVariant = fromHsl(h, 0.15f, 0.35f)
        val background = fromHsl(h, 0.10f, 0.96f)
        val onBackground = fromHsl(h, 0.18f, 0.10f)

        return lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            background = background,
            onBackground = onBackground
        )
    }

    val primary = fromHsl(h, 0.85f.coerceAtMost(s.coerceAtLeast(0.6f)), 0.65f)
    val onPrimary = fromHsl(h, 0.9f, 0.10f)
    val primaryContainer = fromHsl(h, 0.7f, 0.22f)
    val onPrimaryContainer = fromHsl(h, 0.8f, 0.92f)

    val secondary = fromHsl((h + 30f) % 360f, 0.6f, 0.70f)
    val onSecondary = fromHsl((h + 30f) % 360f, 0.8f, 0.12f)
    val secondaryContainer = fromHsl((h + 30f) % 360f, 0.5f, 0.20f)
    val onSecondaryContainer = fromHsl((h + 30f) % 360f, 0.7f, 0.90f)

    val tertiary = fromHsl((h + 60f) % 360f, 0.65f, 0.75f)
    val onTertiary = fromHsl((h + 60f) % 360f, 0.8f, 0.12f)

    val surface = fromHsl(h, 0.22f, 0.06f)
    val onSurface = fromHsl(h, 0.10f, 0.94f)
    val surfaceVariant = fromHsl(h, 0.18f, 0.14f)
    val onSurfaceVariant = fromHsl(h, 0.12f, 0.80f)

    val surfaceContainerLowest = fromHsl(h, 0.25f, 0.03f)
    val surfaceContainerLow = fromHsl(h, 0.22f, 0.08f)
    val surfaceContainer = fromHsl(h, 0.20f, 0.11f)
    val surfaceContainerHigh = fromHsl(h, 0.18f, 0.15f)
    val surfaceContainerHighest = fromHsl(h, 0.16f, 0.20f)
    val background = fromHsl(h, 0.22f, 0.05f)
    val onBackground = fromHsl(h, 0.10f, 0.94f)

    val outline = fromHsl(h, 0.15f, 0.50f)
    val outlineVariant = fromHsl(h, 0.18f, 0.28f)

    return darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceContainerLowest = surfaceContainerLowest,
        surfaceContainerLow = surfaceContainerLow,
        surfaceContainer = surfaceContainer,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = surfaceContainerHighest,
        background = background,
        onBackground = onBackground,
        outline = outline,
        outlineVariant = outlineVariant
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
        AppThemeMode.AMOLED -> AmoledColorScheme
        AppThemeMode.MONOCHROME -> if (isDark) MonochromeColorScheme else MonochromeLightScheme
        AppThemeMode.DYNAMIC -> {
            if (seedColor != null) {
                generateArtworkColorScheme(seedColor, isDark)
            } else {
                if (isDark) PremiumOrangeDarkScheme else PremiumOrangeLightScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = M3Shapes,
        content = content
    )
}
