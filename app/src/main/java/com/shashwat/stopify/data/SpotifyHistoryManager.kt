package com.shashwat.stopify.data

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.util.Collections

class SpotifyHistoryManager {
    companion object {
        private const val TAG = "SpotifyHistoryManager"
        private const val KEY = "stopify_history_v3"
        private const val MAX = 100

        /** Puts [track] at the front, removing any older entry of the same track. */
        fun withPlay(current: List<SpotifyTrack>, track: SpotifyTrack): List<SpotifyTrack> =
            (listOf(track.copy(playedAt = System.currentTimeMillis())) + current.filter { it.uri != track.uri }).take(MAX)

        /**
         * Local plays stay in front of Spotify's recents (they are newer than the last server sync);
         * Spotify's list fills in plays made on other devices. Duplicates are collapsed by uri.
         */
        fun mergeWithRemote(local: List<SpotifyTrack>, remote: List<SpotifyTrack>): List<SpotifyTrack> {
            val remoteUris = remote.map { it.uri }.toSet()
            // Local entries that the server already knows about take the server's order.
            val localOnly = local.takeWhile { it.uri !in remoteUris }
            return (localOnly + remote + local).distinctBy { it.uri }.take(MAX)
        }

        fun saveLocal(prefs: android.content.SharedPreferences, list: List<SpotifyTrack>) {
            try {
                val arr = JSONArray()
                list.take(MAX).forEach { t ->
                    arr.put(JSONObject().apply {
                        put("id", t.id); put("uri", t.uri); put("name", t.name)
                        put("artist", t.artist); put("album", t.album)
                        put("cover", t.coverUrl ?: ""); put("dur", t.durationMs)
                        put("albumUri", t.albumUri ?: ""); put("artistUri", t.artistUri ?: "")
                        put("playedAt", t.playedAt)
                    })
                }
                prefs.edit().putString(KEY, arr.toString()).apply()
            } catch (e: Exception) {
                Log.w(TAG, "Could not save history", e)
            }
        }

        fun loadLocal(prefs: android.content.SharedPreferences): List<SpotifyTrack> {
            return try {
                val arr = JSONArray(prefs.getString(KEY, "[]") ?: "[]")
                (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    SpotifyTrack(
                        id = o.optString("id"), uri = o.optString("uri"), name = o.optString("name"),
                        artist = o.optString("artist"), album = o.optString("album"),
                        coverUrl = o.optString("cover").ifEmpty { null },
                        durationMs = o.optLong("dur"),
                        albumUri = o.optString("albumUri").ifEmpty { null },
                        artistUri = o.optString("artistUri").ifEmpty { null },
                        playedAt = o.optLong("playedAt", 0L)
                    )
                }.filter { it.uri.isNotEmpty() }
            } catch (e: Exception) {
                emptyList()
            }
        }
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
            _playbackHistory.add(0, track.copy(playedAt = System.currentTimeMillis()))
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
