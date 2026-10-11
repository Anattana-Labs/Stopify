package com.shashwat.stopify.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

class SpotifyLyricsService(
    private val client: SpotifyWebClient
) {
    companion object {
        private const val TAG = "SpotifyLyricsService"
    }

    private val lyricsCache = ConcurrentHashMap<String, SpotifyColorLyrics>()

    fun getCachedColorLyrics(trackId: String): SpotifyColorLyrics? {
        val clean = trackId.substringAfterLast(":")
        return lyricsCache[clean]
    }

    suspend fun fetchColorLyrics(trackId: String, coverUrl: String? = null): Result<SpotifyColorLyrics> = withContext(Dispatchers.IO) {
        val cleanId = trackId.substringAfterLast(":")
        lyricsCache[cleanId]?.let { return@withContext Result.success(it) }

        val token = client.getAccessToken().getOrElse { client.customAccessToken ?: SpotifyWebClient.FALLBACK_BEARER_TOKEN }
        val clientToken = client.customClientToken ?: SpotifyWebClient.CLIENT_TOKEN

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
                setRequestProperty("app-platform", SpotifyWebClient.APP_PLATFORM)
                setRequestProperty("spotify-app-version", SpotifyWebClient.SPOTIFY_APP_VERSION)
                setRequestProperty("referer", "https://open.spotify.com/")
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("user-agent", SpotifyWebClient.USER_AGENT)
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
                val colorLyrics = SpotifyColorLyrics(
                    syncType = syncType,
                    lines = linesList,
                    backgroundColor = bgColor,
                    textColor = txtColor,
                    highlightTextColor = highlightColor,
                    providerDisplayName = provider
                )
                lyricsCache[cleanId] = colorLyrics
                Result.success(colorLyrics)
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
