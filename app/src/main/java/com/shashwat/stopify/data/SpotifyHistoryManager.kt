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
        val jsonStr = prefs.getString("local_playback_history_v2", null) ?: return
        try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<SpotifyTrack>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    SpotifyTrack(
                        id = obj.getString("id"),
                        uri = obj.getString("uri"),
                        name = obj.getString("name"),
                        artist = obj.getString("artist"),
                        album = obj.optString("album", ""),
                        coverUrl = obj.optString("coverUrl").takeIf { it.isNotEmpty() },
                        durationMs = obj.optLong("durationMs", 0L),
                        albumUri = obj.optString("albumUri").takeIf { it.isNotEmpty() }
                    )
                )
            }
            synchronized(_playbackHistory) {
                _playbackHistory.clear()
                _playbackHistory.addAll(list)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error loading history from prefs", e)
        }
    }

    fun saveHistoryToPrefs(prefs: android.content.SharedPreferences) {
        try {
            val arr = JSONArray()
            val currentList = playbackHistory
            for (track in currentList.take(50)) {
                val obj = JSONObject().apply {
                    put("id", track.id)
                    put("uri", track.uri)
                    put("name", track.name)
                    put("artist", track.artist)
                    put("album", track.album)
                    put("coverUrl", track.coverUrl ?: "")
                    put("durationMs", track.durationMs)
                    put("albumUri", track.albumUri ?: "")
                }
                arr.put(obj)
            }
            prefs.edit().putString("local_playback_history_v2", arr.toString()).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Error saving history to prefs", e)
        }
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
