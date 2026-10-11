package com.shashwat.stopify.player

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
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
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.shashwat.stopify.MainActivity
import com.shashwat.stopify.data.SpotifyTrack
import com.shashwat.stopify.data.SpotifyWebClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    private var mediaSession: MediaSession? = null

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

    fun playTrack(track: SpotifyTrack, audioUrl: String, accessToken: String) {
        Log.d(TAG, "Request to play track: '${track.name}' by '${track.artist}' with URL: $audioUrl")

        releasePlayer()
        _playbackState.value = PlaybackState(
            currentTrack = track,
            isBuffering = true
        )

        try {
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent(SpotifyWebClient.USER_AGENT)
                .setDefaultRequestProperties(
                    mapOf(
                        "Origin" to "https://open.spotify.com",
                        "Referer" to "https://open.spotify.com/"
                    )
                )

            val drmHeaders = mapOf(
                "authorization" to "Bearer $accessToken",
                "client-token" to SpotifyWebClient.CLIENT_TOKEN,
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

            val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory)

            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    15_000, // minBufferMs
                    50_000, // maxBufferMs
                    500,    // bufferForPlaybackMs
                    1_000   // bufferForPlaybackAfterRebufferMs
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
                                durationMs = if (duration > 0) duration else track.durationMs
                            )
                        }
                        Player.STATE_ENDED -> {
                            Log.d(TAG, "ExoPlayer playback ended for track: ${track.name}")
                            _playbackState.value = _playbackState.value.copy(
                                isPlaying = false,
                                isBuffering = false,
                                currentPositionMs = 0L
                            )
                            onTrackCompleted?.invoke(track)
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

            activePlayer = player

            try {
                val serviceIntent = Intent(appContext, PlaybackService::class.java)
                appContext.startService(serviceIntent)
            } catch (e: Exception) {
                Log.w(TAG, "Could not start PlaybackService", e)
            }

            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true
            exoPlayer = player

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
        try {
            mediaSession?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Exception releasing MediaSession", e)
        }
        mediaSession = null
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
