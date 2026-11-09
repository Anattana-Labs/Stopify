package com.shashwat.stopify

import androidx.compose.runtime.mutableStateListOf
import com.shashwat.stopify.data.SpotifyTrack

/** Process-wide queue, so it survives the activity being destroyed while music keeps playing. */
object PlaybackQueueStore {
    val queue = mutableStateListOf<SpotifyTrack>()
    val previous = mutableStateListOf<SpotifyTrack>()
}
