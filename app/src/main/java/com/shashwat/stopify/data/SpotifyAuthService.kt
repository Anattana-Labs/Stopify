package com.shashwat.stopify.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class SpotifyAuthService(
    private val client: SpotifyWebClient
) {
    companion object {
        private const val TAG = "SpotifyAuthService"
    }

    suspend fun getAccessToken(forceRefresh: Boolean = false): Result<String> = withContext(Dispatchers.IO) {
        val customToken = client.customAccessToken
        if (!forceRefresh && !customToken.isNullOrEmpty() && System.currentTimeMillis() < client.tokenExpiryMs) {
            return@withContext Result.success(customToken)
        }
        val cached = client.cachedAccessToken
        if (!forceRefresh && !cached.isNullOrEmpty() && System.currentTimeMillis() < client.tokenExpiryMs) {
            Log.d(TAG, "Using cached access token")
            return@withContext Result.success(cached)
        }

        // Try background token refresh hook first if available
        val hook = client.tokenRefreshHook
        if (hook != null) {
            Log.d(TAG, "Triggering background token refresh hook...")
            val success = hook()
            if (success && !client.customAccessToken.isNullOrEmpty()) {
                Log.d(TAG, "Background hook retrieved fresh access token!")
                return@withContext Result.success(client.customAccessToken!!)
            }
        }

        // 1. Fetch fresh web-player access token via TOTP & sp_dc cookie
        try {
            val tokenViaTotp = fetchTokenViaTotp()
            if (tokenViaTotp != null) {
                client.cachedAccessToken = tokenViaTotp
                client.customAccessToken = tokenViaTotp
                Log.d(TAG, "Access token fetched successfully via TOTP! Expires in: ${(client.tokenExpiryMs - System.currentTimeMillis()) / 1000}s")
                return@withContext Result.success(tokenViaTotp)
            }
        } catch (e: Exception) {
            Log.w(TAG, "TOTP token fetch error, falling back to direct endpoint", e)
        }

        try {
            val urlString = "https://open.spotify.com/get_access_token?reason=transport&productType=web_player"
            Log.d(TAG, "Requesting Access Token from: $urlString")

            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("User-Agent", SpotifyWebClient.USER_AGENT)
                setRequestProperty("Cookie", client.cookieHeaderString)
                setRequestProperty("Accept", "application/json")
                setRequestProperty("app-platform", SpotifyWebClient.APP_PLATFORM)
                setRequestProperty("spotify-app-version", SpotifyWebClient.SPOTIFY_APP_VERSION)
            }

            val responseCode = conn.responseCode
            Log.d(TAG, "GetAccessToken HTTP Status: $responseCode")

            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseBody = BufferedReader(InputStreamReader(stream)).readText()
            conn.disconnect()

            if (responseCode in 200..299) {
                val json = JSONObject(responseBody)
                val token = json.getString("accessToken")
                client.tokenExpiryMs = json.optLong("accessTokenExpirationTimestampMs", System.currentTimeMillis() + 86400000L)
                client.cachedAccessToken = token
                client.customAccessToken = token
                Log.d(TAG, "Access token fetched successfully! Expires in: ${(client.tokenExpiryMs - System.currentTimeMillis()) / 1000}s")
                Result.success(token)
            } else {
                Log.w(TAG, "Failed to get access token from endpoint: HTTP $responseCode - using fallback token")
                client.cachedAccessToken = SpotifyWebClient.FALLBACK_BEARER_TOKEN
                client.tokenExpiryMs = System.currentTimeMillis() + 86400000L
                Result.success(SpotifyWebClient.FALLBACK_BEARER_TOKEN)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching access token - using fallback token", e)
            client.cachedAccessToken = SpotifyWebClient.FALLBACK_BEARER_TOKEN
            client.tokenExpiryMs = System.currentTimeMillis() + 86400000L
            Result.success(SpotifyWebClient.FALLBACK_BEARER_TOKEN)
        }
    }

    private fun fetchTokenViaTotp(): String? {
        val spDc = extractCookieValue("sp_dc")
        if (spDc.isEmpty()) return null
        val spKey = extractCookieValue("sp_key")

        try {
            // Step 1: fetch nuance from community gist
            val gistUrl = URL("https://api.github.com/gists/22ed9c6ba463899e933427f7de1f0eef")
            val gistConn = (gistUrl.openConnection() as HttpURLConnection).apply {
                setRequestProperty("User-Agent", SpotifyWebClient.USER_AGENT)
                connectTimeout = 8000
                readTimeout = 8000
            }
            if (gistConn.responseCode !in 200..299) {
                gistConn.disconnect()
                return null
            }
            val gistText = BufferedReader(InputStreamReader(gistConn.inputStream)).readText()
            gistConn.disconnect()

            val gistObj = JSONObject(gistText)
            val files = gistObj.optJSONObject("files") ?: return null
            val fileKey = files.keys().asSequence().firstOrNull() ?: return null
            val content = files.getJSONObject(fileKey).getString("content")
            val nuancesArr = JSONArray(content)

            var bestSecret = ""
            var bestVer = 0
            for (i in 0 until nuancesArr.length()) {
                val nObj = nuancesArr.getJSONObject(i)
                val v = nObj.optInt("v", 0)
                if (v >= bestVer) {
                    bestVer = v
                    bestSecret = nObj.getString("s")
                }
            }
            if (bestSecret.isEmpty()) return null

            // Step 2: Server time
            val timeUrl = URL("https://open.spotify.com/api/server-time")
            val timeConn = (timeUrl.openConnection() as HttpURLConnection).apply {
                setRequestProperty("User-Agent", SpotifyWebClient.USER_AGENT)
                connectTimeout = 6000
                readTimeout = 6000
            }
            val serverTimeSec = if (timeConn.responseCode in 200..299) {
                val tBody = BufferedReader(InputStreamReader(timeConn.inputStream)).readText()
                timeConn.disconnect()
                JSONObject(tBody).getLong("serverTime")
            } else {
                timeConn.disconnect()
                System.currentTimeMillis() / 1000L
            }

            // Step 3: Compute TOTP
            val totp = generateTotpString(bestSecret, serverTimeSec)

            // Step 4: Call Spotify internal token endpoint
            val tokenUrlStr = "https://open.spotify.com/api/token?reason=transport&productType=web-player&totp=$totp&totpServer=$totp&totpVer=$bestVer"
            val tokenConn = (URL(tokenUrlStr).openConnection() as HttpURLConnection).apply {
                setRequestProperty("User-Agent", SpotifyWebClient.USER_AGENT)
                val cHeader = if (spKey.isNotEmpty()) "sp_dc=$spDc; sp_key=$spKey" else "sp_dc=$spDc"
                setRequestProperty("Cookie", cHeader)
                connectTimeout = 8000
                readTimeout = 8000
            }
            if (tokenConn.responseCode in 200..299) {
                val tBody = BufferedReader(InputStreamReader(tokenConn.inputStream)).readText()
                tokenConn.disconnect()
                val tObj = JSONObject(tBody)
                val token = tObj.optString("accessToken")
                if (token.isNotEmpty() && !tObj.optBoolean("isAnonymous", false)) {
                    client.tokenExpiryMs = tObj.optLong("accessTokenExpirationTimestampMs", System.currentTimeMillis() + 3600000L)
                    return token
                }
            } else {
                tokenConn.disconnect()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error generating TOTP token", e)
        }
        return null
    }

    private fun extractCookieValue(name: String): String {
        for (part in client.cookieHeaderString.split(";")) {
            val p = part.trim()
            if (p.startsWith("$name=")) {
                return p.substringAfter("=").trim()
            }
        }
        return if (name == "sp_dc") SpotifyWebClient.SP_DC else if (name == "sp_key") SpotifyWebClient.SP_KEY else ""
    }

    private fun generateTotpString(secret: String, serverTimeSec: Long): String {
        val key = base32Decode(secret)
        val timeStep = serverTimeSec / 30L
        val timeBytes = ByteArray(8)
        var value = timeStep
        for (i in 7 downTo 0) {
            timeBytes[i] = (value and 0xFF).toByte()
            value = value shr 8
        }
        val mac = javax.crypto.Mac.getInstance("HmacSHA1")
        mac.init(javax.crypto.spec.SecretKeySpec(key, "HmacSHA1"))
        val hash = mac.doFinal(timeBytes)
        val offset = hash[hash.size - 1].toInt() and 0x0F
        val code = ((hash[offset].toInt() and 0x7F) shl 24) or
                ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                (hash[offset + 3].toInt() and 0xFF)
        val otp = code % 1000000
        return String.format(java.util.Locale.US, "%06d", otp)
    }

    private fun base32Decode(input: String): ByteArray {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val cleaned = input.uppercase().replace("=", "")
        val output = mutableListOf<Byte>()
        var buffer = 0
        var bitsLeft = 0
        for (c in cleaned) {
            val value = alphabet.indexOf(c)
            if (value < 0) continue
            buffer = (buffer shl 5) or value
            bitsLeft += 5
            if (bitsLeft >= 8) {
                bitsLeft -= 8
                output.add(((buffer shr bitsLeft) and 0xFF).toByte())
            }
        }
        return output.toByteArray()
    }
}
