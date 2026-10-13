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
        // Do not load local history; purge legacy cache key if present
        try {
            if (prefs.contains("local_playback_history_v2")) {
                prefs.edit().remove("local_playback_history_v2").apply()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error purging legacy local history", e)
        }
    }

    fun saveHistoryToPrefs(prefs: android.content.SharedPreferences) {
        // Do not store history locally; ensure legacy key is removed
        try {
            if (prefs.contains("local_playback_history_v2")) {
                prefs.edit().remove("local_playback_history_v2").apply()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error clearing legacy history prefs", e)
        }
    }

    fun setRemoteHistory(remoteTracks: List<SpotifyTrack>) {
        synchronized(_playbackHistory) {
            _playbackHistory.clear()
            _playbackHistory.addAll(remoteTracks)
        }
    }

    fun addLocalHistoryTrack(track: SpotifyTrack, prefs: android.content.SharedPreferences? = null) {
        // In-memory session tracking only, do not persist locally to disk
        synchronized(_playbackHistory) {
            _playbackHistory.removeAll { item: SpotifyTrack -> item.uri == track.uri }
            _playbackHistory.add(0, track)
            if (_playbackHistory.size > 100) {
                _playbackHistory.removeAt(_playbackHistory.lastIndex)
            }
        }
    }

    fun mergeRemoteHistory(remoteTracks: List<SpotifyTrack>, prefs: android.content.SharedPreferences? = null): List<SpotifyTrack> {
        setRemoteHistory(remoteTracks)
        return playbackHistory
    }
}
