package com.shashwat.stopify.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Color
import com.shashwat.stopify.ui.theme.NavPillShape
import com.shashwat.stopify.ui.theme.LocalIsDark
import com.shashwat.stopify.ui.theme.PrismTokens
import com.shashwat.stopify.ui.theme.glassEdge
import com.shashwat.stopify.ui.theme.glassFill
import com.shashwat.stopify.ui.theme.hairline
import com.shashwat.stopify.ui.theme.ink3
import com.shashwat.stopify.ui.theme.spectrumBrush
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
 * Two separate floating objects: the mini player (its own rounded card) and, beneath it, a compact navigation pill
 * that only is as wide as its three tabs. The current tab expands to icon + label; the others stay icon-only.
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp)
            .padding(bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (hasPlayer) {
            val cardShape = RoundedCornerShape(22.dp)
            Box(
                Modifier
                    .fillMaxWidth()
                    .glassSurface(cardShape, 12.dp)
            ) { player() }
        }
        Row(
            modifier = Modifier
                .glassSurface(NavPillShape, 10.dp)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationTab.values().forEach { item ->
                DockTab(item = item, selected = currentTab == item, onClick = { onTabSelect(item) })
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
        if (selected) scheme.onSurface else scheme.ink3, tween(PrismTokens.Motion.STANDARD), label = "tab_fg"
    )
    val bg by animateColorAsState(
        if (selected) glassFill(2.6f) else Color.Transparent, tween(PrismTokens.Motion.STANDARD), label = "tab_bg"
    )
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(NavPillShape)
            .background(bg)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = if (selected) 16.dp else 14.dp)
            .animateContentSize(tween(PrismTokens.Motion.STANDARD)),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            item.icon,
            contentDescription = if (selected) null else item.title,
            tint = fg,
            modifier = Modifier.size(22.dp)
        )
        if (selected) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge,
                color = fg,
                maxLines = 1
            )
        }
    }
}
