package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LiveStream
import com.example.ui.components.FloatingReactionsContainer
import com.example.ui.components.GiftTrayBottomSheet
import com.example.ui.components.LiveChatOverlay
import com.example.ui.theme.*
import com.example.viewmodel.ZaraLiveUiState
import com.example.viewmodel.ZaraLiveViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveStreamPlayerScreen(
    uiState: ZaraLiveUiState,
    viewModel: ZaraLiveViewModel,
    onBack: () -> Unit,
    onOpenGuestRoom: () -> Unit
) {
    val stream = uiState.selectedStream
    var isFollowed by remember { mutableStateOf(false) }
    var showGiftTray by remember { mutableStateOf(false) }
    var reactionTrigger by remember { mutableLongStateOf(0L) }
    var streamQuality by remember { mutableStateOf(stream.resolution) }
    var showQualityPicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Simulated Live High-FPS Broadcast Video Canvas
        AnimatedStreamBackground(modifier = Modifier.fillMaxSize())

        // Top HUD Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Streamer Avatar
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ZaraPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stream.streamerName.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stream.streamerName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                        if (stream.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ZaraCyan,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Text(
                        text = "${String.format("%,d", stream.viewersCount)} Viewers • ${stream.category.displayName}",
                        color = ZaraTextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Follow / Sub Button
                Button(
                    onClick = { isFollowed = !isFollowed },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowed) ZaraSurfaceElevated else ZaraPurple
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (isFollowed) "Following" else "Follow",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Quality Button
                IconButton(
                    onClick = { showQualityPicker = !showQualityPicker },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Quality",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Multi-Platform Simulcast Bar
            Row(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(ZaraGreenLive)
                )
                Text(
                    text = "Simulcast: ${stream.platforms.joinToString(" • ")}",
                    color = ZaraGreenLive,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = streamQuality,
                    color = ZaraCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Center Action if Guest Room
        if (stream.isGuestRoomStream) {
            Button(
                onClick = onOpenGuestRoom,
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(24.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = ZaraPurple.copy(alpha = 0.9f))
            ) {
                Text(text = "🎙️ Join 8+1 Guest Stage", fontWeight = FontWeight.Bold)
            }
        }

        // Floating Reactions Animation Layer
        FloatingReactionsContainer(
            reactionTrigger = reactionTrigger,
            emoji = listOf("❤️", "🔥", "🚀", "👑", "💯").random()
        )

        // Bottom Live Chat Stream Overlay
        LiveChatOverlay(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            messages = uiState.chatMessages,
            activeGiftAlert = uiState.activeGiftAlert,
            onSendMessage = { viewModel.sendChatMessage(it) },
            onOpenGiftTray = { showGiftTray = true },
            onHeartTap = { reactionTrigger = System.currentTimeMillis() }
        )

        // Gift Tray BottomSheet
        if (showGiftTray) {
            GiftTrayBottomSheet(
                coinBalance = uiState.currentUserProfile.coinBalance,
                onSendGift = { viewModel.sendGift(it) },
                onClaimCoins = { viewModel.claimDailyCoins() },
                onDismiss = { showGiftTray = false }
            )
        }

        // Quality Picker Dialog
        if (showQualityPicker) {
            AlertDialog(
                onDismissRequest = { showQualityPicker = false },
                containerColor = ZaraSurfaceDark,
                title = { Text("Stream Resolution Quality", color = Color.White, fontSize = 16.sp) },
                text = {
                    Column {
                        listOf("1080p60 (Source)", "720p60 (HD)", "480p (Data Saver)", "Audio Only 🎧").forEach { q ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        streamQuality = q
                                        showQualityPicker = false
                                    }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = q, color = Color.White, fontSize = 14.sp)
                                if (streamQuality.startsWith(q.take(5))) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = ZaraCyan)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showQualityPicker = false }) {
                        Text("Done", color = ZaraPurple)
                    }
                }
            )
        }
    }
}

@Composable
fun AnimatedStreamBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "streamAnim")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Dark modern cyberspace gaming stream canvas
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF2C1355),
                    Color(0xFF130A24),
                    Color(0xFF07040D)
                ),
                center = Offset(width * 0.5f, height * 0.4f),
                radius = width * 0.8f
            )
        )

        // Draw animated gaming pulse rays
        val numLines = 6
        for (i in 0 until numLines) {
            val angle = ((animOffset + (i * (360f / numLines))) * (Math.PI / 180.0)).toFloat()
            val startX = width * 0.5f
            val startY = height * 0.4f
            val endX = startX + (kotlin.math.cos(angle) * width * 0.7f)
            val endY = startY + (kotlin.math.sin(angle) * height * 0.7f)

            drawLine(
                color = Color(0x1A00E5FF),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 3f
            )
        }
    }
}
