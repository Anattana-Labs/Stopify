package com.shashwat.stopify

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.ui.zIndex
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.shashwat.stopify.data.*
import com.shashwat.stopify.player.PlaybackState
import com.shashwat.stopify.player.SpotifyAudioPlayer
import com.shashwat.stopify.ui.components.*
import com.shashwat.stopify.ui.player.*
import com.shashwat.stopify.ui.screens.*
import com.shashwat.stopify.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    companion object {
        var globalPrefs: android.content.SharedPreferences? = null
    }

    private val spotifyClient = SpotifyWebClient()
    private lateinit var audioPlayer: SpotifyAudioPlayer
    private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<androidx.media3.session.MediaController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        audioPlayer = SpotifyAudioPlayer.getInstance(this)

        val sessionToken = androidx.media3.session.SessionToken(
            this,
            android.content.ComponentName(this, com.shashwat.stopify.player.PlaybackService::class.java)
        )
        controllerFuture = androidx.media3.session.MediaController.Builder(this, sessionToken).buildAsync()



        // Restore saved session tokens, cookies and playback history if present
        val prefs = getSharedPreferences("stopify_prefs", MODE_PRIVATE)
        globalPrefs = prefs
        val savedClientToken = prefs.getString("spotify_client_token", null)
        if (!savedClientToken.isNullOrEmpty()) {
            spotifyClient.setClientToken(savedClientToken)
        }
        val savedCookies = prefs.getString("spotify_cookies", null)
        if (!savedCookies.isNullOrEmpty() && SpotifyWebClient.hasValidSpDc(savedCookies)) {
            spotifyClient.updateCookies(savedCookies)
        }
        val savedAccessToken = prefs.getString("spotify_access_token", null)
        if (!savedAccessToken.isNullOrEmpty()) {
            spotifyClient.setAccessToken(savedAccessToken)
        }
        
        // Trigger token and cookie refresh in background webview (showing blocker only if no token is saved yet)
        SpotifyTokenManager.refreshAccessTokenInBackground(
            this,
            spotifyClient,
            showLoadingModal = false
        )

        // Set up the token refresh hook for automatic background generation when an API gives up (401)
        spotifyClient.tokenRefreshHook = {
            val token = SpotifyTokenManager.awaitTokenRefresh(this@MainActivity, spotifyClient)
            !token.isNullOrEmpty()
        }

        // Initialize download manager with spotifyClient
        com.shashwat.stopify.data.StopifyDownloadManager.getInstance(this).client = spotifyClient

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        setContent {
            val playbackState by audioPlayer.playbackState.collectAsState()
            val context = LocalContext.current
            var artworkSeedColor by remember { mutableStateOf<Color?>(null) }
            val savedThemeModeStr = prefs.getString("stopify_theme_mode", AppThemeMode.DYNAMIC.name) ?: AppThemeMode.DYNAMIC.name
            var currentThemeMode by remember {
                mutableStateOf(
                    try {
                        AppThemeMode.valueOf(savedThemeModeStr)
                    } catch (e: Exception) {
                        AppThemeMode.DYNAMIC
                    }
                )
            }
            val savedColorModeStr = prefs.getString("stopify_color_mode", ThemeColorMode.SYSTEM.name) ?: ThemeColorMode.SYSTEM.name
            var currentColorMode by remember {
                mutableStateOf(
                    try {
                        ThemeColorMode.valueOf(savedColorModeStr)
                    } catch (e: Exception) {
                        ThemeColorMode.SYSTEM
                    }
                )
            }

            var dynamicColorEnabled by remember { mutableStateOf(prefs.getBoolean("pref_dynamic_color", true)) }

            val savedGlassStr = prefs.getString("stopify_glass_style", GlassStyle.BLUR.name) ?: GlassStyle.BLUR.name
            var currentGlassStyle by remember {
                mutableStateOf(
                    try { GlassStyle.valueOf(savedGlassStr) } catch (e: Exception) { GlassStyle.BLUR }
                )
            }

            // Dynamic color extraction from active track artwork
            LaunchedEffect(playbackState.currentTrack?.coverUrl, dynamicColorEnabled) {
                val coverUrl = playbackState.currentTrack?.coverUrl
                if (dynamicColorEnabled && !coverUrl.isNullOrEmpty()) {
                    withContext(Dispatchers.IO) {
                        try {
                            val req = ImageRequest.Builder(context)
                                .data(coverUrl)
                                .allowHardware(false)
                                .build()
                            val result = context.imageLoader.execute(req)
                            if (result is SuccessResult) {
                                val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                                if (bitmap != null) {
                                    val palette = Palette.from(bitmap).generate()
                                    val rgb = palette.vibrantSwatch?.rgb
                                        ?: palette.dominantSwatch?.rgb
                                        ?: palette.mutedSwatch?.rgb
                                    if (rgb != null) {
                                        artworkSeedColor = Color(rgb)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Fallback to default palette
                        }
                    }
                } else {
                    artworkSeedColor = null
                }
            }

            StopifyTheme(
                themeMode = currentThemeMode,
                colorMode = currentColorMode,
                seedColor = artworkSeedColor,
                dynamicColors = dynamicColorEnabled
            ) {
                CompositionLocalProvider(LocalGlassStyle provides currentGlassStyle) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    SimpMusicApp(
                        client = spotifyClient,
                        player = audioPlayer,
                        themeMode = currentThemeMode,
                        onThemeChange = { newMode ->
                            currentThemeMode = newMode
                            prefs.edit().putString("stopify_theme_mode", newMode.name).apply()
                        },
                        colorMode = currentColorMode,
                        onColorModeChange = { newColorMode ->
                            currentColorMode = newColorMode
                            prefs.edit().putString("stopify_color_mode", newColorMode.name).apply()
                        },
                        dynamicColorEnabled = dynamicColorEnabled,
                        onDynamicColorToggle = { on ->
                            dynamicColorEnabled = on
                            prefs.edit().putBoolean("pref_dynamic_color", on).apply()
                        },
                        glassStyle = currentGlassStyle,
                        onGlassStyleChange = { g ->
                            currentGlassStyle = g
                            prefs.edit().putString("stopify_glass_style", g.name).apply()
                        },
                        prefs = prefs
                    )

                }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controllerFuture?.let {
            androidx.media3.session.MediaController.releaseFuture(it)
        }
        if (isFinishing) {
            audioPlayer.releasePlayer()
        }
    }
}

@Composable
fun SimpMusicApp(
    modifier: Modifier = Modifier,
    client: SpotifyWebClient,
    player: SpotifyAudioPlayer,
    themeMode: AppThemeMode = AppThemeMode.DYNAMIC,
    onThemeChange: (AppThemeMode) -> Unit = {},
    colorMode: ThemeColorMode = ThemeColorMode.SYSTEM,
    onColorModeChange: (ThemeColorMode) -> Unit = {},
    dynamicColorEnabled: Boolean = true,
    onDynamicColorToggle: (Boolean) -> Unit = {},
    glassStyle: GlassStyle = GlassStyle.BLUR,
    onGlassStyleChange: (GlassStyle) -> Unit = {},
    prefs: android.content.SharedPreferences? = null
) {
    val coroutineScope = remember { kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + Dispatchers.Main.immediate) }
    val context = LocalContext.current
    val appContext = context.applicationContext
    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
    var homeData by remember { mutableStateOf<SpotifyHomeData?>(null) }
    val homeListState = rememberLazyListState()
    var isHomeLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SpotifyMediaItem>>(emptyList()) }
    var isSearchLoading by remember { mutableStateOf(false) }
    var statusLog by remember { mutableStateOf("") }
    var showCookieDialog by remember { mutableStateOf(false) }
    var cookieDialogInitialTab by remember { mutableIntStateOf(0) }
    val hasCompletedOnboarding = prefs?.getBoolean("onboarding_completed", false) == true
    val hasAnySession = SpotifyWebClient.hasValidSpDc(client.customCookies) || 
            SpotifyWebClient.hasValidSpDc(prefs?.getString("spotify_cookies", null))
    var showOnboarding by remember { mutableStateOf(!hasCompletedOnboarding && !hasAnySession) }
    var showFullPlayer by remember { mutableStateOf(false) }
    val playbackState by player.playbackState.collectAsState()
    val playbackQueue = remember { PlaybackQueueStore.queue }
    val previousTracks = remember { PlaybackQueueStore.previous }
    val playbackHistory = remember { mutableStateListOf<SpotifyTrack>() }
    var isAutoQueueEnabled by remember { mutableStateOf(true) }
    // Auto queue bookkeeping: which queue entries were picked by autoplay, and whether the song now
    // playing was chosen by the user (then autoplay starts fresh for it, unless it came from a group).
    val autoAdded = remember { mutableSetOf<String>() }
    var needFreshAuto by remember { mutableStateOf(true) }
    var isShuffle by remember { mutableStateOf(false) }
    var isRepeat by remember { mutableStateOf(false) }

    // Navigation Subscreen overlays
    var viewingAlbumUri by remember { mutableStateOf<String?>(null) }
    var viewingPlaylistUri by remember { mutableStateOf<String?>(null) }
    var viewingArtistUri by remember { mutableStateOf<String?>(null) }
    var viewingArtistName by remember { mutableStateOf<String?>(null) }
    var viewingArtistCover by remember { mutableStateOf<String?>(null) }
    var viewingShowUri by remember { mutableStateOf<String?>(null) }
    var viewingShowName by remember { mutableStateOf<String?>(null) }
    var viewingShowCover by remember { mutableStateOf<String?>(null) }
    var viewingSettings by remember { mutableStateOf(false) }
    var viewingRecents by remember { mutableStateOf(false) }
    var trackToAddToPlaylist by remember { mutableStateOf<SpotifyTrack?>(null) }
    var favoriteTrackIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var userProfile by remember { mutableStateOf<SpotifyUserProfile?>(null) }
    var canvasEnabled by remember { mutableStateOf(prefs?.getBoolean("pref_canvas_enabled", true) ?: true) }

    // History: local plays are recorded instantly and persisted; Spotify's recents are merged underneath.
    fun recordLocalPlay(track: SpotifyTrack) {
        val merged = SpotifyHistoryManager.withPlay(playbackHistory.toList(), track)
        playbackHistory.clear()
        playbackHistory.addAll(merged)
        prefs?.let { SpotifyHistoryManager.saveLocal(it, merged) }
    }

    suspend fun mergeRemoteHistory() {
        val recentsRes = client.fetchSpotifyRecentlyPlayed(prefs)
        if (recentsRes.isSuccess) {
            val merged = SpotifyHistoryManager.mergeWithRemote(playbackHistory.toList(), recentsRes.getOrThrow())
            playbackHistory.clear()
            playbackHistory.addAll(merged)
            prefs?.let { SpotifyHistoryManager.saveLocal(it, merged) }
        }
    }

    // Initialize playback history and favorites purely from live Spotify data
    LaunchedEffect(Unit) {
        prefs?.let {
            client.loadFavoritesFromPrefs(it)
            favoriteTrackIds = it.getStringSet("favorite_tracks_set", emptySet())?.toSet() ?: emptySet()
            it.edit().remove("local_playback_history_v2").apply()
        }
        // Show the saved history immediately, then merge Spotify's recents in the background
        prefs?.let {
            playbackHistory.clear()
            playbackHistory.addAll(SpotifyHistoryManager.loadLocal(it))
        }

        coroutineScope.launch {
            val uRes = client.fetchCurrentUserProfile(prefs)
            if (uRes.isSuccess) {
                userProfile = uRes.getOrNull()
            }
        }
        coroutineScope.launch {
            mergeRemoteHistory()
        }
    }

    LaunchedEffect(Unit) {
        val downloadManager = com.shashwat.stopify.data.StopifyDownloadManager.getInstance(appContext)
        downloadManager.downloadStatusMessage.collect { msg ->
            if (!msg.isNullOrEmpty()) {
                statusLog = msg
            }
        }
    }

    val onToggleFavoriteLambda: (SpotifyTrack) -> Unit = { track ->
        val wasFav = favoriteTrackIds.contains(track.id)
        val newFav = !wasFav
        favoriteTrackIds = if (newFav) favoriteTrackIds + track.id else favoriteTrackIds - track.id
        statusLog = if (newFav) "Added to Favourites" else "Removed from Favourites"
        coroutineScope.launch {
            client.setTrackFavorite(track, newFav, prefs)
        }
    }

    // Lyrics background color and Spotify Canvas URL for player synchronization
    var currentTrackLyricsColor by remember { mutableStateOf<Color?>(null) }
    var currentCanvasUrl by remember { mutableStateOf<String?>(null) }
    var hasReported30sScrobble by remember { mutableStateOf(false) }
    var hasReportedStartedPlaying by remember { mutableStateOf(false) }

    // Fetch color lyrics and canvas for the current track whenever it changes or canvasEnabled changes
    LaunchedEffect(playbackState.currentTrack?.id, canvasEnabled) {
        val current = playbackState.currentTrack
        hasReported30sScrobble = false
        hasReportedStartedPlaying = false
        if (!canvasEnabled) {
            currentCanvasUrl = null
        }
        if (current != null) {
            val cached = client.getCachedColorLyrics(current.id)
            if (cached != null) {
                currentTrackLyricsColor = argbToColor(cached.backgroundColor)
            } else {
                val res = client.fetchColorLyrics(current.id, current.coverUrl)
                if (res.isSuccess) {
                    currentTrackLyricsColor = argbToColor(res.getOrThrow().backgroundColor)
                }
            }
            if (canvasEnabled) {
                currentCanvasUrl = client.fetchCanvasUrl(current.id)
            } else {
                currentCanvasUrl = null
            }
        } else {
            currentTrackLyricsColor = null
            currentCanvasUrl = null
        }
    }

    // Playback progress ticker and discrete state machine event reporting
    LaunchedEffect(playbackState.isPlaying, playbackState.currentTrack?.id) {
        while (playbackState.isPlaying) {
            player.updateProgress()
            val track = playbackState.currentTrack
            if (track != null) {
                // Report started_playing after playback begins (~1s)
                if (!hasReportedStartedPlaying && playbackState.currentPositionMs >= 1000L) {
                    hasReportedStartedPlaying = true
                    coroutineScope.launch {
                        client.reportPlaybackProgress(track, playbackState.currentPositionMs, prefs, "started_playing")
                    }
                }

                // 30s milestone report (played_threshold_reached) - scrobbles to Spotify Recently Played
                if (!hasReported30sScrobble && playbackState.currentPositionMs >= 30000L) {
                    hasReported30sScrobble = true
                    coroutineScope.launch {
                        client.reportPlaybackProgress(track, playbackState.currentPositionMs, prefs, "played_threshold_reached")
                        delay(2500)
                        mergeRemoteHistory()
                    }
                }
            }
            delay(500)
        }
    }

    // Only one load is ever in flight: a newer tap cancels the older resolve so stale results can't override it.
    val playJobHolder = remember { arrayOfNulls<kotlinx.coroutines.Job>(1) }

    /** Resolves (or finds offline) the audio and hands it to the player. Returns false if it could not start. */
    suspend fun startStream(track: SpotifyTrack, forceFreshToken: Boolean): Boolean {
        val downloadManager = com.shashwat.stopify.data.StopifyDownloadManager.getInstance(appContext)
        val localAudio = downloadManager.getDownloadedAudioFile(track.id)
        if (localAudio != null && localAudio.exists()) {
            val token = client.getAccessToken().getOrElse { SpotifyWebClient.FALLBACK_BEARER_TOKEN }
            statusLog = "Playing offline '${track.name}'"
            player.playTrack(track, android.net.Uri.fromFile(localAudio).toString(), token, client.customClientToken)
            return true
        }
        statusLog = "Resolving stream for '${track.name}'..."
        if (forceFreshToken) SpotifyTokenManager.awaitTokenRefresh(appContext, client)
        // First failure usually means an expired session token: renew it silently and retry once.
        val streamRes = client.resolveTrackAudioStream(track.uri).let { first ->
            if (first.isSuccess) first else {
                SpotifyTokenManager.awaitTokenRefresh(appContext, client)
                client.resolveTrackAudioStream(track.uri)
            }
        }
        if (streamRes.isFailure) {
            statusLog = "Stream error: ${streamRes.exceptionOrNull()?.message}"
            return false
        }
        val token = client.getAccessToken().getOrElse { SpotifyWebClient.FALLBACK_BEARER_TOKEN }
        statusLog = "Playing '${track.name}'"
        player.playTrack(track, streamRes.getOrThrow(), token, client.customClientToken)
        return true
    }

    fun playTrackInternal(track: SpotifyTrack, addToHistory: Boolean = true, addToPrevious: Boolean = true, userChosen: Boolean = true) {
        if (userChosen) {
            // A song the user picked: the earlier autoplay picks are dropped and a fresh set is made for it.
            playbackQueue.removeAll { it.uri in autoAdded }
            autoAdded.clear()
            needFreshAuto = true
        }
        if (addToHistory) recordLocalPlay(track)
        // Instantly notify player of loading state so buffering spinners show immediately
        player.notifyLoading(track)

        val current = playbackState.currentTrack
        if (addToPrevious && current != null && current.uri != track.uri) {
            previousTracks.add(current)
            if (previousTracks.size > 50) previousTracks.removeAt(0)
        }

        playJobHolder[0]?.cancel()
        playJobHolder[0] = coroutineScope.launch {
            var started = false
            var attempt = 0
            while (!started && attempt < 3) {
                if (startStream(track, forceFreshToken = attempt > 0)) {
                    // Watchdog: a track still buffering after 20s, or one that errors, is retried.
                    val state = kotlinx.coroutines.withTimeoutOrNull(20_000L) {
                        player.playbackState.first { it.currentTrack?.id == track.id && (!it.isBuffering || it.error != null) }
                    }
                    if (state != null && state.error == null) started = true
                } else {
                    delay(600)
                }
                attempt++
            }
            if (!started) {
                player.notifyFailed(track, "Couldn't load this track. Tap play to try again.")
                return@launch
            }

            if (addToHistory) {
                launch {
                    client.reportPlaybackToApi(track, 0L, prefs)
                    client.connectService.updateConnectClusterState(
                        track = track,
                        isPlaying = true,
                        positionMs = 0L,
                        deviceId = SpotifyWebClient.getDeviceId(prefs)
                    )
                }
            }
            playbackQueue.firstOrNull()?.let { nextQ -> launch { client.preResolveTrack(nextQ.uri) } }

        }
    }

    fun playNextTrack() {
        if (playbackQueue.isNotEmpty()) {
            val next = playbackQueue.removeAt(if (isShuffle) playbackQueue.indices.random() else 0)
            playTrackInternal(next, userChosen = false)
            // Pre-resolve subsequent track
            playbackQueue.firstOrNull()?.let { nextNext ->
                coroutineScope.launch { client.preResolveTrack(nextNext.uri) }
            }
        } else {
            val current = playbackState.currentTrack
            if (current != null && isAutoQueueEnabled) {
                statusLog = "Autoqueue fetching next tracks for '${current.artist}'..."
                coroutineScope.launch {
                    val autoTracks = client.fetchAutoplayQueue(current, (playbackHistory.take(30) + previousTracks.takeLast(20)).map { it.uri }.toSet())
                    val filtered = autoTracks.filter { it.uri != current.uri }
                    if (filtered.isNotEmpty()) {
                        val next = filtered.first()
                        if (filtered.size > 1) {
                            playbackQueue.addAll(filtered.drop(1))
                            playbackQueue.firstOrNull()?.let { client.preResolveTrack(it.uri) }
                        }
                        playTrackInternal(next, userChosen = false)
                    }
                }
            }
        }
    }

    fun playPreviousTrack() {
        if (playbackState.currentPositionMs > 3000L) {
            player.seekTo(0L)
        } else if (previousTracks.isNotEmpty()) {
            val prev = previousTracks.removeAt(previousTracks.lastIndex)
            playTrackInternal(prev, addToPrevious = false, userChosen = false)
        } else {
            player.seekTo(0L)
        }
    }

    // Autoqueue and history scrobble on track completion hook
    DisposableEffect(player) {
        player.onTrackCompleted = { completedTrack ->
            coroutineScope.launch {
                if (isRepeat) {
                    playTrackInternal(completedTrack, addToHistory = false, addToPrevious = false, userChosen = false)
                    return@launch
                }
                launch {
                    client.reportPlaybackToApi(completedTrack, completedTrack.durationMs, prefs, isFinished = true)
                }
                playNextTrack()
            }
        }
        player.onSkipToNext = {
            playNextTrack()
        }
        player.onSkipToPrevious = {
            playPreviousTrack()
        }
        player.onNearEnd = { cur ->
            coroutineScope.launch {
                if (isRepeat || client.connectCluster.value.playingElsewhere) return@launch
                var next: SpotifyTrack? = if (playbackQueue.isNotEmpty()) {
                    if (isShuffle) playbackQueue.random() else playbackQueue.first()
                } else null
                if (next == null && isAutoQueueEnabled) {
                    val auto = client.fetchAutoplayQueue(cur, (playbackHistory.take(30) + previousTracks.takeLast(20)).map { it.uri }.toSet())
                        .filter { it.uri != cur.uri }
                    if (auto.isNotEmpty()) {
                        playbackQueue.addAll(auto)
                        next = auto.first()
                    }
                }
                val n = next ?: return@launch
                val local = com.shashwat.stopify.data.StopifyDownloadManager.getInstance(appContext).getDownloadedAudioFile(n.id)
                val url = if (local != null) android.net.Uri.fromFile(local).toString()
                else client.resolveTrackAudioStream(n.uri).getOrNull()
                if (url != null && player.playbackState.value.currentTrack?.id == cur.id) {
                    val token = client.getAccessToken().getOrElse { SpotifyWebClient.FALLBACK_BEARER_TOKEN }
                    player.prepareNext(n, url, token, client.customClientToken)
                }
            }
        }
        player.onAutoAdvanced = { finished, started ->
            val idx = playbackQueue.indexOfFirst { it.uri == started.uri }
            if (idx >= 0) playbackQueue.removeAt(idx)
            previousTracks.add(finished)
            if (previousTracks.size > 50) previousTracks.removeAt(0)
            recordLocalPlay(started)
            coroutineScope.launch {
                client.reportPlaybackToApi(finished, finished.durationMs, prefs, isFinished = true)
                client.reportPlaybackToApi(started, 0L, prefs)
            }
        }
        // Callbacks stay registered when the activity goes away: playback must keep advancing with the screen off.
        onDispose { }
    }

    // Auto queue: the list Spotify's own player shows after the playing track. It is read from the
    // playback report's answer; on every new track the earlier auto picks are replaced by it, anything
    // the user queued stays.
    val autoTrack = playbackState.currentTrack
    LaunchedEffect(autoTrack?.uri, playbackQueue.size, isAutoQueueEnabled) {
        val cur = autoTrack ?: return@LaunchedEffect
        if (!isAutoQueueEnabled || cur.uri.startsWith("spotify:episode:")) return@LaunchedEffect
        if (needFreshAuto) {
            // Chosen by the user. From a group (album, playlist, own queue): keep that group's order,
            // autoplay only takes over once it runs out. Alone: make a fresh set for this song.
            if (playbackQueue.any { it.uri !in autoAdded }) { needFreshAuto = false; return@LaunchedEffect }
        } else if (playbackQueue.size >= 3) return@LaunchedEffect
        val auto = try {
            client.fetchAutoplayQueue(cur, (playbackHistory.take(30) + previousTracks.takeLast(20)).map { it.uri }.toSet())
        } catch (e: Exception) { emptyList() }
        needFreshAuto = false
        val fresh = auto.filter { a -> a.uri != cur.uri && playbackQueue.none { q -> q.uri == a.uri } }
        if (fresh.isNotEmpty()) {
            autoAdded.addAll(fresh.map { it.uri })
            playbackQueue.addAll(fresh)
            client.preResolveTrack(playbackQueue.first().uri)
        }
    }

    /**
     * Plays a list (album, playlist, artist top songs, podcast episodes) from [startIndex]: the songs after it
     * become the queue in order (shuffled when asked), so the rest of the context plays before autoplay takes over.
     */
    fun playContext(tracks: List<SpotifyTrack>, startIndex: Int, shuffle: Boolean) {
        val playable = tracks.filter { it.isPlayable }
        if (playable.isEmpty()) return
        val start = tracks.getOrNull(startIndex)?.takeIf { it.isPlayable }
            ?: playable.first()
        val rest = playable.filter { it.uri != start.uri }
        val ordered = if (shuffle) rest.shuffled() else {
            val at = playable.indexOfFirst { it.uri == start.uri }
            playable.drop(at + 1)
        }
        val first = if (shuffle) playable.random() else start
        val queueRest = if (shuffle) playable.filter { it.uri != first.uri }.shuffled() else ordered
        playbackQueue.clear()
        playbackQueue.addAll(queueRest)
        playTrackInternal(first)
    }

    /**
     * Resolves and plays or opens any media item.
     * Songs and albums clicked on Home play immediately!
     */
    fun playMediaItem(item: SpotifyMediaItem, autoPlayAlbums: Boolean = false) {
        if (item.uri.contains(":track:") || item.type == SpotifyItemType.TRACK) {
            playTrackInternal(item.toTrack())
            return
        }
        if (item.uri.contains(":episode:") || item.type == SpotifyItemType.EPISODE) {
            playTrackInternal(item.toTrack().copy(artist = item.subtitle, album = item.subtitle))
            return
        }
        if (item.uri.contains(":show:") || item.type == SpotifyItemType.PODCAST) {
            viewingShowUri = item.uri
            viewingShowName = item.name
            viewingShowCover = item.coverUrl
            return
        }
        if (item.uri.contains(":artist:") || item.type == SpotifyItemType.ARTIST) {
            viewingArtistUri = item.uri
            viewingArtistName = item.name
            viewingArtistCover = item.coverUrl
            return
        }
        if (item.uri.contains(":playlist:") || item.uri.startsWith("spotify:user:") || item.type == SpotifyItemType.PLAYLIST) {
            viewingPlaylistUri = item.uri
            return
        }
        if (item.uri.contains(":album:") || item.type == SpotifyItemType.ALBUM) {
            if (!autoPlayAlbums) {
                // Tapping an album opens its screen (track list + Play all); it never starts playback by itself.
                viewingAlbumUri = item.uri
                return
            }
            coroutineScope.launch {
                statusLog = "Playing '${item.name}'..."
                val albumRes = client.fetchAlbumDetails(item.uri)
                if (albumRes.isSuccess) {
                    val album = albumRes.getOrThrow()
                    if (album.tracks.isNotEmpty()) {
                        playTrackInternal(album.tracks[0])
                        for (i in 1 until album.tracks.size) {
                            playbackQueue.add(album.tracks[i])
                        }
                        return@launch
                    }
                }
                viewingAlbumUri = item.uri
            }
            return
        }
        statusLog = "Selected: ${item.name}"
    }

    // Seeking right after a stream is started is lost; wait until the track is loaded first.
    suspend fun seekWhenReady(trackId: String, ms: Long) {
        if (ms <= 0L) return
        kotlinx.coroutines.withTimeoutOrNull(20_000L) {
            player.playbackState.first { it.currentTrack?.id == trackId && !it.isBuffering && it.durationMs > 0 && it.error == null }
        }
        player.seekTo(ms)
    }

    // Spotify Connect Remote Control sync (handles play/pause/seek/skip from browser, desktop, phone)
    DisposableEffect(client, player) {
        client.onRemoteCommandListener = object : SpotifyRemoteCommandListener {
            override fun onPlay(contextUri: String?, trackUri: String?, seekToMs: Long?) {
                coroutineScope.launch {
                    val target = trackUri ?: contextUri
                    if (!target.isNullOrEmpty()) {
                        if (target.contains(":track:")) {
                            val remoteTrack = SpotifyTrack(
                                id = target.removePrefix("spotify:track:"),
                                uri = target,
                                name = "Playing via Spotify Connect",
                                artist = "",
                                album = "",
                                coverUrl = null,
                                durationMs = 0L,
                                isPlayable = true
                            )
                            client.invalidateConnectPlay()
                            playTrackInternal(remoteTrack)
                            seekToMs?.let { seekWhenReady(remoteTrack.id, it) }
                        } else {
                            playMediaItem(SpotifyMediaItem(
                                id = target.substringAfterLast(":"),
                                uri = target,
                                name = "Remote Context",
                                subtitle = "",
                                coverUrl = null,
                                type = if (target.contains(":album:")) SpotifyItemType.ALBUM else SpotifyItemType.PLAYLIST
                            ), autoPlayAlbums = true)
                        }
                    } else {
                        player.play()
                    }
                }
            }

            override fun onPause() {
                player.pause()
            }

            override fun onResume() {
                player.play()
            }

            override fun onSeekTo(positionMs: Long) {
                player.seekTo(positionMs)
            }

            override fun onSkipNext() {
                playNextTrack()
            }

            override fun onSkipPrev() {
                playPreviousTrack()
            }

            override fun onSetVolume(fraction: Float) {
                player.setVolume(fraction)
            }
        }

        onDispose {
            client.onRemoteCommandListener = null
        }
    }

    // Tell Spotify when playback is paused or resumed (what other devices show). Spotify's own player does
    // this with the same state report as its progress updates; the first start is reported on load.
    var lastReportedPlaying by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(playbackState.isPlaying, playbackState.currentTrack?.uri, playbackState.isBuffering) {
        val t = playbackState.currentTrack ?: return@LaunchedEffect
        if (client.connectCluster.value.playingElsewhere || playbackState.isBuffering) return@LaunchedEffect
        val prev = lastReportedPlaying
        lastReportedPlaying = playbackState.isPlaying
        if (prev == null || prev == playbackState.isPlaying) return@LaunchedEffect
        kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
            client.reportPauseState(t, player.playbackState.value.currentPositionMs, paused = !playbackState.isPlaying)
        }
    }

    // Home loads silently. Existing content is never cleared or replaced by a spinner;
    // failures retry quietly in the background with backoff.
    fun loadHome(force: Boolean = false) {
        if (isHomeLoading) return
        if (homeData != null && !force) return
        isHomeLoading = true
        coroutineScope.launch {
            var attempt = 0
            while (attempt < 4) {
                val res = client.fetchHomeSections()
                if (res.isSuccess) {
                    val fresh = res.getOrThrow()
                    if (fresh.sections.isNotEmpty() || homeData == null) homeData = fresh
                    break
                }
                attempt++
                delay(1500L * attempt)
            }
            mergeRemoteHistory()
            isHomeLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadHome()
    }

    // A fresh token only retries Home when it never loaded; it never reloads visible content.
    LaunchedEffect(Unit) {
        SpotifyTokenManager.tokenRefreshEvents.collect {
            if (homeData == null) loadHome()
        }
    }

    fun performSearch(query: String) {
        if (query.isBlank()) {
            searchResults = emptyList()
            return
        }
        isSearchLoading = true
        coroutineScope.launch {
            val res = client.searchMulti(query, null)
            if (res.isSuccess) {
                searchResults = res.getOrThrow()
            }
            isSearchLoading = false
        }
    }

    // Sub-screens stack in the order they were opened: whichever opened last sits on top (an album opened from
    // an artist page must cover it, and back must close it first).
    val zCounter = remember { intArrayOf(0) }
    val settingsZ = remember(viewingSettings) { if (viewingSettings) ++zCounter[0] else 0 }
    val albumZ = remember(viewingAlbumUri) { if (viewingAlbumUri != null) ++zCounter[0] else 0 }
    val playlistZ = remember(viewingPlaylistUri) { if (viewingPlaylistUri != null) ++zCounter[0] else 0 }
    val artistZ = remember(viewingArtistUri) { if (viewingArtistUri != null) ++zCounter[0] else 0 }
    val recentsZ = remember(viewingRecents) { if (viewingRecents) ++zCounter[0] else 0 }
    val showZ = remember(viewingShowUri) { if (viewingShowUri != null) ++zCounter[0] else 0 }

    // Back handling
    BackHandler(enabled = viewingShowUri != null || viewingRecents || viewingSettings || viewingAlbumUri != null || viewingPlaylistUri != null || viewingArtistUri != null) {
        val top = maxOf(maxOf(settingsZ, albumZ), maxOf(maxOf(playlistZ, artistZ), maxOf(recentsZ, showZ)))
        when (top) {
            showZ -> viewingShowUri = null
            recentsZ -> viewingRecents = false
            settingsZ -> viewingSettings = false
            albumZ -> viewingAlbumUri = null
            playlistZ -> viewingPlaylistUri = null
            else -> viewingArtistUri = null
        }
    }

    // Registered after the other handlers so back closes the full player first
    BackHandler(enabled = showFullPlayer) { showFullPlayer = false }

    // ── Spotify Connect sync ──────────────────────────────────────────────────
    // Keep the Dealer socket up while signed in, mirror what other devices play, and let the user move playback.
    val cluster by client.connectCluster.collectAsState()
    var showDevices by remember { mutableStateOf(false) }
    var localPlayStartedAt by remember { mutableLongStateOf(0L) }
    var remoteTick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val remoteNow = if (cluster.playingElsewhere) cluster.nowPlaying else null
    val remoteDeviceName = if (cluster.playingElsewhere) cluster.activeDevice?.name else null

    LaunchedEffect(client.customCookies) {
        if (SpotifyWebClient.hasValidSpDc(client.customCookies)) {
            val token = client.getAccessToken().getOrNull()
            if (token != null) client.ensureDeviceRegistered(token)
        }
    }
    LaunchedEffect(playbackState.isPlaying) {
        if (playbackState.isPlaying) localPlayStartedAt = System.currentTimeMillis()
    }
    // Another device took over: stop here instead of playing two things at once.
    LaunchedEffect(cluster.activeDeviceId) {
        if (cluster.playingElsewhere && playbackState.isPlaying &&
            System.currentTimeMillis() - localPlayStartedAt > 6000L
        ) {
            player.pause()
            statusLog = "Playing on ${cluster.activeDevice?.name ?: "another device"}"
        }
    }
    LaunchedEffect(remoteNow?.isPlaying, remoteNow?.trackUri) {
        while (remoteNow?.isPlaying == true) {
            remoteTick = System.currentTimeMillis()
            delay(500)
        }
    }
    val remoteTrack = remember(remoteNow?.trackUri, remoteNow?.title, remoteNow?.coverUrl) {
        remoteNow?.let {
            SpotifyTrack(
                id = it.trackUri.substringAfterLast(":"),
                uri = it.trackUri,
                name = it.title.ifBlank { "Playing on another device" },
                artist = it.artist,
                album = "",
                coverUrl = it.coverUrl,
                durationMs = it.durationMs
            )
        }
    }
    val uiTrack: SpotifyTrack? = remoteTrack ?: playbackState.currentTrack
    val uiPlayback: PlaybackState = if (remoteNow != null && remoteTrack != null) {
        PlaybackState(
            currentTrack = remoteTrack,
            isPlaying = remoteNow.isPlaying,
            currentPositionMs = remoteNow.livePositionMs(remoteTick),
            durationMs = remoteNow.durationMs
        )
    } else playbackState
    val remoteId = cluster.activeDeviceId

    fun remote(block: suspend (String) -> Unit) {
        val id = remoteId ?: return
        coroutineScope.launch { block(id) }
    }

    fun selectDevice(d: ConnectDevice) {
        showDevices = false
        if (d.isActive) return
        coroutineScope.launch {
            if (d.isSelf) {
                // Pull playback back to this phone from whichever device has it.
                val rt = remoteTrack
                val pos = remoteNow?.livePositionMs() ?: 0L
                val wasPlaying = remoteNow?.isPlaying == true
                remoteId?.let { client.connectControl.pause(it) }
                if (rt != null) {
                    // Forget the earlier "play" command for this song, so Spotify makes this device the active one again.
                    client.invalidateConnectPlay()
                    playTrackInternal(rt, addToHistory = false, addToPrevious = false, userChosen = false)
                    seekWhenReady(rt.id, pos)
                    if (!wasPlaying) player.pause()
                }
            } else {
                val ok = client.connectControl.transfer(d.id, playbackState.isPlaying)
                if (ok) {
                    client.invalidateConnectPlay()
                    if (playbackState.isPlaying) player.pause()
                    statusLog = "Playing on ${d.name}"
                } else statusLog = "Could not reach ${d.name}"
            }
        }
    }

    val backdrop = rememberBackdrop(MaterialTheme.colorScheme.background)

    CompositionLocalProvider(LocalBackdrop provides backdrop) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
        // Content container that serves as the backdrop source for glass surfaces
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
        ) {
            // Main Tab Content
        when (currentTab) {
            NavigationTab.HOME -> {
                SimpHomeScreen(
                    homeData = homeData,
                    isLoading = isHomeLoading,
                    onRefresh = { loadHome(force = true) },
                    listState = homeListState,
                    onOpenSettings = { viewingSettings = true },
                    onOpenRecents = { viewingRecents = true },
                    onMediaItemClick = { playMediaItem(it) },
                    recentlyPlayed = playbackHistory,
                    onPlayTrack = { playTrackInternal(it) },
                    userProfile = userProfile
                )
            }
            NavigationTab.SEARCH -> {
                SimpSearchScreen(
                    searchQuery = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { performSearch(it) },
                    searchResults = searchResults,
                    isSearching = isSearchLoading,
                    playbackState = playbackState,
                    onMediaItemClick = { playMediaItem(it) },
                    onPlayTrack = { playTrackInternal(it) },
                    onPlayNext = { track ->
                        playbackQueue.add(0, track)
                        statusLog = "'${track.name}' will play next"
                    },
                    onAddToQueue = { track ->
                        playbackQueue.add(track)
                        statusLog = "Added '${track.name}' to queue"
                    },
                    onAddToPlaylist = { trackToAddToPlaylist = it },
                    onViewAlbum = { viewingAlbumUri = it },
                    onViewArtist = { artistUri, artistName ->
                        viewingArtistUri = artistUri
                        viewingArtistName = artistName
                        viewingArtistCover = null
                    },
                    onToggleFavorite = onToggleFavoriteLambda,
                    isFavorite = { favoriteTrackIds.contains(it.id) }
                )
            }
            NavigationTab.LIBRARY -> {
                SimpLibraryScreen(
                    client = client,
                    playbackHistory = playbackHistory,
                    playbackState = playbackState,
                    onOpenSettings = { viewingSettings = true },
                    onPlayTrack = { playTrackInternal(it) },
                    onPlayCollection = { uri, name ->
                        if (uri.contains(":track:")) {
                            // If track uri, play directly!
                            val tr = SpotifyTrack(
                                id = uri.substringAfterLast(":"),
                                uri = uri,
                                name = name,
                                artist = "",
                                album = "",
                                coverUrl = null,
                                durationMs = 0L
                            )
                            playTrackInternal(tr)
                        } else if (uri.contains(":album:")) {
                            viewingAlbumUri = uri
                        } else if (uri.contains(":artist:")) {
                            viewingArtistUri = uri
                            viewingArtistName = name
                            viewingArtistCover = null
                        } else if (uri.contains(":show:")) {
                            viewingShowUri = uri
                            viewingShowName = name
                            viewingShowCover = null
                        } else if (uri.contains(":audiobook:")) {
                            statusLog = "Audiobooks aren't playable here yet"
                        } else {
                            viewingPlaylistUri = uri
                        }
                    },
                    onPlayNext = { track ->
                        playbackQueue.add(0, track)
                        statusLog = "'${track.name}' will play next"
                    },
                    onAddToQueue = { track ->
                        playbackQueue.add(track)
                        statusLog = "Added '${track.name}' to queue"
                    },
                    onAddToPlaylist = { trackToAddToPlaylist = it },
                    onViewAlbum = { viewingAlbumUri = it },
                    onViewArtist = { artistUri, artistName ->
                        viewingArtistUri = artistUri
                        viewingArtistName = artistName
                        viewingArtistCover = null
                    },
                    onToggleFavorite = onToggleFavoriteLambda,
                    isFavorite = { favoriteTrackIds.contains(it.id) },
                    userProfile = userProfile
                )
            }
        }

        // Subscreen Overlays (Animated slide-in). They live inside the backdrop source so glass
        // on top of a sub-screen refracts that sub-screen, not the tab underneath it.
        AnimatedVisibility(
            visible = viewingAlbumUri != null,
            modifier = Modifier.fillMaxSize().zIndex(albumZ.toFloat()),
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            viewingAlbumUri?.let { albUri ->
                SimpAlbumScreen(
                    albumUri = albUri,
                    client = client,
                    playbackState = playbackState,
                    onBack = { viewingAlbumUri = null },
                    onPlayTrack = { playTrackInternal(it) },
                    onPlayContext = { l, i, sh -> playContext(l, i, sh) },
                    onTogglePlayback = { player.togglePlayPause() },
                    onPlayNext = { track ->
                        playbackQueue.add(0, track)
                        statusLog = "'${track.name}' will play next"
                    },
                    onAddToQueue = { track ->
                        playbackQueue.add(track)
                        statusLog = "Added '${track.name}' to queue"
                    },
                    onAddToPlaylist = { trackToAddToPlaylist = it },
                    onViewArtist = { artistUri, artistName ->
                        viewingArtistUri = artistUri
                        viewingArtistName = artistName
                        viewingArtistCover = null
                    },
                    onToggleFavorite = onToggleFavoriteLambda,
                    isFavorite = { favoriteTrackIds.contains(it.id) }
                )
            }
        }

        AnimatedVisibility(
            visible = viewingPlaylistUri != null,
            modifier = Modifier.fillMaxSize().zIndex(playlistZ.toFloat()),
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            viewingPlaylistUri?.let { plUri ->
                SimpPlaylistScreen(
                    playlistUri = plUri,
                    client = client,
                    playbackState = playbackState,
                    onBack = { viewingPlaylistUri = null },
                    onPlayTrack = { playTrackInternal(it) },
                    onPlayContext = { l, i, sh -> playContext(l, i, sh) },
                    onTogglePlayback = { player.togglePlayPause() },
                    onPlayNext = { track ->
                        playbackQueue.add(0, track)
                        statusLog = "'${track.name}' will play next"
                    },
                    onAddToQueue = { track ->
                        playbackQueue.add(track)
                        statusLog = "Added '${track.name}' to queue"
                    },
                    onAddToPlaylist = { trackToAddToPlaylist = it },
                    onViewAlbum = { viewingAlbumUri = it },
                    onViewArtist = { artistUri, artistName ->
                        viewingArtistUri = artistUri
                        viewingArtistName = artistName
                        viewingArtistCover = null
                    },
                    onToggleFavorite = onToggleFavoriteLambda,
                    isFavorite = { favoriteTrackIds.contains(it.id) }
                )
            }
        }

        AnimatedVisibility(
            visible = viewingArtistUri != null,
            modifier = Modifier.fillMaxSize().zIndex(artistZ.toFloat()),
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            viewingArtistUri?.let { artUri ->
                SimpArtistScreen(
                    artistUri = artUri,
                    artistName = viewingArtistName ?: "Artist",
                    artistCoverUrl = viewingArtistCover,
                    client = client,
                    playbackState = playbackState,
                    onBack = { viewingArtistUri = null },
                    onPlayTrack = { playTrackInternal(it) },
                    onPlayContext = { l, i, sh -> playContext(l, i, sh) },
                    onTogglePlayback = { player.togglePlayPause() },
                    onViewArtist = { u, n -> viewingArtistUri = u; viewingArtistName = n; viewingArtistCover = null },
                    onPlayNext = { track ->
                        playbackQueue.add(0, track)
                        statusLog = "'${track.name}' will play next"
                    },
                    onAddToQueue = { track ->
                        playbackQueue.add(track)
                        statusLog = "Added '${track.name}' to queue"
                    },
                    onAddToPlaylist = { trackToAddToPlaylist = it },
                    onViewAlbum = { viewingAlbumUri = it },
                    onToggleFavorite = onToggleFavoriteLambda,
                    isFavorite = { favoriteTrackIds.contains(it.id) }
                )
            }
        }

        AnimatedVisibility(
            visible = viewingShowUri != null,
            modifier = Modifier.fillMaxSize().zIndex(showZ.toFloat()),
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            viewingShowUri?.let { shUri ->
                SimpShowScreen(
                    showUri = shUri,
                    showName = viewingShowName ?: "Podcast",
                    showCoverUrl = viewingShowCover,
                    client = client,
                    playbackState = playbackState,
                    onBack = { viewingShowUri = null },
                    onPlayContext = { l, i, sh -> playContext(l, i, sh) },
                    onTogglePlayback = { player.togglePlayPause() },
                    onPlayNext = { track ->
                        playbackQueue.add(0, track)
                        statusLog = "'${track.name}' will play next"
                    },
                    onAddToQueue = { track ->
                        playbackQueue.add(track)
                        statusLog = "Added '${track.name}' to queue"
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = viewingRecents,
            modifier = Modifier.fillMaxSize().zIndex(recentsZ.toFloat()),
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            com.shashwat.stopify.ui.screens.SimpRecentsScreen(
                tracks = playbackHistory.toList(),
                playbackState = playbackState,
                onBack = { viewingRecents = false },
                onPlayTrack = { playTrackInternal(it) },
                onPlayAll = { list ->
                    if (list.isNotEmpty()) {
                        playbackQueue.clear()
                        playbackQueue.addAll(list.drop(1))
                        playTrackInternal(list.first())
                    }
                },
                onPlayNext = { track ->
                    playbackQueue.add(0, track)
                    statusLog = "'${track.name}' will play next"
                },
                onAddToQueue = { track ->
                    playbackQueue.add(track)
                    statusLog = "Added '${track.name}' to queue"
                },
                onAddToPlaylist = { trackToAddToPlaylist = it },
                onViewAlbum = { viewingAlbumUri = it },
                onViewArtist = { artistUri, artistName ->
                    viewingArtistUri = artistUri
                    viewingArtistName = artistName
                        viewingArtistCover = null
                },
                onToggleFavorite = onToggleFavoriteLambda,
                isFavorite = { favoriteTrackIds.contains(it.id) }
            )
        }

        AnimatedVisibility(
            visible = viewingSettings,
            modifier = Modifier.fillMaxSize().zIndex(settingsZ.toFloat()),
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            SimpSettingsScreen(
                client = client,
                currentThemeMode = themeMode,
                onThemeChange = onThemeChange,
                currentColorMode = colorMode,
                onColorModeChange = onColorModeChange,
                currentGlassStyle = glassStyle,
                onGlassStyleChange = onGlassStyleChange,
                dynamicColorEnabled = dynamicColorEnabled,
                onDynamicColorToggle = onDynamicColorToggle,
                onOpenCookieManager = { showCookieDialog = true },
                onPlayTrack = { playTrackInternal(it) },
                onPlayNext = { track ->
                    playbackQueue.add(0, track)
                    statusLog = "'${track.name}' will play next"
                },
                onAddToQueue = { track ->
                    playbackQueue.add(track)
                    statusLog = "Added '${track.name}' to queue"
                },
                onAddToPlaylist = { trackToAddToPlaylist = it },
                onBack = { viewingSettings = false },
                onViewAlbum = { viewingAlbumUri = it },
                onViewArtist = { artistUri, artistName ->
                    viewingArtistUri = artistUri
                    viewingArtistName = artistName
                        viewingArtistCover = null
                },
                onToggleFavorite = onToggleFavoriteLambda,
                isFavorite = { favoriteTrackIds.contains(it.id) },
                userProfile = userProfile,
                canvasEnabled = canvasEnabled,
                onCanvasToggle = { enabled ->
                    canvasEnabled = enabled
                    prefs?.edit()?.putBoolean("pref_canvas_enabled", enabled)?.apply()
                    if (!enabled) {
                        currentCanvasUrl = null
                    }
                }
            )
        }

        } // end of backdrop source

    // Bottom dock: player strip + navigation fused into one object
    SimpDock(
        modifier = Modifier.align(Alignment.BottomCenter).zIndex(1000f),
        currentTab = currentTab,
        onTabSelect = { tab ->
            viewingAlbumUri = null
            viewingPlaylistUri = null
            viewingArtistUri = null
            viewingSettings = false
            viewingRecents = false
            currentTab = tab
        },
        hasPlayer = uiTrack != null,
        player = {
            uiTrack?.let { currentTrack ->
                SimpBottomMiniPlayer(
                    track = currentTrack,
                    playbackState = uiPlayback,
                    backdrop = backdrop,
                    onTogglePlayPause = {
                        if (remoteNow != null) remote { if (remoteNow.isPlaying) client.connectControl.pause(it) else client.connectControl.resume(it) }
                        else if (playbackState.error != null) playTrackInternal(currentTrack, addToHistory = false, addToPrevious = false, userChosen = false)
                        else player.togglePlayPause()
                    },
                    onNextTrack = { if (remoteNow != null) remote { client.connectControl.skipNext(it) } else playNextTrack() },
                    onPreviousTrack = { if (remoteNow != null) remote { client.connectControl.skipPrev(it) } else playPreviousTrack() },
                    onSeekTo = { if (remoteNow != null) remote { id -> client.connectControl.seekTo(id, it) } else player.seekTo(it) },
                    onExpand = { showFullPlayer = true },
                    isFavorite = favoriteTrackIds.contains(currentTrack.id),
                    onToggleFavorite = { onToggleFavoriteLambda(currentTrack) },
                    backgroundColor = if (dynamicColorEnabled) currentTrackLyricsColor else null,
                    deviceName = remoteDeviceName
                )
            }
        }
    )

    if (showDevices) {
        ConnectDevicesSheet(
            cluster = cluster,
            onDismiss = { showDevices = false },
            onSelect = { selectDevice(it) },
            onVolume = { d, v ->
                if (d.isSelf) player.setVolume(v)
                else coroutineScope.launch { client.connectControl.setVolume(d.id, v) }
            }
        )
    }

    // Full Screen Player Dialog / Sheet
        AnimatedVisibility(
            visible = showFullPlayer && uiTrack != null,
            modifier = Modifier
                .zIndex(2000f)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            enter = slideInVertically(tween(480, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f))) { it } +
                fadeIn(tween(320)) + scaleIn(tween(480, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)), initialScale = 0.96f),
            exit = slideOutVertically(tween(340, easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f))) { it } +
                fadeOut(tween(260)) + scaleOut(tween(340), targetScale = 0.96f)
        ) {
            uiTrack?.let { currentTrack ->
                val fullBackdrop = rememberBackdrop(MaterialTheme.colorScheme.background)
                CompositionLocalProvider(LocalBackdrop provides fullBackdrop) {
                Box(Modifier.fillMaxSize().layerBackdrop(fullBackdrop)) {
                SimpFullPlayer(
                    track = currentTrack,
                    playbackState = uiPlayback,
                    client = client,
                    canvasUrl = if (canvasEnabled && remoteNow == null) currentCanvasUrl else null,
                    canvasEnabled = canvasEnabled,
                    deviceName = remoteDeviceName,
                    onOpenDevices = { showDevices = true },
                    onTogglePlayPause = {
                        if (remoteNow != null) remote { if (remoteNow.isPlaying) client.connectControl.pause(it) else client.connectControl.resume(it) }
                        else if (playbackState.error != null) playTrackInternal(currentTrack, addToHistory = false, addToPrevious = false, userChosen = false)
                        else player.togglePlayPause()
                    },
                    isShuffle = remoteNow?.shuffle ?: isShuffle,
                    isRepeat = remoteNow?.let { it.repeatContext || it.repeatTrack } ?: isRepeat,
                    onToggleShuffle = {
                        if (remoteNow != null) {
                            val on = !remoteNow.shuffle
                            remote { client.connectControl.setShuffle(it, on) }
                        } else isShuffle = !isShuffle
                    },
                    onToggleRepeat = {
                        if (remoteNow != null) {
                            val on = !(remoteNow.repeatContext || remoteNow.repeatTrack)
                            remote { client.connectControl.setRepeat(it, if (on) 1 else 0) }
                        } else isRepeat = !isRepeat
                    },
                    onNext = { if (remoteNow != null) remote { client.connectControl.skipNext(it) } else playNextTrack() },
                    onPrevious = { if (remoteNow != null) remote { client.connectControl.skipPrev(it) } else playPreviousTrack() },
                    onSeekTo = { if (remoteNow != null) remote { id -> client.connectControl.seekTo(id, it) } else player.seekTo(it) },
                    onCollapse = { showFullPlayer = false },
                    playbackQueue = playbackQueue,
                    onClearQueue = { playbackQueue.clear() },
                    onPlayTrackFromQueue = { index ->
                        if (index in playbackQueue.indices) {
                            val target = playbackQueue.removeAt(index)
                            playTrackInternal(target, userChosen = false)
                        }
                    },
                    onMoveQueueItem = { from, to ->
                        if (from in playbackQueue.indices && to in playbackQueue.indices) {
                            val item = playbackQueue.removeAt(from)
                            playbackQueue.add(to, item)
                        }
                    },
                    onRemoveFromQueue = { index ->
                        if (index in playbackQueue.indices) {
                            playbackQueue.removeAt(index)
                        }
                    },
                    onAddToPlaylist = { trackToAddToPlaylist = it },
                    onViewAlbum = { albUri ->
                        showFullPlayer = false
                        viewingAlbumUri = albUri
                    },
                    onPlayNext = { track ->
                        playbackQueue.add(0, track)
                        statusLog = "'${track.name}' will play next"
                    },
                    onAddToQueue = { track ->
                        playbackQueue.add(track)
                        statusLog = "Added '${track.name}' to queue"
                    },
                    onViewArtist = { artistUri, artistName ->
                        showFullPlayer = false
                        viewingArtistUri = artistUri
                        viewingArtistName = artistName
                        viewingArtistCover = null
                        viewingArtistCover = playbackState.currentTrack?.coverUrl
                    },
                    onToggleFavorite = onToggleFavoriteLambda,
                    isFavoriteTrack = playbackState.currentTrack?.id?.let { favoriteTrackIds.contains(it) } ?: false,
                    isFavorite = { favoriteTrackIds.contains(it.id) }
                )
                }
                }
            }
        }

        // Add to Playlist Dialog
        if (trackToAddToPlaylist != null) {
            AddToPlaylistDialog(
                track = trackToAddToPlaylist!!,
                client = client,
                prefs = prefs,
                onDismiss = { trackToAddToPlaylist = null },
                onTrackAdded = { plName ->
                    statusLog = "Added to '$plName'!"
                    trackToAddToPlaylist = null
                }
            )
        }

        // Cookie & Session Manager Dialog
        if (showCookieDialog) {
            SpotifyCookieDialog(
                client = client,
                initialTab = cookieDialogInitialTab,
                onDismiss = {
                    showCookieDialog = false
                    if (SpotifyWebClient.hasValidSpDc(client.customCookies) || SpotifyWebClient.hasValidSpDc(prefs?.getString("spotify_cookies", null))) {
                        prefs?.edit()?.putBoolean("onboarding_completed", true)?.apply()
                        showOnboarding = false
                    }
                },
                onCookiesUpdated = {
                    prefs?.edit()?.putBoolean("onboarding_completed", true)?.apply()
                    showOnboarding = false
                    statusLog = "Session updated"
                }
            )
        }

        // Onboarding Screen (shown on first launch)
        if (showOnboarding) {
            com.shashwat.stopify.ui.screens.OnboardingScreen(
                onSelectWebLogin = {
                    cookieDialogInitialTab = 0
                    showCookieDialog = true
                },
                onSelectCookiesTxt = {
                    cookieDialogInitialTab = 1
                    showCookieDialog = true
                },
                onSkip = {
                    prefs?.edit()?.putBoolean("onboarding_completed", true)?.apply()
                    showOnboarding = false
                }
            )
        }
        }
    }
}