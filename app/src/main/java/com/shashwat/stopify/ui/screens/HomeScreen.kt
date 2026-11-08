package com.shashwat.stopify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.semantics.Role
import com.shashwat.stopify.ui.components.RoundAction
import com.shashwat.stopify.ui.components.ScreenHeader
import com.shashwat.stopify.ui.components.SectionHeader
import com.shashwat.stopify.ui.components.SpectrumLine
import com.shashwat.stopify.ui.theme.PaneShape
import com.shashwat.stopify.ui.theme.PrismTokens
import com.shashwat.stopify.ui.theme.glassEdge
import com.shashwat.stopify.ui.theme.ink3
import com.shashwat.stopify.ui.theme.pane
import com.shashwat.stopify.data.SpotifyHomeData
import com.shashwat.stopify.data.SpotifyItemType
import com.shashwat.stopify.data.SpotifyMediaItem
import com.shashwat.stopify.data.SpotifyTrack
import com.shashwat.stopify.data.SpotifyUserProfile
import com.shashwat.stopify.ui.components.SimpArtistCircleCard
import com.shashwat.stopify.ui.components.SimpSquareCard
import java.util.Calendar

/**
 * Home. Never shows a blocking loader and never clears what is on screen:
 * shelves appear when they arrive, and a background refresh swaps data in place.
 * Songs play on tap; albums and playlists open their own screen (decided by the caller).
 */
@Composable
fun SimpHomeScreen(
    homeData: SpotifyHomeData?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
    onMediaItemClick: (SpotifyMediaItem) -> Unit,
    recentlyPlayed: List<SpotifyTrack> = emptyList(),
    onPlayTrack: (SpotifyTrack) -> Unit = {},
    userProfile: SpotifyUserProfile? = null,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val scheme = MaterialTheme.colorScheme
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 4..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }
    }
    val firstName = userProfile?.displayName?.trim()?.substringBefore(' ').orEmpty()

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background),
        contentPadding = PaddingValues(start = PrismTokens.Space.gutter, end = PrismTokens.Space.gutter, bottom = 200.dp),
        verticalArrangement = Arrangement.spacedBy(PrismTokens.Space.section)
    ) {
        // Masthead: Prism's page title, light and large, with round glass actions
        item(key = "masthead") {
            ScreenHeader(
                title = if (firstName.isNotEmpty()) firstName else "Your music",
                subtitle = greeting,
                start = 0.dp,
                end = 0.dp
            ) {
                RoundAction(Icons.Rounded.Refresh, "Refresh", onClick = onRefresh)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(scheme.primaryContainer)
                        .border(1.dp, glassEdge(), CircleShape)
                        .clickable(role = Role.Button, onClickLabel = "Open settings") { onOpenSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    if (!userProfile?.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = userProfile!!.avatarUrl,
                            contentDescription = "Settings",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = (userProfile?.displayName?.firstOrNull() ?: 'S').uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = scheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Resume slab: the last thing you played
        recentlyPlayed.firstOrNull()?.let { last ->
            item(key = "resume") {
                ResumeSlab(track = last, onClick = { onPlayTrack(last) })
            }
        }

        // Recents ledger
        if (recentlyPlayed.size > 1) {
            item(key = "ledger") {
                Column {
                    SectionHeader(title = "Recently played", strapline = "${recentlyPlayed.size} songs")
                    Spacer(Modifier.height(4.dp))
                    recentlyPlayed.drop(1).take(5).forEachIndexed { i, t ->
                        LedgerRow(index = i + 1, track = t, onClick = { onPlayTrack(t) })
                    }
                }
            }
        }

        // Shelves
        if (homeData != null && homeData.sections.isNotEmpty()) {
            items(homeData.sections) { section ->
                val isArtistShelf = section.items.isNotEmpty() &&
                        section.items.all { it.type == SpotifyItemType.ARTIST || it.uri.contains(":artist:") }
                Column {
                    SectionHeader(title = section.title, strapline = section.subtitle?.takeIf { it.isNotBlank() })
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(section.items) { item ->
                            if (isArtistShelf || item.type == SpotifyItemType.ARTIST) {
                                SimpArtistCircleCard(item = item, onClick = { onMediaItemClick(item) })
                            } else {
                                SimpSquareCard(item = item, onClick = { onMediaItemClick(item) })
                            }
                        }
                    }
                }
            }
        } else if (homeData == null) {
            // First load only: quiet placeholders, no spinner and no text
            items(2) {
                Column {
                    Box(
                        Modifier
                            .width(140.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(scheme.surfaceContainer)
                    )
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(4) {
                            Box(
                                Modifier
                                    .size(152.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(scheme.surfaceContainer)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumeSlab(track: SpotifyTrack, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pane(PaneShape)
            .clickable(onClickLabel = "Play ${track.name}") { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Cover(url = track.coverUrl, size = 84.dp, radius = 14.dp)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Pick up where you left off",
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = track.name,
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            SpectrumLine(fraction = 1f, modifier = Modifier.width(36.dp), height = 2.dp)
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(scheme.onSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = scheme.surface, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
private fun LedgerRow(index: Int, track: SpotifyTrack, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClickLabel = "Play ${track.name}") { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = index.toString(),
            style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum"),
            color = scheme.ink3,
            modifier = Modifier.width(28.dp)
        )
        Cover(url = track.coverUrl, size = 48.dp, radius = 10.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.name,
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun Cover(url: String?, size: androidx.compose.ui.unit.Dp, radius: androidx.compose.ui.unit.Dp) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(radius))
            .background(scheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center
    ) {
        if (!url.isNullOrEmpty()) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(Icons.Rounded.Album, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(size / 2))
        }
    }
}
