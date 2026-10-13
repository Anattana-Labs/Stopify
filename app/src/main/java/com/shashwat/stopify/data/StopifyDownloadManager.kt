package com.shashwat.stopify.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class StopifyDownloadManager private constructor(private val appContext: Context) {

    companion object {
        private const val TAG = "StopifyDownloadMgr"
        private const val DOWNLOADS_DIR_NAME = "downloads"

        @Volatile
        private var INSTANCE: StopifyDownloadManager? = null

        fun getInstance(context: Context): StopifyDownloadManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StopifyDownloadManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val downloadsDir = File(appContext.filesDir, DOWNLOADS_DIR_NAME).apply {
        if (!exists()) mkdirs()
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val _downloadedTrackIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadedTrackIds: StateFlow<Set<String>> = _downloadedTrackIds.asStateFlow()

    private val _downloadedTracks = MutableStateFlow<List<SpotifyTrack>>(emptyList())
    val downloadedTracks: StateFlow<List<SpotifyTrack>> = _downloadedTracks.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()

    private val managerScope = CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    private val _downloadStatusMessage = MutableStateFlow<String?>(null)
    val downloadStatusMessage: StateFlow<String?> = _downloadStatusMessage.asStateFlow()

    private val activeDownloads = ConcurrentHashMap<String, Boolean>()

    @Volatile
    var client: SpotifyWebClient? = null

    init {
        loadDownloadedTracks()
    }

    fun toggleDownload(track: SpotifyTrack, clientOverride: SpotifyWebClient? = null) {
        if (isDownloaded(track.id)) {
            deleteDownload(track.id)
            _downloadStatusMessage.value = "Removed download: '${track.name}'"
        } else {
            val activeClient = clientOverride ?: client
            if (activeClient != null) {
                _downloadStatusMessage.value = "Downloading '${track.name}'..."
                managerScope.launch {
                    val res = downloadTrack(track, activeClient)
                    if (res.isSuccess) {
                        _downloadStatusMessage.value = "Downloaded '${track.name}'"
                    } else {
                        val err = res.exceptionOrNull()?.message ?: "Unknown error"
                        _downloadStatusMessage.value = "Download failed: $err"
                        Log.e(TAG, "Download failed for ${track.name}: $err")
                    }
                }
            } else {
                Log.w(TAG, "Cannot start download: client is null")
                _downloadStatusMessage.value = "Download failed: Spotify client not ready"
            }
        }
    }

    fun isDownloaded(trackId: String): Boolean {
        return _downloadedTrackIds.value.contains(trackId)
    }

    fun isDownloading(trackId: String): Boolean {
        return activeDownloads[trackId] == true
    }

    fun getDownloadedAudioFile(trackId: String): File? {
        val file = File(File(downloadsDir, trackId), "audio.mp4")
        return if (file.exists() && file.length() > 0) file else null
    }

    fun getDownloadedTrack(trackId: String): SpotifyTrack? {
        return _downloadedTracks.value.firstOrNull { it.id == trackId }
    }

    fun loadDownloadedTracks() {
        try {
            val trackDirs = downloadsDir.listFiles { f -> f.isDirectory } ?: emptyArray()
            val list = mutableListOf<SpotifyTrack>()
            val ids = mutableSetOf<String>()

            for (dir in trackDirs) {
                val audioFile = File(dir, "audio.mp4")
                val metaFile = File(dir, "metadata.json")
                if (audioFile.exists() && audioFile.length() > 0 && metaFile.exists()) {
                    try {
                        val json = JSONObject(metaFile.readText())
                        val track = SpotifyTrack(
                            id = json.optString("id", dir.name),
                            uri = json.optString("uri", "spotify:track:${dir.name}"),
                            name = json.optString("name", "Unknown Track"),
                            artist = json.optString("artist", "Unknown Artist"),
                            album = json.optString("album", ""),
                            coverUrl = json.optString("coverUrl").ifEmpty { null },
                            durationMs = json.optLong("durationMs", 0L)
                        )
                        list.add(track)
                        ids.add(track.id)
                    } catch (e: Exception) {
                        Log.w(TAG, "Error parsing metadata for downloaded track ${dir.name}", e)
                    }
                }
            }

            _downloadedTracks.value = list
            _downloadedTrackIds.value = ids
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load downloaded tracks from disk", e)
        }
    }

    suspend fun downloadTrack(
        track: SpotifyTrack,
        client: SpotifyWebClient,
        onProgress: ((Float) -> Unit)? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (activeDownloads[track.id] == true) {
            return@withContext Result.failure(Exception("Download already in progress"))
        }

        activeDownloads[track.id] = true
        updateProgress(track.id, 0.05f)
        onProgress?.invoke(0.05f)

        val trackDir = File(downloadsDir, track.id).apply { if (!exists()) mkdirs() }
        val tempAudioFile = File(trackDir, "audio.mp4.tmp")
        val finalAudioFile = File(trackDir, "audio.mp4")
        val metaFile = File(trackDir, "metadata.json")

        try {
            Log.d(TAG, "Starting download for track '${track.name}' (${track.id})")
            val streamRes = client.resolveTrackAudioStream(track.uri)
            if (streamRes.isFailure) {
                val err = streamRes.exceptionOrNull() ?: Exception("Failed to resolve audio stream")
                Log.e(TAG, "Failed stream resolve for ${track.id}", err)
                cleanup(track.id, tempAudioFile)
                return@withContext Result.failure(err)
            }

            val cdnUrl = streamRes.getOrThrow()
            updateProgress(track.id, 0.15f)
            onProgress?.invoke(0.15f)

            val req = Request.Builder()
                .url(cdnUrl)
                .header("User-Agent", SpotifyWebClient.USER_AGENT)
                .header("Origin", "https://open.spotify.com")
                .header("Referer", "https://open.spotify.com/")
                .header("Accept", "*/*")
                .header("Accept-Encoding", "identity")
                .build()

            val response = okHttpClient.newCall(req).execute()
            Log.d(TAG, "CDN HTTP response for '${track.name}': ${response.code}")
            if (!response.isSuccessful) {
                val code = response.code
                response.close()
                cleanup(track.id, tempAudioFile)
                Log.e(TAG, "CDN error HTTP $code downloading ${track.name}")
                return@withContext Result.failure(Exception("HTTP $code from Spotify CDN"))
            }

            val body = response.body ?: run {
                cleanup(track.id, tempAudioFile)
                return@withContext Result.failure(Exception("Empty body downloading track"))
            }

            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            body.byteStream().use { input ->
                FileOutputStream(tempAudioFile).use { output ->
                    val buffer = ByteArray(32 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloadedBytes += read
                        if (totalBytes > 0) {
                            val prog = 0.15f + ((downloadedBytes.toFloat() / totalBytes) * 0.80f)
                            updateProgress(track.id, prog)
                            onProgress?.invoke(prog)
                        }
                    }
                    output.flush()
                }
            }

            if (tempAudioFile.length() == 0L) {
                cleanup(track.id, tempAudioFile)
                return@withContext Result.failure(Exception("Downloaded file has 0 bytes"))
            }

            if (finalAudioFile.exists()) finalAudioFile.delete()
            if (!tempAudioFile.renameTo(finalAudioFile)) {
                tempAudioFile.copyTo(finalAudioFile, overwrite = true)
                tempAudioFile.delete()
            }

            // Save metadata
            val metaJson = JSONObject().apply {
                put("id", track.id)
                put("uri", track.uri)
                put("name", track.name)
                put("artist", track.artist)
                put("album", track.album)
                put("coverUrl", track.coverUrl ?: "")
                put("durationMs", track.durationMs)
                put("downloadedAt", System.currentTimeMillis())
            }
            metaFile.writeText(metaJson.toString())

            // Optionally cache cover image locally
            if (!track.coverUrl.isNullOrEmpty()) {
                try {
                    val coverReq = Request.Builder().url(track.coverUrl).build()
                    val coverResp = okHttpClient.newCall(coverReq).execute()
                    if (coverResp.isSuccessful) {
                        val coverFile = File(trackDir, "cover.jpg")
                        coverResp.body?.byteStream()?.use { cin ->
                            FileOutputStream(coverFile).use { cout -> cin.copyTo(cout) }
                        }
                    }
                    coverResp.close()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed caching cover image locally", e)
                }
            }

            updateProgress(track.id, 1.0f)
            onProgress?.invoke(1.0f)
            loadDownloadedTracks()
            Log.d(TAG, "Track '${track.name}' downloaded successfully!")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading track ${track.name}", e)
            cleanup(track.id, tempAudioFile)
            Result.failure(e)
        } finally {
            activeDownloads.remove(track.id)
            val currentMap = _downloadProgress.value.toMutableMap()
            currentMap.remove(track.id)
            _downloadProgress.value = currentMap
        }
    }

    private fun updateProgress(trackId: String, progress: Float) {
        val currentMap = _downloadProgress.value.toMutableMap()
        currentMap[trackId] = progress.coerceIn(0f, 1f)
        _downloadProgress.value = currentMap
    }

    private fun cleanup(trackId: String, tempFile: File) {
        if (tempFile.exists()) tempFile.delete()
        activeDownloads.remove(trackId)
        val currentMap = _downloadProgress.value.toMutableMap()
        currentMap.remove(trackId)
        _downloadProgress.value = currentMap
    }

    fun deleteDownload(trackId: String): Boolean {
        try {
            val trackDir = File(downloadsDir, trackId)
            if (trackDir.exists()) {
                trackDir.deleteRecursively()
            }
            loadDownloadedTracks()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting downloaded track $trackId", e)
            return false
        }
    }
}
