package com.shashwat.stopify.data

enum class SpotifyItemType {
    TRACK, ARTIST, ALBUM, PLAYLIST, PODCAST, EPISODE
}

data class SpotifyMediaItem(
    val id: String,
    val uri: String,
    val name: String,
    val subtitle: String,
    val coverUrl: String?,
    val type: SpotifyItemType,
    val durationMs: Long = 0L,
    val isPlayable: Boolean = true
) {
    fun toTrack(): SpotifyTrack = SpotifyTrack(
        id = id,
        uri = uri,
        name = name,
        artist = subtitle,
        album = subtitle,
        coverUrl = coverUrl,
        durationMs = durationMs,
        isPlayable = isPlayable
    )
}

data class SpotifyHomeSection(
    val title: String,
    val subtitle: String? = null,
    val items: List<SpotifyMediaItem>
)

data class SpotifyHomeData(
    val greeting: String,
    val sections: List<SpotifyHomeSection>
)

data class SpotifyTrack(
    val id: String,
    val uri: String,
    val name: String,
    val artist: String,
    val album: String,
    val coverUrl: String?,
    val durationMs: Long,
    val isPlayable: Boolean = true,
    val albumUri: String? = null,
    val artistUri: String? = null
)

data class SpotifyAlbumDetails(
    val uri: String,
    val id: String,
    val name: String,
    val artist: String,
    val coverUrl: String?,
    val releaseYear: String?,
    val trackCount: Int,
    val tracks: List<SpotifyTrack>
)

data class SpotifyPlaylistDetails(
    val uri: String,
    val id: String,
    val name: String,
    val ownerName: String,
    val description: String?,
    val coverUrl: String?,
    val trackCount: Int,
    val tracks: List<SpotifyTrack>
)

data class SpotifyPlaylistSummary(
    val id: String,
    val uri: String,
    val name: String,
    val ownerName: String,
    val coverUrl: String?,
    val trackCount: Int
)

data class SpotifyLyricLine(
    val startTimeMs: Long,
    val words: String,
    val endTimeMs: Long = 0L
)

data class SpotifyColorLyrics(
    val syncType: String,
    val lines: List<SpotifyLyricLine>,
    val backgroundColor: Int,
    val textColor: Int,
    val highlightTextColor: Int,
    val providerDisplayName: String = "Spotify"
)

data class SpotifyUserProfile(
    val id: String,
    val displayName: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val product: String = "Free",
    val country: String? = null
)
