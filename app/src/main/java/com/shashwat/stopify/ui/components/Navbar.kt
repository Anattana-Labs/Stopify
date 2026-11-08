package com.shashwat.stopify.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import com.shashwat.stopify.ui.theme.NavPillShape
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

enum class NavigationTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    SEARCH("Search", Icons.Rounded.Search),
    LIBRARY("Library", Icons.Rounded.LibraryMusic)
}

class NoRippleInteractionSource : MutableInteractionSource {
    override val interactions: Flow<Interaction> = emptyFlow()
    override suspend fun emit(interaction: Interaction) {}
    override fun tryEmit(interaction: Interaction) = true
}

/**
 * The dock: one full-width object fused to the bottom edge. Optional player strip on top,
 * three tabs below. The selected tab gets a short coral bar above its icon.
 */
@Composable
fun SimpDock(
    currentTab: NavigationTab,
    onTabSelect: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
    hasPlayer: Boolean = false,
    player: @Composable () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    // Two separate floating objects: the mini player card above, the navigation pill below.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (hasPlayer) {
            player()
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, NavPillShape)
                .clip(NavPillShape)
                .background(scheme.surfaceContainerHigh)
                .border(1.dp, scheme.outlineVariant, NavPillShape)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavigationTab.values().forEach { item ->
                DockTab(
                    item = item,
                    selected = currentTab == item,
                    onClick = { onTabSelect(item) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DockTab(
    item: NavigationTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val fg by animateColorAsState(
        if (selected) scheme.onSurface else scheme.onSurfaceVariant, tween(160), label = "tab_fg"
    )
    val barWidth by animateDpAsState(if (selected) 24.dp else 0.dp, tween(200), label = "tab_bar")
    Column(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(24.dp))
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(barWidth)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(scheme.primary)
        )
        Spacer(Modifier.height(5.dp))
        Icon(item.icon, contentDescription = null, tint = if (selected) scheme.primary else fg, modifier = Modifier.size(24.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = fg,
            maxLines = 1
        )
    }
}
