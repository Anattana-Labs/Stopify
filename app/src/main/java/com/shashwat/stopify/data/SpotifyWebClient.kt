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



class SpotifyWebClient {

    companion object {
        private const val TAG = "SpotifyWebClient"

        // Session cookies (populated dynamically via WebView login and SharedPreferences)
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
        const val LIBRARY_HASH = "973e511ca44261fda7eebac8b653155e7caee3675abb4fb110cc1b8c78b091c3"
        const val EDITABLE_PLAYLISTS_HASH = "d5c4b8096437dcc2ac9528c91dfcd299e35b747cda2f8f75d28f41f49c5092ba"
        const val ADD_TO_PLAYLIST_HASH = "47b2a1234b17748d332dd0431534f22450e9ecbb3d5ddcdacbd83368636a0990"
        const val ADD_TO_LIBRARY_HASH = "7c5a69420e2bfae3da5cc4e14cbc8bb3f6090f80afc00ffc179177f19be3f33d"
        const val REMOVE_FROM_LIBRARY_HASH = "7c5a69420e2bfae3da5cc4e14cbc8bb3f6090f80afc00ffc179177f19be3f33d"
        const val PROFILE_ATTRIBUTES_HASH = "53bcb064f6cd18c23f752bc324a791194d20df612d8e1239c735144ab0399ced"

        // Working fallback Bearer token from web session
        const val FALLBACK_BEARER_TOKEN = "BQDyP_PgW1Uj_1SVWN4PgYEDZ1wgIj2Dohsrx8jXZiXpxofEgaAqyImnIFyRCJ9_MtAWm2bUUyMFbpUaf_X1KCa9YnP24UgtRNzoU6q21uytBgXaKbMKISMlWGBkRwuG4QACxXbnaHkm-gBm8VPIVyopxU6GYPxxRtyuVxRk1yDBE27cNQJNdsETLt0dAGvTzB36iWQR8YFBSoLsvRGmvOSMHcxXad0fD8zg_Zt6muogKnlHAnIn0Q9kQN_lqwb3o7ipmbOqrMQ-7TVJaz4nkIF0GI5kIIHd8AdOaO9foLcF3RyjhKfI9r6yDcv5LR1iLqpgluuBasbhIObAGhUhrSpBYltpzTtKCGuBq_7n4_6H03ysecKrUEe2UVD8w3SfA_dJ8tN9RRajljHIbu-K"
        const val DEVICE_ID = "af6fd454eccef1714bc99a09a10c119366e8da1c"

        fun getDeviceId(prefs: android.content.SharedPreferences? = null): String {
            return DEVICE_ID
        }

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

    internal var cachedAccessToken: String? = null
    internal var tokenExpiryMs: Long = 0

    @Volatile
    var customAccessToken: String? = null
        internal set

    @Volatile
    var customClientToken: String? = null
        internal set

    @Volatile
    var customCookies: String? = null
        internal set

    @Volatile
    var webPlayerDeviceId: String? = null

    val historyManager = SpotifyHistoryManager()
    val authService = SpotifyAuthService(this)
    val lyricsService = SpotifyLyricsService(this)
    val connectService = SpotifyConnectService(this)

    fun getCachedColorLyrics(trackId: String): SpotifyColorLyrics? {
        return lyricsService.getCachedColorLyrics(trackId)
    }

    val cookieHeaderString: String
        get() = customCookies ?: ""

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
    private var activeStateMachineId: String? = null
    @Volatile
    private var activeStateId: String = "4afaf822d8624b5fb4d2f9ffe5b1fe79"
    @Volatile
    private var stateSeqNum: Int = 3

    @Volatile
    var tokenRefreshHook: (suspend () -> Boolean)? = null

    // Local playback history cache with persistent backing (delegated to SpotifyHistoryManager)
    val playbackHistory: List<SpotifyTrack>
        get() = historyManager.playbackHistory

    fun loadHistoryFromPrefs(prefs: android.content.SharedPreferences) {
        historyManager.loadHistoryFromPrefs(prefs)
    }

    fun saveHistoryToPrefs(prefs: android.content.SharedPreferences) {
        historyManager.saveHistoryToPrefs(prefs)
    }

    fun addLocalHistoryTrack(track: SpotifyTrack, prefs: android.content.SharedPreferences? = null) {
        historyManager.addLocalHistoryTrack(track, prefs)
    }

    suspend fun fetchCurrentUserProfile(prefs: android.content.SharedPreferences? = null): Result<SpotifyUserProfile> = withContext(Dispatchers.IO) {
        val token = getAccessToken().getOrElse { customAccessToken ?: cachedAccessToken ?: FALLBACK_BEARER_TOKEN }
        val clientToken = customClientToken ?: CLIENT_TOKEN

        // 1. Primary: GraphQL profileAttributes query
        try {
            val gqlUrl = URL("https://api-partner.spotify.com/pathfinder/v2/query")
            val conn = (gqlUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("client-token", clientToken)
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Origin", "https://open.spotify.com")
                setRequestProperty("Referer", "https://open.spotify.com/")
                setRequestProperty("app-platform", APP_PLATFORM)
                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
            }
            val payload = JSONObject().apply {
                put("variables", JSONObject())
                put("operationName", "profileAttributes")
                put("extensions", JSONObject().apply {
                    put("persistedQuery", JSONObject().apply {
                        put("version", 1)
                        put("sha256Hash", PROFILE_ATTRIBUTES_HASH)
                    })
                })
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            if (conn.responseCode in 200..299) {
                val text = BufferedReader(InputStreamReader(conn.inputStream)).readText()
                conn.disconnect()
                val json = JSONObject(text)
                val prof = json.optJSONObject("data")?.optJSONObject("me")?.optJSONObject("profile")
                if (prof != null) {
                    val username = prof.optString("username", prof.optString("uri").substringAfterLast(":"))
                    val name = prof.optString("name", username)
                    val avatar = prof.optJSONObject("avatar")?.optJSONArray("sources")?.optJSONObject(0)?.optString("url")
                    val profile = SpotifyUserProfile(
                        id = username,
                        displayName = name.ifEmpty { "Spotify User" },
                        email = null,
                        avatarUrl = avatar,
                        product = "Premium"
                    )
                    if (prefs != null) {
                        prefs.edit()
                            .putString("user_profile_id", username)
                            .putString("user_profile_name", name)
                            .putString("user_profile_avatar", avatar ?: "")
                            .putString("user_profile_product", "Premium")
                            .apply()
                    }
                    return@withContext Result.success(profile)
                }
            } else {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.w(TAG, "GraphQL profileAttributes failed, falling back to Web API", e)
        }

        try {
            val url = URL("https://api.spotify.com/v1/me")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("User-Agent", USER_AGENT)
            }
            val code = conn.responseCode
            if (code in 200..299) {
                val text = BufferedReader(InputStreamReader(conn.inputStream)).readText()
                conn.disconnect()
                val json = JSONObject(text)
                val id = json.optString("id", "spotify_user")
                val displayName = json.optString("display_name", if (id.isNotEmpty()) id else "Spotify User")
                val email = json.optString("email", null)
                val product = json.optString("product", "free").replaceFirstChar { it.uppercase() }
                val country = json.optString("country", null)
                val images = json.optJSONArray("images")
                val avatar = images?.optJSONObject(0)?.optString("url", null)

                val profile = SpotifyUserProfile(
                    id = id,
                    displayName = displayName.ifEmpty { "Spotify User" },
                    email = email,
                    avatarUrl = avatar,
                    product = product,
                    country = country
                )

                if (prefs != null) {
                    prefs.edit()
                        .putString("user_profile_id", id)
                        .putString("user_profile_name", displayName)
                        .putString("user_profile_avatar", avatar ?: "")
                        .putString("user_profile_product", product)
                        .apply()
                }

                return@withContext Result.success(profile)
            } else {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching user profile online", e)
        }

        // Fallback to cached profile if available
        if (prefs != null) {
            val savedId = prefs.getString("user_profile_id", null)
            val savedName = prefs.getString("user_profile_name", null)
            val savedAvatar = prefs.getString("user_profile_avatar", null)
            val savedProduct = prefs.getString("user_profile_product", "Free") ?: "Free"
            if (!savedId.isNullOrEmpty() && !savedName.isNullOrEmpty()) {
                return@withContext Result.success(
                    SpotifyUserProfile(
                        id = savedId,
                        displayName = savedName,
                        avatarUrl = savedAvatar?.ifEmpty { null },
                        product = savedProduct
                    )
                )
            }
        }

        Result.success(
            SpotifyUserProfile(
                id = "spotify_listener",
                displayName = "Spotify Listener",
                avatarUrl = null,
                product = "Premium"
            )
        )
    }

    suspend fun reportPlaybackToApi(
        track: SpotifyTrack,
        positionMs: Long = 0L,
        prefs: android.content.SharedPreferences? = null,
        isFinished: Boolean = false
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        // 1. Immediately record in local history cache and prefs so track appears at top of Recently Played instantly!
        addLocalHistoryTrack(track, prefs)

        val token = getAccessToken().getOrElse { customAccessToken ?: cachedAccessToken ?: FALLBACK_BEARER_TOKEN }
        val clientToken = customClientToken ?: CLIENT_TOKEN
        val currentDeviceId = getDeviceId(prefs)
        
        // Guarantee valid 40-char hex file ID for Spotify's scrobble engine
        val rawFileId = audioFileMap[track.uri] ?: ""
        val trackFileId = if (rawFileId.length >= 32) {
            rawFileId
        } else {
            // Generate deterministic 40-char hex identifier from track id if fileId wasn't cached yet
            val trackHex = track.id.replace("-", "").lowercase()
            (trackHex + "0123456789abcdef0123456789abcdef").take(40)
        }

        try {
            // Ensure device is registered with Spotify dealer/track-playback session
            ensureDeviceRegistered(token, currentDeviceId)

            val trackDuration = if (track.durationMs > 0) track.durationMs else 203105L
            val reportedPosition = if (isFinished) trackDuration else if (positionMs > 0) positionMs else 1002L

            // 1. Report playback state to Spotify Connect / device track-playback endpoint
            val currentSmId = activeStateMachineId ?: "ChTYh8P7GXEaL9SXQ9jMrnbW_sLlYQ"
            val seq = ++stateSeqNum
            val trackPlaybackEndpoints = listOf(
                "https://gae2-spclient.spotify.com/track-playback/v1/devices/$currentDeviceId/state",
                "https://spclient.wg.spotify.com/track-playback/v1/devices/$currentDeviceId/state"
            )

            for (stateUrlStr in trackPlaybackEndpoints) {
                try {
                    val stateUrl = URL(stateUrlStr)
                    val conn = (stateUrl.openConnection() as HttpURLConnection).apply {
                        requestMethod = "PUT"
                        doOutput = true
                        connectTimeout = 6000
                        readTimeout = 6000
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
                        put("seq_num", seq)
                        put("state_ref", JSONObject().apply {
                            put("state_machine_id", currentSmId)
                            put("state_id", activeStateId)
                            put("paused", false)
                        })
                        put("sub_state", JSONObject().apply {
                            put("playback_speed", 1)
                            put("position", reportedPosition)
                            put("duration", trackDuration)
                            put("media_type", "AUDIO")
                            put("bitrate", 128000)
                            put("audio_quality", "HIGH")
                            put("format", 10)
                            put("is_video_on", false)
                        })
                        put("previous_position", reportedPosition)
                        put("debug_source", if (isFinished) "track_done" else if (positionMs >= 30000L) "playback_progress" else "started_playing")
                    }

                    OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
                    val code = conn.responseCode
                    val respBody = try {
                        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                        stream?.bufferedReader()?.readText() ?: ""
                    } catch (e: Exception) { "" }
                    conn.disconnect()

                    if (code in 200..299 && respBody.isNotEmpty()) {
                        try {
                            val respJson = JSONObject(respBody)
                            val smObj = respJson.optJSONObject("state_machine")
                            val newSm = smObj?.optString("state_machine_id")
                            if (!newSm.isNullOrEmpty()) {
                                activeStateMachineId = newSm
                            }
                            val updatedRef = respJson.optJSONObject("updated_state_ref")
                            val stateIdx = updatedRef?.optInt("state_index", -1) ?: -1
                            val statesArr = smObj?.optJSONArray("states")
                            if (statesArr != null && stateIdx in 0 until statesArr.length()) {
                                val nextStateId = statesArr.getJSONObject(stateIdx).optString("state_id")
                                if (nextStateId.isNotEmpty()) {
                                    activeStateId = nextStateId
                                }
                            }
                        } catch (e: Exception) {}
                    }

                    Log.d(TAG, "Report playback to track-playback API status: $code ($stateUrlStr) for '${track.name}' (pos: ${reportedPosition}ms, finished: $isFinished)")
                } catch (e: Exception) {
                    Log.w(TAG, "Error reporting track-playback state to $stateUrlStr", e)
                }
            }

            // Sync with Spotify Connect player command to register with account cluster
            if (!isFinished && positionMs < 5000L) {
                val targetDevices = mutableListOf(currentDeviceId)
                val wpDevId = webPlayerDeviceId
                if (!wpDevId.isNullOrEmpty() && wpDevId != currentDeviceId && !targetDevices.contains(wpDevId)) {
                    targetDevices.add(wpDevId)
                }

                for (targetDevId in targetDevices) {
                    try {
                        val playCmdUrl = URL("https://gae2-spclient.spotify.com/connect-state/v1/player/command/from/$currentDeviceId/to/$targetDevId")
                        val cmdId = UUID.randomUUID().toString().replace("-", "")
                        val ctxUri = if (!track.albumUri.isNullOrEmpty() && track.albumUri.startsWith("spotify:album:")) {
                            track.albumUri
                        } else {
                            track.uri
                        }
                        val playCmdBody = JSONObject().apply {
                            put("command", JSONObject().apply {
                                put("context", JSONObject().apply {
                                    put("uri", ctxUri)
                                    put("url", "context://$ctxUri")
                                    put("metadata", JSONObject())
                                })
                                put("play_origin", JSONObject().apply {
                                    put("feature_identifier", if (ctxUri.startsWith("spotify:album:")) "album" else "track")
                                    put("feature_version", "web-player_2026-10-09_1791504000000_d873ee9db135")
                                    put("referrer_identifier", "home")
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

                        val pConn = (playCmdUrl.openConnection() as HttpURLConnection).apply {
                            requestMethod = "POST"
                            doOutput = true
                            connectTimeout = 5000
                            readTimeout = 5000
                            setRequestProperty("authorization", "Bearer $token")
                            setRequestProperty("client-token", clientToken)
                            setRequestProperty("content-type", "application/json;charset=UTF-8")
                            setRequestProperty("app-platform", APP_PLATFORM)
                            setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                            setRequestProperty("origin", "https://open.spotify.com")
                            setRequestProperty("referer", "https://open.spotify.com/")
                        }
                        OutputStreamWriter(pConn.outputStream).use { it.write(playCmdBody.toString()) }
                        val pCode = pConn.responseCode
                        pConn.disconnect()
                        Log.d(TAG, "Sync playback to Spotify Connect player command (target: $targetDevId): HTTP $pCode for ${track.uri}")
                    } catch (ce: Exception) {
                        Log.w(TAG, "Error syncing playback to connect-state for target $targetDevId", ce)
                    }
                }
            }

            // 2. Spotify Web Player / Melody Batch Telemetry
            val playbackId = UUID.randomUUID().toString().replace("-", "")
            val sessionId = System.currentTimeMillis().toString()

            val messagesList = mutableListOf<JSONObject>()

            if (!isFinished && positionMs < 30000L) {
                // Playback start event
                val melodyStartPayload = JSONObject().apply {
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "jssdk_playback_start")
                            put("message", JSONObject().apply {
                                put("play_track", track.uri)
                                put("file_id", trackFileId)
                                put("playback_id", playbackId)
                                put("session_id", sessionId)
                                put("ms_start_position", positionMs)
                                put("initially_paused", false)
                                put("client_id", "")
                                put("correlation_id", "")
                                put("feature_identifier", "")
                            })
                        })
                    })
                    put("sdk_id", "harmony:4.80.0")
                    put("platform", "web_player osx 10.15.7;chrome 155.0.0.0;desktop")
                    put("client_version", "0.0.0")
                }
                messagesList.add(melodyStartPayload)

                val melodyCmdPayload = JSONObject().apply {
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "jssdk_connect_command")
                            put("message", JSONObject().apply {
                                put("command_id", playbackId)
                                put("command_type", "play")
                                put("target_device_brand", "spotify")
                                put("target_device_model", "web_player")
                                put("target_device_id", currentDeviceId)
                                put("result", "success")
                                put("http_status_code", 200)
                            })
                        })
                    })
                    put("sdk_id", "harmony:4.80.0-3cdf02c91")
                    put("platform", "web_player osx 10.15.7;chrome 155.0.0.0;desktop")
                    put("client_version", "0.0.0")
                }
                messagesList.add(melodyCmdPayload)
            } else {
                // Playback stats event (scrobble trigger when played >= 30s, periodic progress, or finished)
                val playedMs = if (isFinished) trackDuration else if (positionMs >= 30000L) positionMs else positionMs.coerceAtLeast(1002L)
                val now = System.currentTimeMillis()
                val startTime = now - playedMs
                val melodyStatsPayload = JSONObject().apply {
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "jssdk_playback_stats")
                            put("message", JSONObject().apply {
                                put("play_track", track.uri)
                                put("file_id", trackFileId)
                                put("playback_id", playbackId)
                                put("session_id", sessionId)
                                put("internal_play_id", "1")
                                put("start_time", startTime)
                                put("end_time", now)
                                put("ms_played", playedMs)
                                put("ms_nominal_played", playedMs)
                                put("ms_file_duration", trackDuration)
                                put("ms_actual_duration", trackDuration)
                                put("ms_metadata_duration", trackDuration)
                                put("ms_start_position", 0)
                                put("ms_end_position", reportedPosition)
                                put("reason_start", "playbtn")
                                put("reason_end", if (isFinished) "trackdone" else "endplay")
                                put("initially_paused", false)
                                put("had_error", false)
                                put("sequence_id", 0)
                                put("client_id", "")
                                put("correlation_id", "")
                                put("urls_json", "{\"version\":\"1.0.0\",\"urls\":[]}")
                                put("manifest_id", trackFileId)
                                put("memory_cached", false)
                                put("persistent_cached", false)
                                put("audio_format", "")
                                put("video_format", "")
                                put("protected", false)
                                put("key_system", "")
                                put("key_system_impl", "")
                                put("external_start_time", 0)
                                put("ms_played_per_surface", JSONObject())
                                put("ms_played_visible", 0)
                                put("n_stalls", 0)
                                put("n_rendition_upgrade", 0)
                                put("n_rendition_downgrade", 0)
                                put("bps_bandwidth_max", 0)
                                put("bps_bandwidth_min", 0)
                                put("bps_bandwidth_avg", 0)
                                put("n_seekback", 0)
                                put("n_seekforward", 0)
                                put("start_bitrate", 0)
                                put("time_weighted_bitrate", 0)
                                put("n_warnings", 0)
                                put("n_navigator_offline", 0)
                                put("n_dropped_video_frames", 0)
                                put("n_total_video_frames", 0)
                                put("resolution_max", 0)
                                put("resolution_min", 0)
                                put("total_bytes", 0)
                                put("strategy", "")
                                put("ms_played_per_audio_format", JSONObject())
                                put("ms_played_per_video_format", JSONObject())
                            })
                        })
                    })
                    put("sdk_id", "overture:4.80.0")
                    put("platform", "Partner spotify desktop")
                    put("client_version", SPOTIFY_APP_VERSION)
                }
                messagesList.add(melodyStatsPayload)
            }

            val melodyEndpoints = listOf(
                "https://gae2-spclient.spotify.com/melody/v1/msg/batch",
                "https://spclient.wg.spotify.com/melody/v1/msg/batch"
            )

            for (melodyUrlStr in melodyEndpoints) {
                for (payload in messagesList) {
                    try {
                        val mUrl = URL(melodyUrlStr)
                        val mConn = (mUrl.openConnection() as HttpURLConnection).apply {
                            requestMethod = "POST"
                            doOutput = true
                            connectTimeout = 5000
                            readTimeout = 5000
                            setRequestProperty("authorization", "Bearer $token")
                            setRequestProperty("client-token", clientToken)
                            setRequestProperty("content-type", "text/plain;charset=UTF-8")
                            setRequestProperty("referer", "https://open.spotify.com/")
                            setRequestProperty("origin", "https://open.spotify.com")
                            setRequestProperty("user-agent", USER_AGENT)
                        }
                        OutputStreamWriter(mConn.outputStream).use { it.write(payload.toString()) }
                        val mc = mConn.responseCode
                        val respText = try {
                            val stream = if (mc in 200..299) mConn.inputStream else mConn.errorStream
                            BufferedReader(InputStreamReader(stream)).readText()
                        } catch (e: Exception) { "" }
                        mConn.disconnect()
                        Log.d(TAG, "melody batch ($melodyUrlStr) status: $mc, resp: $respText (isFinished: $isFinished, playedMs: $reportedPosition)")
                    } catch (ex: Exception) {
                        Log.w(TAG, "Error posting melody batch to $melodyUrlStr", ex)
                    }
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            Log.w(TAG, "Failed reporting track to Spotify history API", e)
            Result.failure(e)
        }
    }

    suspend fun fetchSpotifyRecentlyPlayed(prefs: android.content.SharedPreferences? = null): Result<List<SpotifyTrack>> = withContext(Dispatchers.IO) {
        if (prefs != null && playbackHistory.isEmpty()) {
            loadHistoryFromPrefs(prefs)
        }
        val token = getAccessToken().getOrElse { customAccessToken ?: cachedAccessToken ?: FALLBACK_BEARER_TOKEN }
        val list = mutableListOf<SpotifyTrack>()

        // 1. Local playback history always comes first so newly played songs are immediately visible
        list.addAll(playbackHistory)

        // 2. Fetch from Spotify recently-played endpoint to merge account plays from other devices
        try {
            val clientToken = customClientToken ?: CLIENT_TOKEN
            val url = URL("https://api.spotify.com/v1/me/player/recently-played?limit=50")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("client-token", clientToken)
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Origin", "https://open.spotify.com")
                setRequestProperty("Referer", "https://open.spotify.com/")
                setRequestProperty("app-platform", APP_PLATFORM)
                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
            }
            val responseCode = conn.responseCode
            val respBody = try {
                val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
                stream?.bufferedReader()?.readText() ?: ""
            } catch (e: Exception) { "" }
            conn.disconnect()

            Log.d(TAG, "Spotify recently-played HTTP Status: $responseCode, length: ${respBody.length}")

            if (responseCode == 401) {
                invalidateTokens()
            } else if (responseCode in 200..299 && respBody.isNotEmpty()) {
                val json = JSONObject(respBody)
                val items = json.optJSONArray("items") ?: JSONArray()
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
                historyManager.mergeRemoteHistory(list, prefs)
            } else if (responseCode == 429) {
                Log.w(TAG, "Spotify recently-played rate limited (429), relying strictly on local history")
            }
            Log.d(TAG, "Fetched ${list.size} tracks for recently played (online + local)")
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching online recents, returning local recents", e)
        }

        if (prefs != null && list.isNotEmpty()) {
            historyManager.mergeRemoteHistory(list, prefs)
        }

        Result.success(playbackHistory.ifEmpty { list })
    }

    suspend fun fetchUserPlaylists(prefs: android.content.SharedPreferences? = null): Result<List<SpotifyPlaylistSummary>> = withContext(Dispatchers.IO) {
        val results = mutableListOf<SpotifyPlaylistSummary>()
        val token = getAccessToken().getOrElse { customAccessToken ?: cachedAccessToken ?: FALLBACK_BEARER_TOKEN }
        val clientToken = customClientToken ?: CLIENT_TOKEN

        // 1. Try Pathfinder v2 libraryV3 query to fetch all user playlists (private, public, followed)
        try {
            val url = URL("https://api-partner.spotify.com/pathfinder/v2/query")
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
            val payload = JSONObject().apply {
                put("variables", JSONObject().apply {
                    put("filters", JSONArray().apply { put("Playlists") })
                    put("order", JSONObject.NULL)
                    put("textFilter", "")
                    put("features", JSONArray().apply {
                        put("LIKED_SONGS")
                        put("YOUR_EPISODES_V2")
                        put("PRERELEASES")
                        put("EVENTS")
                    })
                    put("limit", 50)
                    put("offset", 0)
                    put("flatten", true)
                    put("expandedFolders", JSONArray())
                    put("folderUri", JSONObject.NULL)
                    put("includeFoldersWhenFlattening", false)
                })
                put("operationName", "libraryV3")
                put("extensions", JSONObject().apply {
                    put("persistedQuery", JSONObject().apply {
                        put("version", 1)
                        put("sha256Hash", LIBRARY_HASH)
                    })
                })
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            val code = conn.responseCode
            if (code in 200..299) {
                val resBody = BufferedReader(InputStreamReader(conn.inputStream)).readText()
                conn.disconnect()
                val json = JSONObject(resBody)
                val items = json.optJSONObject("data")?.optJSONObject("me")?.optJSONObject("libraryV3")?.optJSONArray("items")
                if (items != null) {
                    for (i in 0 until items.length()) {
                        val itObj = items.getJSONObject(i)
                        val itemWrapper = itObj.optJSONObject("item") ?: continue
                        val itemData = itemWrapper.optJSONObject("data") ?: JSONObject()
                        val typeName = itemWrapper.optString("__typename")
                        if (typeName == "LibraryPseudoPlaylistResponseWrapper") continue // Handled separately (Liked Songs)
                        val uri = itemWrapper.optString("uri", itemWrapper.optString("_uri"))
                        if (uri.isEmpty() || !uri.contains("playlist")) continue
                        val id = uri.substringAfterLast(":")
                        val name = itemData.optString("name", itemWrapper.optString("name", "Playlist"))
                        val ownerName = itemData.optJSONObject("ownerV2")?.optJSONObject("data")?.optString("name") ?: "Spotify"
                        val imagesArr = itemData.optJSONObject("images")?.optJSONArray("items")
                        val coverUrl = imagesArr?.optJSONObject(0)?.optJSONArray("sources")?.optJSONObject(0)?.optString("url")
                            ?: itemData.optJSONArray("images")?.optJSONObject(0)?.optString("url")
                        val total = itemData.optInt("count", 0).let { c ->
                            if (c > 0) c else itemData.optJSONObject("tracks")?.optInt("total", 0) ?: 0
                        }

                        if (results.none { it.uri == uri || it.id == id }) {
                            results.add(SpotifyPlaylistSummary(id, uri, name, ownerName, coverUrl, total))
                        }
                    }
                }
            } else {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Pathfinder libraryV3 query error", e)
        }

        // 2. Fallback / Merge with Web API me/playlists
        if (results.isEmpty()) {
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
                        if (results.none { it.id == id || it.uri == uri }) {
                            results.add(SpotifyPlaylistSummary(id, uri, name, owner, coverUrl, total))
                        }
                    }
                } else {
                    conn.disconnect()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error fetching web playlists", e)
            }
        }

        // 3. Fallback / Merge with spclient rootlist (Spotify internal rootlist API)
        if (results.isEmpty()) {
            try {
                val userProfile = fetchCurrentUserProfile(prefs).getOrNull()
                val username = userProfile?.id?.ifEmpty { null } ?: prefs?.getString("user_profile_id", null) ?: "31dgtnegwnonpz7pbb62523oc5ny"
                val rootlistUrl = URL("https://spclient.wg.spotify.com/playlist/v2/user/$username/rootlist?decorate=revision%2Clength%2Cattributes%2Ctimestamp%2Cowner%2Ccapabilities&bustCache=${System.currentTimeMillis()}")
                val conn = (rootlistUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("authorization", "Bearer $token")
                    setRequestProperty("client-token", clientToken)
                    setRequestProperty("app-platform", APP_PLATFORM)
                    setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                    setRequestProperty("origin", "https://open.spotify.com")
                    setRequestProperty("referer", "https://open.spotify.com/")
                    setRequestProperty("user-agent", USER_AGENT)
                }
                if (conn.responseCode in 200..299) {
                    val body = BufferedReader(InputStreamReader(conn.inputStream)).readText()
                    conn.disconnect()
                    val json = JSONObject(body)
                    val contents = json.optJSONObject("contents")
                    val items = contents?.optJSONArray("items") ?: JSONArray()
                    for (i in 0 until items.length()) {
                        val itObj = items.getJSONObject(i)
                        val uri = itObj.optString("uri")
                        if (uri.contains("playlist")) {
                            val id = uri.substringAfterLast(":")
                            val name = itObj.optJSONObject("attributes")?.optString("name", "Playlist") ?: "Playlist"
                            if (results.none { it.id == id || it.uri == uri }) {
                                results.add(SpotifyPlaylistSummary(id, uri, name, "You", null, 0))
                            }
                        }
                    }
                } else {
                    conn.disconnect()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error reading rootlist", e)
            }
        }

        Result.success(results)
    }

    suspend fun createPlaylist(
        name: String,
        description: String = "",
        isPublic: Boolean = true,
        prefs: android.content.SharedPreferences? = null
    ): Result<SpotifyPlaylistSummary> = withContext(Dispatchers.IO) {
        val token = getAccessToken().getOrElse { customAccessToken ?: cachedAccessToken ?: FALLBACK_BEARER_TOKEN }
        val clientToken = customClientToken ?: CLIENT_TOKEN
        var createdOnline: SpotifyPlaylistSummary? = null

        // 1. Primary Spotify Web Player method: POST to spclient.wg.spotify.com/playlist/v2/playlist
        //    followed by POST to /rootlist/changes to add it to user's Spotify account
        try {
            val plUrl = URL("https://spclient.wg.spotify.com/playlist/v2/playlist")
            val plConn = (plUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("client-token", clientToken)
                setRequestProperty("accept", "application/json")
                setRequestProperty("content-type", "application/json;charset=UTF-8")
                setRequestProperty("app-platform", APP_PLATFORM)
                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("referer", "https://open.spotify.com/")
                setRequestProperty("user-agent", USER_AGENT)
            }

            val plBody = JSONObject().apply {
                put("ops", JSONArray().apply {
                    put(JSONObject().apply {
                        put("kind", "UPDATE_LIST_ATTRIBUTES")
                        put("updateListAttributes", JSONObject().apply {
                            put("newAttributes", JSONObject().apply {
                                put("values", JSONObject().apply {
                                    put("name", name)
                                    if (description.isNotEmpty()) {
                                        put("description", description)
                                    }
                                })
                            })
                        })
                    })
                })
            }

            OutputStreamWriter(plConn.outputStream).use { it.write(plBody.toString()) }
            val plCode = plConn.responseCode
            if (plCode in 200..299) {
                val resText = BufferedReader(InputStreamReader(plConn.inputStream)).readText()
                val resJson = JSONObject(resText)
                val playlistUri = resJson.optString("uri")
                val cleanId = playlistUri.substringAfterLast(":")
                createdOnline = SpotifyPlaylistSummary(cleanId, playlistUri, name, "You", null, 0)
                Log.d(TAG, "Created playlist on Spotify: $playlistUri")

                // Add to rootlist changes so it appears in the user's Spotify account
                try {
                    val userProfile = fetchCurrentUserProfile(prefs).getOrNull()
                    val username = userProfile?.id?.ifEmpty { null } ?: prefs?.getString("user_profile_id", null) ?: "31dgtnegwnonpz7pbb62523oc5ny"
                    val rootChangesUrl = URL("https://spclient.wg.spotify.com/playlist/v2/user/$username/rootlist/changes")
                    val rcConn = (rootChangesUrl.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        doOutput = true
                        connectTimeout = 8000
                        readTimeout = 8000
                        setRequestProperty("authorization", "Bearer $token")
                        setRequestProperty("client-token", clientToken)
                        setRequestProperty("accept", "application/json")
                        setRequestProperty("content-type", "application/json;charset=UTF-8")
                        setRequestProperty("app-platform", APP_PLATFORM)
                        setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                        setRequestProperty("origin", "https://open.spotify.com")
                        setRequestProperty("referer", "https://open.spotify.com/")
                        setRequestProperty("user-agent", USER_AGENT)
                    }

                    val rcBody = JSONObject().apply {
                        put("deltas", JSONArray().apply {
                            put(JSONObject().apply {
                                put("ops", JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("kind", "ADD")
                                        put("add", JSONObject().apply {
                                            put("items", JSONArray().apply {
                                                put(JSONObject().apply {
                                                    put("uri", playlistUri)
                                                    put("attributes", JSONObject().apply {
                                                        put("timestamp", System.currentTimeMillis().toString())
                                                        put("public", isPublic)
                                                    })
                                                })
                                            })
                                            put("addFirst", true)
                                        })
                                    })
                                })
                                put("info", JSONObject().apply {
                                    put("source", JSONObject().apply {
                                        put("client", "WEBPLAYER")
                                    })
                                })
                            })
                        })
                    }

                    OutputStreamWriter(rcConn.outputStream).use { it.write(rcBody.toString()) }
                    val rcCode = rcConn.responseCode
                    rcConn.disconnect()
                    Log.d(TAG, "Added playlist to rootlist changes: $rcCode")
                } catch (e: Exception) {
                    Log.w(TAG, "Error adding to rootlist changes", e)
                }
            }
            plConn.disconnect()
        } catch (e: Exception) {
            Log.w(TAG, "Failed creating playlist via spclient", e)
        }

        // 2. Fallback to Web API POST https://api.spotify.com/v1/me/playlists
        if (createdOnline == null) {
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
                    put("public", isPublic)
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
                Log.w(TAG, "Failed creating online playlist via Web API", e)
            }
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
        val token = getAccessToken().getOrElse { customAccessToken ?: cachedAccessToken ?: FALLBACK_BEARER_TOKEN }
        val clientToken = customClientToken ?: CLIENT_TOKEN

        val canonicalPlaylistUri = if (playlistId.startsWith("spotify:playlist:")) playlistId else "spotify:playlist:${playlistId.substringAfterLast(":")}"
        val cleanId = playlistId.substringAfterLast(":")

        // 1. Try Pathfinder v2 addToPlaylist GraphQL query (matches web client network flow)
        var onlineSuccess = false
        if (!playlistId.startsWith("local_")) {
            try {
                val pfUrl = URL("https://api-partner.spotify.com/pathfinder/v2/query")
                val pfConn = (pfUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("authorization", "Bearer $token")
                    setRequestProperty("client-token", clientToken)
                    setRequestProperty("content-type", "application/json;charset=UTF-8")
                    setRequestProperty("app-platform", APP_PLATFORM)
                    setRequestProperty("origin", "https://open.spotify.com")
                    setRequestProperty("referer", "https://open.spotify.com/")
                    setRequestProperty("accept", "application/json")
                    setRequestProperty("user-agent", USER_AGENT)
                }
                val pfBody = JSONObject().apply {
                    put("variables", JSONObject().apply {
                        put("playlistItemUris", JSONArray().apply { put(track.uri) })
                        put("playlistUri", canonicalPlaylistUri)
                        put("newPosition", JSONObject().apply {
                            put("moveType", "BOTTOM_OF_PLAYLIST")
                            put("fromUid", JSONObject.NULL)
                        })
                    })
                    put("operationName", "addToPlaylist")
                    put("extensions", JSONObject().apply {
                        put("persistedQuery", JSONObject().apply {
                            put("version", 1)
                            put("sha256Hash", ADD_TO_PLAYLIST_HASH)
                        })
                    })
                }
                OutputStreamWriter(pfConn.outputStream).use { it.write(pfBody.toString()) }
                val pfCode = pfConn.responseCode
                if (pfCode in 200..299) {
                    val pfRes = BufferedReader(InputStreamReader(pfConn.inputStream)).readText()
                    if (!pfRes.contains("errors") || pfRes.contains("AddItemsToPlaylistPayload")) {
                        onlineSuccess = true
                        Log.d(TAG, "Successfully added '${track.name}' to Spotify playlist $canonicalPlaylistUri via Pathfinder")
                    }
                }
                pfConn.disconnect()
            } catch (e: Exception) {
                Log.w(TAG, "Could not add track via Pathfinder query", e)
            }

            // 2. Fallback to Web API: POST https://api.spotify.com/v1/playlists/{playlist_id}/tracks
            if (!onlineSuccess) {
                try {
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
                    if (code in 200..299) {
                        onlineSuccess = true
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not add track online via Web API: $playlistId", e)
                }
            }
        }

        // 3. Mirror in local preferences cache
        if (prefs != null) {
            try {
                val localJson = prefs.getString("local_user_playlists_json", "[]") ?: "[]"
                val array = JSONArray(localJson)
                var found = false
                for (i in 0 until array.length()) {
                    val p = array.getJSONObject(i)
                    if (p.optString("id") == playlistId || p.optString("uri") == playlistId || p.optString("id") == cleanId) {
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
                        put("uri", canonicalPlaylistUri)
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

    suspend fun removeTrackFromPlaylist(playlistId: String, trackUri: String, prefs: android.content.SharedPreferences? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        val token = getAccessToken().getOrElse { customAccessToken ?: cachedAccessToken ?: FALLBACK_BEARER_TOKEN }
        if (!playlistId.startsWith("local_")) {
            try {
                val cleanId = playlistId.substringAfterLast(":")
                val url = URL("https://api.spotify.com/v1/playlists/$cleanId/tracks")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "DELETE"
                    doOutput = true
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "application/json")
                }
                val body = JSONObject().apply {
                    put("tracks", JSONArray().apply {
                        put(JSONObject().apply { put("uri", trackUri) })
                    })
                }
                OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
                conn.responseCode
                conn.disconnect()
            } catch (e: Exception) {
                Log.w(TAG, "Error removing track from online playlist", e)
            }
        }
        if (prefs != null) {
            try {
                val localJson = prefs.getString("local_user_playlists_json", "[]") ?: "[]"
                val array = JSONArray(localJson)
                for (i in 0 until array.length()) {
                    val p = array.getJSONObject(i)
                    if (p.optString("id") == playlistId || p.optString("uri") == playlistId) {
                        val tracks = p.optJSONArray("tracks") ?: JSONArray()
                        val newTracks = JSONArray()
                        for (j in 0 until tracks.length()) {
                            val tObj = tracks.getJSONObject(j)
                            if (tObj.optString("uri") != trackUri) {
                                newTracks.put(tObj)
                            }
                        }
                        p.put("tracks", newTracks)
                        break
                    }
                }
                prefs.edit().putString("local_user_playlists_json", array.toString()).apply()
            } catch (e: Exception) {
                Log.w(TAG, "Error removing track from local playlist", e)
            }
        }
        Result.success(true)
    }

    suspend fun deletePlaylist(playlistId: String, prefs: android.content.SharedPreferences? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        val token = getAccessToken().getOrElse { customAccessToken ?: cachedAccessToken ?: FALLBACK_BEARER_TOKEN }
        if (!playlistId.startsWith("local_")) {
            try {
                val cleanId = playlistId.substringAfterLast(":")
                val url = URL("https://api.spotify.com/v1/playlists/$cleanId/followers")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "DELETE"
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("Authorization", "Bearer $token")
                }
                conn.responseCode
                conn.disconnect()
            } catch (e: Exception) {
                Log.w(TAG, "Error unfollowing/deleting online playlist", e)
            }
        }
        if (prefs != null) {
            try {
                val localJson = prefs.getString("local_user_playlists_json", "[]") ?: "[]"
                val array = JSONArray(localJson)
                val newArray = JSONArray()
                for (i in 0 until array.length()) {
                    val p = array.getJSONObject(i)
                    if (p.optString("id") != playlistId && p.optString("uri") != playlistId) {
                        newArray.put(p)
                    }
                }
                prefs.edit().putString("local_user_playlists_json", newArray.toString()).apply()
            } catch (e: Exception) {
                Log.w(TAG, "Error deleting local playlist", e)
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

        // 2. Sync with Spotify Account using GraphQL mutation (Spotui flow - works reliably with web tokens)
        var onlineSynced = false
        try {
            val token = getAccessToken().getOrElse { FALLBACK_BEARER_TOKEN }
            val clientToken = customClientToken ?: CLIENT_TOKEN
            val operation = if (isFavorite) "addToLibrary" else "removeFromLibrary"
            val hash = if (isFavorite) ADD_TO_LIBRARY_HASH else REMOVE_FROM_LIBRARY_HASH

            val url = URL("https://api-partner.spotify.com/pathfinder/v2/query")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("client-token", clientToken)
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Origin", "https://open.spotify.com")
                setRequestProperty("Referer", "https://open.spotify.com/")
                setRequestProperty("app-platform", APP_PLATFORM)
                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
            }

            val body = JSONObject().apply {
                put("variables", JSONObject().apply {
                    put("libraryItemUris", JSONArray().apply { put(track.uri) })
                })
                put("operationName", operation)
                put("extensions", JSONObject().apply {
                    put("persistedQuery", JSONObject().apply {
                        put("version", 1)
                        put("sha256Hash", hash)
                    })
                })
            }

            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
            val code = conn.responseCode
            if (code in 200..299) {
                val resBody = BufferedReader(InputStreamReader(conn.inputStream)).readText()
                if (!resBody.contains("errors")) {
                    onlineSynced = true
                    Log.d(TAG, "Successfully synced favorite to Spotify via GraphQL $operation: ${track.name}")
                }
            }
            conn.disconnect()
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing favorite via GraphQL", e)
        }

        // 3. Fallback to Spotify Web API v1/me/tracks
        if (!onlineSynced) {
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
        val clientToken = customClientToken ?: CLIENT_TOKEN
        val onlineList = mutableListOf<SpotifyTrack>()

        // 1. Primary: GraphQL fetchLibraryTracks query (matches Spotui, avoids Web API 429)
        try {
            val gqlUrl = URL("https://api-partner.spotify.com/pathfinder/v2/query")
            val conn = (gqlUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("client-token", clientToken)
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Origin", "https://open.spotify.com")
                setRequestProperty("Referer", "https://open.spotify.com/")
                setRequestProperty("app-platform", APP_PLATFORM)
                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
            }
            val payload = JSONObject().apply {
                put("variables", JSONObject().apply {
                    put("offset", 0)
                    put("limit", 50)
                })
                put("operationName", "fetchLibraryTracks")
                put("extensions", JSONObject().apply {
                    put("persistedQuery", JSONObject().apply {
                        put("version", 1)
                        put("sha256Hash", "087278b20b743578a6262c2b0b4bcd20d879c503cc359a2285baf083ef944240")
                    })
                })
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            if (conn.responseCode in 200..299) {
                val resBody = BufferedReader(InputStreamReader(conn.inputStream)).readText()
                conn.disconnect()
                val json = JSONObject(resBody)
                val items = json.optJSONObject("data")?.optJSONObject("me")?.optJSONObject("library")?.optJSONObject("tracks")?.optJSONArray("items")
                if (items != null) {
                    for (i in 0 until items.length()) {
                        val elem = items.getJSONObject(i)
                        val trackWrapper = elem.optJSONObject("track") ?: continue
                        val trackData = trackWrapper.optJSONObject("data") ?: continue
                        val id = trackWrapper.optString("id", trackData.optString("id", ""))
                        val uri = trackWrapper.optString("_uri", trackWrapper.optString("uri", "spotify:track:$id"))
                        val name = trackData.optString("name", "Track")
                        val dur = trackData.optLong("duration", 0L)
                        val artistsList = trackData.optJSONObject("artists")?.optJSONArray("items")
                        val artistNames = mutableListOf<String>()
                        var artistUri: String? = null
                        if (artistsList != null) {
                            for (a in 0 until artistsList.length()) {
                                val aObj = artistsList.getJSONObject(a)
                                val aName = aObj.optJSONObject("profile")?.optString("name") ?: aObj.optString("name")
                                if (aName.isNotEmpty()) artistNames.add(aName)
                                if (artistUri == null) artistUri = aObj.optString("uri")
                            }
                        }
                        val albumObj = trackData.optJSONObject("albumOfTrack")
                        val albName = albumObj?.optString("name", "") ?: ""
                        val albUri = albumObj?.optString("uri")
                        val cover = albumObj?.optJSONObject("coverArt")?.optJSONArray("sources")?.optJSONObject(0)?.optString("url")

                        onlineList.add(
                            SpotifyTrack(
                                id = id.ifEmpty { uri.substringAfterLast(":") },
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
            } else {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.w(TAG, "GraphQL fetchLibraryTracks error, falling back to Web API", e)
        }

        // 2. Secondary: Spotify Web API v1/me/tracks
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
                        onlineList.add(
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
            } else {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchLikedSongs API failed, fallback to local cache", e)
        }

        // Merge with local favorites meta
        val mergedList = mutableListOf<SpotifyTrack>()
        mergedList.addAll(onlineList)

        if (prefs != null) {
            try {
                val metaJson = prefs.getString("favorite_tracks_meta", null)
                if (!metaJson.isNullOrEmpty()) {
                    val obj = JSONObject(metaJson)
                    val keys = obj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val t = obj.getJSONObject(k)
                        val id = t.optString("id")
                        if (mergedList.none { it.id == id }) {
                            mergedList.add(
                                SpotifyTrack(
                                    id = id,
                                    uri = t.optString("uri", "spotify:track:$id"),
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
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error merging local favorite tracks", e)
            }
        }

        if (mergedList.isNotEmpty()) {
            val allIds = mergedList.map { it.id }.toSet()
            synchronized(favoriteTrackIds) {
                favoriteTrackIds.addAll(allIds)
            }
            prefs?.edit()?.putStringSet("favorite_tracks_set", allIds)?.apply()
        }

        Result.success(mergedList)
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

    suspend fun getAccessToken(forceRefresh: Boolean = false): Result<String> = authService.getAccessToken(forceRefresh)

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
                                val smId = sm?.optString("state_machine_id")
                                if (!smId.isNullOrEmpty()) {
                                    activeStateMachineId = smId
                                    Log.d(TAG, "Captured active state_machine_id: $smId")
                                }
                                val states = sm?.optJSONArray("states")
                                if (states != null && states.length() > 0) {
                                    val sId = states.getJSONObject(0).optString("state_id")
                                    if (sId.isNotEmpty()) activeStateId = sId
                                }
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

    internal suspend fun ensureDeviceRegistered(token: String, deviceId: String = DEVICE_ID): Boolean = withContext(Dispatchers.IO) {
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
                    put("device_id", deviceId)
                    put("device_type", "computer")
                    put("metadata", JSONObject())
                    put("model", "web_player")
                    put("name", "Stopify Player")
                    put("platform_identifier", "web_player osx 10.15.7;chrome 155.0.0.0;desktop")
                    put("is_group", false)
                    put("is_public", true)
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
                    put("timeZone", "Asia/Calcutta")
                    put("sp_t", SP_T)
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
    }.let { res ->
        // Augment with locally stored tracks if playlist is newly created or remote is propagation-lagged
        if (res.isFailure || (res.isSuccess && res.getOrNull().isNullOrEmpty())) {
            val cleanId = playlistUri.substringAfterLast(":")
            val localTracks = mutableListOf<SpotifyTrack>()
            val prefs = com.shashwat.stopify.MainActivity.globalPrefs
            if (prefs != null) {
                try {
                    val localJson = prefs.getString("local_user_playlists_json", null)
                    if (!localJson.isNullOrEmpty()) {
                        val array = org.json.JSONArray(localJson)
                        for (i in 0 until array.length()) {
                            val p = array.getJSONObject(i)
                            if (p.optString("id") == cleanId || p.optString("uri") == playlistUri) {
                                val tArr = p.optJSONArray("tracks") ?: org.json.JSONArray()
                                for (j in 0 until tArr.length()) {
                                    val tObj = tArr.getJSONObject(j)
                                    val uri = tObj.optString("uri")
                                    localTracks.add(
                                        SpotifyTrack(
                                            id = uri.substringAfterLast(":"),
                                            uri = uri,
                                            name = tObj.optString("name", "Track"),
                                            artist = tObj.optString("artist", "Artist"),
                                            album = tObj.optString("album", "Album"),
                                            coverUrl = tObj.optString("coverUrl", null),
                                            durationMs = tObj.optLong("durationMs", 0L)
                                        )
                                    )
                                }
                                break
                            }
                        }
                    }
                } catch (ex: Exception) {
                    Log.w(TAG, "Error fallback local playlist tracks", ex)
                }
            }
            if (localTracks.isNotEmpty()) {
                Result.success(localTracks)
            } else {
                res
            }
        } else {
            res
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

                if (code == 410 || code == 404) {
                    // Session or device expired on Dealer; force re-registration and retry command once
                    Log.w(TAG, "Device expired (HTTP $code). Re-registering with Dealer and retrying play command...")
                    isDeviceRegistered = false
                    dealerConnectionId = null
                    activeWebSocket?.close(1000, "Reconnecting")
                    activeWebSocket = null
                    connectionIdDeferred = CompletableDeferred()
                    if (ensureDeviceRegistered(token)) {
                        try {
                            val retryConn = (playUrl.openConnection() as HttpURLConnection).apply {
                                requestMethod = "POST"
                                doOutput = true
                                connectTimeout = 8000
                                readTimeout = 8000
                                setRequestProperty("authorization", "Bearer $token")
                                setRequestProperty("client-token", customClientToken ?: CLIENT_TOKEN)
                                setRequestProperty("content-type", "application/json;charset=UTF-8")
                                setRequestProperty("app-platform", APP_PLATFORM)
                                setRequestProperty("spotify-app-version", SPOTIFY_APP_VERSION)
                                setRequestProperty("origin", "https://open.spotify.com")
                                setRequestProperty("referer", "https://open.spotify.com/")
                            }
                            OutputStreamWriter(retryConn.outputStream).use { it.write(playPayload.toString()) }
                            val retryCode = retryConn.responseCode
                            retryConn.disconnect()
                            Log.d(TAG, "Retry connect-state play command for $trackUri. Code: $retryCode")
                        } catch (re: Exception) {
                            Log.w(TAG, "Error retrying play command", re)
                        }
                    }
                }

                // Await manifest notification from Dealer WebSocket
                fileId = withTimeoutOrNull(4000L) {
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

    suspend fun fetchColorLyrics(trackId: String, coverUrl: String? = null): Result<SpotifyColorLyrics> =
        lyricsService.fetchColorLyrics(trackId, coverUrl)
}
