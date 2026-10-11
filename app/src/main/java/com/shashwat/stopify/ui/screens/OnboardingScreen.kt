package com.shashwat.stopify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF09090C))
    ) {
        // Subtle ambient radial glow behind header
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .size(300.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF1ED760).copy(alpha = 0.18f), Color.Transparent)
                    )
                )
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
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                // Spotify-themed icon badge
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1ED760))
                        .border(4.dp, Color(0xFF1ED760).copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = "Stopify Logo",
                        tint = Color.Black,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Welcome to Stopify",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ad-free, high fidelity Spotify playback powered by Widevine DRM. Connect your account to get started.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // ── 2 LOGIN METHOD CARDS ─────────────────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "CHOOSE HOW TO SIGN IN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1ED760),
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )

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
                        color = Color.White.copy(alpha = 0.55f)
                    )
                }

                Text(
                    text = "Your credentials stay completely local on your device.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.35f),
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
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF16161D),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPrimary) Color(0xFF1ED760).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isPrimary) Color(0xFF1ED760).copy(alpha = 0.15f) else Color(0xFF24242E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isPrimary) Color(0xFF1ED760) else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isPrimary) Color(0xFF1ED760) else Color(0xFF2A2A38)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPrimary) Color.Black else Color.White.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = if (isPrimary) Color(0xFF1ED760) else Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
