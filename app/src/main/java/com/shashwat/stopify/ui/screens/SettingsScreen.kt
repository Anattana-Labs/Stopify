package com.shashwat.stopify.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.shashwat.stopify.ui.theme.AppThemeMode
import com.shashwat.stopify.ui.theme.ThemeColorMode
import kotlinx.coroutines.launch

// Spotui-style section title (green accent, bold, small)
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

// Spotui-style switch row
@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, color = Color(0xFFB3B3B3), fontSize = 12.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color(0xFFB3B3B3),
                uncheckedTrackColor = Color(0xFF333333),
            ),
        )
    }
}

// Spotui-style info row (no badge, just label + value)
@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

// Spotui-style source/option row with check mark
@Composable
private fun SourceSettingRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .background(if (selected) Color(0xFF1A1A20) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color(0xFFB3B3B3), fontSize = 12.sp)
        }
        if (selected) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

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

    val primaryColor = MaterialTheme.colorScheme.primary
    val background = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = bottomPadding)
        ) {
            // ── TOP APP BAR ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(26.dp)
                        .clickable { onBack() }
                )
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ── ACCOUNT ──────────────────────────────────────────────
            SectionTitle("Account")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3A3A3A)),
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
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = userProfile?.displayName ?: "Spotify User",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!userProfile?.email.isNullOrBlank()) {
                        Text(userProfile!!.email, color = Color.Gray, fontSize = 13.sp)
                    }
                    if (!userProfile?.product.isNullOrBlank()) {
                        Text(
                            "Spotify ${userProfile!!.product}",
                            color = primaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF2A2A2A), modifier = Modifier.padding(vertical = 4.dp))

            // Manage login
            Text(
                text = if (userProfile != null || !client.customAccessToken.isNullOrEmpty()) "Manage Login" else "Sign In",
                color = primaryColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onOpenCookieManager() }
                    .padding(vertical = 12.dp)
            )

            if (userProfile != null || !client.customAccessToken.isNullOrEmpty()) {
                Text(
                    text = "Log out",
                    color = Color(0xFFE57373),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            prefs.edit().remove("spotify_cookies").remove("spotify_access_token").apply()
                            client.updateCookies("")
                            tokenDisplay = null
                        }
                        .padding(vertical = 12.dp)
                )
            }

            // ── SESSION ─────────────────────────────────────────────
            SectionTitle("Session & Token")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Bearer Access Token",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (!tokenDisplay.isNullOrEmpty()) "Live in background" else "Not yet generated",
                        color = if (!tokenDisplay.isNullOrEmpty()) primaryColor else Color(0xFFB3B3B3),
                        fontSize = 12.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (!tokenDisplay.isNullOrEmpty()) primaryColor.copy(alpha = 0.2f) else Color(0xFF333333)
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
            if (tokenStatusMsg.isNotEmpty()) {
                Text(tokenStatusMsg, color = primaryColor, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isRefreshingToken) "Refreshing…" else "Refresh Token",
                    color = primaryColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = !isRefreshingToken) {
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
                        }
                        .padding(vertical = 12.dp)
                )
                Text(
                    text = "Web / Cookies",
                    color = primaryColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onOpenCookieManager() }
                        .padding(vertical = 12.dp)
                )
            }

            // ── AUDIO & PLAYBACK ─────────────────────────────────────
            SectionTitle("Audio & Playback")
            SettingsSwitchRow(
                title = "Normalize Volume",
                subtitle = "Equalize loud and quiet tracks",
                checked = normalizeVolume,
                onCheckedChange = {
                    normalizeVolume = it
                    prefs.edit().putBoolean("pref_normalize_volume", it).apply()
                }
            )
            HorizontalDivider(color = Color(0xFF222222), modifier = Modifier.padding(vertical = 2.dp))
            SettingsSwitchRow(
                title = "Gapless Playback",
                subtitle = "Seamless audio transitions between songs",
                checked = gaplessPlayback,
                onCheckedChange = {
                    gaplessPlayback = it
                    prefs.edit().putBoolean("pref_gapless_playback", it).apply()
                }
            )

            // ── DISPLAY ─────────────────────────────────────────────
            SectionTitle("Display")
            SettingsSwitchRow(
                title = "Canvas",
                subtitle = "Show looping video animations in the player",
                checked = canvasToggleLocal,
                onCheckedChange = {
                    canvasToggleLocal = it
                    prefs.edit().putBoolean("pref_canvas_enabled", it).apply()
                    onCanvasToggle(it)
                }
            )
            HorizontalDivider(color = Color(0xFF222222), modifier = Modifier.padding(vertical = 2.dp))
            Text(
                text = "Display Mode",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            listOf(
                Triple("Auto", "Device system", currentColorMode == ThemeColorMode.SYSTEM) to { onColorModeChange(ThemeColorMode.SYSTEM) },
                Triple("Dark", "Always dark", currentColorMode == ThemeColorMode.DARK) to { onColorModeChange(ThemeColorMode.DARK) },
                Triple("Light", "Clean light", currentColorMode == ThemeColorMode.LIGHT) to { onColorModeChange(ThemeColorMode.LIGHT) }
            ).forEach { (triple, action) ->
                val (title, subtitle, selected) = triple
                SourceSettingRow(title = title, subtitle = subtitle, selected = selected, onClick = action)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Accent & Palette",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            listOf(
                Triple("Dynamic", "Luxury Orange & Artwork", currentThemeMode == AppThemeMode.DYNAMIC) to { onThemeChange(AppThemeMode.DYNAMIC) },
                Triple("Monochrome", "Minimal Slate", currentThemeMode == AppThemeMode.MONOCHROME) to { onThemeChange(AppThemeMode.MONOCHROME) },
                Triple("Pitch Black (AMOLED)", "Pure black #000000", currentThemeMode == AppThemeMode.AMOLED) to { onThemeChange(AppThemeMode.AMOLED) }
            ).forEach { (triple, action) ->
                val (title, subtitle, selected) = triple
                SourceSettingRow(title = title, subtitle = subtitle, selected = selected, onClick = action)
            }

            // ── HISTORY & SYNC ───────────────────────────────────────
            SectionTitle("History & Sync")
            SettingsSwitchRow(
                title = "Sync Played Tracks",
                subtitle = "Post playback pings to Spotify account",
                checked = syncSpotifyHistory,
                onCheckedChange = {
                    syncSpotifyHistory = it
                    prefs.edit().putBoolean("pref_sync_history", it).apply()
                }
            )
            HorizontalDivider(color = Color(0xFF222222), modifier = Modifier.padding(vertical = 2.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showRecentsSection = !showRecentsSection }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Spotify Account Recents", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("${spotifyRecents.size} tracks fetched", color = Color(0xFFB3B3B3), fontSize = 12.sp)
                }
                Icon(
                    imageVector = if (showRecentsSection) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFFB3B3B3)
                )
            }
            AnimatedVisibility(visible = showRecentsSection) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isRecentsLoading) {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = primaryColor)
                        }
                    } else if (spotifyRecents.isEmpty()) {
                        Text(
                            "No Spotify recents recorded yet.",
                            color = Color(0xFFB3B3B3),
                            fontSize = 12.sp,
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
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp))
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(track.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(track.artist, color = Color(0xFFB3B3B3), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                IconButton(onClick = { onPlayTrack(track) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", tint = primaryColor, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // ── ABOUT ────────────────────────────────────────────────
            SectionTitle("About & Engine")
            SettingsInfoRow("App Version", "Stopify 1.2.0 (Build 43)")
            SettingsInfoRow("DRM Engine", "Widevine L3 Software CDM")
            SettingsInfoRow("Audio Engine", "AndroidX Media3 ExoPlayer")
            SettingsInfoRow("Client Platform", "Web Player 4.80 (Harmony API)")
            Spacer(Modifier.height(40.dp))
        }
    }
}
