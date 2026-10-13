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

class SpotifyConnectService(
    private val client: SpotifyWebClient
) {
    companion object {
        private const val TAG = "SpotifyConnectService"
    }

    suspend fun sendConnectPlayerCommand(
        endpoint: String,
        contextUri: String? = null,
        targetDeviceId: String = SpotifyWebClient.DEVICE_ID,
        sourceDeviceId: String = SpotifyWebClient.DEVICE_ID
    ): Result<String> = withContext(Dispatchers.IO) {
        val token = client.getAccessToken().getOrElse {
            client.customAccessToken ?: SpotifyWebClient.FALLBACK_BEARER_TOKEN
        }
        val clientToken = client.customClientToken ?: SpotifyWebClient.CLIENT_TOKEN

        try {
            client.ensureDeviceRegistered(token, sourceDeviceId)

            val cmdUrl = URL("https://gae2-spclient.spotify.com/connect-state/v1/player/command/from/$sourceDeviceId/to/$targetDeviceId")
            val cmdId = UUID.randomUUID().toString().replace("-", "")

            val payload = JSONObject().apply {
                put("command", JSONObject().apply {
                    if (!contextUri.isNullOrEmpty()) {
                        put("context", JSONObject().apply {
                            put("uri", contextUri)
                            put("url", "context://$contextUri")
                            put("metadata", JSONObject())
                        })
                    }
                    put("play_origin", JSONObject().apply {
                        put("feature_identifier", "search")
                        put("feature_version", "web-player_2026-10-12_1791795388600_2b2ea800fe12")
                        put("referrer_identifier", "search")
                    })
                    put("options", JSONObject().apply {
                        put("license", "tft")
                        put("skip_to", JSONObject())
                        put("player_options_override", JSONObject())
                    })
                    put("logging_params", JSONObject().apply {
                        put("page_instance_ids", JSONArray().apply { put("b5bed12a-02cd-4529-8659-da9f174cdeca") })
                        put("interaction_ids", JSONArray().apply { put("ee768f0f-6e4c-49ed-8f17-338dad1a83f2") })
                        put("command_id", cmdId)
                    })
                    put("endpoint", endpoint)
                })
            }

            val conn = (cmdUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("client-token", clientToken)
                setRequestProperty("content-type", "application/json;charset=UTF-8")
                setRequestProperty("app-platform", SpotifyWebClient.APP_PLATFORM)
                setRequestProperty("spotify-app-version", SpotifyWebClient.SPOTIFY_APP_VERSION)
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("referer", "https://open.spotify.com/")
                setRequestProperty("user-agent", SpotifyWebClient.USER_AGENT)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            val code = conn.responseCode
            val respBody = try {
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                stream?.bufferedReader()?.readText() ?: ""
            } catch (e: Exception) { "" }
            conn.disconnect()

            Log.d(TAG, "Sent Connect player command '$endpoint' to $targetDeviceId: status $code, resp: $respBody")

            // Send Melody JSSDK telemetry for the connect command
            reportConnectTelemetry(token, clientToken, targetDeviceId, endpoint, cmdId, code)

            Result.success(respBody)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to send Connect player command '$endpoint'", e)
            Result.failure(e)
        }
    }

    private suspend fun reportConnectTelemetry(
        token: String,
        clientToken: String,
        targetDeviceId: String,
        commandType: String,
        commandId: String,
        statusCode: Int
    ) = withContext(Dispatchers.IO) {
        try {
            val melodyCmdPayload = JSONObject().apply {
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("type", "jssdk_connect_command")
                        put("message", JSONObject().apply {
                            put("ms_ack_duration", 500)
                            put("ms_request_latency", 200)
                            put("command_id", commandId)
                            put("command_type", commandType)
                            put("target_device_brand", "spotify")
                            put("target_device_model", "web_player")
                            put("target_device_client_id", "d8a5ed958d274c2e8ee717e6a4b0971d")
                            put("target_device_id", targetDeviceId)
                            put("interaction_ids", "")
                            put("play_origin", "")
                            put("result", if (statusCode in 200..299) "success" else "error")
                            put("http_response", "")
                            put("http_status_code", statusCode)
                        })
                    })
                })
                put("sdk_id", "harmony:4.80.0-3cdf02c91")
                put("platform", "web_player osx 10.15.7;chrome 155.0.0.0;desktop")
                put("client_version", "0.0.0")
            }

            val mUrl = URL("https://gae2-spclient.spotify.com/melody/v1/msg/batch")
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
                setRequestProperty("user-agent", SpotifyWebClient.USER_AGENT)
            }
            OutputStreamWriter(mConn.outputStream).use { it.write(melodyCmdPayload.toString()) }
            val mc = mConn.responseCode
            mConn.disconnect()
            Log.d(TAG, "Sent Melody telemetry for $commandType: HTTP $mc")
        } catch (e: Exception) {
            Log.w(TAG, "Melody telemetry failed", e)
        }
    }

    suspend fun updateConnectClusterState(
        track: SpotifyTrack?,
        isPlaying: Boolean,
        positionMs: Long,
        deviceId: String = SpotifyWebClient.DEVICE_ID
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val token = client.getAccessToken().getOrNull() ?: client.customAccessToken ?: return@withContext Result.failure(Exception("No token"))
        val clientToken = client.customClientToken ?: SpotifyWebClient.CLIENT_TOKEN
        val connId = client.dealerConnectionId ?: ""

        try {
            val url = URL("https://gae2-spclient.spotify.com/connect-state/v1/devices/hobs_$deviceId")
            val payload = JSONObject().apply {
                put("member_type", "CONNECT_STATE")
                put("device", JSONObject().apply {
                    put("device_info", JSONObject().apply {
                        put("can_play", true)
                        put("volume", 65535)
                        put("name", "Stopify Player")
                        put("device_id", deviceId)
                        put("device_type", "COMPUTER")
                        put("device_software_version", "1.3.5")
                        put("spirc_version", "3.2.6")
                        put("capabilities", JSONObject().apply {
                            put("can_be_player", true)
                            put("gaia_eq_connect_id", true)
                            put("supports_command_request", true)
                            put("is_observable", true)
                            put("volume_steps", 64)
                            put("supported_types", JSONArray().apply {
                                put("audio/episode")
                                put("audio/track")
                            })
                        })
                    })
                    if (track != null) {
                        put("player_state", JSONObject().apply {
                            put("timestamp", System.currentTimeMillis())
                            put("context_uri", track.albumUri ?: track.uri)
                            put("track", JSONObject().apply {
                                put("uri", track.uri)
                                put("uid", "")
                                put("name", track.name)
                                put("album", JSONObject().apply {
                                    put("name", track.album)
                                    put("uri", track.albumUri ?: "")
                                })
                                put("artists", JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("name", track.artist)
                                        put("uri", track.artistUri ?: "")
                                    })
                                })
                                put("metadata", JSONObject().apply {
                                    put("title", track.name)
                                    put("artist_name", track.artist)
                                    put("album_title", track.album)
                                    if (!track.coverUrl.isNullOrEmpty()) {
                                        put("image_url", track.coverUrl)
                                    }
                                })
                            })
                            put("playback_speed", if (isPlaying) 1.0 else 0.0)
                            put("position_as_of_timestamp", positionMs)
                            put("duration", track.durationMs)
                            put("is_playing", isPlaying)
                            put("is_paused", !isPlaying)
                            put("play_origin", JSONObject().apply {
                                put("feature_identifier", "stopify")
                            })
                        })
                    }
                })
            }

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("client-token", clientToken)
                setRequestProperty("content-type", "application/json;charset=UTF-8")
                if (connId.isNotEmpty()) {
                    setRequestProperty("x-spotify-connection-id", connId)
                }
                setRequestProperty("app-platform", SpotifyWebClient.APP_PLATFORM)
                setRequestProperty("spotify-app-version", SpotifyWebClient.SPOTIFY_APP_VERSION)
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("referer", "https://open.spotify.com/")
                setRequestProperty("user-agent", SpotifyWebClient.USER_AGENT)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            val code = conn.responseCode
            conn.disconnect()
            Log.d(TAG, "Sent connect cluster state update (playing=$isPlaying, pos=${positionMs}ms): HTTP $code")
            Result.success(code in 200..299)
        } catch (e: Exception) {
            Log.w(TAG, "Failed updating connect cluster state", e)
            Result.failure(e)
        }
    }
}
