package com.shashwat.stopify.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Fetches Spotify Canvas — the short looping videos that play behind a track.
 * Uses Spotify's internal `canvaz-cache` service, which speaks Protobuf.
 * Hand-encoded request and hand-decoded response (zero protobuf runtime dependency):
 *
 *   EntityCanvazRequest { repeated Entity { string entity_uri = 1 } = 1 }
 *   EntityCanvazResponse { repeated Canvaz { string url = 2; string entity_uri = 5 } = 1 }
 *
 * Auth reuses the web-player access token (same Bearer token as color-lyrics).
 */
object SpotifyCanvas {
    private const val TAG = "SpotifyCanvas"
    private const val ENDPOINT = "https://spclient.wg.spotify.com/canvaz-cache/v0/canvases"

    private val cache = ConcurrentHashMap<String, String>()

    suspend fun fetchCanvasUrl(
        trackId: String,
        accessToken: String,
        clientToken: String? = null
    ): String? = withContext(Dispatchers.IO) {
        val cleanId = trackId.substringAfterLast(":")
        if (cleanId.isBlank() || accessToken.isBlank()) return@withContext null

        cache[cleanId]?.let { cached ->
            return@withContext if (cached.isEmpty()) null else cached
        }

        val uri = "spotify:track:$cleanId"
        val body = encodeRequest(uri)
        try {
            val url = URL(ENDPOINT)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                setRequestProperty("Authorization", "Bearer $accessToken")
                setRequestProperty("Accept", "application/protobuf")
                setRequestProperty("Content-Type", "application/protobuf")
                setRequestProperty("User-Agent", SpotifyWebClient.USER_AGENT)
                setRequestProperty("app-platform", SpotifyWebClient.APP_PLATFORM)
                if (!clientToken.isNullOrBlank()) {
                    setRequestProperty("client-token", clientToken)
                }
            }
            conn.outputStream.use { it.write(body) }

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                Log.d(TAG, "canvas($cleanId) HTTP $responseCode")
                conn.disconnect()
                cache[cleanId] = ""
                return@withContext null
            }

            val respBytes = conn.inputStream.use { it.readBytes() }
            conn.disconnect()
            val canvasUrl = decodeFirstUrl(respBytes)
            Log.d(TAG, "canvas($cleanId) -> ${canvasUrl ?: "none"}")
            cache[cleanId] = canvasUrl ?: ""
            canvasUrl
        } catch (e: Exception) {
            Log.w(TAG, "canvas($cleanId) failed: ${e.message}")
            null
        }
    }

    // ── Minimal protobuf wire encode/decode ─────────────────────────────────

    /** EntityCanvazRequest with a single Entity{entity_uri}. */
    private fun encodeRequest(uri: String): ByteArray {
        val uriBytes = uri.toByteArray(Charsets.UTF_8)
        // Entity: field 1 (entity_uri), wire type 2 (length-delimited).
        val entity = ByteArrayBuilder().apply {
            writeTag(1, 2); writeVarint(uriBytes.size.toLong()); write(uriBytes)
        }.toByteArray()
        // EntityCanvazRequest: field 1 (entities), wire type 2.
        return ByteArrayBuilder().apply {
            writeTag(1, 2); writeVarint(entity.size.toLong()); write(entity)
        }.toByteArray()
    }

    /** Walk EntityCanvazResponse → first Canvaz → its url (field 2). */
    private fun decodeFirstUrl(data: ByteArray): String? {
        val r = Reader(data)
        while (r.hasMore()) {
            val (field, wire) = r.readTag()
            if (field == 1 && wire == 2) {
                // canvases[i] — a nested Canvaz message.
                val canvaz = r.readBytes()
                val url = readCanvazUrl(canvaz)
                if (url != null) return url
            } else {
                r.skip(wire)
            }
        }
        return null
    }

    private fun readCanvazUrl(canvaz: ByteArray): String? {
        val r = Reader(canvaz)
        while (r.hasMore()) {
            val (field, wire) = r.readTag()
            if (field == 2 && wire == 2) {
                return String(r.readBytes(), Charsets.UTF_8)
            }
            r.skip(wire)
        }
        return null
    }

    private class ByteArrayBuilder {
        private val out = ByteArrayOutputStream()
        fun write(b: ByteArray) = out.write(b)
        fun writeByte(b: Int) = out.write(b)
        fun writeTag(field: Int, wire: Int) = writeVarint(((field shl 3) or wire).toLong())
        fun writeVarint(value: Long) {
            var v = value
            while (true) {
                val bits = (v and 0x7F).toInt()
                v = v ushr 7
                if (v != 0L) writeByte(bits or 0x80) else { writeByte(bits); break }
            }
        }
        fun toByteArray(): ByteArray = out.toByteArray()
    }

    private class Reader(private val data: ByteArray) {
        private var pos = 0
        fun hasMore() = pos < data.size
        fun readTag(): Pair<Int, Int> {
            val v = readVarint().toInt()
            return (v ushr 3) to (v and 0x7)
        }
        fun readVarint(): Long {
            var result = 0L; var shift = 0
            while (pos < data.size) {
                val b = data[pos++].toInt() and 0xFF
                result = result or ((b and 0x7F).toLong() shl shift)
                if (b and 0x80 == 0) return result
                shift += 7
                if (shift >= 64) break
            }
            return result
        }
        fun readBytes(): ByteArray {
            val len = readVarint().toInt()
            if (len <= 0) return ByteArray(0)
            val end = (pos + len).coerceAtMost(data.size)
            val out = if (pos < data.size) data.copyOfRange(pos, end) else ByteArray(0)
            pos = end
            return out
        }
        fun skip(wire: Int) {
            when (wire) {
                0 -> readVarint()
                1 -> pos = (pos + 8).coerceAtMost(data.size)
                2 -> { val len = readVarint().toInt().coerceAtLeast(0); pos = (pos + len).coerceAtMost(data.size) }
                5 -> pos = (pos + 4).coerceAtMost(data.size)
                else -> pos = data.size
            }
        }
    }
}
