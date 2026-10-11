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
        val jsonStr = prefs.getString("playback_history_json", null) ?: return
        try {
            val array = JSONArray(jsonStr)
            synchronized(_playbackHistory) {
                _playbackHistory.clear()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    _playbackHistory.add(
                        SpotifyTrack(
                            id = obj.getString("id"),
                            uri = obj.getString("uri"),
                            name = obj.getString("name"),
                            artist = obj.getString("artist"),
                            album = obj.optString("album", ""),
                            coverUrl = obj.optString("coverUrl", null).takeIf { it != "null" },
                            durationMs = obj.optLong("durationMs", 0L),
                            isPlayable = true
                        )
                    )
                }
            }
            Log.d(TAG, "Loaded ${_playbackHistory.size} history tracks from persistent storage")
        } catch (e: Exception) {
            Log.w(TAG, "Error loading history from prefs", e)
        }
    }

    fun saveHistoryToPrefs(prefs: android.content.SharedPreferences) {
        try {
            val array = JSONArray()
            synchronized(_playbackHistory) {
                for (track in _playbackHistory.take(50)) {
                    val obj = JSONObject().apply {
                        put("id", track.id)
                        put("uri", track.uri)
                        put("name", track.name)
                        put("artist", track.artist)
                        put("album", track.album)
                        put("coverUrl", track.coverUrl)
                        put("durationMs", track.durationMs)
                    }
                    array.put(obj)
                }
            }
            prefs.edit().putString("playback_history_json", array.toString()).apply()
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
}
