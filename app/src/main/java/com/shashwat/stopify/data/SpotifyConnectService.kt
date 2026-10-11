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
                        put("feature_identifier", "album")
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
}
