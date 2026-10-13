package com.shashwat.stopify.ui.player

import android.graphics.Color as AndroidColor
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

/**
 * Plays a Spotify Canvas clip: a short, muted, looping video filling the
 * now-playing background. Uses a dedicated ExoPlayer released when the
 * composable leaves.
 */
@OptIn(UnstableApi::class)
@Composable
fun CanvasVideo(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exo = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            playWhenReady = true
            prepare()
        }
    }
    DisposableEffect(url) {
        onDispose { exo.release() }
    }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exo
                useController = false
                controllerAutoShow = false
                setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                setArtworkDisplayMode(PlayerView.ARTWORK_DISPLAY_MODE_OFF)
                setUseArtwork(false)
                setDefaultArtwork(null)
                hideController()
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setBackgroundColor(AndroidColor.TRANSPARENT)
            }
        },
    )
}
