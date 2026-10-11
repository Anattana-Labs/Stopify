package com.shashwat.stopify.player

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.shashwat.stopify.MainActivity

class PlaybackService : MediaSessionService() {

    companion object {
        private const val TAG = "PlaybackService"
    }

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val audioPlayer = SpotifyAudioPlayer.getInstance(this)
        val basePlayer = audioPlayer.getOrCreatePlayer()

        val sessionActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val sessionActivity = PendingIntent.getActivity(
            this, 0, sessionActivityIntent, pendingIntentFlags
        )

        val forwardingPlayer = object : ForwardingPlayer(basePlayer) {
            override fun getAvailableCommands(): Player.Commands =
                super.getAvailableCommands().buildUpon()
                    .add(COMMAND_SEEK_TO_NEXT)
                    .add(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                    .add(COMMAND_SEEK_TO_PREVIOUS)
                    .add(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .add(COMMAND_SEEK_FORWARD)
                    .add(COMMAND_SEEK_BACK)
                    .build()

            override fun isCommandAvailable(command: Int): Boolean = when (command) {
                COMMAND_SEEK_TO_NEXT, COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                COMMAND_SEEK_TO_PREVIOUS, COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                COMMAND_SEEK_FORWARD, COMMAND_SEEK_BACK -> true
                else -> super.isCommandAvailable(command)
            }

            override fun hasNextMediaItem() = true
            override fun hasPreviousMediaItem() = true
            override fun seekToNext() {
                audioPlayer.onSkipToNext?.invoke()
            }
            override fun seekToNextMediaItem() {
                audioPlayer.onSkipToNext?.invoke()
            }
            override fun seekToPrevious() {
                audioPlayer.onSkipToPrevious?.invoke()
            }
            override fun seekToPreviousMediaItem() {
                audioPlayer.onSkipToPrevious?.invoke()
            }
        }

        mediaSession = MediaSession.Builder(this, forwardingPlayer)
            .setId("StopifyMediaSession")
            .setSessionActivity(sessionActivity)
            .build()

        SpotifyAudioPlayer.activeMediaSession = mediaSession
        Log.d(TAG, "PlaybackService created system MediaSession successfully")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = SpotifyAudioPlayer.getInstance(this).getOrCreatePlayer()
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        SpotifyAudioPlayer.activeMediaSession = null
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
