package com.shashwat.stopify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.shashwat.stopify.ui.components.Eyebrow
import com.shashwat.stopify.ui.theme.PaneShape
import com.shashwat.stopify.ui.theme.glassFill
import com.shashwat.stopify.ui.theme.ink3
import com.shashwat.stopify.ui.theme.pane
import com.shashwat.stopify.ui.theme.spectrumRing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(
    onSelectWebLogin: () -> Unit,
    onSelectCookiesTxt: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Immersive wash: the accent fades from the top into the page
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(Brush.verticalGradient(listOf(primaryColor.copy(alpha = 0.55f), primaryColor.copy(alpha = 0.18f), Color.Transparent)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── TOP LOGO & HEADER ────────────────────────────────
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(top = 28.dp).fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = "Stopify Logo",
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Welcome to Stopify",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ad-free, high fidelity Spotify playback powered by Widevine DRM. Connect your account to get started.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── 2 LOGIN METHOD CARDS ─────────────────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Eyebrow("Choose how to sign in", Modifier.padding(start = 4.dp, bottom = 2.dp))

                // Method 1: Web Login
                LoginOptionCard(
                    title = "Web Login",
                    subtitle = "Log in securely through Spotify Web. Session cookies & Bearer tokens are captured automatically.",
                    icon = Icons.Rounded.Language,
                    badge = "RECOMMENDED",
                    isPrimary = true,
                    onClick = onSelectWebLogin
                )

                // Method 2: cookies.txt File / Paste
                LoginOptionCard(
                    title = "Import cookies.txt",
                    subtitle = "Pick a Netscape cookies.txt file from your storage or paste header tokens directly.",
                    icon = Icons.Rounded.Description,
                    badge = "DIRECT FILE",
                    isPrimary = false,
                    onClick = onSelectCookiesTxt
                )
            }

            // ── FOOTER / SKIP ────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                TextButton(
                    onClick = onSkip,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "Explore as Guest (Skip for now)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Your credentials stay completely local on your device.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.ink3,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LoginOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badge: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pane(PaneShape)
            .then(if (isPrimary) Modifier.spectrumRing(PaneShape, 1.5.dp) else Modifier)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(glassFill(1.8f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = badge.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(glassFill(1.6f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.ink3,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
