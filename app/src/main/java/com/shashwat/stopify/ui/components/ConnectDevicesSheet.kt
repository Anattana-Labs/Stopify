package com.shashwat.stopify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material.icons.rounded.Tablet
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shashwat.stopify.data.ConnectClusterState
import com.shashwat.stopify.data.ConnectDevice

private fun iconFor(type: String): ImageVector = when {
    type.contains("smartphone") || type.contains("phone") -> Icons.Rounded.PhoneAndroid
    type.contains("tablet") -> Icons.Rounded.Tablet
    type.contains("tv") -> Icons.Rounded.Tv
    type.contains("speaker") || type.contains("avr") || type.contains("audio") || type.contains("cast") -> Icons.Rounded.Speaker
    type.contains("computer") -> Icons.Rounded.Computer
    else -> Icons.Rounded.Devices
}

/** The Connect device menu: pick where playback happens, set that device's volume. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectDevicesSheet(
    cluster: ConnectClusterState,
    onDismiss: () -> Unit,
    onSelect: (ConnectDevice) -> Unit,
    onVolume: (ConnectDevice, Float) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    PrismSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Text(
                "Connect to a device",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp)
            )
            if (cluster.devices.size <= 1) {
                Text(
                    "Open Spotify on another device signed in to this account and it will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp)
                )
            }
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                items(cluster.devices, key = { it.id }) { d ->
                    DeviceRow(d, onClick = { onSelect(d) }, onVolume = { onVolume(d, it) })
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(d: ConnectDevice, onClick: () -> Unit, onVolume: (Float) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val tint = if (d.isActive) scheme.primary else scheme.onSurface
    var vol by remember(d.id, d.volume) { mutableFloatStateOf(d.volume) }
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(scheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) { Icon(iconFor(d.type), null, tint = tint, modifier = Modifier.size(22.dp)) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (d.isSelf) "This device" else d.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = tint,
                    maxLines = 1
                )
                Text(
                    when {
                        d.isActive -> "Playing here"
                        d.isSelf -> "Stopify"
                        else -> "Spotify Connect"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (d.isActive) scheme.primary else scheme.onSurfaceVariant
                )
            }
            if (d.isActive) Icon(Icons.Rounded.GraphicEq, "Active device", tint = scheme.primary)
        }
        if (d.isActive && d.canChangeVolume) {
            Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.VolumeUp, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Slider(
                    value = vol,
                    onValueChange = { vol = it },
                    onValueChangeFinished = { onVolume(vol) },
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
            }
        }
    }
}
