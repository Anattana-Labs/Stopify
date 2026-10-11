package com.shashwat.stopify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shashwat.stopify.data.SpotifyHomeData
import com.shashwat.stopify.data.SpotifyItemType
import com.shashwat.stopify.data.SpotifyMediaItem
import com.shashwat.stopify.data.SpotifyTrack
import com.shashwat.stopify.data.SpotifyUserProfile
import com.shashwat.stopify.ui.components.AmbientThemeGlow
import com.shashwat.stopify.ui.components.SimpArtistCircleCard
import com.shashwat.stopify.ui.components.SimpSquareCard
import java.util.Calendar

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
    modifier: Modifier = Modifier
) {
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filterTabs = remember { listOf("All", "Music", "Podcasts") }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0F))
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Spotify-style top row: profile avatar on left (opens Settings), then filter pills inline
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 16.dp, bottom = 4.dp)
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8622C))
                            .clickable { onOpenSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userProfile?.avatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = userProfile!!.avatarUrl,
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = (userProfile?.displayName?.firstOrNull() ?: 'S').uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filterTabs.size) { idx ->
                            val tab = filterTabs[idx]
                            val isSelected = selectedFilterIndex == idx
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) Color(0xFF1ED760) else Color(0xFF2A2A2A))
                                    .clickable { selectedFilterIndex = idx }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tab,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Spotify Iconic 6-card "Jump back in" Quick Grid
            if (recentlyPlayed.isNotEmpty()) {
                item {
                    val quickItems = recentlyPlayed.take(6)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in quickItems.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val item1 = quickItems[i]
                                QuickPlayCard(
                                    track = item1,
                                    onClick = { onPlayTrack(item1) },
                                    modifier = Modifier.weight(1f)
                                )

                                if (i + 1 < quickItems.size) {
                                    val item2 = quickItems[i + 1]
                                    QuickPlayCard(
                                        track = item2,
                                        onClick = { onPlayTrack(item2) },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Recently Played horizontal shelf (when more than 2 tracks exist)
            if (recentlyPlayed.size > 2) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recently played",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${recentlyPlayed.size} tracks",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(recentlyPlayed) { rTrack ->
                                Column(
                                    modifier = Modifier
                                        .width(168.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onPlayTrack(rTrack) }
                                        .padding(2.dp)
                                 ) {
                                    Box(
                                        modifier = Modifier
                                            .size(164.dp)
                                            .shadow(4.dp, RoundedCornerShape(6.dp), spotColor = Color.Black.copy(alpha = 0.45f))
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF242424))
                                            .border(
                                                0.75.dp,
                                                Brush.verticalGradient(
                                                    listOf(Color.White.copy(0.2f), Color.White.copy(0.05f))
                                                ),
                                                RoundedCornerShape(6.dp)
                                            )
                                    ) {
                                        if (!rTrack.coverUrl.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = rTrack.coverUrl,
                                                contentDescription = rTrack.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Album,
                                                    contentDescription = null,
                                                    tint = Color(0xFF1ED760),
                                                    modifier = Modifier.size(36.dp)
                                                )
                                            }
                                        }

                                        // Dynamic Play Button
                                        val cardPrimary = MaterialTheme.colorScheme.primary
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(6.dp)
                                                .size(34.dp)
                                                .shadow(6.dp, CircleShape, spotColor = cardPrimary.copy(alpha = 0.5f))
                                                .clip(CircleShape)
                                                .background(cardPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.PlayArrow,
                                                contentDescription = "Play",
                                                tint = Color.Black,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = rTrack.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = rTrack.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.65f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Shelves from Spotify Home API
            if (isLoading) {
                items(3) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(
                            modifier = Modifier
                                .width(160.dp)
                                .height(22.dp),
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF282828)
                        ) {}
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            items(4) {
                                Surface(
                                    modifier = Modifier.size(150.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF222222)
                                ) {}
                            }
                        }
                    }
                }
            } else if (homeData != null && homeData.sections.isNotEmpty()) {
                items(homeData.sections) { section ->
                    val isArtistSection = section.title.contains("Artist", ignoreCase = true) ||
                            (section.items.isNotEmpty() && section.items.all { it.type == SpotifyItemType.ARTIST || it.uri.contains("artist") })

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(section.items) { item ->
                                if (isArtistSection || item.type == SpotifyItemType.ARTIST) {
                                    SimpArtistCircleCard(
                                        item = item,
                                        onClick = { onMediaItemClick(item) }
                                    )
                                } else {
                                    SimpSquareCard(
                                        item = item,
                                        onClick = { onMediaItemClick(item) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPlayCard(
    track: SpotifyTrack,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFF2A2A2A),
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(Color(0xFF333333)),
                contentAlignment = Alignment.Center
            ) {
                if (!track.coverUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = track.coverUrl,
                        contentDescription = track.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Album,
                        contentDescription = null,
                        tint = Color(0xFF1ED760),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = track.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.5.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            )

            // Dynamic play circle on right edge of quick play card
            val qpPrimary = MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(30.dp)
                    .shadow(4.dp, CircleShape, spotColor = qpPrimary.copy(alpha = 0.5f))
                    .clip(CircleShape)
                    .background(qpPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
