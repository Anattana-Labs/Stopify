package com.shashwat.stopify.data

import org.json.JSONObject

/** A device that appears in the Spotify Connect device list (phone, desktop, speaker, this app...). */
data class ConnectDevice(
    val id: String,
    val name: String,
    val type: String,
    val volume: Float,
    val isActive: Boolean,
    val isSelf: Boolean,
    val canChangeVolume: Boolean = true
)

/** What the Connect cluster says is playing right now, on whichever device is active. */
data class ConnectNowPlaying(
    val trackUri: String,
    val title: String,
    val artist: String,
    val coverUrl: String?,
    val isPlaying: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    /** Wall clock (ms) at which [positionMs] was measured. */
    val timestampMs: Long,
    val shuffle: Boolean,
    val repeatContext: Boolean,
    val repeatTrack: Boolean
) {
    fun livePositionMs(now: Long = System.currentTimeMillis()): Long {
        if (!isPlaying) return positionMs
        val p = positionMs + (now - timestampMs).coerceAtLeast(0)
        return if (durationMs > 0) p.coerceAtMost(durationMs) else p
    }
}

data class ConnectClusterState(
    val devices: List<ConnectDevice> = emptyList(),
    val activeDeviceId: String? = null,
    val nowPlaying: ConnectNowPlaying? = null
) {
    val activeDevice: ConnectDevice? get() = devices.firstOrNull { it.id == activeDeviceId }
    /** Another device (not Stopify) currently owns playback. */
    val playingElsewhere: Boolean
        get() = !activeDeviceId.isNullOrEmpty() && activeDeviceId != SpotifyWebClient.DEVICE_ID

    companion object {
        /** Parses the `cluster` object of a `hm://connect-state/v1/cluster` dealer message. */
        fun parse(cluster: JSONObject, selfId: String): ConnectClusterState {
            val active = cluster.optString("active_device_id").ifEmpty { null }
            val devices = mutableListOf<ConnectDevice>()
            val dev = cluster.optJSONObject("devices")
            if (dev != null) {
                val keys = dev.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val d = dev.optJSONObject(k) ?: continue
                    val id = d.optString("device_id", k)
                    val caps = d.optJSONObject("capabilities")
                    devices.add(
                        ConnectDevice(
                            id = id,
                            name = d.optString("name", "Unknown device"),
                            type = d.optString("device_type", "unknown").lowercase(),
                            volume = (d.optInt("volume", 65535).coerceIn(0, 65535)) / 65535f,
                            isActive = id == active,
                            isSelf = id == selfId,
                            canChangeVolume = caps?.optBoolean("can_change_volume", true) ?: true
                        )
                    )
                }
            }
            devices.sortWith(compareByDescending<ConnectDevice> { it.isSelf }.thenBy { it.name })

            val ps = cluster.optJSONObject("player_state")
            val track = ps?.optJSONObject("track")
            val now = if (ps != null && track != null && track.optString("uri").isNotEmpty()) {
                val meta = track.optJSONObject("metadata")
                val opts = ps.optJSONObject("options")
                val paused = ps.optBoolean("is_paused", false)
                val playing = ps.optBoolean("is_playing", !paused) && !paused
                ConnectNowPlaying(
                    trackUri = track.optString("uri"),
                    title = meta?.optString("title").orEmpty(),
                    artist = meta?.optString("artist_name").orEmpty(),
                    coverUrl = meta?.optString("image_xlarge_url")?.ifEmpty { null }
                        ?: meta?.optString("image_large_url")?.ifEmpty { null }
                        ?: meta?.optString("image_url")?.ifEmpty { null },
                    isPlaying = playing,
                    positionMs = ps.optString("position_as_of_timestamp", "0").toLongOrNull() ?: 0L,
                    durationMs = ps.optString("duration", "0").toLongOrNull() ?: 0L,
                    timestampMs = ps.optString("timestamp", "0").toLongOrNull()?.takeIf { it > 0 }
                        ?: System.currentTimeMillis(),
                    shuffle = opts?.optBoolean("shuffling_context", false) ?: false,
                    repeatContext = opts?.optBoolean("repeating_context", false) ?: false,
                    repeatTrack = opts?.optBoolean("repeating_track", false) ?: false
                )
            } else null
            return ConnectClusterState(devices, active, now)
        }
    }
}
