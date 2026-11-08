package com.shashwat.stopify.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

import androidx.compose.ui.text.font.FontWeight

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

@Composable
fun SimpBottomNavigationBar(
    currentTab: NavigationTab,
    onTabSelect: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Any? = null
) {
    val scheme = MaterialTheme.colorScheme
    // Floating dock: the active tab expands into an accent pill with its label.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(scheme.surfaceContainerHigh)
                .border(1.dp, scheme.outlineVariant, androidx.compose.foundation.shape.CircleShape)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationTab.values().forEach { item ->
                val selected = currentTab == item
                val bg by animateColorAsState(
                    if (selected) scheme.primary else Color.Transparent, label = "nav_bg"
                )
                val fg by animateColorAsState(
                    if (selected) scheme.onPrimary else scheme.onSurfaceVariant, label = "nav_fg"
                )
                Row(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(bg)
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = { onTabSelect(item) }
                        )
                        .padding(horizontal = if (selected) 18.dp else 16.dp)
                        .animateContentSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = if (selected) null else item.title,
                        tint = fg,
                        modifier = Modifier.size(22.dp)
                    )
                    if (selected) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = fg,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
