package com.shashwat.stopify.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.shashwat.stopify.data.SpotifyTrack
import com.shashwat.stopify.data.SpotifyWebClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class PlaybackState(
    val currentTrack: SpotifyTrack? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val error: String? = null
)

class SpotifyAudioPlayer private constructor(private val appContext: Context) {

    companion object {
        private const val TAG = "SpotifyAudioPlayer"
        private const val WIDEVINE_LICENSE_URL = "https://gae2-spclient.spotify.com/widevine-license/v1/audio/license"

        @Volatile
        var activeMediaSession: MediaSession? = null
            internal set

        @Volatile
        var activePlayer: ExoPlayer? = null
            internal set

        @Volatile
        private var INSTANCE: SpotifyAudioPlayer? = null

        fun getInstance(context: Context): SpotifyAudioPlayer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SpotifyAudioPlayer(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private var exoPlayer: ExoPlayer? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    @Volatile
    var onTrackCompleted: ((SpotifyTrack) -> Unit)? = null

    @Volatile
    var onSkipToNext: (() -> Unit)? = null

    @Volatile
    var onSkipToPrevious: (() -> Unit)? = null

    fun notifyLoading(track: SpotifyTrack) {
        _playbackState.value = _playbackState.value.copy(
            currentTrack = track,
            isPlaying = false,
            isBuffering = true,
            error = null
        )
    }

    fun getOrCreatePlayer(): ExoPlayer {
        exoPlayer?.let { return it }

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(SpotifyWebClient.USER_AGENT)
            .setDefaultRequestProperties(
                mapOf(
                    "Origin" to "https://open.spotify.com",
                    "Referer" to "https://open.spotify.com/"
                )
            )

        val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                15_000,
                50_000,
                500,
                1_000
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val player = ExoPlayer.Builder(appContext)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> {
                        Log.d(TAG, "ExoPlayer buffering...")
                        _playbackState.value = _playbackState.value.copy(isBuffering = true)
                    }
                    Player.STATE_READY -> {
                        val duration = player.duration.coerceAtLeast(0L)
                        Log.d(TAG, "ExoPlayer ready! Duration: ${duration}ms, isPlaying: ${player.isPlaying}")
                        _playbackState.value = _playbackState.value.copy(
                            isPlaying = player.isPlaying,
                            isBuffering = false,
                            durationMs = if (duration > 0) duration else (_playbackState.value.currentTrack?.durationMs ?: 0L)
                        )
                    }
                    Player.STATE_ENDED -> {
                        Log.d(TAG, "ExoPlayer playback ended")
                        _playbackState.value = _playbackState.value.copy(
                            isPlaying = false,
                            isBuffering = false,
                            currentPositionMs = 0L
                        )
                        _playbackState.value.currentTrack?.let { onTrackCompleted?.invoke(it) }
                    }
                    Player.STATE_IDLE -> {
                        Log.d(TAG, "ExoPlayer state idle")
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d(TAG, "ExoPlayer onIsPlayingChanged: $isPlaying")
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = isPlaying,
                    isBuffering = false
                )
            }

            override fun onPlayerError(error: PlaybackException) {
                val errorMsg = "ExoPlayer Error [${error.errorCodeName}]: ${error.message}"
                Log.e(TAG, errorMsg, error)
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    isBuffering = false,
                    error = errorMsg
                )
            }
        })

        exoPlayer = player
        activePlayer = player
        return player
    }

    fun playTrack(track: SpotifyTrack, audioUrl: String, accessToken: String, clientToken: String? = null) {
        Log.d(TAG, "Request to play track: '${track.name}' by '${track.artist}' with URL: $audioUrl")

        _playbackState.value = PlaybackState(
            currentTrack = track,
            isBuffering = true
        )

        try {
            val drmHeaders = mapOf(
                "authorization" to "Bearer $accessToken",
                "client-token" to (clientToken?.ifEmpty { null } ?: SpotifyWebClient.CLIENT_TOKEN),
                "app-platform" to SpotifyWebClient.APP_PLATFORM,
                "spotify-app-version" to SpotifyWebClient.SPOTIFY_APP_VERSION,
                "origin" to "https://open.spotify.com",
                "referer" to "https://open.spotify.com/",
                "user-agent" to SpotifyWebClient.USER_AGENT
            )

            val drmConfiguration = MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                .setLicenseUri(Uri.parse(WIDEVINE_LICENSE_URL))
                .setLicenseRequestHeaders(drmHeaders)
                .build()

            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(track.name)
                .setArtist(track.artist)
                .setAlbumTitle(track.album)
                .setArtworkUri(if (!track.coverUrl.isNullOrEmpty()) Uri.parse(track.coverUrl) else null)
                .build()

            val mediaItem = MediaItem.Builder()
                .setMediaId(track.id)
                .setUri(Uri.parse(audioUrl))
                .setMediaMetadata(mediaMetadata)
                .setDrmConfiguration(drmConfiguration)
                .build()

            val player = getOrCreatePlayer()
            player.stop()
            player.clearMediaItems()
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true

            // Fetch artwork data in background for instant lockscreen / media notification rendering
            if (!track.coverUrl.isNullOrEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val loader = ImageLoader(appContext)
                        val req = ImageRequest.Builder(appContext)
                            .data(track.coverUrl)
                            .build()
                        val result = loader.execute(req)
                        if (result is SuccessResult) {
                            val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                            if (bitmap != null) {
                                val stream = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                val data = stream.toByteArray()
                                withContext(Dispatchers.Main) {
                                    val currentItem = player.currentMediaItem
                                    if (currentItem?.mediaId == track.id) {
                                        val updatedMetadata = currentItem.mediaMetadata.buildUpon()
                                            .setArtworkData(data, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                                            .build()
                                        val updatedItem = currentItem.buildUpon()
                                            .setMediaMetadata(updatedMetadata)
                                            .build()
                                        player.replaceMediaItem(player.currentMediaItemIndex, updatedItem)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not cache artwork data bytes", e)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ExoPlayer", e)
            _playbackState.value = _playbackState.value.copy(
                isPlaying = false,
                isBuffering = false,
                error = e.localizedMessage ?: "Playback initialization failed"
            )
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            Log.d(TAG, "Pausing ExoPlayer")
            player.pause()
        } else {
            Log.d(TAG, "Resuming ExoPlayer")
            player.play()
        }
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun resume() {
        exoPlayer?.play()
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    fun seekBy(deltaMs: Long) {
        exoPlayer?.let { player ->
            val totalDur = if (player.duration > 0) player.duration else Long.MAX_VALUE
            val newPos = (player.currentPosition + deltaMs).coerceIn(0L, totalDur)
            player.seekTo(newPos)
            updateProgress()
        }
    }

    fun updateProgress() {
        exoPlayer?.let { player ->
            val pos = player.currentPosition.coerceAtLeast(0L)
            val dur = player.duration.coerceAtLeast(0L)
            _playbackState.value = _playbackState.value.copy(
                currentPositionMs = pos,
                durationMs = if (dur > 0) dur else _playbackState.value.durationMs
            )
        }
    }

    fun releasePlayer() {
        activeMediaSession = null
        activePlayer = null

        try {
            exoPlayer?.stop()
            exoPlayer?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Exception releasing ExoPlayer", e)
        }
        exoPlayer = null
    }
}
