package com.shashwat.stopify.player

import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return SpotifyAudioPlayer.activeMediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = SpotifyAudioPlayer.activePlayer
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        SpotifyAudioPlayer.activeMediaSession = null
        super.onDestroy()
    }
}
