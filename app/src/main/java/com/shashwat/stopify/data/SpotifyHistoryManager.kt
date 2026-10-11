package com.shashwat.stopify.data

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.util.Collections

class SpotifyHistoryManager {
    companion object {
        private const val TAG = "SpotifyHistoryManager"
    }

    private val _playbackHistory = Collections.synchronizedList(mutableListOf<SpotifyTrack>())
    val playbackHistory: List<SpotifyTrack>
        get() = synchronized(_playbackHistory) { _playbackHistory.toList() }

    fun loadHistoryFromPrefs(prefs: android.content.SharedPreferences) {
        // Disabled: do not load history from local preferences; fetch live from Spotify
    }

    fun saveHistoryToPrefs(prefs: android.content.SharedPreferences) {
        // Disabled: do not store history locally in SharedPreferences; sync live with Spotify
    }

    fun addLocalHistoryTrack(track: SpotifyTrack, prefs: android.content.SharedPreferences? = null) {
        synchronized(_playbackHistory) {
            _playbackHistory.removeAll { item: SpotifyTrack -> item.uri == track.uri }
            _playbackHistory.add(0, track)
            if (_playbackHistory.size > 100) {
                _playbackHistory.removeAt(_playbackHistory.lastIndex)
            }
        }
        if (prefs != null) {
            saveHistoryToPrefs(prefs)
        }
    }

    fun mergeRemoteHistory(remoteTracks: List<SpotifyTrack>, prefs: android.content.SharedPreferences? = null): List<SpotifyTrack> {
        synchronized(_playbackHistory) {
            // Keep local order for tracks that were just played locally, and append remote tracks not in local history
            for (remoteTrack in remoteTracks) {
                if (_playbackHistory.none { it.uri == remoteTrack.uri }) {
                    _playbackHistory.add(remoteTrack)
                }
            }
            if (_playbackHistory.size > 100) {
                while (_playbackHistory.size > 100) {
                    _playbackHistory.removeAt(_playbackHistory.lastIndex)
                }
            }
        }
        if (prefs != null) {
            saveHistoryToPrefs(prefs)
        }
        return playbackHistory
    }
}
