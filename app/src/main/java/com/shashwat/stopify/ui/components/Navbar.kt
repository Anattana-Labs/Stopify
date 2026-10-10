package com.shashwat.stopify.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

enum class NavigationTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    LIBRARY("Library", Icons.Rounded.LibraryMusic),
    SEARCH("Search", Icons.Rounded.Search)
}

private val FlatTabWidth = 96.dp
private val FlatBarHeight = 64.dp
private val FlatIndicatorHeight = 54.dp
private val CapsuleInset = 5.dp

@Composable
fun SimpBottomNavigationBar(
    currentTab: NavigationTab,
    onTabSelect: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: PlatformBackdrop? = null
) {
    val barTabs = listOf(NavigationTab.HOME, NavigationTab.LIBRARY)
    val isSearchSelected = currentTab == NavigationTab.SEARCH
    val selectedIndex = barTabs.indexOf(currentTab)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(NavigationBarDefaults.windowInsets)
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp, bottom = 8.dp)
    ) {
        // FlatTabBar Capsule
        val tabWidth = FlatTabWidth
        val fullWidth = tabWidth * barTabs.size + CapsuleInset * 2
        val indicatorOffset by animateDpAsState(
            targetValue = if (selectedIndex >= 0) {
                tabWidth * selectedIndex.toFloat()
            } else {
                0.dp
            },
            animationSpec = spring(),
            label = "flatBarIndicator"
        )

        Box(
            modifier = Modifier
                .height(FlatBarHeight)
                .width(fullWidth)
                .shadow(12.dp, RoundedCornerShape(FlatBarHeight / 2), spotColor = Color.Black.copy(alpha = 0.3f))
                .then(
                    if (backdrop != null) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { RoundedCornerShape(FlatBarHeight / 2) },
                            effects = {
                                vibrancy()
                                colorControls(
                                    brightness = 0.05f,
                                    contrast = 1f,
                                    saturation = 1.4f
                                )
                                blur(12f.dp.toPx())
                                lens(size.minDimension / 4f, size.minDimension / 2f, false)
                            },
                            onDrawSurface = {
                                drawRect(Color.Black.copy(alpha = 0.45f))
                            }
                        )
                    } else {
                        Modifier
                            .clip(RoundedCornerShape(FlatBarHeight / 2))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f))
                    }
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = CapsuleInset),
                contentAlignment = Alignment.CenterStart
            ) {
                // Sliding indicator
                if (selectedIndex >= 0) {
                    Box(
                        modifier = Modifier
                            .offset(x = indicatorOffset)
                            .size(width = tabWidth, height = FlatIndicatorHeight)
                            .clip(RoundedCornerShape(FlatIndicatorHeight / 2))
                            .background(
                                if (backdrop != null) {
                                    Color.White.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                }
                            )
                    )
                }

                Row(modifier = Modifier.fillMaxSize()) {
                    barTabs.forEachIndexed { index, tab ->
                        val isSelected = currentTab == tab
                        val contentColor = if (isSelected) {
                            if (backdrop != null) Color.White else MaterialTheme.colorScheme.primary
                        } else {
                            if (backdrop != null) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Column(
                            modifier = Modifier
                                .width(tabWidth)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(FlatIndicatorHeight / 2))
                                .clickable { onTabSelect(tab) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = contentColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = contentColor,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.size(12.dp))

        // Circular Search Button
        Box(
            modifier = Modifier
                .size(FlatBarHeight)
                .shadow(12.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.3f))
                .then(
                    if (backdrop != null) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { CircleShape },
                            effects = {
                                vibrancy()
                                colorControls(
                                    brightness = 0.05f,
                                    contrast = 1f,
                                    saturation = 1.4f
                                )
                                blur(12f.dp.toPx())
                                lens(size.minDimension / 4f, size.minDimension / 2f, false)
                            },
                            onDrawSurface = {
                                val alpha = if (isSearchSelected) 0.6f else 0.45f
                                drawRect(Color.Black.copy(alpha = alpha))
                            }
                        )
                    } else {
                        Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSearchSelected) {
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f)
                                }
                            )
                    }
                )
                .clickable { onTabSelect(NavigationTab.SEARCH) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = if (isSearchSelected) {
                    if (backdrop != null) Color.White else MaterialTheme.colorScheme.primary
                } else {
                    if (backdrop != null) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
