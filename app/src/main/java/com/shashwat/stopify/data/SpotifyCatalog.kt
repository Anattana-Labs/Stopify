package com.shashwat.stopify.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/*
 * Catalog endpoints (artist page, podcasts, song radio) used by the artist, show and autoplay features.
 * The request shapes (operation names, variables and response layout) follow the approach of
 * https://github.com/kota-zo/myspoti (GPL-3.0), re-implemented natively for Stopify.
 */

private const val CATALOG_TAG = "SpotifyCatalog"

private const val ARTIST_OVERVIEW_HASH = "9f8134ef565e78621f1e1793555bd6633c5ac144ae0f89604ed3ae3f80b3c8e6"
private const val ARTIST_DISCOGRAPHY_HASH = "5e07d323febb57b4a56a42abbf781490e58764aa45feb6e3dc0591564fc56599"
private const val SHOW_METADATA_HASH = "b475447846f37cc426add800b995a7859c169c53c72b62de6338c0c40a5dacea"
private const val SHOW_EPISODES_HASH = "3539d746cf882f3909660de40b4ef472b3f5893a0761d617c282541de5d412c5"

// ── Models ───────────────────────────────────────────────────────────────────

data class SpotifyArtistPage(
    val uri: String,
    val name: String,
    val avatarUrl: String?,
    val headerUrl: String?,
    val monthlyListeners: Long?,
    val verified: Boolean,
    val biography: String?,
    val topTracks: List<SpotifyTrack>,
    /** Albums, singles and compilations, newest first. */
    val releases: List<SpotifyMediaItem>,
    val appearsOn: List<SpotifyMediaItem>,
    val relatedArtists: List<SpotifyMediaItem>
)

data class SpotifyShowDetails(
    val uri: String,
    val name: String,
    val publisher: String,
    val description: String?,
    val coverUrl: String?,
    val topics: List<String>
)

data class SpotifyEpisodePage(
    val episodes: List<SpotifyTrack>,
    val total: Int
)

// ── Tiny JSON helpers ────────────────────────────────────────────────────────

internal fun JSONObject?.o(key: String): JSONObject? = this?.optJSONObject(key)
internal fun JSONObject?.a(key: String): JSONArray? = this?.optJSONArray(key)
internal fun JSONObject?.s(key: String): String? = this?.optString(key, "")?.takeIf { it.isNotEmpty() && it != "null" }
internal fun JSONObject?.path(vararg keys: String): JSONObject? {
    var cur: JSONObject? = this
    for (k in keys) cur = cur?.optJSONObject(k)
    return cur
}
internal fun JSONArray?.objs(): List<JSONObject> {
    if (this == null) return emptyList()
    val out = ArrayList<JSONObject>(length())
    for (i in 0 until length()) optJSONObject(i)?.let { out.add(it) }
    return out
}

/** The widest image of a `sources` array (Spotify does not order them consistently). */
internal fun JSONArray?.bestImage(): String? {
    var best: String? = null
    var bestW = -1
    for (src in objs()) {
        val url = src.s("url") ?: continue
        val w = src.optInt("width", src.optInt("maxWidth", 0))
        if (w > bestW || best == null) { best = url; bestW = w }
    }
    return best
}

private fun stripHtml(s: String?): String? =
    s?.replace(Regex("<[^>]*>"), " ")?.replace("&amp;", "&")?.replace("&quot;", "\"")
        ?.replace("&#39;", "'")?.replace(Regex("\\s+"), " ")?.trim()?.takeIf { it.isNotEmpty() }

private fun artistNames(artists: JSONObject?): Pair<String, String?> {
    val items = artists.a("items").objs()
    val names = items.mapNotNull { it.path("profile").s("name") ?: it.s("name") }
    return names.joinToString(", ") to items.firstOrNull()?.s("uri")
}

private fun mapTrack(t: JSONObject?, fallbackCover: String? = null): SpotifyTrack? {
    val uri = t.s("uri") ?: return null
    if (!uri.startsWith("spotify:track:")) return null
    val (artist, artistUri) = artistNames(t.o("artists").takeIf { it?.a("items") != null } ?: t.o("firstArtist"))
    val album = t.o("albumOfTrack")
    return SpotifyTrack(
        id = uri.substringAfterLast(":"),
        uri = uri,
        name = t.s("name") ?: "Track",
        artist = artist.ifEmpty { "Artist" },
        album = album.s("name") ?: "",
        coverUrl = album.path("coverArt").a("sources").bestImage() ?: fallbackCover,
        durationMs = t.path("duration")?.optLong("totalMilliseconds", 0L)?.takeIf { it > 0L }
            ?: t.path("trackDuration")?.optLong("totalMilliseconds", 0L)
            ?: 0L,
        isPlayable = t.path("playability")?.optBoolean("playable", true) ?: true,
        albumUri = album.s("uri"),
        artistUri = artistUri
    )
}

private fun mapRelease(d: JSONObject?): SpotifyMediaItem? {
    val a = d.o("releases").a("items").objs().firstOrNull() ?: d ?: return null
    val uri = a.s("uri") ?: return null
    val dt = a.o("date")
    val year = dt.s("isoString")?.take(4) ?: dt?.optInt("year", 0)?.takeIf { it > 0 }?.toString()
    val type = a.s("type")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Album"
    return SpotifyMediaItem(
        id = uri.substringAfterLast(":"),
        uri = uri,
        name = a.s("name") ?: "Album",
        subtitle = listOfNotNull(year, type).joinToString(" • "),
        coverUrl = a.o("coverArt").a("sources").bestImage(),
        type = SpotifyItemType.ALBUM
    )
}

private fun mapArtistCard(d: JSONObject?): SpotifyMediaItem? {
    val uri = d.s("uri") ?: return null
    return SpotifyMediaItem(
        id = uri.substringAfterLast(":"),
        uri = uri,
        name = d.path("profile").s("name") ?: "Artist",
        subtitle = "Artist",
        coverUrl = d.path("visuals", "avatarImage").a("sources").bestImage(),
        type = SpotifyItemType.ARTIST
    )
}

// ── HTTP ─────────────────────────────────────────────────────────────────────

private suspend fun SpotifyWebClient.pathfinder(operation: String, hash: String, variables: JSONObject): JSONObject =
    withContext(Dispatchers.IO) {
        var lastError: Exception? = null
        repeat(2) { attempt ->
            val token = getAccessToken(forceRefresh = attempt > 0).getOrElse { SpotifyWebClient.FALLBACK_BEARER_TOKEN }
            val payload = JSONObject().apply {
                put("variables", variables)
                put("operationName", operation)
                put("extensions", JSONObject().put("persistedQuery", JSONObject().put("version", 1).put("sha256Hash", hash)))
            }
            val conn = (URL("https://api-partner.spotify.com/pathfinder/v2/query").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15000
                readTimeout = 20000
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("client-token", customClientToken ?: SpotifyWebClient.CLIENT_TOKEN)
                setRequestProperty("content-type", "application/json;charset=UTF-8")
                setRequestProperty("app-platform", SpotifyWebClient.APP_PLATFORM)
                setRequestProperty("spotify-app-version", SpotifyWebClient.SPOTIFY_APP_VERSION)
                setRequestProperty("origin", "https://open.spotify.com")
                setRequestProperty("referer", "https://open.spotify.com/")
                setRequestProperty("user-agent", SpotifyWebClient.USER_AGENT)
            }
            try {
                OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
                val code = conn.responseCode
                val body = BufferedReader(InputStreamReader(if (code in 200..299) conn.inputStream else conn.errorStream)).readText()
                if (code in 200..299) {
                    val json = JSONObject(body)
                    if (json.has("errors") && json.optJSONObject("data") == null) {
                        throw Exception("$operation: ${json.optJSONArray("errors")?.optJSONObject(0)?.optString("message")}")
                    }
                    return@withContext json
                }
                if (code == 401) invalidateTokens()
                lastError = Exception("$operation HTTP $code: ${body.take(200)}")
            } catch (e: Exception) {
                lastError = e
            } finally {
                conn.disconnect()
            }
        }
        throw lastError ?: Exception("$operation failed")
    }

private suspend fun SpotifyWebClient.spGet(url: String): JSONObject? = withContext(Dispatchers.IO) {
    try {
        val token = getAccessToken().getOrElse { SpotifyWebClient.FALLBACK_BEARER_TOKEN }
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 20000
            setRequestProperty("authorization", "Bearer $token")
            setRequestProperty("client-token", customClientToken ?: SpotifyWebClient.CLIENT_TOKEN)
            setRequestProperty("app-platform", SpotifyWebClient.APP_PLATFORM)
            setRequestProperty("spotify-app-version", SpotifyWebClient.SPOTIFY_APP_VERSION)
            setRequestProperty("origin", "https://open.spotify.com")
            setRequestProperty("referer", "https://open.spotify.com/")
            setRequestProperty("user-agent", SpotifyWebClient.USER_AGENT)
        }
        val code = conn.responseCode
        val body = BufferedReader(InputStreamReader(if (code in 200..299) conn.inputStream else conn.errorStream)).readText()
        conn.disconnect()
        if (code in 200..299) JSONObject(body) else { Log.w(CATALOG_TAG, "GET $url -> $code"); null }
    } catch (e: Exception) {
        Log.w(CATALOG_TAG, "GET failed: $url", e)
        null
    }
}

// ── Artist ───────────────────────────────────────────────────────────────────

suspend fun SpotifyWebClient.fetchArtistPage(artistUri: String): Result<SpotifyArtistPage> = try {
    val json = pathfinder(
        "queryArtistOverview", ARTIST_OVERVIEW_HASH,
        JSONObject().put("uri", artistUri).put("locale", "").put("preReleaseV2", false)
    )
    val a = json.path("data", "artistUnion") ?: throw Exception("Artist not found")
    val avatar = a.path("visuals", "avatarImage").a("sources").bestImage()
    val header = a.path("headerImage", "data").a("sources").bestImage()
    val disc = a.o("discography")

    val tops = disc.path("topTracks").a("items").objs().mapNotNull { mapTrack(it.o("track"), avatar) }

    val releases = LinkedHashMap<String, SpotifyMediaItem>()
    for (group in listOf("popularReleasesAlbums", "albums", "singles", "compilations")) {
        disc.path(group).a("items").objs().mapNotNull { mapRelease(it) }.forEach { releases.putIfAbsent(it.uri, it) }
    }
    // The overview only carries a handful of releases; the full list is a separate call.
    try {
        val all = pathfinder(
            "queryArtistDiscographyAll", ARTIST_DISCOGRAPHY_HASH,
            JSONObject().put("uri", artistUri).put("offset", 0).put("limit", 100).put("order", "DATE_DESC")
        ).path("data", "artistUnion", "discography", "all")
        val full = all.a("items").objs().mapNotNull { mapRelease(it) }
        if (full.isNotEmpty()) { releases.clear(); full.forEach { releases.putIfAbsent(it.uri, it) } }
    } catch (e: Exception) {
        Log.w(CATALOG_TAG, "Full discography unavailable, using overview", e)
    }

    Result.success(
        SpotifyArtistPage(
            uri = artistUri,
            name = a.path("profile").s("name") ?: "Artist",
            avatarUrl = avatar,
            headerUrl = header,
            monthlyListeners = a.path("stats").let { st -> st?.optLong("monthlyListeners", 0L)?.takeIf { it > 0 } },
            verified = a.path("profile")?.optBoolean("verified", false) ?: false,
            biography = stripHtml(a.path("profile", "biography").s("text")),
            topTracks = tops,
            releases = releases.values.toList(),
            appearsOn = a.path("relatedContent", "appearsOn").a("items").objs().mapNotNull { mapRelease(it) },
            relatedArtists = a.path("relatedContent", "relatedArtists").a("items").objs().mapNotNull { mapArtistCard(it) }
        )
    )
} catch (e: Exception) {
    Log.w(CATALOG_TAG, "fetchArtistPage failed", e)
    Result.failure(e)
}

// ── Podcasts ─────────────────────────────────────────────────────────────────

suspend fun SpotifyWebClient.fetchShowDetails(showUri: String): Result<SpotifyShowDetails> = try {
    val json = pathfinder(
        "queryShowMetadataV2", SHOW_METADATA_HASH,
        JSONObject().put("uri", showUri).put("includeContentCapabilityTrait", true).put("includeEpisodeContentRatingsV2", true)
    )
    val s = json.path("data", "podcastUnionV2") ?: throw Exception("Show not found")
    Result.success(
        SpotifyShowDetails(
            uri = showUri,
            name = s.s("name") ?: "Podcast",
            publisher = s.path("publisher").s("name") ?: "",
            description = stripHtml(s.s("htmlDescription") ?: s.s("description")),
            coverUrl = s.path("coverArt").a("sources").bestImage(),
            topics = s.path("topics").a("items").objs().mapNotNull { it.s("title") }
        )
    )
} catch (e: Exception) {
    Result.failure(e)
}

suspend fun SpotifyWebClient.fetchShowEpisodes(showUri: String, showName: String, showCover: String?, offset: Int = 0, limit: Int = 50): Result<SpotifyEpisodePage> = try {
    val json = pathfinder(
        "queryPodcastEpisodes", SHOW_EPISODES_HASH,
        JSONObject().put("uri", showUri).put("offset", offset).put("limit", limit).put("includeEpisodeContentRatingsV2", true)
    )
    val eps = json.path("data", "podcastUnionV2", "episodesV2")
    val list = eps.a("items").objs().mapNotNull { row ->
        val d = row.path("entity", "data") ?: return@mapNotNull null
        val uri = d.s("uri") ?: return@mapNotNull null
        SpotifyTrack(
            id = uri.substringAfterLast(":"),
            uri = uri,
            name = d.s("name") ?: "Episode",
            artist = showName,
            album = showName,
            coverUrl = d.path("coverArt").a("sources").bestImage() ?: showCover,
            durationMs = d.path("duration")?.optLong("totalMilliseconds", 0L) ?: 0L,
            isPlayable = d.path("playability")?.optBoolean("playable", true) ?: true,
            albumUri = showUri
        )
    }
    Result.success(SpotifyEpisodePage(list, eps?.optInt("totalCount", list.size) ?: list.size))
} catch (e: Exception) {
    Result.failure(e)
}

// ── Song radio (Spotify's own autoplay seed) ─────────────────────────────────

/**
 * Spotify's "song radio" for a track: the web player asks the inspiredby-mix service for a
 * playlist seeded on the track, which is what its autoplay is made from.
 */
/**
 * What open.spotify.com queues after a song played on its own: the context "spotify:station:track:<id>",
 * resolved by the context-resolve service (about 50 track URIs), then decorated in one pathfinder call.
 */
suspend fun SpotifyWebClient.fetchAutoplayStation(trackUri: String): List<SpotifyTrack> {
    val trackRe = Regex("spotify:track:[A-Za-z0-9]{22}")
    val id = trackUri.substringAfterLast(":")
    val sources = listOf(
        "https://spclient.wg.spotify.com/context-resolve/v1/spotify:station:track:$id",
        "https://spclient.wg.spotify.com/radio-apollo/v3/stations/$trackUri?autoplay=true"
    )
    for (src in sources) {
        val raw = spGetRaw(src) ?: continue
        val uris = trackRe.findAll(raw).map { it.value }.distinct().filter { it != trackUri }.take(50).toList()
        if (uris.isEmpty()) continue
        val tracks = decorateContextTracks(uris)
        Log.d(CATALOG_TAG, "Autoplay station for $trackUri: ${uris.size} uris, ${tracks.size} decorated")
        if (tracks.isNotEmpty()) {
            // keep Spotify's order
            val order = uris.withIndex().associate { it.value to it.index }
            return tracks.sortedBy { order[it.uri] ?: Int.MAX_VALUE }
        }
    }
    return emptyList()
}

suspend fun SpotifyWebClient.fetchSongRadio(seedUri: String): List<SpotifyTrack> {
    val trackRe = Regex("spotify:track:[A-Za-z0-9]{22}")
    fun urisIn(text: String?): List<String> = text?.let { trackRe.findAll(it).map { m -> m.value }.distinct().toList() }.orEmpty()

    val seedRaw = spGetRaw("https://spclient.wg.spotify.com/inspiredby-mix/v2/seed_to_playlist/$seedUri?response-format=json")
        ?: return emptyList()
    // The seed answer names a generated playlist; the web player then reads its track URIs and
    // decorates them in one pathfinder call (decorateContextTracks).
    var uris = urisIn(seedRaw).filter { it != seedUri }
    if (uris.isEmpty()) {
        val playlistUri = Regex("spotify:playlist:[A-Za-z0-9]+").find(seedRaw)?.value ?: return emptyList()
        val id = playlistUri.substringAfterLast(":")
        val sources = listOf(
            "https://spclient.wg.spotify.com/context-resolve/v1/$playlistUri",
            "https://spclient.wg.spotify.com/playlist/v2/playlist/$id?format=json",
            "https://spclient.wg.spotify.com/playlist/v1/playlist/$id?format=json"
        )
        for (src in sources) {
            uris = urisIn(spGetRaw(src)).filter { it != seedUri }
            if (uris.isNotEmpty()) break
        }
        if (uris.isEmpty()) {
            return fetchPlaylistTracks(playlistUri).getOrDefault(emptyList())
        }
    }
    val decorated = decorateContextTracks(uris.take(50))
    Log.d(CATALOG_TAG, "Song radio for $seedUri: ${uris.size} uris, ${decorated.size} decorated")
    return decorated
}

private suspend fun SpotifyWebClient.spGetRaw(url: String): String? = withContext(Dispatchers.IO) {
    try {
        val token = getAccessToken().getOrElse { SpotifyWebClient.FALLBACK_BEARER_TOKEN }
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 20000
            setRequestProperty("authorization", "Bearer $token")
            setRequestProperty("client-token", customClientToken ?: SpotifyWebClient.CLIENT_TOKEN)
            setRequestProperty("app-platform", SpotifyWebClient.APP_PLATFORM)
            setRequestProperty("spotify-app-version", SpotifyWebClient.SPOTIFY_APP_VERSION)
            setRequestProperty("accept", "application/json")
            setRequestProperty("origin", "https://open.spotify.com")
            setRequestProperty("referer", "https://open.spotify.com/")
            setRequestProperty("user-agent", SpotifyWebClient.USER_AGENT)
        }
        val code = conn.responseCode
        val body = (if (code in 200..299) conn.inputStream else conn.errorStream)?.bufferedReader()?.readText().orEmpty()
        conn.disconnect()
        if (code in 200..299) body else { Log.w(CATALOG_TAG, "GET $url -> $code"); null }
    } catch (e: Exception) {
        Log.w(CATALOG_TAG, "GET failed: $url", e)
        null
    }
}
