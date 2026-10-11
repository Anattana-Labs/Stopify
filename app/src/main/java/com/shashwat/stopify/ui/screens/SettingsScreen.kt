package com.shashwat.stopify.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shashwat.stopify.data.SpotifyTokenManager
import com.shashwat.stopify.data.SpotifyTrack
import com.shashwat.stopify.data.SpotifyUserProfile
import com.shashwat.stopify.data.SpotifyWebClient
import com.shashwat.stopify.ui.components.TrackOptionsMenu
import com.shashwat.stopify.ui.theme.AppThemeMode
import com.shashwat.stopify.ui.theme.ThemeColorMode
import kotlinx.coroutines.launch

@Composable
fun SimpSettingsScreen(
    client: SpotifyWebClient,
    currentThemeMode: AppThemeMode = AppThemeMode.DYNAMIC,
    onThemeChange: (AppThemeMode) -> Unit = {},
    currentColorMode: ThemeColorMode = ThemeColorMode.SYSTEM,
    onColorModeChange: (ThemeColorMode) -> Unit = {},
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

    // Toggle preferences
    val prefs = remember { context.getSharedPreferences("stopify_prefs", Context.MODE_PRIVATE) }
    var normalizeVolume by remember { mutableStateOf(prefs.getBoolean("pref_normalize_volume", true)) }
    var gaplessPlayback by remember { mutableStateOf(prefs.getBoolean("pref_gapless_playback", true)) }
    var syncSpotifyHistory by remember { mutableStateOf(prefs.getBoolean("pref_sync_history", true)) }

    fun reloadRecents() {
        isRecentsLoading = true
        coroutineScope.launch {
            val res = client.fetchSpotifyRecentlyPlayed()
            if (res.isSuccess) {
                spotifyRecents = res.getOrThrow()
            }
            isRecentsLoading = false
        }
    }

    LaunchedEffect(Unit) {
        reloadRecents()
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = bottomPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── TOP APP BAR ──────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 16.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // ── ACCOUNT HERO CARD ────────────────────────────────────
            item {
                val hasActiveUser = userProfile != null || !tokenDisplay.isNullOrEmpty()
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    primaryColor.copy(alpha = 0.4f),
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                )
                            ),
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .border(2.dp, primaryColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!userProfile?.avatarUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = userProfile!!.avatarUrl,
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = userProfile?.displayName ?: "Spotify Connected",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (hasActiveUser) primaryColor else Color.Gray)
                                    )
                                }

                                Text(
                                    text = userProfile?.email ?: (if (hasActiveUser) "Active Web Session" else "Not signed in"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = primaryColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = (userProfile?.product ?: if (hasActiveUser) "PREMIUM ACCESS" else "FREE TIER").uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onOpenCookieManager,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (hasActiveUser) "Manage Login" else "Sign In",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            if (hasActiveUser) {
                                OutlinedButton(
                                    onClick = {
                                        prefs.edit().remove("spotify_cookies").remove("spotify_access_token").apply()
                                        client.updateCookies("")
                                        tokenDisplay = null
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f))
                                ) {
                                    Icon(Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Log out", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // ── APPEARANCE & THEME ────────────────────────────────────
            item {
                SettingsSection(title = "Appearance & Themes", icon = Icons.Rounded.Palette) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Display Mode",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeCard(
                                title = "Auto",
                                subtitle = "Device system",
                                icon = Icons.Rounded.BrightnessAuto,
                                isSelected = currentColorMode == ThemeColorMode.SYSTEM,
                                onClick = { onColorModeChange(ThemeColorMode.SYSTEM) },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeCard(
                                title = "Dark",
                                subtitle = "Always dark",
                                icon = Icons.Rounded.DarkMode,
                                isSelected = currentColorMode == ThemeColorMode.DARK,
                                onClick = { onColorModeChange(ThemeColorMode.DARK) },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeCard(
                                title = "Light",
                                subtitle = "Clean light",
                                icon = Icons.Rounded.LightMode,
                                isSelected = currentColorMode == ThemeColorMode.LIGHT,
                                onClick = { onColorModeChange(ThemeColorMode.LIGHT) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Accent & Palette",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeCard(
                                title = "Dynamic",
                                subtitle = "Luxury Orange & Artwork",
                                icon = Icons.Rounded.AutoAwesome,
                                isSelected = currentThemeMode == AppThemeMode.DYNAMIC,
                                onClick = { onThemeChange(AppThemeMode.DYNAMIC) },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeCard(
                                title = "Monochrome",
                                subtitle = "Minimal Slate",
                                icon = Icons.Rounded.Contrast,
                                isSelected = currentThemeMode == AppThemeMode.MONOCHROME,
                                onClick = { onThemeChange(AppThemeMode.MONOCHROME) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        ThemeCard(
                            title = "Pitch Black (AMOLED)",
                            subtitle = "Pure black #000000 with luxury orange",
                            icon = Icons.Rounded.Contrast,
                            isSelected = currentThemeMode == AppThemeMode.AMOLED,
                            onClick = { onThemeChange(AppThemeMode.AMOLED) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ── AUDIO & PLAYBACK ─────────────────────────────────────
            item {
                SettingsSection(title = "Audio & Playback", icon = Icons.Rounded.GraphicEq) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        SettingsRow(
                            title = "Streaming Quality",
                            subtitle = "Widevine Modular DRM • 320 kbps High",
                            badge = "320 KBPS"
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))

                        SettingsToggleRow(
                            title = "Normalize Volume",
                            subtitle = "Equalize loud and quiet tracks",
                            checked = normalizeVolume,
                            onCheckedChange = {
                                normalizeVolume = it
                                prefs.edit().putBoolean("pref_normalize_volume", it).apply()
                            }
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))

                        SettingsToggleRow(
                            title = "Gapless Playback",
                            subtitle = "Seamless audio transitions between songs",
                            checked = gaplessPlayback,
                            onCheckedChange = {
                                gaplessPlayback = it
                                prefs.edit().putBoolean("pref_gapless_playback", it).apply()
                            }
                        )
                    }
                }
            }

            // ── SESSION & TOKEN STATUS ───────────────────────────────
            item {
                SettingsSection(title = "Session & Token Status", icon = Icons.Rounded.VpnKey) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Bearer Access Token",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (!tokenDisplay.isNullOrEmpty()) "Live in background" else "Not yet generated",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (!tokenDisplay.isNullOrEmpty()) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (!tokenDisplay.isNullOrEmpty()) primaryColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Text(
                                    text = if (!tokenDisplay.isNullOrEmpty()) "ACTIVE" else "IDLE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!tokenDisplay.isNullOrEmpty()) primaryColor else Color.Gray,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (!tokenDisplay.isNullOrEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${tokenDisplay!!.take(16)}...${tokenDisplay!!.takeLast(8)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        if (tokenStatusMsg.isNotEmpty()) {
                            Text(
                                text = tokenStatusMsg,
                                style = MaterialTheme.typography.bodySmall,
                                color = primaryColor
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
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
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF242430),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isRefreshingToken) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                } else {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text("Refresh Token", fontSize = 13.sp)
                            }

                            Button(
                                onClick = onOpenCookieManager,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF242430),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Web / Cookies", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // ── HISTORY & SPOTIFY SYNC ───────────────────────────────
            item {
                SettingsSection(title = "History & Sync", icon = Icons.Rounded.History) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsToggleRow(
                            title = "Sync Played Tracks",
                            subtitle = "Post playback pings to Spotify account",
                            checked = syncSpotifyHistory,
                            onCheckedChange = {
                                syncSpotifyHistory = it
                                prefs.edit().putBoolean("pref_sync_history", it).apply()
                            }
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showRecentsSection = !showRecentsSection }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Spotify Account Recents",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${spotifyRecents.size} tracks fetched",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }

                            Icon(
                                imageVector = if (showRecentsSection) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f)
                            )
                        }

                        // Collapsible preview of Spotify recents
                        AnimatedVisibility(visible = showRecentsSection) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (isRecentsLoading) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
                                    }
                                } else if (spotifyRecents.isEmpty()) {
                                    Text(
                                        text = "No Spotify recents recorded yet.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    spotifyRecents.take(5).forEach { track ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onPlayTrack(track) }
                                                .padding(vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = track.coverUrl,
                                                contentDescription = track.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = track.name,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = track.artist,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            IconButton(
                                                onClick = { onPlayTrack(track) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.PlayArrow,
                                                    contentDescription = "Play",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── ABOUT & SYSTEM INFO ──────────────────────────────────
            item {
                SettingsSection(title = "About & Engine", icon = Icons.Rounded.Info) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsInfoRow("App Version", "Stopify 1.2.0 (Build 43)")
                        SettingsInfoRow("DRM Engine", "Widevine L3 Software CDM")
                        SettingsInfoRow("Audio Engine", "AndroidX Media3 ExoPlayer")
                        SettingsInfoRow("Client Platform", "Web Player 4.80 (Harmony API)")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(14.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun ThemeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) primaryColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, primaryColor) else null,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    badge: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f)
            )
        }
        if (badge != null) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )
    }
}

@Composable
private fun SettingsInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}
