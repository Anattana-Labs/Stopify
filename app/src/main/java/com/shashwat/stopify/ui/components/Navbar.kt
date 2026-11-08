package com.shashwat.stopify.ui.components

import androidx.compose.animation.animateColorAsState
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
 * Prism's floating bar: one lifted piece of glass 12 dp above the bottom edge. The mini player is joined to its top
 * (same surface, a hairline between), the tabs sit below. The current tab is lifted on the glass and carries a short
 * line of split light under its label.
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
    val shape = if (hasPlayer) RoundedCornerShape(28.dp) else NavPillShape
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .shadow(14.dp, shape, ambientColor = Color.Black.copy(alpha = 0.25f), spotColor = Color.Black.copy(alpha = 0.25f))
                .clip(shape)
                .background(scheme.surfaceContainerHigh)
                .background(glassFill(0.8f))
                .border(0.8.dp, glassEdge(), shape)
        ) {
            if (hasPlayer) {
                player()
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(0.75.dp)
                        .background(scheme.hairline)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(6.dp),
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
}

@Composable
private fun DockTab(
    item: NavigationTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalIsDark.current
    val fg by animateColorAsState(
        if (selected) scheme.onSurface else scheme.ink3, tween(PrismTokens.Motion.STANDARD), label = "tab_fg"
    )
    val lifted = glassFill(2.6f)
    val bg by animateColorAsState(if (selected) lifted else Color.Transparent, tween(PrismTokens.Motion.STANDARD), label = "tab_bg")
    val mark by animateFloatAsState(if (selected) 1f else 0f, tween(PrismTokens.Motion.STANDARD), label = "tab_line")
    Column(
        modifier = modifier
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .drawWithContent {
                drawContent()
                if (mark > 0f) {
                    val w = 16.dp.toPx() * mark
                    val x = (size.width - w) / 2
                    drawRoundRect(
                        PrismTokens.spectrum(dark, x, x + w),
                        Offset(x, size.height - 6.dp.toPx()), Size(w, 2.dp.toPx()), CornerRadius(1.dp.toPx())
                    )
                }
            }
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(item.icon, contentDescription = null, tint = fg, modifier = Modifier.size(24.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            maxLines = 1,
            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
        )
    }
}
