package com.shashwat.stopify.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

enum class NavigationTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    SEARCH("Search", Icons.Rounded.Search),
    LIBRARY("Your Library", Icons.Rounded.LibraryMusic)
}

private val TabWidth = 98.dp
private val BarHeight = 64.dp
private val IndicatorHeight = 52.dp
private val CapsuleInset = 6.dp

@Composable
fun SimpBottomNavigationBar(
    currentTab: NavigationTab,
    onTabSelect: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: PlatformBackdrop? = null
) {
    val barTabs = remember { listOf(NavigationTab.HOME, NavigationTab.SEARCH, NavigationTab.LIBRARY) }
    val selectedIndex = barTabs.indexOf(currentTab).coerceAtLeast(0)
    val tabCornerShape = RoundedCornerShape(BarHeight / 2)

    val totalWidth = TabWidth * barTabs.size + CapsuleInset * 2

    val indicatorOffset by animateDpAsState(
        targetValue = TabWidth * selectedIndex.toFloat(),
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.82f),
        label = "navIndicatorOffset"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(NavigationBarDefaults.windowInsets)
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp, bottom = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .height(BarHeight)
                .width(totalWidth)
                .shadow(20.dp, tabCornerShape, spotColor = Color.Black.copy(alpha = 0.55f))
                .border(
                    width = 0.75.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.28f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    ),
                    shape = tabCornerShape
                )
                .then(
                    if (backdrop != null) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { tabCornerShape },
                            effects = {
                                vibrancy()
                                colorControls(
                                    brightness = 0.04f,
                                    contrast = 1.05f,
                                    saturation = 1.45f
                                )
                                blur(16f.dp.toPx())
                                lens(size.minDimension / 4f, size.minDimension / 2f, false)
                            },
                            onDrawSurface = {
                                drawRect(Color(0xFF0F1210).copy(alpha = 0.65f))
                            }
                        )
                    } else {
                        Modifier
                            .clip(tabCornerShape)
                            .background(Color(0xFF141715).copy(alpha = 0.94f))
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
                // Sliding indicator pill with subtle liquid glow
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .size(width = TabWidth, height = IndicatorHeight)
                        .clip(RoundedCornerShape(IndicatorHeight / 2))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.16f),
                                    Color.White.copy(alpha = 0.06f)
                                )
                            )
                        )
                        .border(
                            0.5.dp,
                            Color.White.copy(alpha = 0.18f),
                            RoundedCornerShape(IndicatorHeight / 2)
                        )
                )

                // Tab items
                Row(modifier = Modifier.fillMaxSize()) {
                    barTabs.forEach { tab ->
                        val isSelected = currentTab == tab
                        val interactionSource = remember { MutableInteractionSource() }

                        Column(
                            modifier = Modifier
                                .width(TabWidth)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(IndicatorHeight / 2))
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { onTabSelect(tab) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.65f),
                                modifier = Modifier.size(23.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f),
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
