package com.shashwat.stopify.data

import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

enum class SpotifyItemType {
    TRACK, ARTIST, ALBUM, PLAYLIST, PODCAST, EPISODE
}

data class SpotifyMediaItem(
    val id: String,
    val uri: String,
    val name: String,
    val subtitle: String,
    val coverUrl: String?,
    val type: SpotifyItemType,
    val durationMs: Long = 0L,
    val isPlayable: Boolean = true
) {
    fun toTrack(): SpotifyTrack = SpotifyTrack(
        id = id,
        uri = uri,
        name = name,
        artist = subtitle,
        album = subtitle,
        coverUrl = coverUrl,
        durationMs = durationMs,
        isPlayable = isPlayable
    )
}

data class SpotifyHomeSection(
    val title: String,
    val subtitle: String? = null,
    val items: List<SpotifyMediaItem>
)

data class SpotifyHomeData(
    val greeting: String,
    val sections: List<SpotifyHomeSection>
)

data class SpotifyTrack(
    val id: String,
    val uri: String,
    val name: String,
    val artist: String,
    val album: String,
    val coverUrl: String?,
    val durationMs: Long,
    val isPlayable: Boolean = true,
    val albumUri: String? = null,
    val artistUri: String? = null
)

data class SpotifyAlbumDetails(
    val uri: String,
    val id: String,
    val name: String,
    val artist: String,
    val coverUrl: String?,
    val releaseYear: String?,
    val trackCount: Int,
    val tracks: List<SpotifyTrack>
)

data class SpotifyPlaylistDetails(
    val uri: String,
    val id: String,
    val name: String,
    val ownerName: String,
    val description: String?,
    val coverUrl: String?,
    val trackCount: Int,
    val tracks: List<SpotifyTrack>
)

data class SpotifyPlaylistSummary(
    val id: String,
    val uri: String,
    val name: String,
    val ownerName: String,
    val coverUrl: String?,
    val trackCount: Int
)

data class SpotifyLyricLine(
    val startTimeMs: Long,
    val words: String,
    val endTimeMs: Long = 0L
)

data class SpotifyColorLyrics(
    val syncType: String,
    val lines: List<SpotifyLyricLine>,
    val backgroundColor: Int,
    val textColor: Int,
    val highlightTextColor: Int,
    val providerDisplayName: String = "Spotify"
)

class SpotifyWebClient {

    companion object {
        private const val TAG = "SpotifyWebClient"

        // Default session cookies (empty; loaded dynamically via user login / settings)
        const val SP_DC = ""
        const val SP_KEY = ""
        const val SP_T = ""

        // Client token captured from webplayer in open.spotify.com
        const val CLIENT_TOKEN = "AAGCCRiw7WlW3Oy33uN5L1dI2knSs986nqFaI1YTbq66MFLs0oeinjyYUbv2zo9+9kleXdls6EnujpM6OElxp5zJtj+0ZP9RU9I0ApWvFeXJwZRYmCVMWBngB3AN6Ux1imVm+8ImYf9OvVc3mJJBjxLH4S3k77qOsXB6EekBL9hZQgx8umkPOLZZEfolSOP8wURVuQX5sZjnwIfPmLf/8ydY/LeeqBGTvkyZwV4InPyVxMlOI7DQViCkvmXQ2ZkpQASm2NrJ40wdF+BcpIDZV0YA+czHR2bx2K+tTKIaNMa7GUTB/By4+ls8IO6TL4zLlLoKGWnXa8N0xzthzHW9B2zdsW/kTD7JttelP1GHt/1lDuiRvvqtp2WGx6ZeArHvStJTLeO+sZHGs6IxJBY="

        const val USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/155.0.0.0 Safari/537.36"
        const val APP_PLATFORM = "WebPlayer"
        const val SPOTIFY_APP_VERSION = "1.3.5.316.gd873ee9db135"

        // Pathfinder persisted query hashes
        const val GET_TRACK_HASH = "a8ef9e9f02b836feb0da3003c31dbb30decc6f4b473ef89ca88c882386d668de"
        const val SEARCH_TOP_RESULTS_HASH = "dd78eaff943eba629ed70ee25517b9cea0dcaa41193e2592ae2727660b21892c"
        const val SEARCH_TRACKS_HASH = "b02683192a98dde7966b5e6655a79eeb62713eab703eda9902c932818dd52751"
        const val SEARCH_ALBUMS_HASH = "202cb3305e31e5a0767ba7925f28bd728cf8f8b0217e6da43909056071cd70e9"
        const val SEARCH_ARTISTS_HASH = "7bf95d754fdbe32c8b161fbbe54d1ae50974900df4dce4c8f1afcbcad153224d"
        const val SEARCH_PLAYLISTS_HASH = "d520014e748f9ea44f7707d8df1819867ac1205e8b7f3e28f22fe5fc858921b1"
        const val SEARCH_PODCASTS_HASH = "0195d9f61b43606d490bca64c3456e3593528cea6cc05c7e822c7c42beed0f4e"
        const val FETCH_PLAYLIST_HASH = "8964e8eafb21aa992a7d951d256d83285c04be2105d209262901de70cb97584a"
        const val GET_ALBUM_HASH = "11c9cad9cb24c4f9e25ef9456f68621cfd6006a90f679e729c1d2f2fd70dfe16"
        const val HOME_HASH = "5fb7da7a03ee2776c0670856609cb85a189a3cecc36cc160b91cde79063b9cf1"
        const val LIBRARY_HASH = "390c78e5b951029bad359785e69b07b536a509c581cbcd0aded5e5067f187455"

        // Working fallback Bearer token from web session
        const val FALLBACK_BEARER_TOKEN = "BQDyP_PgW1Uj_1SVWN4PgYEDZ1wgIj2Dohsrx8jXZiXpxofEgaAqyImnIFyRCJ9_MtAWm2bUUyMFbpUaf_X1KCa9YnP24UgtRNzoU6q21uytBgXaKbMKISMlWGBkRwuG4QACxXbnaHkm-gBm8VPIVyopxU6GYPxxRtyuVxRk1yDBE27cNQJNdsETLt0dAGvTzB36iWQR8YFBSoLsvRGmvOSMHcxXad0fD8zg_Zt6muogKnlHAnIn0Q9kQN_lqwb3o7ipmbOqrMQ-7TVJaz4nkIF0GI5kIIHd8AdOaO9foLcF3RyjhKfI9r6yDcv5LR1iLqpgluuBasbhIObAGhUhrSpBYltpzTtKCGuBq_7n4_6H03ysecKrUEe2UVD8w3SfA_dJ8tN9RRajljHIbu-K"
        const val DEVICE_ID = "af6fd454eccef1714bc99a09a10c119366e8da1c"

        fun parseCookieInput(input: String): String {
            val trimmed = input.trim()
            if (trimmed.isEmpty()) return ""

            val cookieMap = mutableMapOf<String, String>()

            // Check if Netscape cookies.txt format (tab-separated columns)
            val lines = trimmed.lines()
            var isNetscape = false
            for (line in lines) {
                val lineTrim = line.trim()
                if (lineTrim.startsWith("#") || lineTrim.isEmpty()) continue
                val parts = lineTrim.split("\t")
                if (parts.size >= 7) {
                    isNetscape = true
                    val name = parts[5].trim()
                    val value = parts[6].trim()
                    cookieMap[name] = value
                }
            }

            if (isNetscape && cookieMap.isNotEmpty()) {
                return cookieMap.entries.joinToString("; ") { "${it.key}=${it.value}" }
            }

            // Otherwise parse semicolon or newline separated key=value pairs
            for (segment in trimmed.split(";", "\n")) {
                val seg = segment.trim()
                if (seg.isEmpty() || seg.startsWith("#")) continue
                val eqIdx = seg.indexOf('=')
                if (eqIdx > 0) {
                    val key = seg.substring(0, eqIdx).trim()
                    val value = seg.substring(eqIdx + 1).trim()
                    cookieMap[key] = value
                }
            }

            return if (cookieMap.isNotEmpty()) {
                cookieMap.entries.joinToString("; ") { "${it.key}=${it.value}" }
            } else {
                trimmed
            }
        }
    }

    private var cachedAccessToken: String? = null
    private var tokenExpiryMs: Long = 0

    @Volatile
    var customAccessToken: String? = null
        private set

    @Volatile
    var customClientToken: String? = null
        private set

    @Volatile
    var customCookies: String? = null
        private set

    val cookieHeaderString: String
        get() {
            customCookies?.let { if (it.isNotEmpty()) return it }
            val defaults = listOfNotNull(
                if (SP_DC.isNotEmpty()) "sp_dc=$SP_DC" else null,
                if (SP_KEY.isNotEmpty()) "sp_key=$SP_KEY" else null,
                if (SP_T.isNotEmpty()) "sp_t=$SP_T" else null
            ).joinToString("; ")
            return defaults
        }

    fun setAccessToken(token: String) {
        val clean = token.removePrefix("Bearer ").trim()
        if (clean.isNotEmpty()) {
            customAccessToken = clean
            cachedAccessToken = clean
            tokenExpiryMs = System.currentTimeMillis() + 3500000L
            Log.d(TAG, "Custom access token updated: ${clean.take(20)}...")
        }
    }

    fun setClientToken(token: String) {
        val clean = token.trim()
        if (clean.isNotEmpty()) {
            customClientToken = clean
            Log.d(TAG, "Custom client token updated: ${clean.take(20)}...")
        }
    }

    fun updateCookies(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return

        // Auto-detect access token in input
        val tokenRegex = Regex("""(BQ[A-Za-z0-9_-]{80,})""")
        val match = tokenRegex.find(trimmed)
        if (match != null) {
            setAccessToken(match.value)
        }

        val parsed = parseCookieInput(trimmed)
        customCookies = parsed
        Log.d(TAG, "Updated Spotify cookies: ${parsed.take(50)}...")
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private var activeWebSocket: WebSocket? = null
    private var dealerConnectionId: String? = null
    private var isDeviceRegistered = false
    private var connectionIdDeferred = CompletableDeferred<String>()

    private val audioFileMap = ConcurrentHashMap<String, String>()
    private val pendingTrackResolutions = ConcurrentHashMap<String, CompletableDeferred<String>>()

    @Volatile
    var tokenRefreshHook: (suspend () -> Boolean)? = null

    // Local playback history cache with persistent backing
    private val _playbackHistory = Collections.synchronizedList(mutableListOf<SpotifyTrack>())
    val playbackHistory: List<SpotifyTrack>
        get() = synchronized(_playbackHistory) { _playbackHistory.toList() }

    fun loadHistoryFromPrefs(prefs: android.content.SharedPreferences) {
        val jsonStr = prefs.getString("playback_history_json", null) ?: return
        try {
            val array = JSONArray(jsonStr)
            synchronized(_playbackHistory) {
                _playbackHistory.clear()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    _playbackHistory.add(
                        SpotifyTrack(
                            id = obj.getString("id"),
                            uri = obj.getString("uri"),
                            name = obj.getString("name"),
                            artist = obj.getString("artist"),
                            album = obj.optString("album", ""),
                            coverUrl = obj.optString("coverUrl", null).takeIf { it != "null" },
                            durationMs = obj.optLong("durationMs", 0L),
                            isPlayable = true
                        )
                    )
                }
            }
            Log.d(TAG, "Loaded ${_playbackHistory.size} history tracks from persistent storage")
        } catch (e: Exception) {
            Log.w(TAG, "Error loading history from prefs", e)
        }
    }

    fun saveHistoryToPrefs(prefs: android.content.SharedPreferences) {
        try {
            val array = JSONArray()
            synchronized(_playbackHistory) {
                for (track in _playbackHistory.take(50)) {
                    val obj = JSONObject().apply {
                        put("id", track.id)
                        put("uri", track.uri)
                        put("name", track.name)
                        put("artist", track.artist)
                        put("album", track.album)
                        put("coverUrl", track.coverUrl)
                        put("durationMs", track.durationMs)
                    }
                    array.put(obj)
                }
            }
            prefs.edit().putString("playback_history_json", array.toString()).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Error saving history to prefs", e)
        }
    }

    fun addLocalHistoryTrack(track: SpotifyTrack, prefs: android.content.SharedPreferences? = null) {
        synchronized(_playbackHistory) {
            _playbackHistory.removeAll { item: SpotifyTrack -> item.uri == track.uri }
            _playbackHistory.add(0, track)
            if (_playbackHistory.size > 100) {
                _playbackHistory.removeAt(_playbackHistory.lastIndex)
            }
        }
        if (prefs != null) {
            saveHistoryToPrefs(prefs)
        }
    }

    suspend fun reportPlaybackToApi(
        track: SpotifyTrack,
        positionMs: Long = 0L,
        prefs: android.content.SharedPreferences? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val token = getAccessToken().getOrElse { customAccessToken ?: cachedAccessToken ?: FALLBACK_BEARER_TOKEN }
        val clientToken = customClientToken ?: CLIENT_TOKEN

        try {
            // 1. Connect-State put_state to register active playback on Spotify Account
            try {
                val putStateUrl = URL("https://gae2-spclient.spotify.com/connect-state/v1/devices/put_state")
                val putStateConn = (putStateUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    doOutput = true
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("authorization", "Bearer $token")
                    setRequestProperty("client-token", clientToken)
                    setRequestProperty("content-type", "application/json")
                    setRequestProperty("app-platform", APP_PLATFORM)
                    setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                    setRequestProperty("origin", "https://open.spotify.com")
                    setRequestProperty("referer", "https://open.spotify.com/")
                    setRequestProperty("user-agent", USER_AGENT)
                    if (dealerConnectionId != null) {
                        setRequestProperty("x-spotify-connection-id", dealerConnectionId)
                    }
                }

                val putStatePayload = JSONObject().apply {
                    put("member_type", "CONNECT_STATE")
                    put("device", JSONObject().apply {
                        put("device_info", JSONObject().apply {
                            put("can_play", true)
                            put("volume", 65535)
                            put("name", "Web Player (Chrome)")
                            put("device_id", DEVICE_ID)
                            put("device_type", "computer")
                        })
                        put("player_state", JSONObject().apply {
                            put("timestamp", System.currentTimeMillis())
                            put("context_uri", track.uri)
                            put("track", JSONObject().apply {
                                put("uri", track.uri)
                                put("metadata", JSONObject().apply {
                                    put("title", track.name)
                                    put("artist_name", track.artist)
                                    put("album_title", track.album)
                                })
                            })
                            put("playback_speed", 1.0)
                            put("position_as_of_timestamp", positionMs)
                            put("duration", track.durationMs)
                            put("is_playing", true)
                            put("is_paused", false)
                        })
                    })
                    put("is_active", true)
                }

                OutputStreamWriter(putStateConn.outputStream).use { it.write(putStatePayload.toString()) }
                val psc = putStateConn.responseCode
                putStateConn.disconnect()
                Log.d(TAG, "Report playback to connect-state put_state: $psc")
            } catch (e: Exception) {
                Log.w(TAG, "Error reporting to connect-state put_state", e)
            }

            // 2. Report playback state to Spotify Connect / device track-playback endpoint
            val stateUrl = URL("https://gae2-spclient.spotify.com/track-playback/v1/devices/$DEVICE_ID/state")
            val conn = (stateUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("client-token", clientToken)
                setRequestProperty("content-type", "application/json")
                setRequestProperty("app-platform", APP_PLATFORM)
                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("referer", "https://open.spotify.com/")
                setRequestProperty("user-agent", USER_AGENT)
            }

            val body = JSONObject().apply {
                put("seq_num", 1)
                put("state_ref", JSONObject.NULL)
                put("sub_state", JSONObject().apply {
                    put("playback_speed", 1)
                    put("format", "file_urls_external")
                    put("is_video_on", false)
                })
                put("debug_source", "play")
            }

            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
            val code = conn.responseCode
            conn.disconnect()
            Log.d(TAG, "Report playback to track-playback API status: $code for '${track.name}'")

            // 3. Trigger connect-state play command to ensure Spotify backend registers account play
            try {
                val cmdUrl = URL("https://gae2-spclient.spotify.com/connect-state/v1/player/command/from/$DEVICE_ID/to/$DEVICE_ID")
                val cmdPayload = JSONObject().apply {
                    put("command", JSONObject().apply {
                        put("context", JSONObject().apply {
                            put("uri", track.uri)
                            put("url", "context://${track.uri}")
                            put("metadata", JSONObject())
                        })
                        put("play_origin", JSONObject().apply {
                            put("feature_identifier", "auto_play_on_load")
                            put("feature_version", "web-player_2026-10-09_1791504000000_d873ee9db135")
                            put("referrer_identifier", "search")
                        })
                        put("options", JSONObject().apply {
                            put("license", "tft")
                            put("skip_to", JSONObject())
                            put("player_options_override", JSONObject())
                        })
                        put("logging_params", JSONObject().apply {
                            put("page_instance_ids", JSONArray())
                            put("interaction_ids", JSONArray())
                            put("command_id", UUID.randomUUID().toString().replace("-", ""))
                        })
                        put("endpoint", "play")
                    })
                }

                val cmdConn = (cmdUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("authorization", "Bearer $token")
                    setRequestProperty("client-token", clientToken)
                    setRequestProperty("content-type", "application/json;charset=UTF-8")
                    setRequestProperty("app-platform", APP_PLATFORM)
                    setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                    setRequestProperty("origin", "https://open.spotify.com")
                    setRequestProperty("referer", "https://open.spotify.com/")
                    setRequestProperty("user-agent", USER_AGENT)
                }
                OutputStreamWriter(cmdConn.outputStream).use { it.write(cmdPayload.toString()) }
                val cmdCode = cmdConn.responseCode
                cmdConn.disconnect()
                Log.d(TAG, "Triggered connect-state account play command: $cmdCode")
            } catch (e: Exception) {
                Log.w(TAG, "Error sending connect-state play command", e)
            }

            Result.success(true)
        } catch (e: Exception) {
            Log.w(TAG, "Failed reporting track to Spotify history API", e)
            Result.failure(e)
        }
    }

    suspend fun fetchSpotifyRecentlyPlayed(): Result<List<SpotifyTrack>> = withContext(Dispatchers.IO) {
        val token = getAccessToken().getOrElse { FALLBACK_BEARER_TOKEN }
        try {
            val url = URL("https://api.spotify.com/v1/me/player/recently-played?limit=50")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "application/json")
            }
            val responseCode = conn.responseCode
            if (responseCode == 401) {
                invalidateTokens()
                conn.disconnect()
                return@withContext Result.failure(Exception("HTTP 401 Unauthorized"))
            }
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseBody = BufferedReader(InputStreamReader(stream)).readText()
            conn.disconnect()

            if (responseCode in 200..299) {
                val json = JSONObject(responseBody)
                val items = json.optJSONArray("items") ?: JSONArray()
                val list = mutableListOf<SpotifyTrack>()
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val tObj = item.optJSONObject("track") ?: continue
                    val uri = tObj.optString("uri", "")
                    val name = tObj.optString("name", "Unknown Track")
                    val durationMs = tObj.optLong("duration_ms", 0L)
                    val artistsArr = tObj.optJSONArray("artists")
                    val artistNames = mutableListOf<String>()
                    if (artistsArr != null) {
                        for (j in 0 until artistsArr.length()) {
                            val a = artistsArr.getJSONObject(j).optString("name")
                            if (!a.isNullOrEmpty()) artistNames.add(a)
                        }
                    }
                    val artist = artistNames.joinToString(", ").ifEmpty { "Artist" }
                    val albumObj = tObj.optJSONObject("album")
                    val albumName = albumObj?.optString("name", "Album") ?: "Album"
                    val coverUrl = albumObj?.optJSONArray("images")?.optJSONObject(0)?.optString("url")
                    if (uri.isNotEmpty() && list.none { it.uri == uri }) {
                        list.add(
                            SpotifyTrack(
                                id = uri.substringAfterLast(":"),
                                uri = uri,
                                name = name,
                                artist = artist,
                                album = albumName,
                                coverUrl = coverUrl,
                                durationMs = durationMs,
                                albumUri = albumObj?.optString("uri")
                            )
                        )
                    }
                }
                Result.success(list)
            } else {
                Result.failure(Exception("HTTP $responseCode: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUserPlaylists(prefs: android.content.SharedPreferences? = null): Result<List<SpotifyPlaylistSummary>> = withContext(Dispatchers.IO) {
        val results = mutableListOf<SpotifyPlaylistSummary>()
        val token = getAccessToken().getOrElse { FALLBACK_BEARER_TOKEN }

        try {
            val url = URL("https://api.spotify.com/v1/me/playlists?limit=50")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "application/json")
            }
            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val responseBody = BufferedReader(InputStreamReader(conn.inputStream)).readText()
                conn.disconnect()
                val json = JSONObject(responseBody)
                val items = json.optJSONArray("items") ?: JSONArray()
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val id = item.optString("id")
                    val uri = item.optString("uri", "spotify:playlist:$id")
                    val name = item.optString("name", "Playlist")
                    val owner = item.optJSONObject("owner")?.optString("display_name") ?: "Spotify"
                    val coverUrl = item.optJSONArray("images")?.optJSONObject(0)?.optString("url")
                    val total = item.optJSONObject("tracks")?.optInt("total", 0) ?: 0
                    results.add(SpotifyPlaylistSummary(id, uri, name, owner, coverUrl, total))
                }
            } else {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching web playlists", e)
        }

        if (prefs != null) {
            try {
                val localJson = prefs.getString("local_user_playlists_json", null)
                if (!localJson.isNullOrEmpty()) {
                    val array = JSONArray(localJson)
                    for (i in 0 until array.length()) {
                        val pObj = array.getJSONObject(i)
                        val id = pObj.optString("id")
                        val uri = pObj.optString("uri", "spotify:playlist:$id")
                        val name = pObj.optString("name", "My Playlist")
                        val cover = pObj.optString("coverUrl", null)
                        val tracksArr = pObj.optJSONArray("tracks") ?: JSONArray()
                        if (results.none { it.id == id }) {
                            results.add(SpotifyPlaylistSummary(id, uri, name, "You", cover, tracksArr.length()))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error loading local playlists", e)
            }
        }

        Result.success(results)
    }

    suspend fun createPlaylist(name: String, description: String = "", prefs: android.content.SharedPreferences? = null): Result<SpotifyPlaylistSummary> = withContext(Dispatchers.IO) {
        val token = getAccessToken().getOrElse { FALLBACK_BEARER_TOKEN }
        var createdOnline: SpotifyPlaylistSummary? = null

        try {
            val url = URL("https://api.spotify.com/v1/me/playlists")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "application/json")
            }
            val body = JSONObject().apply {
                put("name", name)
                put("description", description.ifEmpty { "Created with Stopify" })
                put("public", false)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
            val code = conn.responseCode
            if (code in 200..299) {
                val resText = BufferedReader(InputStreamReader(conn.inputStream)).readText()
                val json = JSONObject(resText)
                val id = json.optString("id")
                val uri = json.optString("uri", "spotify:playlist:$id")
                createdOnline = SpotifyPlaylistSummary(id, uri, name, "You", null, 0)
            }
            conn.disconnect()
        } catch (e: Exception) {
            Log.w(TAG, "Failed creating online playlist, saving locally", e)
        }

        val summary = createdOnline ?: SpotifyPlaylistSummary(
            id = "local_" + System.currentTimeMillis(),
            uri = "spotify:playlist:local_" + System.currentTimeMillis(),
            name = name,
            ownerName = "You",
            coverUrl = null,
            trackCount = 0
        )

        if (prefs != null) {
            try {
                val localJson = prefs.getString("local_user_playlists_json", "[]") ?: "[]"
                val array = JSONArray(localJson)
                val newP = JSONObject().apply {
                    put("id", summary.id)
                    put("uri", summary.uri)
                    put("name", summary.name)
                    put("ownerName", summary.ownerName)
                    put("tracks", JSONArray())
                }
                array.put(newP)
                prefs.edit().putString("local_user_playlists_json", array.toString()).apply()
            } catch (e: Exception) {
                Log.w(TAG, "Error saving new playlist to prefs", e)
            }
        }

        Result.success(summary)
    }

    suspend fun addTrackToPlaylist(playlistId: String, track: SpotifyTrack, prefs: android.content.SharedPreferences? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        val token = getAccessToken().getOrElse { FALLBACK_BEARER_TOKEN }

        if (!playlistId.startsWith("local_")) {
            try {
                val cleanId = playlistId.substringAfterLast(":")
                val url = URL("https://api.spotify.com/v1/playlists/$cleanId/tracks")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "application/json")
                }
                val body = JSONObject().apply {
                    put("uris", JSONArray().apply { put(track.uri) })
                }
                OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
                val code = conn.responseCode
                conn.disconnect()
            } catch (e: Exception) {
                Log.w(TAG, "Could not add track online: $playlistId", e)
            }
        }

        if (prefs != null) {
            try {
                val localJson = prefs.getString("local_user_playlists_json", "[]") ?: "[]"
                val array = JSONArray(localJson)
                var found = false
                for (i in 0 until array.length()) {
                    val p = array.getJSONObject(i)
                    if (p.optString("id") == playlistId || p.optString("uri") == playlistId) {
                        val tracks = p.optJSONArray("tracks") ?: JSONArray()
                        val trackObj = JSONObject().apply {
                            put("id", track.id)
                            put("uri", track.uri)
                            put("name", track.name)
                            put("artist", track.artist)
                            put("album", track.album)
                            put("coverUrl", track.coverUrl)
                            put("durationMs", track.durationMs)
                        }
                        tracks.put(trackObj)
                        p.put("tracks", tracks)
                        if (p.optString("coverUrl").isNullOrEmpty()) {
                            p.put("coverUrl", track.coverUrl)
                        }
                        found = true
                        break
                    }
                }
                if (!found) {
                    val newP = JSONObject().apply {
                        put("id", playlistId)
                        put("uri", if (playlistId.startsWith("spotify:")) playlistId else "spotify:playlist:$playlistId")
                        put("name", "Playlist")
                        put("ownerName", "You")
                        put("coverUrl", track.coverUrl)
                        put("tracks", JSONArray().apply {
                            put(JSONObject().apply {
                                put("id", track.id)
                                put("uri", track.uri)
                                put("name", track.name)
                                put("artist", track.artist)
                                put("album", track.album)
                                put("coverUrl", track.coverUrl)
                                put("durationMs", track.durationMs)
                            })
                        })
                    }
                    array.put(newP)
                }
                prefs.edit().putString("local_user_playlists_json", array.toString()).apply()
            } catch (e: Exception) {
                Log.w(TAG, "Error saving track to local playlist", e)
            }
        }

        Result.success(true)
    }

    val favoriteTrackIds: MutableSet<String> = Collections.synchronizedSet(mutableSetOf<String>())

    fun loadFavoritesFromPrefs(prefs: android.content.SharedPreferences) {
        try {
            val set = prefs.getStringSet("favorite_tracks_set", emptySet()) ?: emptySet()
            synchronized(favoriteTrackIds) {
                favoriteTrackIds.clear()
                favoriteTrackIds.addAll(set)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error loading favorites from prefs", e)
        }
    }

    fun isTrackFavorite(trackId: String): Boolean {
        return favoriteTrackIds.contains(trackId)
    }

    suspend fun setTrackFavorite(track: SpotifyTrack, isFavorite: Boolean, prefs: android.content.SharedPreferences? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        if (isFavorite) {
            favoriteTrackIds.add(track.id)
        } else {
            favoriteTrackIds.remove(track.id)
        }
        // 1. Immediately persist locally
        prefs?.let { p ->
            try {
                val currentSet = p.getStringSet("favorite_tracks_set", emptySet())?.toMutableSet() ?: mutableSetOf()
                if (isFavorite) currentSet.add(track.id) else currentSet.remove(track.id)
                p.edit().putStringSet("favorite_tracks_set", currentSet).apply()

                val metaJson = p.getString("favorite_tracks_meta", "{}") ?: "{}"
                val metaObj = try { JSONObject(metaJson) } catch (e: Exception) { JSONObject() }
                if (isFavorite) {
                    val tObj = JSONObject().apply {
                        put("id", track.id)
                        put("uri", track.uri)
                        put("name", track.name)
                        put("artist", track.artist)
                        put("album", track.album)
                        put("coverUrl", track.coverUrl ?: "")
                        put("durationMs", track.durationMs)
                        put("albumUri", track.albumUri ?: "")
                        put("artistUri", track.artistUri ?: "")
                    }
                    metaObj.put(track.id, tObj)
                } else {
                    metaObj.remove(track.id)
                }
                p.edit().putString("favorite_tracks_meta", metaObj.toString()).apply()
            } catch (e: Exception) {
                Log.w(TAG, "Failed updating local favorites pref", e)
            }
        }

        // 2. Sync with Spotify Web API
        try {
            val token = getAccessToken().getOrElse { FALLBACK_BEARER_TOKEN }
            val method = if (isFavorite) "PUT" else "DELETE"
            val url = URL("https://api.spotify.com/v1/me/tracks?ids=${track.id}")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("content-length", "0")
                setRequestProperty("user-agent", USER_AGENT)
            }
            val code = conn.responseCode
            conn.disconnect()
            Log.d(TAG, "Spotify favorite sync status: $code ($method ${track.id})")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync favorite with Spotify API", e)
        }

        Result.success(true)
    }

    suspend fun checkTrackInLibrary(trackId: String): Boolean = withContext(Dispatchers.IO) {
        val token = getAccessToken().getOrElse { FALLBACK_BEARER_TOKEN }
        val clientToken = customClientToken ?: CLIENT_TOKEN
        try {
            val url = URL("https://api-partner.spotify.com/pathfinder/v2/query")
            val payload = JSONObject().apply {
                put("variables", JSONObject().apply {
                    put("uris", JSONArray().apply { put("spotify:track:$trackId") })
                })
                put("operationName", "areEntitiesInLibrary")
                put("extensions", JSONObject().apply {
                    put("persistedQuery", JSONObject().apply {
                        put("version", 1)
                        put("sha256Hash", "134337999233cc6fdd6b1e6dbf94841409f04a946c5c7b744b09ba0dfe5a85ed")
                    })
                })
            }
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("client-token", clientToken)
                setRequestProperty("content-type", "application/json;charset=UTF-8")
                setRequestProperty("app-platform", APP_PLATFORM)
                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("referer", "https://open.spotify.com/")
                setRequestProperty("user-agent", USER_AGENT)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            val code = conn.responseCode
            if (code in 200..299) {
                val res = BufferedReader(InputStreamReader(conn.inputStream)).readText()
                conn.disconnect()
                val json = JSONObject(res)
                val lookup = json.optJSONObject("data")?.optJSONArray("lookup")
                if (lookup != null && lookup.length() > 0) {
                    return@withContext lookup.getJSONObject(0).optJSONObject("data")?.optBoolean("saved", false) ?: false
                }
            } else {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.w(TAG, "areEntitiesInLibrary failed", e)
        }
        false
    }

    suspend fun fetchLikedSongs(prefs: android.content.SharedPreferences? = null): Result<List<SpotifyTrack>> = withContext(Dispatchers.IO) {
        val token = getAccessToken().getOrElse { FALLBACK_BEARER_TOKEN }
        try {
            val url = URL("https://api.spotify.com/v1/me/tracks?limit=50")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("user-agent", USER_AGENT)
            }
            val code = conn.responseCode
            if (code in 200..299) {
                val text = BufferedReader(InputStreamReader(conn.inputStream)).readText()
                conn.disconnect()
                val json = JSONObject(text)
                val items = json.optJSONArray("items")
                val list = mutableListOf<SpotifyTrack>()
                if (items != null) {
                    for (i in 0 until items.length()) {
                        val tObj = items.getJSONObject(i).optJSONObject("track") ?: continue
                        val id = tObj.optString("id")
                        val name = tObj.optString("name")
                        val uri = tObj.optString("uri", "spotify:track:$id")
                        val dur = tObj.optLong("duration_ms", 0L)
                        val artistsArr = tObj.optJSONArray("artists")
                        val artistNames = mutableListOf<String>()
                        var artistUri: String? = null
                        if (artistsArr != null) {
                            for (j in 0 until artistsArr.length()) {
                                val aObj = artistsArr.getJSONObject(j)
                                val aName = aObj.optString("name")
                                if (aName.isNotEmpty()) artistNames.add(aName)
                                if (artistUri == null) artistUri = aObj.optString("uri", null)
                            }
                        }
                        val albObj = tObj.optJSONObject("album")
                        val albName = albObj?.optString("name", "") ?: ""
                        val albUri = albObj?.optString("uri", null)
                        val imgs = albObj?.optJSONArray("images")
                        val cover = imgs?.optJSONObject(0)?.optString("url", null)
                        list.add(
                            SpotifyTrack(
                                id = id,
                                uri = uri,
                                name = name,
                                artist = artistNames.joinToString(", ").ifEmpty { "Artist" },
                                album = albName,
                                coverUrl = cover,
                                durationMs = dur,
                                albumUri = albUri,
                                artistUri = artistUri
                            )
                        )
                    }
                }
                if (list.isNotEmpty()) {
                    prefs?.let { p ->
                        val ids = list.map { it.id }.toSet()
                        p.edit().putStringSet("favorite_tracks_set", ids).apply()
                    }
                    return@withContext Result.success(list)
                }
            } else {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchLikedSongs API failed, fallback to local cache", e)
        }

        // Local cache fallback
        if (prefs != null) {
            try {
                val metaJson = prefs.getString("favorite_tracks_meta", null)
                if (!metaJson.isNullOrEmpty()) {
                    val obj = JSONObject(metaJson)
                    val list = mutableListOf<SpotifyTrack>()
                    val keys = obj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val t = obj.getJSONObject(k)
                        list.add(
                            SpotifyTrack(
                                id = t.optString("id"),
                                uri = t.optString("uri"),
                                name = t.optString("name"),
                                artist = t.optString("artist"),
                                album = t.optString("album"),
                                coverUrl = t.optString("coverUrl", null).ifEmpty { null },
                                durationMs = t.optLong("durationMs", 0L),
                                albumUri = t.optString("albumUri", null).ifEmpty { null },
                                artistUri = t.optString("artistUri", null).ifEmpty { null }
                            )
                        )
                    }
                    if (list.isNotEmpty()) return@withContext Result.success(list)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error reading local favorite tracks", e)
            }
        }
        Result.success(emptyList())
    }

    suspend fun fetchAutoplayQueue(currentTrack: SpotifyTrack): List<SpotifyTrack> = withContext(Dispatchers.IO) {
        try {
            val query = if (currentTrack.artist.isNotBlank()) "artist:\"${currentTrack.artist}\"" else currentTrack.name
            val results = searchTracks(query).getOrDefault(emptyList())
            results.filter { it.uri != currentTrack.uri && !it.name.equals(currentTrack.name, ignoreCase = true) }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching autoplay queue", e)
            emptyList()
        }
    }

    fun invalidateTokens() {
        customAccessToken = null
        cachedAccessToken = null
        tokenExpiryMs = 0
    }

    suspend fun getAccessToken(forceRefresh: Boolean = false): Result<String> = withContext(Dispatchers.IO) {
        if (!forceRefresh && !customAccessToken.isNullOrEmpty() && System.currentTimeMillis() < tokenExpiryMs) {
            return@withContext Result.success(customAccessToken!!)
        }
        if (!forceRefresh && !cachedAccessToken.isNullOrEmpty() && System.currentTimeMillis() < tokenExpiryMs) {
            Log.d(TAG, "Using cached access token")
            return@withContext Result.success(cachedAccessToken!!)
        }

        // Try background token refresh hook first if available
        val hook = tokenRefreshHook
        if (hook != null) {
            Log.d(TAG, "Triggering background token refresh hook...")
            val success = hook()
            if (success && !customAccessToken.isNullOrEmpty()) {
                Log.d(TAG, "Background hook retrieved fresh access token!")
                return@withContext Result.success(customAccessToken!!)
            }
        }

        try {
            val urlString = "https://open.spotify.com/get_access_token?reason=transport&productType=web_player"
            Log.d(TAG, "Requesting Access Token from: $urlString")

            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Cookie", cookieHeaderString)
                setRequestProperty("Accept", "application/json")
                setRequestProperty("app-platform", APP_PLATFORM)
                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
            }

            val responseCode = conn.responseCode
            Log.d(TAG, "GetAccessToken HTTP Status: $responseCode")

            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseBody = BufferedReader(InputStreamReader(stream)).readText()
            conn.disconnect()

            if (responseCode in 200..299) {
                val json = JSONObject(responseBody)
                val token = json.getString("accessToken")
                tokenExpiryMs = json.optLong("accessTokenExpirationTimestampMs", System.currentTimeMillis() + 86400000L)
                cachedAccessToken = token
                customAccessToken = token
                Log.d(TAG, "Access token fetched successfully! Expires in: ${(tokenExpiryMs - System.currentTimeMillis()) / 1000}s")
                Result.success(token)
            } else {
                Log.w(TAG, "Failed to get access token from endpoint: HTTP $responseCode - using fallback token")
                cachedAccessToken = FALLBACK_BEARER_TOKEN
                tokenExpiryMs = System.currentTimeMillis() + 86400000L
                Result.success(FALLBACK_BEARER_TOKEN)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching access token - using fallback token", e)
            cachedAccessToken = FALLBACK_BEARER_TOKEN
            tokenExpiryMs = System.currentTimeMillis() + 86400000L
            Result.success(FALLBACK_BEARER_TOKEN)
        }
    }

    private fun connectDealer(token: String) {
        if (activeWebSocket != null) return

        val wsUrl = "wss://gae2-dealer.g2.spotify.com/?access_token=$token"
        val request = Request.Builder().url(wsUrl).build()

        activeWebSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Dealer WebSocket connected successfully")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val headers = json.optJSONObject("headers")
                    val connId = headers?.optString("Spotify-Connection-Id")
                    if (!connId.isNullOrEmpty() && dealerConnectionId == null) {
                        dealerConnectionId = connId
                        Log.d(TAG, "Captured Dealer connection ID: $connId")
                        if (!connectionIdDeferred.isCompleted) {
                            connectionIdDeferred.complete(connId)
                        }
                    }

                    // Process manifest track cluster notifications
                    if (text.contains("file_ids_mp4")) {
                        val payloads = json.optJSONArray("payloads")
                        if (payloads != null) {
                            for (p in 0 until payloads.length()) {
                                val sm = payloads.getJSONObject(p).optJSONObject("state_machine")
                                val tracks = sm?.optJSONArray("tracks") ?: continue
                                for (i in 0 until tracks.length()) {
                                    val tObj = tracks.getJSONObject(i)
                                    val uri = tObj.optJSONObject("metadata")?.optString("uri")
                                    val fileIdsMp4 = tObj.optJSONObject("manifest")?.optJSONArray("file_ids_mp4")
                                    val fileId = fileIdsMp4?.optJSONObject(0)?.optString("file_id")
                                    if (!uri.isNullOrEmpty() && !fileId.isNullOrEmpty()) {
                                        Log.d(TAG, "Dealer manifest resolved: $uri -> $fileId")
                                        audioFileMap[uri] = fileId
                                        pendingTrackResolutions[uri]?.complete(fileId)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error handling Dealer WebSocket message", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.w(TAG, "Dealer WebSocket failure", t)
                activeWebSocket = null
                dealerConnectionId = null
                isDeviceRegistered = false
                if (!connectionIdDeferred.isCompleted) {
                    connectionIdDeferred = CompletableDeferred()
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Dealer WebSocket closed: $reason ($code)")
                activeWebSocket = null
                dealerConnectionId = null
                isDeviceRegistered = false
                if (!connectionIdDeferred.isCompleted) {
                    connectionIdDeferred = CompletableDeferred()
                }
            }
        })
    }

    private suspend fun ensureDeviceRegistered(token: String): Boolean = withContext(Dispatchers.IO) {
        if (isDeviceRegistered && dealerConnectionId != null) return@withContext true

        connectDealer(token)

        val connId = dealerConnectionId ?: withTimeoutOrNull(6000L) {
            connectionIdDeferred.await()
        }

        if (connId == null) {
            Log.e(TAG, "Timed out waiting for Dealer connection ID")
            return@withContext false
        }

        try {
            val regUrl = URL("https://gae2-spclient.spotify.com/track-playback/v1/devices")
            val payload = JSONObject().apply {
                put("device", JSONObject().apply {
                    put("brand", "spotify")
                    put("capabilities", JSONObject().apply {
                        put("change_volume", true)
                        put("enable_play_token", true)
                        put("supports_file_media_type", true)
                        put("play_token_lost_behavior", "pause")
                        put("disable_connect", false)
                        put("audio_podcasts", true)
                        put("video_playback", true)
                        put("manifest_formats", JSONArray().apply {
                            put("file_ids_mp4")
                            put("file_ids_mp4_dual")
                        })
                        put("supports_preferred_media_type", true)
                        put("supports_playback_offsets", true)
                        put("supports_playback_speed", true)
                    })
                    put("device_id", DEVICE_ID)
                    put("device_type", "computer")
                    put("metadata", JSONObject())
                    put("model", "web_player")
                    put("name", "Web Player (Chrome)")
                    put("platform_identifier", "web_player osx 10.15.7;chrome 155.0.0.0;desktop")
                    put("is_group", false)
                    put("is_public", false)
                    put("correlation_id", "9167488b53cb6529d696cc87a46a8235")
                    put("client_version", "harmony:4.80.0-3cdf02c91")
                })
                put("outro_endcontent_snooping", false)
                put("connection_id", connId)
                put("client_version", "harmony:4.80.0-3cdf02c91")
                put("volume", 65535)
            }

            val conn = (regUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("client-token", customClientToken ?: CLIENT_TOKEN)
                setRequestProperty("content-type", "application/json;charset=UTF-8")
                setRequestProperty("app-platform", APP_PLATFORM)
                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("referer", "https://open.spotify.com/")
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val respBody = stream?.let { BufferedReader(InputStreamReader(it)).readText() } ?: ""
            conn.disconnect()

            isDeviceRegistered = (code in 200..299)
            Log.d(TAG, "Device registration HTTP status: $code, body: $respBody, success=$isDeviceRegistered")
            isDeviceRegistered
        } catch (e: Exception) {
            Log.e(TAG, "Error registering device with track-playback", e)
            false
        }
    }

    suspend fun searchTracks(query: String): Result<List<SpotifyTrack>> = withContext(Dispatchers.IO) {
        val tokenResult = getAccessToken()
        val token = tokenResult.getOrElse { FALLBACK_BEARER_TOKEN }

        try {
            val url = URL("https://api-partner.spotify.com/pathfinder/v2/query")
            Log.d(TAG, "Searching tracks for '$query' via Pathfinder v2: $url")

            val payload = JSONObject().apply {
                put("variables", JSONObject().apply {
                    put("query", query)
                    put("limit", 50)
                    put("offset", 0)
                    put("numberOfTopResults", 50)
                    put("includeArtistHasConcertsField", false)
                    put("includeAudiobooks", true)
                    put("includeAuthors", true)
                    put("includePreReleases", true)
                    put("includeAlbumPreReleases", false)
                    put("includeEpisodeContentRatingsV2", true)
                    put("isPrefix", null)
                    put("sectionFilters", JSONArray().apply {
                        put("GENERIC")
                        put("VIDEO_CONTENT")
                    })
                })
                put("operationName", "searchTopResultsList")
                put("extensions", JSONObject().apply {
                    put("persistedQuery", JSONObject().apply {
                        put("version", 1)
                        put("sha256Hash", SEARCH_TOP_RESULTS_HASH)
                    })
                })
            }

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("client-token", customClientToken ?: CLIENT_TOKEN)
                setRequestProperty("content-type", "application/json;charset=UTF-8")
                setRequestProperty("app-platform", APP_PLATFORM)
                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("referer", "https://open.spotify.com/")
                setRequestProperty("user-agent", USER_AGENT)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

            val responseCode = conn.responseCode
            Log.d(TAG, "Search HTTP Status: $responseCode")

            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseBody = BufferedReader(InputStreamReader(stream)).readText()
            conn.disconnect()

            if (responseCode in 200..299) {
                val json = JSONObject(responseBody)
                val searchV2 = json.getJSONObject("data").getJSONObject("searchV2")
                val topResults = searchV2.optJSONObject("topResultsV2")?.optJSONArray("itemsV2")
                val tracksList = mutableListOf<SpotifyTrack>()

                if (topResults != null) {
                    for (i in 0 until topResults.length()) {
                        val hit = topResults.getJSONObject(i)
                        val item = hit.optJSONObject("item") ?: continue
                        val typename = item.optString("__typename")
                        if (typename == "TrackResponseWrapper") {
                            val data = item.getJSONObject("data")
                            val id = data.getString("id")
                            val uri = data.getString("uri")
                            val name = data.getString("name")
                            val durationMs = data.optJSONObject("duration")?.optLong("totalMilliseconds", 0L) ?: 0L
                            val isPlayable = data.optJSONObject("playability")?.optBoolean("playable", true) ?: true

                            // Artists
                            val artistsArray = data.optJSONObject("artists")?.optJSONArray("items")
                            val artistNames = mutableListOf<String>()
                            if (artistsArray != null) {
                                for (j in 0 until artistsArray.length()) {
                                    val artistObj = artistsArray.getJSONObject(j)
                                    val artistName = artistObj.optJSONObject("profile")?.optString("name", "")
                                    if (!artistName.isNullOrEmpty()) {
                                        artistNames.add(artistName)
                                    }
                                }
                            }
                            val artist = artistNames.joinToString(", ").ifEmpty { "Unknown Artist" }

                            // Album & Cover
                            val albumObj = data.optJSONObject("albumOfTrack")
                            val albumName = albumObj?.optString("name", "Unknown Album") ?: "Unknown Album"
                            val sourcesArray = albumObj?.optJSONObject("coverArt")?.optJSONArray("sources")
                            var coverUrl: String? = null
                            if (sourcesArray != null && sourcesArray.length() > 0) {
                                // Prefer 300px source if available, otherwise first
                                for (s in 0 until sourcesArray.length()) {
                                    val src = sourcesArray.getJSONObject(s)
                                    if (src.optInt("height") == 300) {
                                        coverUrl = src.optString("url")
                                        break
                                    }
                                }
                                if (coverUrl == null) {
                                    coverUrl = sourcesArray.getJSONObject(0).optString("url")
                                }
                            }

                            tracksList.add(
                                SpotifyTrack(
                                    id = id,
                                    uri = uri,
                                    name = name,
                                    artist = artist,
                                    album = albumName,
                                    coverUrl = coverUrl,
                                    durationMs = durationMs,
                                    isPlayable = isPlayable
                                )
                            )
                        }
                    }
                }

                Log.d(TAG, "Pathfinder Search succeeded! Found ${tracksList.size} tracks for '$query'")
                Result.success(tracksList)
            } else {
                Log.e(TAG, "Search failed: HTTP $responseCode - $responseBody")
                if (responseCode == 401) {
                    invalidateTokens()
                    tokenRefreshHook?.invoke()
                }
                Result.failure(Exception("HTTP $responseCode: $responseBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing search", e)
            Result.failure(e)
        }
    }

    suspend fun fetchArtistTopTracks(artistUri: String, artistName: String = ""): Result<List<SpotifyTrack>> = withContext(Dispatchers.IO) {
        val q = if (artistName.isNotBlank()) "artist:\"$artistName\"" else {
            val id = artistUri.substringAfterLast(":")
            "artist:$id"
        }
        searchTracks(q)
    }

    suspend fun fetchClientToken(): Result<String> = withContext(Dispatchers.IO) {
        if (!customClientToken.isNullOrEmpty()) {
            return@withContext Result.success(customClientToken!!)
        }
        try {
            val url = URL("https://clienttoken.spotify.com/v1/clienttoken")
            val payload = JSONObject().apply {
                put("client_data", JSONObject().apply {
                    put("client_version", SPOTIFY_APP_VERSION)
                    put("client_id", "d8a5ed958d274c2e8ee717e6a4b0971d")
                    put("js_sdk_data", JSONObject().apply {
                        put("device_brand", "Apple")
                        put("device_model", "unknown")
                        put("os", "macos")
                        put("os_version", "10.15.7")
                        put("device_id", DEVICE_ID)
                        put("device_type", "computer")
                    })
                })
            }
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("accept", "application/json")
                setRequestProperty("content-type", "application/json")
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("referer", "https://open.spotify.com/")
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val respBody = stream?.let { BufferedReader(InputStreamReader(it)).readText() } ?: ""
            conn.disconnect()

            if (code in 200..299) {
                val json = JSONObject(respBody)
                val token = json.optJSONObject("granted_token")?.optString("token")
                if (!token.isNullOrEmpty()) {
                    setClientToken(token)
                    Log.d(TAG, "Native Client Token received: ${token.take(20)}...")
                    Result.success(token)
                } else {
                    Result.success(CLIENT_TOKEN)
                }
            } else {
                Log.w(TAG, "Failed to get native client token: HTTP $code, fallback used")
                Result.success(CLIENT_TOKEN)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching native client token: ${e.message}, fallback used")
            Result.success(CLIENT_TOKEN)
        }
    }

    suspend fun fetchHomeSections(): Result<SpotifyHomeData> = withContext(Dispatchers.IO) {
        val token = getAccessToken().getOrElse { FALLBACK_BEARER_TOKEN }
        val clientToken = customClientToken ?: CLIENT_TOKEN

        try {
            val url = URL("https://api-partner.spotify.com/pathfinder/v2/query")
            val payload = JSONObject().apply {
                put("variables", JSONObject().apply {
                    put("includeHomeChapterVideoCards", false)
                    put("homeEndUserIntegration", "INTEGRATION_WEB_PLAYER")
                    val sptVal = Regex("""sp_t=([^;]+)""").find(cookieHeaderString)?.groupValues?.get(1) ?: SP_T
                    put("sp_t", sptVal)
                    put("facet", "")
                    put("sectionItemsLimit", 10)
                    put("includeEpisodeContentRatingsV2", true)
                })
                put("operationName", "home")
                put("extensions", JSONObject().apply {
                    put("persistedQuery", JSONObject().apply {
                        put("version", 1)
                        put("sha256Hash", HOME_HASH)
                    })
                })
            }

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("client-token", clientToken)
                setRequestProperty("content-type", "application/json;charset=UTF-8")
                setRequestProperty("app-platform", APP_PLATFORM)
                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("referer", "https://open.spotify.com/")
                setRequestProperty("user-agent", USER_AGENT)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseBody = BufferedReader(InputStreamReader(stream)).readText()
            conn.disconnect()

            if (responseCode in 200..299) {
                val json = JSONObject(responseBody)
                val homeObj = json.getJSONObject("data").getJSONObject("home")
                val greeting = homeObj.optJSONObject("greeting")?.optString("transformedLabel", "Welcome back") ?: "Welcome back"
                val sectionsArray = homeObj.optJSONObject("sectionContainer")?.optJSONObject("sections")?.optJSONArray("items")

                val sectionList = mutableListOf<SpotifyHomeSection>()
                if (sectionsArray != null) {
                    for (i in 0 until sectionsArray.length()) {
                        val secObj = sectionsArray.getJSONObject(i)
                        val dataObj = secObj.optJSONObject("data") ?: continue
                        val title = dataObj.optJSONObject("title")?.optString("transformedLabel") ?: continue
                        val subtitle = dataObj.optJSONObject("subtitle")?.optString("transformedLabel")

                        val itemsArray = secObj.optJSONObject("sectionItems")?.optJSONArray("items") ?: continue
                        val mediaItems = mutableListOf<SpotifyMediaItem>()

                        for (j in 0 until itemsArray.length()) {
                            val itemObj = itemsArray.getJSONObject(j)
                            val content = itemObj.optJSONObject("content") ?: continue
                            val typename = content.optString("__typename")
                            val itemData = content.optJSONObject("data")

                            when (typename) {
                                "TrackResponseWrapper" -> {
                                    if (itemData != null) {
                                        val name = itemData.optString("name", "Track")
                                        val uri = itemData.optString("uri", "")
                                        val artist = itemData.optJSONObject("artists")?.optJSONArray("items")?.optJSONObject(0)?.optJSONObject("profile")?.optString("name", "Artist") ?: "Artist"
                                        val coverUrl = itemData.optJSONObject("albumOfTrack")?.optJSONObject("coverArt")?.optJSONArray("sources")?.optJSONObject(0)?.optString("url")
                                        if (uri.isNotEmpty()) {
                                            mediaItems.add(SpotifyMediaItem(
                                                id = uri.substringAfterLast(":"),
                                                uri = uri,
                                                name = name,
                                                subtitle = artist,
                                                coverUrl = coverUrl,
                                                type = SpotifyItemType.TRACK
                                            ))
                                        }
                                    }
                                }
                                "PlaylistResponseWrapper" -> {
                                    if (itemData != null) {
                                        val name = itemData.optString("name", "Playlist")
                                        val desc = itemData.optString("description", "")
                                        val uri = itemData.optString("uri", "")
                                        val imgArray = itemData.optJSONObject("images")?.optJSONArray("items")
                                        val coverUrl = imgArray?.optJSONObject(0)?.optJSONArray("sources")?.optJSONObject(0)?.optString("url")
                                        val itemType = when {
                                            uri.contains(":track:") -> SpotifyItemType.TRACK
                                            uri.contains(":album:") -> SpotifyItemType.ALBUM
                                            uri.contains(":artist:") -> SpotifyItemType.ARTIST
                                            else -> SpotifyItemType.PLAYLIST
                                        }
                                        if (uri.isNotEmpty()) {
                                            mediaItems.add(SpotifyMediaItem(
                                                id = uri.substringAfterLast(":"),
                                                uri = uri,
                                                name = name,
                                                subtitle = desc.ifEmpty { if (itemType == SpotifyItemType.TRACK) "Track" else "Playlist" },
                                                coverUrl = coverUrl,
                                                type = itemType
                                            ))
                                        }
                                    }
                                }
                                "PromotionResponseWrapper" -> {
                                    if (itemData != null) {
                                        val name = itemData.optJSONObject("titleTranslation")?.optString("transformedLabel")
                                            ?: itemData.optString("name", "Featured")
                                        val desc = itemData.optJSONObject("bodyTranslation")?.optString("transformedLabel", "")
                                        val targetUri = itemData.optString("target", "")
                                        val coverUrl = itemData.optJSONObject("background")?.optJSONObject("image")?.optJSONObject("data")?.optJSONArray("sources")?.optJSONObject(0)?.optString("url")
                                        val itemType = when {
                                            targetUri.contains(":track:") -> SpotifyItemType.TRACK
                                            targetUri.contains(":album:") -> SpotifyItemType.ALBUM
                                            targetUri.contains(":artist:") -> SpotifyItemType.ARTIST
                                            else -> SpotifyItemType.PLAYLIST
                                        }
                                        if (targetUri.isNotEmpty()) {
                                            mediaItems.add(SpotifyMediaItem(
                                                id = targetUri.substringAfterLast(":"),
                                                uri = targetUri,
                                                name = name,
                                                subtitle = desc?.ifEmpty { "Featured" } ?: "Featured",
                                                coverUrl = coverUrl,
                                                type = itemType
                                            ))
                                        }
                                    }
                                }
                                "AlbumResponseWrapper" -> {
                                    if (itemData != null) {
                                        val name = itemData.optString("name", "Album")
                                        val uri = itemData.optString("uri", "")
                                        val artist = itemData.optJSONObject("artists")?.optJSONArray("items")?.optJSONObject(0)?.optJSONObject("profile")?.optString("name", "Artist") ?: "Artist"
                                        val coverUrl = itemData.optJSONObject("coverArt")?.optJSONArray("sources")?.optJSONObject(0)?.optString("url")
                                        val itemType = if (uri.contains(":track:")) SpotifyItemType.TRACK else SpotifyItemType.ALBUM
                                        if (uri.isNotEmpty()) {
                                            mediaItems.add(SpotifyMediaItem(
                                                id = uri.substringAfterLast(":"),
                                                uri = uri,
                                                name = name,
                                                subtitle = artist,
                                                coverUrl = coverUrl,
                                                type = itemType
                                            ))
                                        }
                                    }
                                }
                                "ListResponseWrapper" -> {
                                    val listItems = itemData?.optJSONObject("items")?.optJSONArray("items")
                                    if (listItems != null) {
                                        for (k in 0 until listItems.length()) {
                                            val entityWrap = listItems.getJSONObject(k).optJSONObject("entity") ?: continue
                                            val uri = entityWrap.optString("_uri", "")
                                            val eData = entityWrap.optJSONObject("data") ?: continue
                                            val idTrait = eData.optJSONObject("identityTrait")
                                            val name = idTrait?.optString("name", "Recent") ?: "Recent"
                                            val desc = idTrait?.optString("description", "") ?: ""
                                            val imgSources = eData.optJSONObject("visualIdentityTrait")?.optJSONObject("squareCoverImage")?.optJSONObject("image")?.optJSONObject("data")?.optJSONArray("sources")
                                            val coverUrl = imgSources?.optJSONObject(0)?.optString("url")
                                            val rawType = eData.optJSONObject("entityTypeTrait")?.optString("type", "")
                                            val itemType = when {
                                                uri.contains(":track:") || rawType?.contains("TRACK") == true -> SpotifyItemType.TRACK
                                                uri.contains(":album:") || rawType?.contains("ALBUM") == true -> SpotifyItemType.ALBUM
                                                uri.contains(":artist:") || rawType?.contains("ARTIST") == true -> SpotifyItemType.ARTIST
                                                uri.contains(":playlist:") || uri.contains(":user:") -> SpotifyItemType.PLAYLIST
                                                rawType?.contains("PODCAST") == true || rawType?.contains("EPISODE") == true -> SpotifyItemType.PODCAST
                                                else -> SpotifyItemType.PLAYLIST
                                            }
                                            if (uri.isNotEmpty()) {
                                                mediaItems.add(SpotifyMediaItem(
                                                    id = uri.substringAfterLast(":"),
                                                    uri = uri,
                                                    name = name,
                                                    subtitle = desc.ifEmpty { itemType.name.lowercase().replaceFirstChar { it.uppercase() } },
                                                    coverUrl = coverUrl,
                                                    type = itemType
                                                ))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (mediaItems.isNotEmpty()) {
                            sectionList.add(SpotifyHomeSection(title = title, subtitle = subtitle, items = mediaItems))
                        }
                    }
                }

                Log.d(TAG, "Home fetched successfully! ${sectionList.size} sections found.")
                Result.success(SpotifyHomeData(greeting = greeting, sections = sectionList))
            } else {
                Log.e(TAG, "Fetch home failed: HTTP $responseCode - $responseBody")
                if (responseCode == 401) {
                    invalidateTokens()
                    tokenRefreshHook?.invoke()
                }
                Result.failure(Exception("HTTP $responseCode: $responseBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching home", e)
            Result.failure(e)
        }
    }

    suspend fun searchMulti(query: String, filter: SpotifyItemType? = null): Result<List<SpotifyMediaItem>> = withContext(Dispatchers.IO) {
        val token = getAccessToken().getOrElse { FALLBACK_BEARER_TOKEN }
        val clientToken = customClientToken ?: CLIENT_TOKEN

        try {
            val url = URL("https://api-partner.spotify.com/pathfinder/v2/query")
            val payload = JSONObject().apply {
                put("variables", JSONObject().apply {
                    put("query", query)
                    put("limit", 40)
                    put("offset", 0)
                    put("numberOfTopResults", 40)
                    put("includeArtistHasConcertsField", false)
                    put("includeAudiobooks", true)
                    put("includeAuthors", true)
                    put("includePreReleases", true)
                    put("includeAlbumPreReleases", false)
                    put("includeEpisodeContentRatingsV2", true)
                    put("isPrefix", null)
                    put("sectionFilters", JSONArray().apply {
                        put("GENERIC")
                        put("VIDEO_CONTENT")
                    })
                })
                put("operationName", "searchTopResultsList")
                put("extensions", JSONObject().apply {
                    put("persistedQuery", JSONObject().apply {
                        put("version", 1)
                        put("sha256Hash", SEARCH_TOP_RESULTS_HASH)
                    })
                })
            }

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("client-token", clientToken)
                setRequestProperty("content-type", "application/json;charset=UTF-8")
                setRequestProperty("app-platform", APP_PLATFORM)
                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("referer", "https://open.spotify.com/")
                setRequestProperty("user-agent", USER_AGENT)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseBody = BufferedReader(InputStreamReader(stream)).readText()
            conn.disconnect()

            if (responseCode in 200..299) {
                val json = JSONObject(responseBody)
                val searchV2 = json.getJSONObject("data").getJSONObject("searchV2")
                val topResults = searchV2.optJSONObject("topResultsV2")?.optJSONArray("itemsV2")
                val itemsList = mutableListOf<SpotifyMediaItem>()

                if (topResults != null) {
                    for (i in 0 until topResults.length()) {
                        val hit = topResults.getJSONObject(i)
                        val item = hit.optJSONObject("item") ?: continue
                        val typename = item.optString("__typename")
                        val data = item.optJSONObject("data") ?: continue

                        when (typename) {
                            "TrackResponseWrapper" -> {
                                val id = data.optString("id", "")
                                val uri = data.optString("uri", "")
                                val name = data.optString("name", "Unknown Track")
                                val durationMs = data.optJSONObject("duration")?.optLong("totalMilliseconds", 0L) ?: 0L
                                val isPlayable = data.optJSONObject("playability")?.optBoolean("playable", true) ?: true

                                val artistsArray = data.optJSONObject("artists")?.optJSONArray("items")
                                val artistNames = mutableListOf<String>()
                                if (artistsArray != null) {
                                    for (j in 0 until artistsArray.length()) {
                                        val aName = artistsArray.getJSONObject(j).optJSONObject("profile")?.optString("name")
                                        if (!aName.isNullOrEmpty()) artistNames.add(aName)
                                    }
                                }
                                val artist = artistNames.joinToString(", ").ifEmpty { "Artist" }

                                val albumObj = data.optJSONObject("albumOfTrack")
                                val albumName = albumObj?.optString("name", "Album") ?: "Album"
                                val coverUrl = albumObj?.optJSONObject("coverArt")?.optJSONArray("sources")?.optJSONObject(0)?.optString("url")

                                itemsList.add(
                                    SpotifyMediaItem(
                                        id = id,
                                        uri = uri,
                                        name = name,
                                        subtitle = "$artist • $albumName",
                                        coverUrl = coverUrl,
                                        type = SpotifyItemType.TRACK,
                                        durationMs = durationMs,
                                        isPlayable = isPlayable
                                    )
                                )
                            }
                            "AlbumResponseWrapper" -> {
                                val uri = data.optString("uri", "")
                                val name = data.optString("name", "Unknown Album")
                                val artistsArray = data.optJSONObject("artists")?.optJSONArray("items")
                                val artist = artistsArray?.optJSONObject(0)?.optJSONObject("profile")?.optString("name", "Artist") ?: "Artist"
                                val coverUrl = data.optJSONObject("coverArt")?.optJSONArray("sources")?.optJSONObject(0)?.optString("url")

                                itemsList.add(
                                    SpotifyMediaItem(
                                        id = uri.substringAfterLast(":"),
                                        uri = uri,
                                        name = name,
                                        subtitle = "$artist • Album",
                                        coverUrl = coverUrl,
                                        type = SpotifyItemType.ALBUM
                                    )
                                )
                            }
                            "ArtistResponseWrapper" -> {
                                val uri = data.optString("uri", "")
                                val name = data.optJSONObject("profile")?.optString("name", "Artist") ?: "Artist"
                                val avatarSources = data.optJSONObject("visuals")?.optJSONObject("avatarImage")?.optJSONArray("sources")
                                    ?: data.optJSONObject("visualIdentity")?.optJSONObject("squareCoverImage")?.optJSONObject("image")?.optJSONObject("data")?.optJSONArray("sources")
                                val coverUrl = avatarSources?.optJSONObject(0)?.optString("url")

                                itemsList.add(
                                    SpotifyMediaItem(
                                        id = uri.substringAfterLast(":"),
                                        uri = uri,
                                        name = name,
                                        subtitle = "Artist",
                                        coverUrl = coverUrl,
                                        type = SpotifyItemType.ARTIST
                                    )
                                )
                            }
                            "PlaylistResponseWrapper" -> {
                                val uri = data.optString("uri", "")
                                val name = data.optString("name", "Playlist")
                                val desc = data.optString("description", "Playlist")
                                val coverUrl = data.optJSONObject("images")?.optJSONArray("items")?.optJSONObject(0)?.optJSONArray("sources")?.optJSONObject(0)?.optString("url")

                                itemsList.add(
                                    SpotifyMediaItem(
                                        id = uri.substringAfterLast(":"),
                                        uri = uri,
                                        name = name,
                                        subtitle = desc.ifEmpty { "Playlist" },
                                        coverUrl = coverUrl,
                                        type = SpotifyItemType.PLAYLIST
                                    )
                                )
                            }
                            "EpisodeResponseWrapper", "EpisodeOrChapterResponseWrapper" -> {
                                val uri = data.optString("uri", "")
                                val name = data.optString("name", "Episode")
                                val showName = data.optJSONObject("show")?.optString("name", "Podcast") ?: "Podcast"
                                val coverUrl = data.optJSONObject("coverArt")?.optJSONArray("sources")?.optJSONObject(0)?.optString("url")

                                itemsList.add(
                                    SpotifyMediaItem(
                                        id = uri.substringAfterLast(":"),
                                        uri = uri,
                                        name = name,
                                        subtitle = showName,
                                        coverUrl = coverUrl,
                                        type = SpotifyItemType.EPISODE
                                    )
                                )
                            }
                            "PodcastResponseWrapper", "PodcastOrAudiobookResponseWrapper" -> {
                                val uri = data.optString("uri", "")
                                val name = data.optString("name", "Podcast")
                                val publisher = data.optJSONObject("publisher")?.optString("name", "Podcast") ?: "Podcast"
                                val coverUrl = data.optJSONObject("coverArt")?.optJSONArray("sources")?.optJSONObject(0)?.optString("url")

                                itemsList.add(
                                    SpotifyMediaItem(
                                        id = uri.substringAfterLast(":"),
                                        uri = uri,
                                        name = name,
                                        subtitle = publisher,
                                        coverUrl = coverUrl,
                                        type = SpotifyItemType.PODCAST
                                    )
                                )
                            }
                        }
                    }
                }

                val filtered = if (filter == null) {
                    itemsList
                } else {
                    itemsList.filter {
                        if (filter == SpotifyItemType.PODCAST) {
                            it.type == SpotifyItemType.PODCAST || it.type == SpotifyItemType.EPISODE
                        } else {
                            it.type == filter
                        }
                    }
                }

                Result.success(filtered)
            } else {
                Result.failure(Exception("HTTP $responseCode: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchPlaylistTracks(playlistUri: String): Result<List<SpotifyTrack>> = withContext(Dispatchers.IO) {
        if (playlistUri.contains("library:tracks") || playlistUri.contains("collection/tracks")) {
            return@withContext fetchLikedSongs(null)
        }
        val token = getAccessToken().getOrElse { FALLBACK_BEARER_TOKEN }
        val clientToken = customClientToken ?: CLIENT_TOKEN

        try {
            val url = URL("https://api-partner.spotify.com/pathfinder/v2/query")
            val payload = JSONObject().apply {
                put("variables", JSONObject().apply {
                    put("uri", playlistUri)
                    put("offset", 0)
                    put("limit", 50)
                    put("enableWatchFeedEntrypoint", true)
                    put("includeEpisodeContentRatingsV2", true)
                })
                put("operationName", "fetchPlaylist")
                put("extensions", JSONObject().apply {
                    put("persistedQuery", JSONObject().apply {
                        put("version", 1)
                        put("sha256Hash", FETCH_PLAYLIST_HASH)
                    })
                })
            }

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("client-token", clientToken)
                setRequestProperty("content-type", "application/json;charset=UTF-8")
                setRequestProperty("app-platform", APP_PLATFORM)
                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("referer", "https://open.spotify.com/")
                setRequestProperty("user-agent", USER_AGENT)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseBody = BufferedReader(InputStreamReader(stream)).readText()
            conn.disconnect()

            if (responseCode in 200..299) {
                val json = JSONObject(responseBody)
                val playlistV2 = json.getJSONObject("data").getJSONObject("playlistV2")
                val items = playlistV2.getJSONObject("content").getJSONArray("items")
                val trackList = mutableListOf<SpotifyTrack>()

                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val itemV2 = item.optJSONObject("itemV2") ?: continue
                    val tData = itemV2.optJSONObject("data") ?: continue
                    val uri = tData.optString("uri", "")
                    val name = tData.optString("name", "Unknown Track")
                    val durationMs = tData.optJSONObject("trackDuration")?.optLong("totalMilliseconds", 0L) ?: 0L
                    val isPlayable = tData.optJSONObject("playability")?.optBoolean("playable", true) ?: true

                    val artistsArray = tData.optJSONObject("artists")?.optJSONArray("items")
                    val artistNames = mutableListOf<String>()
                    if (artistsArray != null) {
                        for (j in 0 until artistsArray.length()) {
                            val aName = artistsArray.getJSONObject(j).optJSONObject("profile")?.optString("name")
                            if (!aName.isNullOrEmpty()) artistNames.add(aName)
                        }
                    }
                    val artist = artistNames.joinToString(", ").ifEmpty { "Artist" }

                    val albumObj = tData.optJSONObject("albumOfTrack")
                    val albumName = albumObj?.optString("name", "Album") ?: "Album"
                    val coverSources = albumObj?.optJSONObject("coverArt")?.optJSONArray("sources")
                    val coverUrl = coverSources?.optJSONObject(0)?.optString("url")

                    if (uri.isNotEmpty()) {
                        trackList.add(
                            SpotifyTrack(
                                id = uri.substringAfterLast(":"),
                                uri = uri,
                                name = name,
                                artist = artist,
                                album = albumName,
                                coverUrl = coverUrl,
                                durationMs = durationMs,
                                isPlayable = isPlayable
                            )
                        )
                    }
                }

                Result.success(trackList)
            } else {
                Result.failure(Exception("HTTP $responseCode: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAlbumTracks(albumUri: String): Result<List<SpotifyTrack>> = withContext(Dispatchers.IO) {
        val token = getAccessToken().getOrElse { FALLBACK_BEARER_TOKEN }
        val clientToken = customClientToken ?: CLIENT_TOKEN

        try {
            val url = URL("https://api-partner.spotify.com/pathfinder/v2/query")
            val payload = JSONObject().apply {
                put("variables", JSONObject().apply {
                    put("uri", albumUri)
                    put("locale", "")
                    put("offset", 0)
                    put("limit", 50)
                })
                put("operationName", "getAlbum")
                put("extensions", JSONObject().apply {
                    put("persistedQuery", JSONObject().apply {
                        put("version", 1)
                        put("sha256Hash", GET_ALBUM_HASH)
                    })
                })
            }

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("client-token", clientToken)
                setRequestProperty("content-type", "application/json;charset=UTF-8")
                setRequestProperty("app-platform", APP_PLATFORM)
                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("referer", "https://open.spotify.com/")
                setRequestProperty("user-agent", USER_AGENT)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseBody = BufferedReader(InputStreamReader(stream)).readText()
            conn.disconnect()

            if (responseCode in 200..299) {
                val json = JSONObject(responseBody)
                val albumUnion = json.getJSONObject("data").getJSONObject("albumUnion")
                val albumName = albumUnion.optString("name", "Album")
                val coverSources = albumUnion.optJSONObject("coverArt")?.optJSONArray("sources")
                val coverUrl = coverSources?.optJSONObject(0)?.optString("url")

                val tracksArray = albumUnion.optJSONObject("tracksV2")?.optJSONArray("items")
                val trackList = mutableListOf<SpotifyTrack>()

                if (tracksArray != null) {
                    for (i in 0 until tracksArray.length()) {
                        val tObj = tracksArray.getJSONObject(i).optJSONObject("track") ?: continue
                        val uri = tObj.optString("uri", "")
                        val name = tObj.optString("name", "Unknown Track")
                        val durationMs = tObj.optJSONObject("duration")?.optLong("totalMilliseconds", 0L) ?: 0L
                        val isPlayable = tObj.optJSONObject("playability")?.optBoolean("playable", true) ?: true

                        val artistsArray = tObj.optJSONObject("artists")?.optJSONArray("items")
                        val artistNames = mutableListOf<String>()
                        if (artistsArray != null) {
                            for (j in 0 until artistsArray.length()) {
                                val aName = artistsArray.getJSONObject(j).optJSONObject("profile")?.optString("name")
                                if (!aName.isNullOrEmpty()) artistNames.add(aName)
                            }
                        }
                        val artist = artistNames.joinToString(", ").ifEmpty { "Artist" }

                        if (uri.isNotEmpty()) {
                            trackList.add(
                                SpotifyTrack(
                                    id = uri.substringAfterLast(":"),
                                    uri = uri,
                                    name = name,
                                    artist = artist,
                                    album = albumName,
                                    coverUrl = coverUrl,
                                    durationMs = durationMs,
                                    isPlayable = isPlayable
                                )
                            )
                        }
                    }
                }

                Result.success(trackList)
            } else {
                Result.failure(Exception("HTTP $responseCode: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAlbumDetails(albumUri: String): Result<SpotifyAlbumDetails> = withContext(Dispatchers.IO) {
        val token = getAccessToken().getOrElse { FALLBACK_BEARER_TOKEN }
        val clientToken = customClientToken ?: CLIENT_TOKEN

        try {
            val url = URL("https://api-partner.spotify.com/pathfinder/v2/query")
            val payload = JSONObject().apply {
                put("variables", JSONObject().apply {
                    put("uri", albumUri)
                    put("locale", "")
                    put("offset", 0)
                    put("limit", 50)
                })
                put("operationName", "getAlbum")
                put("extensions", JSONObject().apply {
                    put("persistedQuery", JSONObject().apply {
                        put("version", 1)
                        put("sha256Hash", GET_ALBUM_HASH)
                    })
                })
            }

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("client-token", clientToken)
                setRequestProperty("content-type", "application/json;charset=UTF-8")
                setRequestProperty("app-platform", APP_PLATFORM)
                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("referer", "https://open.spotify.com/")
                setRequestProperty("user-agent", USER_AGENT)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseBody = BufferedReader(InputStreamReader(stream)).readText()
            conn.disconnect()

            if (responseCode in 200..299) {
                val json = JSONObject(responseBody)
                val albumUnion = json.getJSONObject("data").getJSONObject("albumUnion")
                val albumName = albumUnion.optString("name", "Album")
                val coverSources = albumUnion.optJSONObject("coverArt")?.optJSONArray("sources")
                val coverUrl = coverSources?.optJSONObject(0)?.optString("url")
                val year = albumUnion.optJSONObject("date")?.optInt("year")?.toString()

                val artistsArray = albumUnion.optJSONObject("artists")?.optJSONArray("items")
                val artistNames = mutableListOf<String>()
                if (artistsArray != null) {
                    for (j in 0 until artistsArray.length()) {
                        val aName = artistsArray.getJSONObject(j).optJSONObject("profile")?.optString("name")
                        if (!aName.isNullOrEmpty()) artistNames.add(aName)
                    }
                }
                val mainArtist = artistNames.joinToString(", ").ifEmpty { "Artist" }

                val tracksArray = albumUnion.optJSONObject("tracksV2")?.optJSONArray("items")
                val trackList = mutableListOf<SpotifyTrack>()

                if (tracksArray != null) {
                    for (i in 0 until tracksArray.length()) {
                        val tObj = tracksArray.getJSONObject(i).optJSONObject("track") ?: continue
                        val uri = tObj.optString("uri", "")
                        val name = tObj.optString("name", "Unknown Track")
                        val durationMs = tObj.optJSONObject("duration")?.optLong("totalMilliseconds", 0L) ?: 0L
                        val isPlayable = tObj.optJSONObject("playability")?.optBoolean("playable", true) ?: true

                        val tArtistsArray = tObj.optJSONObject("artists")?.optJSONArray("items")
                        val tArtistNames = mutableListOf<String>()
                        if (tArtistsArray != null) {
                            for (j in 0 until tArtistsArray.length()) {
                                val aName = tArtistsArray.getJSONObject(j).optJSONObject("profile")?.optString("name")
                                if (!aName.isNullOrEmpty()) tArtistNames.add(aName)
                            }
                        }
                        val tArtist = tArtistNames.joinToString(", ").ifEmpty { mainArtist }

                        if (uri.isNotEmpty()) {
                            trackList.add(
                                SpotifyTrack(
                                    id = uri.substringAfterLast(":"),
                                    uri = uri,
                                    name = name,
                                    artist = tArtist,
                                    album = albumName,
                                    coverUrl = coverUrl,
                                    durationMs = durationMs,
                                    isPlayable = isPlayable
                                )
                            )
                        }
                    }
                }

                Result.success(
                    SpotifyAlbumDetails(
                        uri = albumUri,
                        id = albumUri.substringAfterLast(":"),
                        name = albumName,
                        artist = mainArtist,
                        coverUrl = coverUrl,
                        releaseYear = year,
                        trackCount = trackList.size,
                        tracks = trackList
                    )
                )
            } else {
                Result.failure(Exception("HTTP $responseCode: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchPlaylistDetails(playlistUri: String, prefs: android.content.SharedPreferences? = null): Result<SpotifyPlaylistDetails> = withContext(Dispatchers.IO) {
        if (playlistUri.contains("library:tracks") || playlistUri.contains("collection/tracks")) {
            val tracks = fetchLikedSongs(prefs).getOrDefault(emptyList())
            return@withContext Result.success(
                SpotifyPlaylistDetails(
                    uri = "spotify:user:library:tracks",
                    id = "liked_songs",
                    name = "Liked Songs",
                    ownerName = "You",
                    description = "Your favorite music",
                    coverUrl = null,
                    trackCount = tracks.size,
                    tracks = tracks
                )
            )
        }
        if (prefs != null && playlistUri.contains("local_")) {
            try {
                val localJson = prefs.getString("local_user_playlists_json", null)
                if (!localJson.isNullOrEmpty()) {
                    val array = JSONArray(localJson)
                    for (i in 0 until array.length()) {
                        val p = array.getJSONObject(i)
                        if (p.optString("id") == playlistUri || p.optString("uri") == playlistUri) {
                            val name = p.optString("name", "My Playlist")
                            val owner = p.optString("ownerName", "You")
                            val cover = p.optString("coverUrl", null)
                            val tracksArr = p.optJSONArray("tracks") ?: JSONArray()
                            val trackList = mutableListOf<SpotifyTrack>()
                            for (j in 0 until tracksArr.length()) {
                                val tObj = tracksArr.getJSONObject(j)
                                trackList.add(
                                    SpotifyTrack(
                                        id = tObj.optString("id"),
                                        uri = tObj.optString("uri"),
                                        name = tObj.optString("name"),
                                        artist = tObj.optString("artist"),
                                        album = tObj.optString("album"),
                                        coverUrl = tObj.optString("coverUrl", null),
                                        durationMs = tObj.optLong("durationMs", 0L)
                                    )
                                )
                            }
                            return@withContext Result.success(
                                SpotifyPlaylistDetails(
                                    uri = playlistUri,
                                    id = playlistUri.substringAfterLast(":"),
                                    name = name,
                                    ownerName = owner,
                                    description = "Custom playlist",
                                    coverUrl = cover,
                                    trackCount = trackList.size,
                                    tracks = trackList
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error reading local playlist details", e)
            }
        }

        val cleanId = when {
            playlistUri.contains("spotify:playlist:") -> playlistUri.substringAfter("spotify:playlist:")
            playlistUri.contains("/playlist/") -> playlistUri.substringAfter("/playlist/").substringBefore("?").substringBefore("/")
            else -> playlistUri.substringAfterLast(":")
        }
        val canonicalUri = "spotify:playlist:$cleanId"

        val token = getAccessToken().getOrElse { FALLBACK_BEARER_TOKEN }
        val clientToken = customClientToken ?: CLIENT_TOKEN

        // 1. Try Pathfinder v2 query first
        try {
            val url = URL("https://api-partner.spotify.com/pathfinder/v2/query")
            val payload = JSONObject().apply {
                put("variables", JSONObject().apply {
                    put("uri", canonicalUri)
                    put("offset", 0)
                    put("limit", 100)
                    put("enableWatchFeedEntrypoint", true)
                    put("includeEpisodeContentRatingsV2", true)
                })
                put("operationName", "fetchPlaylist")
                put("extensions", JSONObject().apply {
                    put("persistedQuery", JSONObject().apply {
                        put("version", 1)
                        put("sha256Hash", FETCH_PLAYLIST_HASH)
                    })
                })
            }

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("client-token", clientToken)
                setRequestProperty("content-type", "application/json;charset=UTF-8")
                setRequestProperty("app-platform", APP_PLATFORM)
                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("referer", "https://open.spotify.com/")
                setRequestProperty("user-agent", USER_AGENT)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseBody = BufferedReader(InputStreamReader(stream)).readText()
            conn.disconnect()

            if (responseCode in 200..299) {
                val json = JSONObject(responseBody)
                val playlistV2 = json.optJSONObject("data")?.optJSONObject("playlistV2")
                if (playlistV2 != null) {
                    val pName = playlistV2.optString("name", "Playlist")
                    val pOwner = playlistV2.optJSONObject("ownerV2")?.optJSONObject("data")?.optString("name") ?: "Spotify"
                    val pDesc = playlistV2.optString("description", "")
                    val pCover = playlistV2.optJSONObject("images")?.optJSONArray("items")?.optJSONObject(0)?.optJSONArray("sources")?.optJSONObject(0)?.optString("url")

                    val items = playlistV2.optJSONObject("content")?.optJSONArray("items") ?: JSONArray()
                    val trackList = mutableListOf<SpotifyTrack>()

                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)
                        val itemV2 = item.optJSONObject("itemV2") ?: continue
                        val tData = itemV2.optJSONObject("data") ?: continue
                        val uri = tData.optString("uri", "")
                        val name = tData.optString("name", "Unknown Track")
                        val durationMs = tData.optJSONObject("trackDuration")?.optLong("totalMilliseconds", 0L) ?: 0L
                        val isPlayable = tData.optJSONObject("playability")?.optBoolean("playable", true) ?: true

                        val artistsArray = tData.optJSONObject("artists")?.optJSONArray("items")
                        val artistNames = mutableListOf<String>()
                        if (artistsArray != null) {
                            for (j in 0 until artistsArray.length()) {
                                val aName = artistsArray.getJSONObject(j).optJSONObject("profile")?.optString("name")
                                if (!aName.isNullOrEmpty()) artistNames.add(aName)
                            }
                        }
                        val artist = artistNames.joinToString(", ").ifEmpty { "Artist" }

                        val albumObj = tData.optJSONObject("albumOfTrack")
                        val albumName = albumObj?.optString("name", "Album") ?: "Album"
                        val coverSources = albumObj?.optJSONObject("coverArt")?.optJSONArray("sources")
                        val coverUrl = coverSources?.optJSONObject(0)?.optString("url") ?: pCover

                        if (uri.isNotEmpty()) {
                            trackList.add(
                                SpotifyTrack(
                                    id = uri.substringAfterLast(":"),
                                    uri = uri,
                                    name = name,
                                    artist = artist,
                                    album = albumName,
                                    coverUrl = coverUrl,
                                    durationMs = durationMs,
                                    isPlayable = isPlayable
                                )
                            )
                        }
                    }

                    if (trackList.isNotEmpty()) {
                        return@withContext Result.success(
                            SpotifyPlaylistDetails(
                                uri = canonicalUri,
                                id = cleanId,
                                name = pName,
                                ownerName = pOwner,
                                description = pDesc,
                                coverUrl = pCover,
                                trackCount = trackList.size,
                                tracks = trackList
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Pathfinder playlist fetch failed, trying Web API fallback", e)
        }

        // 2. Fallback: Spotify Web API endpoint
        try {
            val apiUrl = URL("https://api.spotify.com/v1/playlists/$cleanId")
            val aConn = (apiUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12000
                readTimeout = 12000
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", USER_AGENT)
            }
            val aCode = aConn.responseCode
            val aStream = if (aCode in 200..299) aConn.inputStream else aConn.errorStream
            val aBody = BufferedReader(InputStreamReader(aStream)).readText()
            aConn.disconnect()

            if (aCode in 200..299) {
                val json = JSONObject(aBody)
                val pName = json.optString("name", "Playlist")
                val pDesc = json.optString("description", "")
                val pOwner = json.optJSONObject("owner")?.optString("display_name") ?: "Spotify"
                val pCover = json.optJSONArray("images")?.optJSONObject(0)?.optString("url")

                val tracksObj = json.optJSONObject("tracks")
                val items = tracksObj?.optJSONArray("items") ?: JSONArray()
                val trackList = mutableListOf<SpotifyTrack>()

                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val tData = item.optJSONObject("track") ?: continue
                    val uri = tData.optString("uri", "")
                    val name = tData.optString("name", "Unknown Track")
                    val durationMs = tData.optLong("duration_ms", 0L)
                    val isPlayable = tData.optBoolean("is_playable", true)

                    val artistsArray = tData.optJSONArray("artists")
                    val artistNames = mutableListOf<String>()
                    if (artistsArray != null) {
                        for (j in 0 until artistsArray.length()) {
                            val aName = artistsArray.getJSONObject(j).optString("name")
                            if (!aName.isNullOrEmpty()) artistNames.add(aName)
                        }
                    }
                    val artist = artistNames.joinToString(", ").ifEmpty { "Artist" }

                    val albumObj = tData.optJSONObject("album")
                    val albumName = albumObj?.optString("name", "Album") ?: "Album"
                    val coverSources = albumObj?.optJSONArray("images")
                    val coverUrl = coverSources?.optJSONObject(0)?.optString("url") ?: pCover

                    if (uri.isNotEmpty()) {
                        trackList.add(
                            SpotifyTrack(
                                id = uri.substringAfterLast(":"),
                                uri = uri,
                                name = name,
                                artist = artist,
                                album = albumName,
                                coverUrl = coverUrl,
                                durationMs = durationMs,
                                isPlayable = isPlayable
                            )
                        )
                    }
                }

                return@withContext Result.success(
                    SpotifyPlaylistDetails(
                        uri = canonicalUri,
                        id = cleanId,
                        name = pName,
                        ownerName = pOwner,
                        description = pDesc,
                        coverUrl = pCover,
                        trackCount = trackList.size,
                        tracks = trackList
                    )
                )
            } else {
                Result.failure(Exception("HTTP $aCode: $aBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resolveAudioCdn(fileId: String): Result<String> = withContext(Dispatchers.IO) {
        val tokenResult = getAccessToken()
        val token = tokenResult.getOrElse { FALLBACK_BEARER_TOKEN }

        try {
            val urlString = "https://gae2-spclient.spotify.com/storage-resolve/v2/files/audio/interactive/10/$fileId?version=10000000&product=9&platform=39&alt=json"
            Log.d(TAG, "Resolving audio CDN for fileId $fileId: $urlString")

            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("client-token", customClientToken ?: CLIENT_TOKEN)
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("app-platform", APP_PLATFORM)
                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
            }

            val responseCode = conn.responseCode
            Log.d(TAG, "StorageResolve HTTP Status: $responseCode")

            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseBody = BufferedReader(InputStreamReader(stream)).readText()
            conn.disconnect()

            if (responseCode in 200..299) {
                val json = JSONObject(responseBody)
                val cdnUrls = json.optJSONArray("cdnurl")
                if (cdnUrls != null && cdnUrls.length() > 0) {
                    val cdnUrl = cdnUrls.getString(0)
                    Log.d(TAG, "Resolved CDN URL: $cdnUrl")
                    Result.success(cdnUrl)
                } else {
                    Result.failure(Exception("No cdnurl in response: $responseBody"))
                }
            } else {
                Log.e(TAG, "Storage resolve failed: HTTP $responseCode - $responseBody")
                Result.failure(Exception("HTTP $responseCode: $responseBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Audio resolve error", e)
            Result.failure(e)
        }
    }

    suspend fun resolveTrackAudioStream(trackUriOrId: String): Result<String> = withContext(Dispatchers.IO) {
        val trackUri = if (trackUriOrId.startsWith("spotify:track:")) trackUriOrId else "spotify:track:$trackUriOrId"
        Log.d(TAG, "Resolving audio stream for: $trackUri")

        // 1. Check if audio file ID is already known
        var fileId = audioFileMap[trackUri]

        // 2. If not known, issue connect-state play command to get manifest via Dealer WebSocket
        if (fileId == null) {
            val token = getAccessToken().getOrElse { FALLBACK_BEARER_TOKEN }
            val registered = ensureDeviceRegistered(token)
            if (!registered) {
                return@withContext Result.failure(Exception("Failed to register audio player device"))
            }

            val deferred = CompletableDeferred<String>()
            pendingTrackResolutions[trackUri] = deferred

            try {
                val playUrl = URL("https://gae2-spclient.spotify.com/connect-state/v1/player/command/from/$DEVICE_ID/to/$DEVICE_ID")
                val cmdId = UUID.randomUUID().toString().replace("-", "")
                val playPayload = JSONObject().apply {
                    put("command", JSONObject().apply {
                        put("context", JSONObject().apply {
                            put("uri", trackUri)
                            put("url", "context://$trackUri")
                            put("metadata", JSONObject())
                        })
                        put("play_origin", JSONObject().apply {
                            put("feature_identifier", "auto_play_on_load")
                            put("feature_version", "web-player_2026-10-09_1791504000000_d873ee9db135")
                            put("referrer_identifier", "search")
                        })
                        put("options", JSONObject().apply {
                            put("license", "tft")
                            put("skip_to", JSONObject())
                            put("player_options_override", JSONObject())
                        })
                        put("logging_params", JSONObject().apply {
                            put("page_instance_ids", JSONArray())
                            put("interaction_ids", JSONArray())
                            put("command_id", cmdId)
                        })
                        put("endpoint", "play")
                    })
                }

                val conn = (playUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 15000
                    readTimeout = 15000
                    setRequestProperty("authorization", "Bearer $token")
                    setRequestProperty("client-token", customClientToken ?: CLIENT_TOKEN)
                    setRequestProperty("content-type", "application/json;charset=UTF-8")
                    setRequestProperty("app-platform", APP_PLATFORM)
                    setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                    setRequestProperty("origin", "https://open.spotify.com")
                    setRequestProperty("referer", "https://open.spotify.com/")
                }

                OutputStreamWriter(conn.outputStream).use { it.write(playPayload.toString()) }
                val code = conn.responseCode
                conn.disconnect()
                Log.d(TAG, "Triggered connect-state play command for $trackUri. Code: $code")

                // Await manifest notification from Dealer WebSocket (up to 5s)
                fileId = withTimeoutOrNull(5000L) {
                    deferred.await()
                } ?: audioFileMap[trackUri]

            } finally {
                pendingTrackResolutions.remove(trackUri)
            }
        }

        if (fileId == null) {
            return@withContext Result.failure(Exception("Unable to resolve audio file ID for $trackUri"))
        }

        Log.d(TAG, "Audio file ID resolved: $fileId. Fetching CDN URL...")
        resolveAudioCdn(fileId)
    }

    suspend fun getSeekTable(fileId: String): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val urlString = "https://seektables.spotifycdn.com/v1/seektable/$fileId"
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("referer", "https://open.spotify.com/")
            }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseBody = BufferedReader(InputStreamReader(stream)).readText()
            conn.disconnect()

            if (responseCode in 200..299) {
                Result.success(JSONObject(responseBody))
            } else {
                Result.failure(Exception("HTTP $responseCode: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchColorLyrics(trackId: String, coverUrl: String? = null): Result<SpotifyColorLyrics> = withContext(Dispatchers.IO) {
        val cleanId = trackId.substringAfterLast(":")
        val token = getAccessToken().getOrElse { customAccessToken ?: FALLBACK_BEARER_TOKEN }
        val clientToken = customClientToken ?: CLIENT_TOKEN

        try {
            val encodedCover = if (!coverUrl.isNullOrEmpty()) {
                java.net.URLEncoder.encode(coverUrl, "UTF-8")
            } else ""

            val urlStr = if (encodedCover.isNotEmpty()) {
                "https://spclient.wg.spotify.com/color-lyrics/v2/track/$cleanId/image/$encodedCover?format=json&vocalRemoval=false&market=from_token"
            } else {
                "https://spclient.wg.spotify.com/color-lyrics/v2/track/$cleanId?format=json&vocalRemoval=false&market=from_token"
            }

            Log.d(TAG, "Fetching Spotify color lyrics: $urlStr")
            val url = URL(urlStr)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12000
                readTimeout = 12000
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("client-token", clientToken)
                setRequestProperty("accept", "application/json")
                setRequestProperty("accept-language", "en")
                setRequestProperty("app-platform", APP_PLATFORM)
                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                setRequestProperty("referer", "https://open.spotify.com/")
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("user-agent", USER_AGENT)
            }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseBody = BufferedReader(InputStreamReader(stream)).readText()
            conn.disconnect()

            if (responseCode in 200..299) {
                val json = JSONObject(responseBody)
                val lyricsObj = json.optJSONObject("lyrics")
                val colorsObj = json.optJSONObject("colors")

                val syncType = lyricsObj?.optString("syncType", "UNSYNCED") ?: "UNSYNCED"
                val linesArray = lyricsObj?.optJSONArray("lines")
                val linesList = mutableListOf<SpotifyLyricLine>()
                if (linesArray != null) {
                    for (i in 0 until linesArray.length()) {
                        val lineItem = linesArray.getJSONObject(i)
                        val startTimeStr = lineItem.optString("startTimeMs", "0")
                        val startTimeMs = startTimeStr.toLongOrNull() ?: 0L
                        val words = lineItem.optString("words", "")
                        val endTimeStr = lineItem.optString("endTimeMs", "0")
                        val endTimeMs = endTimeStr.toLongOrNull() ?: 0L
                        linesList.add(SpotifyLyricLine(startTimeMs, words, endTimeMs))
                    }
                }

                val bgColor = colorsObj?.optInt("background", -16744529) ?: -16744529
                val txtColor = colorsObj?.optInt("text", -16777216) ?: -16777216
                val highlightColor = colorsObj?.optInt("highlightText", -1) ?: -1
                val provider = lyricsObj?.optString("providerDisplayName", "Musixmatch") ?: "Musixmatch"

                Log.d(TAG, "Spotify Color Lyrics fetched: ${linesList.size} lines, syncType: $syncType, provider: $provider")
                Result.success(
                    SpotifyColorLyrics(
                        syncType = syncType,
                        lines = linesList,
                        backgroundColor = bgColor,
                        textColor = txtColor,
                        highlightTextColor = highlightColor,
                        providerDisplayName = provider
                    )
                )
            } else {
                Log.w(TAG, "Lyrics request failed: HTTP $responseCode - $responseBody")
                Result.failure(Exception("HTTP $responseCode: $responseBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Spotify color lyrics", e)
            Result.failure(e)
        }
    }
}
