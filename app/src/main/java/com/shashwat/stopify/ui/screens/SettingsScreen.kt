package com.shashwat.stopify.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.shashwat.stopify.data.SpotifyTokenManager
import com.shashwat.stopify.data.SpotifyTrack
import com.shashwat.stopify.data.SpotifyUserProfile
import com.shashwat.stopify.data.SpotifyWebClient
import com.shashwat.stopify.ui.theme.AppThemeMode
import com.shashwat.stopify.ui.theme.PaneShape
import com.shashwat.stopify.ui.theme.PrismTokens
import com.shashwat.stopify.ui.theme.pane
import com.shashwat.stopify.ui.theme.glassFill
import com.shashwat.stopify.ui.theme.glassEdge
import com.shashwat.stopify.ui.components.Eyebrow
import com.shashwat.stopify.ui.components.Hairline
import com.shashwat.stopify.ui.components.PrismOutlinedButton
import com.shashwat.stopify.ui.components.PrismSwitch
import com.shashwat.stopify.ui.components.PrismTonalButton
import com.shashwat.stopify.ui.components.Segmented
import com.shashwat.stopify.ui.components.GlassStyle
import com.shashwat.stopify.ui.components.SubPageHeader
import com.shashwat.stopify.ui.theme.ThemeColorMode
import kotlinx.coroutines.launch

@Composable
private fun SettingsSectionHeader(title: String) {
    Eyebrow(title, Modifier.padding(start = 12.dp, top = PrismTokens.Space.section, bottom = 10.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpSettingsScreen(
    client: SpotifyWebClient,
    currentThemeMode: AppThemeMode = AppThemeMode.DYNAMIC,
    onThemeChange: (AppThemeMode) -> Unit = {},
    currentColorMode: ThemeColorMode = ThemeColorMode.SYSTEM,
    onColorModeChange: (ThemeColorMode) -> Unit = {},
    currentGlassStyle: GlassStyle = GlassStyle.BLUR,
    onGlassStyleChange: (GlassStyle) -> Unit = {},
    onOpenCookieManager: () -> Unit,
    onPlayTrack: (SpotifyTrack) -> Unit,
    onPlayNext: (SpotifyTrack) -> Unit = {},
    onAddToQueue: (SpotifyTrack) -> Unit = {},
    onAddToPlaylist: (SpotifyTrack) -> Unit = {},
    onBack: () -> Unit = {},
    onViewAlbum: (String) -> Unit = {},
    onViewArtist: (artistUri: String, artistName: String) -> Unit = { _, _ -> },
    onToggleFavorite: (SpotifyTrack) -> Unit = {},
    isFavorite: (SpotifyTrack) -> Boolean = { false },
    bottomPadding: Dp = 100.dp,
    userProfile: SpotifyUserProfile? = null,
    canvasEnabled: Boolean = true,
    onCanvasToggle: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRefreshingToken by remember { mutableStateOf(false) }
    var tokenStatusMsg by remember { mutableStateOf("") }
    var tokenDisplay by remember { mutableStateOf(client.customAccessToken) }
    var spotifyRecents by remember { mutableStateOf<List<SpotifyTrack>>(emptyList()) }
    var isRecentsLoading by remember { mutableStateOf(false) }
    var showRecentsSection by remember { mutableStateOf(false) }

    val prefs = remember { context.getSharedPreferences("stopify_prefs", Context.MODE_PRIVATE) }
    var normalizeVolume by remember { mutableStateOf(prefs.getBoolean("pref_normalize_volume", true)) }
    var gaplessPlayback by remember { mutableStateOf(prefs.getBoolean("pref_gapless_playback", true)) }
    var syncSpotifyHistory by remember { mutableStateOf(prefs.getBoolean("pref_sync_history", true)) }
    var canvasToggleLocal by remember { mutableStateOf(canvasEnabled) }

    LaunchedEffect(canvasEnabled) { canvasToggleLocal = canvasEnabled }

    fun reloadRecents() {
        isRecentsLoading = true
        coroutineScope.launch {
            val res = client.fetchSpotifyRecentlyPlayed()
            if (res.isSuccess) spotifyRecents = res.getOrThrow()
            isRecentsLoading = false
        }
    }

    LaunchedEffect(Unit) { reloadRecents() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SubPageHeader(title = "Settings", onBack = onBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = bottomPadding + 16.dp)
        ) {
            // ── 1. ACCOUNT ──────────────────────────────────────────
            SettingsSectionHeader("Account")
            Column(modifier = Modifier.fillMaxWidth().pane(PaneShape).padding(vertical = 4.dp)) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = userProfile?.displayName ?: "Spotify User",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    supportingContent = {
                        val subText = when {
                            !userProfile?.email.isNullOrBlank() -> userProfile!!.email
                            !userProfile?.product.isNullOrBlank() -> "Spotify ${userProfile!!.product}"
                            else -> "Connected via Web Session"
                        }
                        Text(
                            text = subText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingContent = {
                        if (!userProfile?.avatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = userProfile!!.avatarUrl,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    },
                    trailingContent = {
                        if (!userProfile?.product.isNullOrBlank()) {
                            Text(
                                text = userProfile!!.product,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(glassFill(1.6f))
                                    .border(1.dp, glassEdge(), CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                Hairline(Modifier.padding(horizontal = 16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrismTonalButton(
                        onClick = onOpenCookieManager,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Key,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (userProfile != null || !client.customAccessToken.isNullOrEmpty()) "Manage Login" else "Sign In")
                    }

                    if (userProfile != null || !client.customAccessToken.isNullOrEmpty()) {
                        PrismOutlinedButton(
                            onClick = {
                                prefs.edit().remove("spotify_cookies").remove("spotify_access_token").apply()
                                client.updateCookies("")
                                tokenDisplay = null
                            },
                            contentColor = MaterialTheme.colorScheme.error
                        ) {
                            Text("Log out")
                        }
                    }
                }
            }

            // ── 2. SESSION & TOKEN ──────────────────────────────────
            SettingsSectionHeader("Session & Token")
            Column(modifier = Modifier.fillMaxWidth().pane(PaneShape).padding(vertical = 4.dp)) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = "Bearer Access Token",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = if (!tokenDisplay.isNullOrEmpty()) "Live authenticated session" else "Not generated",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.VpnKey,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (!tokenDisplay.isNullOrEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Text(
                                text = if (!tokenDisplay.isNullOrEmpty()) "ACTIVE" else "IDLE",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (!tokenDisplay.isNullOrEmpty()) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                if (tokenStatusMsg.isNotEmpty()) {
                    Text(
                        text = tokenStatusMsg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                Hairline(Modifier.padding(horizontal = 16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrismTonalButton(
                        onClick = {
                            isRefreshingToken = true
                            tokenStatusMsg = "Refreshing in background..."
                            SpotifyTokenManager.refreshAccessTokenInBackground(context, client) { newToken ->
                                isRefreshingToken = false
                                if (newToken != null) {
                                    tokenDisplay = newToken
                                    tokenStatusMsg = "Token refreshed successfully!"
                                } else {
                                    tokenStatusMsg = "Background refresh failed."
                                }
                            }
                        },
                        enabled = !isRefreshingToken,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isRefreshingToken) "Refreshing…" else "Refresh Token")
                    }

                    PrismOutlinedButton(
                        onClick = onOpenCookieManager,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Web / Cookies")
                    }
                }
            }

            // ── 3. PLAYBACK & AUDIO ─────────────────────────────────
            SettingsSectionHeader("Playback & Audio")
            Column(modifier = Modifier.fillMaxWidth().pane(PaneShape).padding(vertical = 4.dp)) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = "Normalize Volume",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "Equalize loud and quiet tracks automatically",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        PrismSwitch(
                            checked = normalizeVolume,
                            onCheckedChange = {
                                normalizeVolume = it
                                prefs.edit().putBoolean("pref_normalize_volume", it).apply()
                            }
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                Hairline(Modifier.padding(horizontal = 16.dp))

                ListItem(
                    headlineContent = {
                        Text(
                            text = "Gapless Playback",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "Seamless audio transitions between consecutive songs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        PrismSwitch(
                            checked = gaplessPlayback,
                            onCheckedChange = {
                                gaplessPlayback = it
                                prefs.edit().putBoolean("pref_gapless_playback", it).apply()
                            }
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                Hairline(Modifier.padding(horizontal = 16.dp))

                ListItem(
                    headlineContent = {
                        Text(
                            text = "Canvas",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "Display looping background animations in the player",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.VideoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        PrismSwitch(
                            checked = canvasToggleLocal,
                            onCheckedChange = {
                                canvasToggleLocal = it
                                prefs.edit().putBoolean("pref_canvas_enabled", it).apply()
                                onCanvasToggle(it)
                            }
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            // ── 4. APPEARANCE & DISPLAY ─────────────────────────────
            SettingsSectionHeader("Appearance & Display")
            Column(modifier = Modifier.fillMaxWidth().pane(PaneShape).padding(vertical = 4.dp)) {
                Text(
                    text = "Display Mode",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                val colorModes = listOf(
                    "System" to ThemeColorMode.SYSTEM,
                    "Dark" to ThemeColorMode.DARK,
                    "Light" to ThemeColorMode.LIGHT
                )
                Segmented(
                    options = colorModes.map { it.second },
                    selected = currentColorMode,
                    label = { m -> colorModes.first { it.second == m }.first },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    onSelect = onColorModeChange
                )

                Spacer(Modifier.height(8.dp))
                Hairline(Modifier.padding(horizontal = 16.dp))

                Text(
                    text = "Color Palette",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                val themeModes = listOf(
                    "Prism" to AppThemeMode.DYNAMIC,
                    "White light" to AppThemeMode.MONOCHROME,
                    "AMOLED" to AppThemeMode.AMOLED
                )
                Segmented(
                    options = themeModes.map { it.second },
                    selected = currentThemeMode,
                    label = { m -> themeModes.first { it.second == m }.first },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    onSelect = onThemeChange
                )
                Spacer(Modifier.height(8.dp))
                Hairline(Modifier.padding(horizontal = 16.dp))

                Text(
                    text = "Glass",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                Segmented(
                    options = GlassStyle.values().toList(),
                    selected = currentGlassStyle,
                    label = { it.label },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    onSelect = onGlassStyleChange
                )
                Text(
                    text = when (currentGlassStyle) {
                        GlassStyle.OFF -> "Plain surfaces. Lightest on battery."
                        GlassStyle.BLUR -> "The dock and mini player blur what scrolls beneath them."
                        GlassStyle.LIQUID -> "Blur with a refracting, lens-like rim."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
                Spacer(Modifier.height(12.dp))
            }

            // ── 5. HISTORY & SYNC ───────────────────────────────────
            SettingsSectionHeader("History & Sync")
            Column(modifier = Modifier.fillMaxWidth().pane(PaneShape).padding(vertical = 4.dp)) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = "Sync Played Tracks",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "Record playback history to your Spotify account",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        PrismSwitch(
                            checked = syncSpotifyHistory,
                            onCheckedChange = {
                                syncSpotifyHistory = it
                                prefs.edit().putBoolean("pref_sync_history", it).apply()
                            }
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                Hairline(Modifier.padding(horizontal = 16.dp))

                ListItem(
                    headlineContent = {
                        Text(
                            text = "Spotify Account Recents",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "${spotifyRecents.size} tracks fetched from Spotify",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        IconButton(onClick = { showRecentsSection = !showRecentsSection }) {
                            Icon(
                                imageVector = if (showRecentsSection) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    modifier = Modifier.clickable { showRecentsSection = !showRecentsSection },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                AnimatedVisibility(visible = showRecentsSection) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isRecentsLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else if (spotifyRecents.isEmpty()) {
                            Text(
                                text = "No Spotify recents recorded yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            spotifyRecents.take(5).forEach { track ->
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            text = track.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            text = track.artist,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    leadingContent = {
                                        AsyncImage(
                                            model = track.coverUrl,
                                            contentDescription = track.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    },
                                    trailingContent = {
                                        IconButton(onClick = { onPlayTrack(track) }) {
                                            Icon(
                                                imageVector = Icons.Rounded.PlayArrow,
                                                contentDescription = "Play",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onPlayTrack(track) },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                )
                            }
                        }
                    }
                }
            }

            // ── 6. ABOUT & ENGINE ───────────────────────────────────
            SettingsSectionHeader("About & Engine")
            Column(modifier = Modifier.fillMaxWidth().pane(PaneShape).padding(vertical = 4.dp)) {
                ListItem(
                    headlineContent = { Text("App Version", style = MaterialTheme.typography.bodyMedium) },
                    trailingContent = {
                        Text(
                            "Stopify 1.2.0 (Build 43)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                Hairline(Modifier.padding(horizontal = 16.dp))
                ListItem(
                    headlineContent = { Text("DRM Engine", style = MaterialTheme.typography.bodyMedium) },
                    trailingContent = {
                        Text(
                            "Widevine L3 Software CDM",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                Hairline(Modifier.padding(horizontal = 16.dp))
                ListItem(
                    headlineContent = { Text("Audio Engine", style = MaterialTheme.typography.bodyMedium) },
                    trailingContent = {
                        Text(
                            "AndroidX Media3 ExoPlayer",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                Hairline(Modifier.padding(horizontal = 16.dp))
                ListItem(
                    headlineContent = { Text("Connect Engine", style = MaterialTheme.typography.bodyMedium) },
                    trailingContent = {
                        Text(
                            "Spotify Web Player (Harmony API)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}

