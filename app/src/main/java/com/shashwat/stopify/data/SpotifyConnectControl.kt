package com.shashwat.stopify.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Remote control for Spotify Connect, the same calls open.spotify.com makes when you pick another device in
 * the device menu: transfer, per-device volume, and player commands addressed to the device that is playing.
 */
class SpotifyConnectControl(private val client: SpotifyWebClient) {
    companion object {
        private const val TAG = "SpotifyConnectControl"
        private const val BASE = "https://gae2-spclient.spotify.com/connect-state/v1"
        private val SELF get() = SpotifyWebClient.DEVICE_ID
    }

    private suspend fun post(method: String, url: String, body: JSONObject): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = client.getAccessToken().getOrElse {
                client.customAccessToken ?: SpotifyWebClient.FALLBACK_BEARER_TOKEN
            }
            client.ensureDeviceRegistered(token, SELF)
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                doOutput = true
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("client-token", client.customClientToken ?: SpotifyWebClient.CLIENT_TOKEN)
                setRequestProperty("content-type", "application/json;charset=UTF-8")
                setRequestProperty("app-platform", SpotifyWebClient.APP_PLATFORM)
                setRequestProperty("spotify-app-version", SpotifyWebClient.SPOTIFY_APP_VERSION)
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("referer", "https://open.spotify.com/")
                setRequestProperty("user-agent", SpotifyWebClient.USER_AGENT)
                client.dealerConnectionId?.let { setRequestProperty("x-spotify-connection-id", it) }
            }
            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
            val code = conn.responseCode
            conn.disconnect()
            Log.d(TAG, "$method $url -> $code")
            code in 200..299
        } catch (e: Exception) {
            Log.w(TAG, "Connect call failed: $url", e)
            false
        }
    }

    private fun command(endpoint: String, build: JSONObject.() -> Unit = {}): JSONObject = JSONObject().apply {
        put("command", JSONObject().apply {
            put("endpoint", endpoint)
            put("logging_params", JSONObject().apply {
                put("command_id", UUID.randomUUID().toString().replace("-", ""))
            })
            build()
        })
    }

    private suspend fun playerCommand(target: String, body: JSONObject) =
        post("POST", "$BASE/player/command/from/$SELF/to/$target", body)

    /** Moves playback to [targetId]. Keeps playing if something is playing now. */
    suspend fun transfer(targetId: String, keepPlaying: Boolean): Boolean {
        val body = JSONObject().apply {
            put("transfer_options", JSONObject().apply {
                put("restore_paused", if (keepPlaying) "resume" else "restore")
            })
            put("command_id", UUID.randomUUID().toString().replace("-", ""))
        }
        return post("POST", "$BASE/connect/transfer/from/$SELF/to/$targetId", body)
    }

    suspend fun setVolume(targetId: String, fraction: Float): Boolean {
        val v = (fraction.coerceIn(0f, 1f) * 65535f).toInt()
        return post("PUT", "$BASE/connect/volume/from/$SELF/to/$targetId", JSONObject().put("volume", v))
    }

    suspend fun pause(targetId: String) = playerCommand(targetId, command("pause"))
    suspend fun resume(targetId: String) = playerCommand(targetId, command("resume"))
    suspend fun skipNext(targetId: String) = playerCommand(targetId, command("skip_next"))
    suspend fun skipPrev(targetId: String) = playerCommand(targetId, command("skip_prev"))

    suspend fun seekTo(targetId: String, positionMs: Long) =
        playerCommand(targetId, command("seek_to") { put("value", positionMs) })

    suspend fun setShuffle(targetId: String, on: Boolean) =
        playerCommand(targetId, command("set_shuffling_context") { put("value", on) })

    /** mode: 0 off, 1 repeat context, 2 repeat track. */
    suspend fun setRepeat(targetId: String, mode: Int): Boolean {
        val a = playerCommand(targetId, command("set_repeating_context") { put("value", mode == 1) })
        val b = playerCommand(targetId, command("set_repeating_track") { put("value", mode == 2) })
        return a && b
    }

    suspend fun addToQueue(targetId: String, trackUri: String) =
        playerCommand(targetId, command("add_to_queue") {
            put("track", JSONObject().put("uri", trackUri))
        })

    /** Start a context (and optionally a specific track in it) on another device. */
    suspend fun play(targetId: String, contextUri: String, trackUri: String? = null): Boolean =
        playerCommand(targetId, command("play") {
            put("context", JSONObject().apply {
                put("uri", contextUri)
                put("url", "context://$contextUri")
                put("metadata", JSONObject())
            })
            put("play_origin", JSONObject().apply {
                put("feature_identifier", "stopify")
                put("feature_version", "web-player_2026-10-12_1791795388600_2b2ea800fe12")
            })
            put("options", JSONObject().apply {
                put("license", "tft")
                put("skip_to", JSONObject().apply { if (trackUri != null) put("track_uri", trackUri) })
                put("player_options_override", JSONObject())
            })
        })
}
