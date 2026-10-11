package com.shashwat.stopify.ui.components

import androidx.compose.foundation.background
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

enum class NavigationTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    SEARCH("Search", Icons.Rounded.Search),
    LIBRARY("Your Library", Icons.Rounded.LibraryMusic)
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
    val navItems = listOf(
        NavigationTab.HOME,
        NavigationTab.SEARCH,
        NavigationTab.LIBRARY
    )

    val navSurface = MaterialTheme.colorScheme.surface
    val navPrimary = MaterialTheme.colorScheme.primary
    val navOnSurface = MaterialTheme.colorScheme.onSurface
    val navUnselected = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        navSurface.copy(alpha = 0.65f),
                        navSurface.copy(alpha = 0.95f),
                        navSurface
                    ),
                    startY = 0f
                )
            )
            .padding(top = 16.dp)
    ) {
        NavigationBar(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            containerColor = Color.Transparent,
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            navItems.forEach { item ->
                val isSelected = currentTab == item
                NavigationBarItem(
                    selected = isSelected,
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                            color = if (isSelected) navPrimary else navUnselected
                        )
                    },
                    onClick = { onTabSelect(item) },
                    alwaysShowLabel = true,
                    interactionSource = NoRippleInteractionSource(),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = navPrimary,
                        unselectedIconColor = navUnselected,
                        indicatorColor = navPrimary.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}

