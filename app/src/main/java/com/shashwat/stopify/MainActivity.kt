package com.shashwat.stopify

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
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
        if (!savedCookies.isNullOrEmpty()) {
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
            showLoadingModal = savedAccessToken.isNullOrEmpty()
        )

        // Load persisted playback history from SharedPreferences
        spotifyClient.loadHistoryFromPrefs(prefs)

        // Set up the token refresh hook for automatic background generation when an API gives up (401)
        spotifyClient.tokenRefreshHook = {
            val token = SpotifyTokenManager.awaitTokenRefresh(this@MainActivity, spotifyClient)
            !token.isNullOrEmpty()
        }

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

            // Dynamic color extraction from active track artwork
            LaunchedEffect(playbackState.currentTrack?.coverUrl) {
                val coverUrl = playbackState.currentTrack?.coverUrl
                if (!coverUrl.isNullOrEmpty()) {
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

            val isSessionLoading by SpotifyTokenManager.isRefreshingState.collectAsState()

            StopifyTheme(
                themeMode = currentThemeMode,
                colorMode = currentColorMode,
                seedColor = artworkSeedColor
            ) {
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
                        prefs = prefs
                    )

                    // Full-screen non-dismissible loading overlay blocking all touch interactions
                    AnimatedVisibility(
                        visible = isSessionLoading,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.78f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { /* Intercept all touches */ }
                                )
                                .pointerInput(Unit) {
                                    // Consume all raw pointer input events to prevent underlying clicks
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            event.changes.forEach { it.consume() }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                                tonalElevation = 8.dp,
                                shadowElevation = 12.dp,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 3.5.dp,
                                        modifier = Modifier.size(46.dp)
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Text(
                                        text = "Connecting Session...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Refreshing Spotify session keys and authorization",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
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
    prefs: android.content.SharedPreferences? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
    var homeData by remember { mutableStateOf<SpotifyHomeData?>(null) }
    var isHomeLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SpotifyMediaItem>>(emptyList()) }
    var isSearchLoading by remember { mutableStateOf(false) }
    var statusLog by remember { mutableStateOf("") }
    var showCookieDialog by remember { mutableStateOf(false) }
    var cookieDialogInitialTab by remember { mutableIntStateOf(0) }
    val hasCompletedOnboarding = prefs?.getBoolean("onboarding_completed", false) == true
    val hasAnySession = !client.customCookies.isNullOrEmpty() || prefs?.getString("spotify_cookies", null)?.isNotEmpty() == true
    var showOnboarding by remember { mutableStateOf(!hasCompletedOnboarding && !hasAnySession) }
    var showFullPlayer by remember { mutableStateOf(false) }
    val playbackState by player.playbackState.collectAsState()
    val playbackQueue = remember { mutableStateListOf<SpotifyTrack>() }
    val previousTracks = remember { mutableStateListOf<SpotifyTrack>() }
    val playbackHistory = remember { mutableStateListOf<SpotifyTrack>() }
    var isAutoQueueEnabled by remember { mutableStateOf(true) }

    // Navigation Subscreen overlays
    var viewingAlbumUri by remember { mutableStateOf<String?>(null) }
    var viewingPlaylistUri by remember { mutableStateOf<String?>(null) }
    var viewingArtistUri by remember { mutableStateOf<String?>(null) }
    var viewingArtistName by remember { mutableStateOf<String?>(null) }
    var viewingArtistCover by remember { mutableStateOf<String?>(null) }
    var viewingSettings by remember { mutableStateOf(false) }
    var trackToAddToPlaylist by remember { mutableStateOf<SpotifyTrack?>(null) }
    var favoriteTrackIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var userProfile by remember { mutableStateOf<SpotifyUserProfile?>(null) }

    // Initialize playback history and favorites purely from live Spotify data
    LaunchedEffect(Unit) {
        prefs?.let {
            client.loadFavoritesFromPrefs(it)
            favoriteTrackIds = it.getStringSet("favorite_tracks_set", emptySet())?.toSet() ?: emptySet()
        }
        playbackHistory.clear()

        // Proactively fetch account profile and live remote recents directly from Spotify
        coroutineScope.launch {
            val uRes = client.fetchCurrentUserProfile(prefs)
            if (uRes.isSuccess) {
                userProfile = uRes.getOrNull()
            }
        }
        coroutineScope.launch {
            val recentsRes = client.fetchSpotifyRecentlyPlayed(prefs)
            if (recentsRes.isSuccess) {
                val list = recentsRes.getOrThrow()
                playbackHistory.clear()
                playbackHistory.addAll(list.distinctBy { it.uri })
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

    // Lyrics background color for miniplayer and full player synchronization
    var currentTrackLyricsColor by remember { mutableStateOf<Color?>(null) }
    var hasReported30sScrobble by remember { mutableStateOf(false) }
    var hasReportedStartedPlaying by remember { mutableStateOf(false) }

    // Fetch color lyrics for the current track whenever it changes
    LaunchedEffect(playbackState.currentTrack?.id) {
        val current = playbackState.currentTrack
        hasReported30sScrobble = false
        hasReportedStartedPlaying = false
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
        } else {
            currentTrackLyricsColor = null
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
                    }
                }
            }
            delay(500)
        }
    }

    fun playTrackInternal(track: SpotifyTrack, addToHistory: Boolean = true, addToPrevious: Boolean = true) {
        // Instantly notify player of loading state so buffering spinners show immediately
        player.notifyLoading(track)

        val current = playbackState.currentTrack
        if (addToPrevious && current != null && current.uri != track.uri) {
            previousTracks.add(current)
            if (previousTracks.size > 50) previousTracks.removeAt(0)
        }

        coroutineScope.launch {
            statusLog = "Resolving stream for '${track.name}'..."
            val streamRes = client.resolveTrackAudioStream(track.uri)
            if (streamRes.isSuccess) {
                val streamUrl = streamRes.getOrThrow()
                val token = client.getAccessToken().getOrElse { SpotifyWebClient.FALLBACK_BEARER_TOKEN }
                statusLog = "Playing '${track.name}'"
                player.playTrack(track, streamUrl, token)

                if (addToHistory) {
                    client.addLocalHistoryTrack(track, prefs)
                    playbackHistory.removeAll { it.uri == track.uri }
                    playbackHistory.add(0, track)
                    coroutineScope.launch {
                        client.reportPlaybackToApi(track, 0L, prefs)
                    }
                }

                // If queue is running low, proactively fetch autoqueue suggestions in background
                if (isAutoQueueEnabled && playbackQueue.size < 3) {
                    coroutineScope.launch {
                        val autoTracks = client.fetchAutoplayQueue(track)
                        val filtered = autoTracks.filter { it.uri != track.uri && playbackQueue.none { q -> q.uri == it.uri } }
                        if (filtered.isNotEmpty()) {
                            playbackQueue.addAll(filtered)
                        }
                    }
                }
            } else {
                val err = streamRes.exceptionOrNull()?.message
                statusLog = "Stream error: $err"
            }
        }
    }

    fun playNextTrack() {
        if (playbackQueue.isNotEmpty()) {
            val next = playbackQueue.removeAt(0)
            playTrackInternal(next)
        } else {
            val current = playbackState.currentTrack
            if (current != null && isAutoQueueEnabled) {
                statusLog = "Autoqueue fetching next tracks for '${current.artist}'..."
                coroutineScope.launch {
                    val autoTracks = client.fetchAutoplayQueue(current)
                    val filtered = autoTracks.filter { it.uri != current.uri }
                    if (filtered.isNotEmpty()) {
                        val next = filtered.first()
                        if (filtered.size > 1) {
                            playbackQueue.addAll(filtered.drop(1))
                        }
                        playTrackInternal(next)
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
            playTrackInternal(prev, addToPrevious = false)
        } else {
            player.seekTo(0L)
        }
    }

    // Autoqueue and history scrobble on track completion hook
    DisposableEffect(player) {
        player.onTrackCompleted = { completedTrack ->
            coroutineScope.launch {
                client.reportPlaybackToApi(completedTrack, completedTrack.durationMs, prefs, isFinished = true)
                playNextTrack()
            }
        }
        player.onSkipToNext = {
            playNextTrack()
        }
        player.onSkipToPrevious = {
            playPreviousTrack()
        }
        onDispose {
            player.onTrackCompleted = null
            player.onSkipToNext = null
            player.onSkipToPrevious = null
        }
    }

    /**
     * Resolves and plays or opens any media item.
     * Songs and albums clicked on Home play immediately!
     */
    fun playMediaItem(item: SpotifyMediaItem) {
        if (item.uri.contains(":track:") || item.type == SpotifyItemType.TRACK) {
            playTrackInternal(item.toTrack())
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

    fun loadHome() {
        if (isHomeLoading) return
        isHomeLoading = true
        coroutineScope.launch {
            val res = client.fetchHomeSections()
            if (res.isSuccess) {
                homeData = res.getOrThrow()
            }
            isHomeLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadHome()
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

    // Back handling
    BackHandler(enabled = viewingSettings || viewingAlbumUri != null || viewingPlaylistUri != null || viewingArtistUri != null) {
        when {
            viewingSettings -> viewingSettings = false
            viewingAlbumUri != null -> viewingAlbumUri = null
            viewingPlaylistUri != null -> viewingPlaylistUri = null
            viewingArtistUri != null -> viewingArtistUri = null
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
                    onRefresh = { loadHome() },
                    onOpenSettings = { viewingSettings = true },
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
                    },
                    onToggleFavorite = onToggleFavoriteLambda,
                    isFavorite = { favoriteTrackIds.contains(it.id) },
                    userProfile = userProfile
                )
            }
        }
    }

        // Subscreen Overlays (Animated slide-in)
        AnimatedVisibility(
            visible = viewingAlbumUri != null,
            modifier = Modifier.fillMaxSize(),
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
                    },
                    onToggleFavorite = onToggleFavoriteLambda,
                    isFavorite = { favoriteTrackIds.contains(it.id) }
                )
            }
        }

        AnimatedVisibility(
            visible = viewingPlaylistUri != null,
            modifier = Modifier.fillMaxSize(),
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
                    },
                    onToggleFavorite = onToggleFavoriteLambda,
                    isFavorite = { favoriteTrackIds.contains(it.id) }
                )
            }
        }

        AnimatedVisibility(
            visible = viewingArtistUri != null,
            modifier = Modifier.fillMaxSize(),
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
            visible = viewingSettings,
            modifier = Modifier.fillMaxSize(),
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            SimpSettingsScreen(
                client = client,
                currentThemeMode = themeMode,
                onThemeChange = onThemeChange,
                currentColorMode = colorMode,
                onColorModeChange = onColorModeChange,
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
                },
                onToggleFavorite = onToggleFavoriteLambda,
                isFavorite = { favoriteTrackIds.contains(it.id) },
                userProfile = userProfile
            )
        }

    // Bottom Controls Dock: MiniPlayer + SimpBottomNavigationBar (Spotui structure)
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.98f)
                    )
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mini Player
        playbackState.currentTrack?.let { currentTrack ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                SimpBottomMiniPlayer(
                    track = currentTrack,
                    playbackState = playbackState,
                    backdrop = backdrop,
                    onTogglePlayPause = {
                        if (playbackState.isPlaying) player.pause() else player.resume()
                    },
                    onNextTrack = { playNextTrack() },
                    onPreviousTrack = { playPreviousTrack() },
                    onSeekTo = { player.seekTo(it) },
                    onExpand = { showFullPlayer = true },
                    isFavorite = favoriteTrackIds.contains(currentTrack.id),
                    onToggleFavorite = { onToggleFavoriteLambda(currentTrack) },
                    backgroundColor = currentTrackLyricsColor
                )
            }
        }

        // Floating Nav Bar Dock
        SimpBottomNavigationBar(
            currentTab = currentTab,
            backdrop = backdrop,
            onTabSelect = { tab ->
                viewingAlbumUri = null
                viewingPlaylistUri = null
                viewingArtistUri = null
                viewingSettings = false
                currentTab = tab
            }
        )
    }

    // Full Screen Player Dialog / Sheet
        AnimatedVisibility(
            visible = showFullPlayer && playbackState.currentTrack != null,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            playbackState.currentTrack?.let { currentTrack ->
                SimpFullPlayer(
                    track = currentTrack,
                    playbackState = playbackState,
                    client = client,
                    onTogglePlayPause = {
                        if (playbackState.isPlaying) player.pause() else player.resume()
                    },
                    onNext = { playNextTrack() },
                    onPrevious = { playPreviousTrack() },
                    onSeekTo = { player.seekTo(it) },
                    onCollapse = { showFullPlayer = false },
                    playbackQueue = playbackQueue,
                    onClearQueue = { playbackQueue.clear() },
                    onPlayTrackFromQueue = { index ->
                        if (index in playbackQueue.indices) {
                            val target = playbackQueue.removeAt(index)
                            playTrackInternal(target)
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
                        viewingArtistCover = playbackState.currentTrack?.coverUrl
                    },
                    onToggleFavorite = onToggleFavoriteLambda,
                    isFavoriteTrack = playbackState.currentTrack?.id?.let { favoriteTrackIds.contains(it) } ?: false,
                    isFavorite = { favoriteTrackIds.contains(it.id) }
                )
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
                    if (!client.customCookies.isNullOrEmpty() || prefs?.getString("spotify_cookies", null)?.isNotEmpty() == true) {
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