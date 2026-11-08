package com.shashwat.stopify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.shashwat.stopify.data.SpotifyItemType
import com.shashwat.stopify.data.SpotifyMediaItem
import com.shashwat.stopify.ui.theme.CardShape

/**
 * Shelf tile. Artwork on top, a small kind tag (Album / Playlist / Song) beside the title row.
 * Tapping a song plays it; albums and playlists open their own screen (handled by the caller).
 */
@Composable
fun SimpSquareCard(
    item: SpotifyMediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val kind = when (item.type) {
        SpotifyItemType.ALBUM -> "Album"
        SpotifyItemType.PLAYLIST -> "Playlist"
        SpotifyItemType.TRACK -> "Song"
        SpotifyItemType.PODCAST, SpotifyItemType.EPISODE -> "Podcast"
        else -> ""
    }
    Column(
        modifier = modifier
            .width(148.dp)
            .clip(CardShape)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(148.dp)
                .clip(CardShape)
                .background(scheme.surfaceContainerHigh)
                .border(1.dp, scheme.outlineVariant, CardShape)
        ) {
            if (!item.coverUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = item.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Album,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(44.dp).align(Alignment.Center)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = if (kind.isNotEmpty() && item.subtitle.isNotBlank()) "$kind · ${item.subtitle}"
            else kind.ifEmpty { item.subtitle },
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SimpArtistCircleCard(
    item: SpotifyMediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .width(104.dp)
            .clip(CardShape)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(scheme.surfaceContainerHigh)
                .border(1.dp, scheme.outlineVariant, CircleShape)
        ) {
            if (!item.coverUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = item.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp).align(Alignment.Center)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Artist",
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
