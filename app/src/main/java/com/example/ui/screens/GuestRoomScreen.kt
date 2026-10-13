package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.CameraPreviewView
import com.example.model.GuestSeat
import com.example.ui.components.FloatingReactionsContainer
import com.example.ui.components.GiftTrayBottomSheet
import com.example.ui.components.HostControlPanelOverlay
import com.example.ui.components.LiveChatOverlay
import com.example.ui.components.StreamSeatGridComponent
import com.example.ui.theme.*
import com.example.viewmodel.ZaraLiveUiState
import com.example.viewmodel.ZaraLiveViewModel

enum class StageLayoutMode {
    SPOTLIGHT_1_PLUS_8, // 1 Large Main Host Window + 8 Smaller Guest Slots (2x4)
    EQUAL_SQUAD_9       // Balanced 3x3 Equal Video Grid
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestRoomScreen(
    uiState: ZaraLiveUiState,
    viewModel: ZaraLiveViewModel,
    onBack: () -> Unit
) {
    val guestRoom = uiState.guestRoom
    var showGiftTray by remember { mutableStateOf(false) }
    var reactionTrigger by remember { mutableLongStateOf(0L) }
    var selectedSeatForAction by remember { mutableStateOf<GuestSeat?>(null) }
    var layoutMode by remember { mutableStateOf(StageLayoutMode.SPOTLIGHT_1_PLUS_8) }

    Scaffold(
        containerColor = ZaraDarkBg,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Zara Live • Video Stage",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ZaraRedLive)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "LIVE",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Text(
                            text = "1 Main Host + 8 Guest Video Slots • ${guestRoom.listenersCount} Viewers",
                            color = ZaraGoldHost,
                            fontSize = 11.sp
                        )
                    }
                },
                actions = {
                    // Layout Mode Switcher Toggle
                    IconButton(
                        onClick = {
                            layoutMode = if (layoutMode == StageLayoutMode.SPOTLIGHT_1_PLUS_8) {
                                StageLayoutMode.EQUAL_SQUAD_9
                            } else {
                                StageLayoutMode.SPOTLIGHT_1_PLUS_8
                            }
                        },
                        modifier = Modifier.testTag("toggle_layout_mode_button")
                    ) {
                        Icon(
                            imageVector = if (layoutMode == StageLayoutMode.SPOTLIGHT_1_PLUS_8) Icons.Default.GridView else Icons.Default.ViewAgenda,
                            contentDescription = "Toggle Grid Layout",
                            tint = ZaraCyan
                        )
                    }

                    // Coins pill
                    Surface(
                        color = ZaraSurfaceElevated,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = ZaraGoldHost,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${uiState.currentUserProfile.coinBalance}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ZaraDarkBg)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                // Sound Effect Banner
                AnimatedVisibility(
                    visible = uiState.soundEffectAlert != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    uiState.soundEffectAlert?.let { effect ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ZaraMagenta.copy(alpha = 0.9f))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔊 $effect",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Seat Action Notice Banner
                AnimatedVisibility(
                    visible = uiState.seatActionNotice != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    uiState.seatActionNotice?.let { notice ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ZaraCyan.copy(alpha = 0.9f))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✨ $notice",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.clearSeatNotice() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // =============================================================
                // REUSABLE 8-GUEST + 1-HOST STREAM SEAT GRID COMPONENT
                // =============================================================
                StreamSeatGridComponent(
                    guestRoom = guestRoom,
                    isCurrentUserInSeat = uiState.isCurrentUserInGuestSeat,
                    currentUserSeatIndex = uiState.currentUserSeatIndex,
                    hasPendingRequest = uiState.hasPendingJoinRequest,
                    isBroadcasting = uiState.isBroadcasting,
                    broadcastMicMuted = uiState.broadcastSettings.isMicMuted,
                    broadcastFrontCamera = uiState.broadcastSettings.isFrontCamera,
                    broadcastTorchOn = uiState.broadcastSettings.isTorchOn,
                    onToggleBroadcastMic = { viewModel.toggleBroadcastMic() },
                    onCameraSwap = { viewModel.flipCamera() },
                    onStopStream = { viewModel.toggleBroadcasting() },
                    onToggleTorch = { viewModel.toggleTorch() },
                    onRequestToJoin = { seat, video, audio, note ->
                        viewModel.requestToJoinStream(seat, video, audio, note)
                    },
                    onCancelJoinRequest = { viewModel.cancelJoinRequest() },
                    onApproveRequest = { reqId, targetSeat ->
                        viewModel.approveJoinRequest(reqId, targetSeat)
                    },
                    onDeclineRequest = { reqId ->
                        viewModel.declineJoinRequest(reqId)
                    },
                    onLeaveSeat = { viewModel.leaveGuestSeat() },
                    onToggleSeatMic = { viewModel.toggleSeatMic(it) },
                    onToggleSeatVideo = { viewModel.toggleSeatVideo(it) },
                    onKickGuest = { viewModel.kickGuestFromSeat(it) },
                    onReassignSeat = { from, to -> viewModel.reassignGuestSeat(from, to) },
                    onOpenGiftTrayForSeat = { showGiftTray = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stage Video & Audio Controls Bar
                StageControlsBar(
                    isUserInSeat = uiState.isCurrentUserInGuestSeat,
                    userSeatIndex = uiState.currentUserSeatIndex,
                    onLeaveSeat = { viewModel.leaveGuestSeat() },
                    onToggleMic = {
                        uiState.currentUserSeatIndex?.let { viewModel.toggleSeatMic(it) }
                    },
                    onToggleVideo = {
                        uiState.currentUserSeatIndex?.let { viewModel.toggleSeatVideo(it) }
                    },
                    onTriggerSound = { viewModel.triggerSoundEffect(it) }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Synchronized Live Chat Stream Overlay
                LiveChatOverlay(
                    modifier = Modifier.weight(1f),
                    messages = uiState.chatMessages,
                    activeGiftAlert = uiState.activeGiftAlert,
                    onSendMessage = { viewModel.sendChatMessage(it) },
                    onOpenGiftTray = { showGiftTray = true },
                    onHeartTap = { reactionTrigger = System.currentTimeMillis() }
                )
            }

            // Floating Reactions
            FloatingReactionsContainer(
                reactionTrigger = reactionTrigger,
                emoji = listOf("❤️", "🔥", "🎙️", "👑", "🚀").random()
            )

            // Gift Tray
            if (showGiftTray) {
                GiftTrayBottomSheet(
                    coinBalance = uiState.currentUserProfile.coinBalance,
                    onSendGift = { viewModel.sendGift(it) },
                    onClaimCoins = { viewModel.claimDailyCoins() },
                    onDismiss = { showGiftTray = false }
                )
            }

            // Seat Action Dialog
            selectedSeatForAction?.let { seat ->
                SeatActionDialog(
                    seat = seat,
                    onDismiss = { selectedSeatForAction = null },
                    onSendGift = {
                        showGiftTray = true
                        selectedSeatForAction = null
                    },
                    onMute = {
                        viewModel.toggleSeatMic(seat.seatNumber)
                        selectedSeatForAction = null
                    }
                )
            }
        }
    }
}

// =========================================================================
// 1 MAIN HOST VIDEO WINDOW (Large 16:9 / Spotlight Window)
// =========================================================================
@Composable
fun MainHostVideoWindow(
    hostSeat: GuestSeat,
    isMuted: Boolean,
    isFrontCamera: Boolean,
    isBroadcasting: Boolean,
    isTorchOn: Boolean,
    onToggleMute: () -> Unit,
    onCameraSwap: () -> Unit,
    onStopStream: () -> Unit,
    onToggleTorch: () -> Unit,
    modifier: Modifier = Modifier,
    onHostClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hostPulse")
    val borderPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onHostClick() }
            .testTag("main_host_video_window"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        border = CardDefaults.outlinedCardBorder().copy(
            width = if (hostSeat.isTalking) 2.5.dp else 1.5.dp,
            brush = if (hostSeat.isTalking) {
                Brush.linearGradient(listOf(ZaraGoldHost, ZaraMagenta.copy(alpha = borderPulse)))
            } else {
                Brush.linearGradient(listOf(ZaraGoldHost.copy(alpha = 0.8f), ZaraPurple))
            }
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Live Video Feed: CameraX Preview when broadcasting local camera, else animated canvas
            if (isBroadcasting) {
                CameraPreviewView(
                    modifier = Modifier.fillMaxSize(),
                    isFrontCamera = isFrontCamera
                )
            } else {
                LiveVideoCanvasBackground(
                    modifier = Modifier.fillMaxSize(),
                    tintColor = ZaraPurpleDark
                )

                // Center Host Cam Simulation / Avatar
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (hostSeat.isTalking) 3.dp else 1.5.dp,
                                color = if (hostSeat.isTalking) ZaraGoldHost else ZaraPurple,
                                shape = CircleShape
                            )
                            .background(ZaraPurpleDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👑", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = hostSeat.userName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Top Left: "👑 MAIN HOST" Badge & Status
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Brush.horizontalGradient(listOf(ZaraGoldHost, Color(0xFFFFB300))))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "👑 MAIN HOST",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                if (hostSeat.isTalking) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ZaraGreenLive.copy(alpha = 0.9f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🎙️ SPEAKING",
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Top Right: Live Audio Visualizer + HD tag
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "1080p 60FPS", color = ZaraCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                AudioWaveVisualizer(
                    isTalking = hostSeat.isTalking,
                    level = hostSeat.audioLevel,
                    color = ZaraGoldHost
                )
            }

            // Host Control Panel Overlay at bottom of host window
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
            ) {
                HostControlPanelOverlay(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    isMuted = isMuted,
                    isFrontCamera = isFrontCamera,
                    isBroadcasting = isBroadcasting,
                    onToggleMute = onToggleMute,
                    onCameraSwap = onCameraSwap,
                    onStopStream = onStopStream,
                    onToggleTorch = onToggleTorch,
                    isTorchOn = isTorchOn
                )
            }
        }
    }
}

// =========================================================================
// 8 SMALLER GUEST VIDEO SLOTS (Grid Tile)
// =========================================================================
@Composable
fun GuestVideoSlotTile(
    seat: GuestSeat,
    isCurrentUser: Boolean,
    onSlotClick: () -> Unit
) {
    val isOccupied = seat.isOccupied
    val isTalking = seat.isTalking

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSlotClick() }
            .testTag("guest_video_slot_${seat.seatNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOccupied) Color(0xFF140F22) else Color(0xFF100C1C).copy(alpha = 0.6f)
        ),
        border = BorderStroke(
            width = if (isTalking) 2.dp else if (isCurrentUser) 1.5.dp else 0.5.dp,
            color = if (isTalking) ZaraGreenLive
            else if (isCurrentUser) ZaraPurple
            else if (isOccupied) ZaraSurfaceBorder
            else ZaraCyan.copy(alpha = 0.25f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isOccupied) {
                // If current user is on this seat and video is active, show CameraX preview!
                if (isCurrentUser && seat.videoOn) {
                    CameraPreviewView(
                        modifier = Modifier.fillMaxSize(),
                        isFrontCamera = true
                    )
                } else {
                    // Simulated Guest Video Stream Feed
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF261845),
                                        Color(0xFF130B24)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isCurrentUser) ZaraPurple else ZaraSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isCurrentUser) "YOU" else seat.userName.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Top Left: Slot Number Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "#${seat.seatNumber}",
                        color = ZaraCyan,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Top Right: Video status / Hand raised
                if (seat.isHandRaised) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    ) {
                        Text(text = "✋", fontSize = 10.sp)
                    }
                }

                // Bottom Overlay Bar: Guest Name + Mic State
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCurrentUser) "You" else seat.userName,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Mic Icon
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .clip(CircleShape)
                            .background(if (seat.isMuted) ZaraRedLive else ZaraGreenLive),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (seat.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                }
            } else {
                // ==============================
                // EMPTY GUEST VIDEO SLOT
                // ==============================
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Join Slot",
                            tint = ZaraCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "Slot #${seat.seatNumber}",
                        color = ZaraCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Tap to Join",
                        color = ZaraTextMuted,
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}

// Host Tile in 3x3 Equal Squad Mode
@Composable
fun HostVideoTile(
    hostSeat: GuestSeat,
    onHostClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onHostClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ZaraPurpleDark),
        border = BorderStroke(width = 2.dp, color = ZaraGoldHost)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "👑", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = hostSeat.userName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ZaraGoldHost)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(text = "HOST", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

// Live Video Background Simulation
@Composable
fun LiveVideoCanvasBackground(
    modifier: Modifier = Modifier,
    tintColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseAnim")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    tintColor,
                    Color(0xFF120B24),
                    Color(0xFF07040D)
                ),
                center = Offset(width * 0.5f, height * 0.5f),
                radius = width * 0.7f
            )
        )

        // Draw animated gaming pulse rays
        val numLines = 4
        for (i in 0 until numLines) {
            val angle = ((animOffset + (i * (360f / numLines))) * (Math.PI / 180.0)).toFloat()
            val startX = width * 0.5f
            val startY = height * 0.5f
            val endX = startX + (kotlin.math.cos(angle) * width * 0.6f)
            val endY = startY + (kotlin.math.sin(angle) * height * 0.6f)

            drawLine(
                color = Color(0x1400E5FF),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
fun AudioWaveVisualizer(
    isTalking: Boolean,
    level: Float,
    color: Color
) {
    Row(
        modifier = Modifier
            .width(28.dp)
            .height(24.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bar1Height = if (isTalking) (10 + (level * 14)).dp else 4.dp
        val bar2Height = if (isTalking) (6 + (level * 18)).dp else 6.dp
        val bar3Height = if (isTalking) (12 + (level * 10)).dp else 3.dp

        Box(
            modifier = Modifier
                .width(3.dp)
                .height(bar1Height)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(bar2Height)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(bar3Height)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
    }
}

@Composable
fun StageControlsBar(
    isUserInSeat: Boolean,
    userSeatIndex: Int?,
    onLeaveSeat: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleVideo: () -> Unit,
    onTriggerSound: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ZaraSurfaceDark)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Soundboard Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val fxList = listOf("👏 Applause", "🎉 Cheer", "📢 Horn", "🥁 Drum")
            fxList.forEach { fx ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ZaraSurfaceElevated)
                        .clickable { onTriggerSound(fx) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(text = fx, fontSize = 10.sp, color = Color.White)
                }
            }
        }

        // Slot Video & Mic Controls
        if (isUserInSeat) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = onToggleMic,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ZaraPurple)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Mic",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onToggleVideo,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ZaraCyan)
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Video",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Button(
                    onClick = onLeaveSeat,
                    colors = ButtonDefaults.buttonColors(containerColor = ZaraRedLive),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = "Leave Slot", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SeatActionDialog(
    seat: GuestSeat,
    onDismiss: () -> Unit,
    onSendGift: () -> Unit,
    onMute: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ZaraSurfaceDark,
        title = {
            Text(
                text = "${seat.roleTitle}: ${seat.userName}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Interactive video stream on Slot #${seat.seatNumber}",
                    color = ZaraTextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Microphone: ${if (seat.isMuted) "Muted 🔇" else "Live 🎙️"}",
                    color = if (seat.isMuted) ZaraRedLive else ZaraGreenLive,
                    fontSize = 12.sp
                )
                Text(
                    text = "Camera: ${if (seat.videoOn) "Streaming Live 📹" else "Off 📴"}",
                    color = if (seat.videoOn) ZaraCyan else ZaraTextMuted,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSendGift,
                colors = ButtonDefaults.buttonColors(containerColor = ZaraMagenta)
            ) {
                Text(text = "Send Gift 🎁", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = ZaraTextSecondary)
            }
        }
    )
}
