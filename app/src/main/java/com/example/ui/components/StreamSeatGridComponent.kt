package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.model.GuestRoom
import com.example.model.GuestSeat
import com.example.model.JoinStreamRequest
import com.example.ui.theme.*

enum class StreamSeatLayoutMode {
    SPOTLIGHT_1_PLUS_8, // 1 Large Main Host Hero Window + 8 Smaller Guest Video Slots
    EQUAL_SQUAD_9       // Balanced 3x3 Equal 9-Seat Video Grid
}

/**
 * Reusable Grid Layout Component that manages the 8-guest + 1-host seat configuration.
 * Allows users to request to join a stream and be assigned a seat by the host.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamSeatGridComponent(
    guestRoom: GuestRoom,
    isCurrentUserInSeat: Boolean,
    currentUserSeatIndex: Int?,
    hasPendingRequest: Boolean,
    isBroadcasting: Boolean,
    broadcastMicMuted: Boolean,
    broadcastFrontCamera: Boolean,
    broadcastTorchOn: Boolean,
    onToggleBroadcastMic: () -> Unit,
    onCameraSwap: () -> Unit,
    onStopStream: () -> Unit,
    onToggleTorch: () -> Unit,
    onRequestToJoin: (requestedSeat: Int?, videoOn: Boolean, audioOn: Boolean, note: String) -> Unit,
    onCancelJoinRequest: () -> Unit,
    onApproveRequest: (requestId: String, targetSeatNumber: Int?) -> Unit,
    onDeclineRequest: (requestId: String) -> Unit,
    onLeaveSeat: () -> Unit,
    onToggleSeatMic: (seatNumber: Int) -> Unit,
    onToggleSeatVideo: (seatNumber: Int) -> Unit,
    onKickGuest: (seatNumber: Int) -> Unit,
    onReassignSeat: (fromSeat: Int, toSeat: Int) -> Unit,
    onOpenGiftTrayForSeat: (GuestSeat) -> Unit,
    modifier: Modifier = Modifier
) {
    var layoutMode by remember { mutableStateOf(StreamSeatLayoutMode.SPOTLIGHT_1_PLUS_8) }
    var showJoinRequestDialog by remember { mutableStateOf(false) }
    var preselectedSeatForRequest by remember { mutableStateOf<Int?>(null) }
    var showRequestsManagementSheet by remember { mutableStateOf(false) }
    var selectedSeatForDetail by remember { mutableStateOf<GuestSeat?>(null) }

    val pendingRequests = guestRoom.pendingJoinRequests
    val occupiedCount = guestRoom.guestSeats.count { it.isOccupied }
    val availableSeats = guestRoom.guestSeats.filter { !it.isOccupied }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stream_seat_grid_component")
    ) {
        // =====================================================================
        // STAGE CONTROL & REQUEST BAR
        // =====================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Slot Stats & Layout Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = ZaraSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable {
                        layoutMode = if (layoutMode == StreamSeatLayoutMode.SPOTLIGHT_1_PLUS_8) {
                            StreamSeatLayoutMode.EQUAL_SQUAD_9
                        } else {
                            StreamSeatLayoutMode.SPOTLIGHT_1_PLUS_8
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (layoutMode == StreamSeatLayoutMode.SPOTLIGHT_1_PLUS_8) {
                                Icons.Default.GridView
                            } else {
                                Icons.Default.ViewAgenda
                            },
                            contentDescription = "Toggle Grid Mode",
                            tint = ZaraCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (layoutMode == StreamSeatLayoutMode.SPOTLIGHT_1_PLUS_8) "Spotlight 1+8" else "Squad 3x3",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "$occupiedCount/8 Guests",
                    color = ZaraTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Right: Host Request Queue Badge & Request to Join Action
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Pending Requests Badge (Host can tap to review)
                if (pendingRequests.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = { showRequestsManagementSheet = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ZaraPurpleDark,
                            contentColor = ZaraGoldHost
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("open_join_requests_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PanTool,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = ZaraGoldHost
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Requests (${pendingRequests.size})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Join / Leave Stream Seat Action
                if (isCurrentUserInSeat) {
                    OutlinedButton(
                        onClick = onLeaveSeat,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ZaraRedLive),
                        border = BorderStroke(1.dp, ZaraRedLive),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("leave_stream_seat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Leave #${currentUserSeatIndex ?: ""}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (hasPendingRequest) {
                    FilledTonalButton(
                        onClick = onCancelJoinRequest,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ZaraRedLive.copy(alpha = 0.2f),
                            contentColor = ZaraRedLive
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("cancel_join_request_button")
                    ) {
                        Text(
                            text = "Pending ⏳ (Cancel)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            preselectedSeatForRequest = null
                            showJoinRequestDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZaraMagenta,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("request_to_join_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PanTool,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Request Seat",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // =====================================================================
        // GRID SEAT CONFIGURATION LAYOUT
        // =====================================================================
        if (layoutMode == StreamSeatLayoutMode.SPOTLIGHT_1_PLUS_8) {
            // Mode A: Spotlight - 1 Large Host Window + 8 Smaller Guest Tiles
            SpotlightHostWindow(
                hostSeat = guestRoom.hostSeat,
                isBroadcasting = isBroadcasting,
                isMuted = broadcastMicMuted,
                isFrontCamera = broadcastFrontCamera,
                isTorchOn = broadcastTorchOn,
                onToggleMute = onToggleBroadcastMic,
                onCameraSwap = onCameraSwap,
                onStopStream = onStopStream,
                onToggleTorch = onToggleTorch,
                onHostClick = { selectedSeatForDetail = guestRoom.hostSeat },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(205.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 8 Guest Video Slots (2 rows x 4 cols)
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .testTag("guest_slots_grid_spotlight"),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(guestRoom.guestSeats, key = { it.seatNumber }) { seat ->
                    GuestSeatTile(
                        seat = seat,
                        isCurrentUser = seat.isCurrentUser,
                        onTileClick = {
                            if (!seat.isOccupied) {
                                preselectedSeatForRequest = seat.seatNumber
                                showJoinRequestDialog = true
                            } else {
                                selectedSeatForDetail = seat
                            }
                        }
                    )
                }
            }
        } else {
            // Mode B: Equal Squad 9-Tile Grid (3x3)
            val all9Seats = listOf(guestRoom.hostSeat) + guestRoom.guestSeats
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .testTag("squad_9_grid"),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(all9Seats, key = { if (it.isHost) 0 else it.seatNumber }) { seat ->
                    if (seat.isHost) {
                        SquadHostTile(
                            hostSeat = seat,
                            isBroadcasting = isBroadcasting,
                            isFrontCamera = broadcastFrontCamera,
                            onHostClick = { selectedSeatForDetail = seat }
                        )
                    } else {
                        GuestSeatTile(
                            seat = seat,
                            isCurrentUser = seat.isCurrentUser,
                            onTileClick = {
                                if (!seat.isOccupied) {
                                    preselectedSeatForRequest = seat.seatNumber
                                    showJoinRequestDialog = true
                                } else {
                                    selectedSeatForDetail = seat
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG: REQUEST TO JOIN STREAM
    // =========================================================================
    if (showJoinRequestDialog) {
        JoinStreamRequestDialog(
            preselectedSeat = preselectedSeatForRequest,
            availableSeats = availableSeats,
            onDismiss = { showJoinRequestDialog = false },
            onSubmitRequest = { seatNum, videoOn, audioOn, note ->
                onRequestToJoin(seatNum, videoOn, audioOn, note)
                showJoinRequestDialog = false
            }
        )
    }

    // =========================================================================
    // BOTTOM SHEET: HOST JOIN REQUESTS QUEUE MANAGEMENT
    // =========================================================================
    if (showRequestsManagementSheet) {
        JoinRequestsManagementSheet(
            requests = pendingRequests,
            availableSeats = availableSeats,
            onApprove = { reqId, targetSeat ->
                onApproveRequest(reqId, targetSeat)
                if (pendingRequests.size <= 1) showRequestsManagementSheet = false
            },
            onDecline = { reqId ->
                onDeclineRequest(reqId)
                if (pendingRequests.size <= 1) showRequestsManagementSheet = false
            },
            onDismiss = { showRequestsManagementSheet = false }
        )
    }

    // =========================================================================
    // DIALOG / SHEET: SEAT DETAIL & ACTIONS (MUTE, REASSIGN, KICK, GIFT)
    // =========================================================================
    selectedSeatForDetail?.let { seat ->
        SeatDetailSheet(
            seat = seat,
            availableSeats = availableSeats,
            onDismiss = { selectedSeatForDetail = null },
            onToggleMic = {
                onToggleSeatMic(seat.seatNumber)
                selectedSeatForDetail = null
            },
            onToggleVideo = {
                onToggleSeatVideo(seat.seatNumber)
                selectedSeatForDetail = null
            },
            onKick = {
                onKickGuest(seat.seatNumber)
                selectedSeatForDetail = null
            },
            onReassign = { toSeat ->
                onReassignSeat(seat.seatNumber, toSeat)
                selectedSeatForDetail = null
            },
            onSendGift = {
                onOpenGiftTrayForSeat(seat)
                selectedSeatForDetail = null
            }
        )
    }
}

// =============================================================================
// SPOTLIGHT HOST HERO WINDOW (16:9 Stage View)
// =============================================================================
@Composable
fun SpotlightHostWindow(
    hostSeat: GuestSeat,
    isBroadcasting: Boolean,
    isMuted: Boolean,
    isFrontCamera: Boolean,
    isTorchOn: Boolean,
    onToggleMute: () -> Unit,
    onCameraSwap: () -> Unit,
    onStopStream: () -> Unit,
    onToggleTorch: () -> Unit,
    onHostClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hostBorderPulse")
    val borderPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onHostClick() }
            .testTag("spotlight_host_window"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        border = BorderStroke(
            width = if (hostSeat.isTalking) 2.5.dp else 1.5.dp,
            brush = if (hostSeat.isTalking) {
                Brush.linearGradient(listOf(ZaraGoldHost, ZaraMagenta.copy(alpha = borderPulse)))
            } else {
                Brush.linearGradient(listOf(ZaraGoldHost.copy(alpha = 0.8f), ZaraPurple))
            }
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isBroadcasting) {
                CameraPreviewView(
                    modifier = Modifier.fillMaxSize(),
                    isFrontCamera = isFrontCamera
                )
            } else {
                LiveVideoCanvasBackdrop(
                    modifier = Modifier.fillMaxSize(),
                    tintColor = ZaraPurpleDark
                )

                // Host Avatar in Center
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

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = hostSeat.userName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Top Left: Host Badge & Live Tag
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
                    Text(
                        text = "👑 MAIN HOST",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
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

            // Top Right: Quality Tag + Visualizer
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
                    Text(
                        text = "1080p 60FPS",
                        color = ZaraCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                SeatAudioWaveVisualizer(
                    isTalking = hostSeat.isTalking,
                    level = hostSeat.audioLevel,
                    color = ZaraGoldHost
                )
            }

            // Bottom Center: Overlay controls
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

// =============================================================================
// GUEST VIDEO SLOT TILE (1 of 8 Slots)
// =============================================================================
@Composable
fun GuestSeatTile(
    seat: GuestSeat,
    isCurrentUser: Boolean,
    onTileClick: () -> Unit
) {
    val isOccupied = seat.isOccupied
    val isTalking = seat.isTalking

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onTileClick() }
            .testTag("guest_seat_tile_${seat.seatNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOccupied) Color(0xFF140F22) else Color(0xFF100C1C).copy(alpha = 0.65f)
        ),
        border = BorderStroke(
            width = if (isTalking) 2.dp else if (isCurrentUser) 1.5.dp else 0.6.dp,
            color = if (isTalking) ZaraGreenLive
            else if (isCurrentUser) ZaraPurple
            else if (isOccupied) ZaraSurfaceBorder
            else ZaraCyan.copy(alpha = 0.25f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isOccupied) {
                // Video active: CameraX if self, else stylish video backdrop
                if (isCurrentUser && seat.videoOn) {
                    CameraPreviewView(
                        modifier = Modifier.fillMaxSize(),
                        isFrontCamera = true
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF261845), Color(0xFF130B24))
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

                // Top Left: Seat Number
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

                // Top Right: Hand Raised
                if (seat.isHandRaised) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    ) {
                        Text(text = "✋", fontSize = 10.sp)
                    }
                }

                // Bottom Overlay: Name + Mic Status
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
                // EMPTY SEAT STATE
                // ==============================
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Join Slot",
                            tint = ZaraCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

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

// Host Tile for 3x3 Squad Grid
@Composable
fun SquadHostTile(
    hostSeat: GuestSeat,
    isBroadcasting: Boolean,
    isFrontCamera: Boolean,
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
            if (isBroadcasting) {
                CameraPreviewView(
                    modifier = Modifier.fillMaxSize(),
                    isFrontCamera = isFrontCamera
                )
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "👑", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = hostSeat.userName,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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

// =============================================================================
// DIALOG: REQUEST TO JOIN STREAM
// =============================================================================
@Composable
fun JoinStreamRequestDialog(
    preselectedSeat: Int?,
    availableSeats: List<GuestSeat>,
    onDismiss: () -> Unit,
    onSubmitRequest: (seatNumber: Int?, videoOn: Boolean, audioOn: Boolean, note: String) -> Unit
) {
    var selectedSeat by remember { mutableStateOf<Int?>(preselectedSeat) }
    var videoEnabled by remember { mutableStateOf(true) }
    var audioEnabled by remember { mutableStateOf(true) }
    var requestNote by remember { mutableStateOf("Ready to talk and share on stream! 🎙️") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ZaraSurfaceDark,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PanTool,
                    contentDescription = null,
                    tint = ZaraMagenta,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Request to Join Stream",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Request a guest slot on the 8+1 live video stage.",
                    color = ZaraTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Seat Choice Selection
                Text(
                    text = "SELECT SEAT CONFIGURATION:",
                    color = ZaraCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Option: Auto-Assign
                Surface(
                    color = if (selectedSeat == null) ZaraPurple.copy(alpha = 0.3f) else ZaraSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (selectedSeat == null) ZaraPurple else Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedSeat = null }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSeat == null,
                            onClick = { selectedSeat = null },
                            colors = RadioButtonDefaults.colors(selectedColor = ZaraCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Auto-Assign (First Available Seat)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Recommended • Instant placement by Host",
                                color = ZaraTextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Specific Available Seats Grid
                if (availableSeats.isNotEmpty()) {
                    Text(
                        text = "Or choose specific vacant slot (1-8):",
                        color = ZaraTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableSeats.take(4).forEach { seat ->
                            val isChosen = selectedSeat == seat.seatNumber
                            Surface(
                                color = if (isChosen) ZaraMagenta.copy(alpha = 0.3f) else ZaraSurfaceElevated,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isChosen) ZaraMagenta else ZaraSurfaceBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedSeat = seat.seatNumber }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#${seat.seatNumber}",
                                        color = if (isChosen) Color.White else ZaraCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    if (availableSeats.size > 4) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availableSeats.drop(4).forEach { seat ->
                                val isChosen = selectedSeat == seat.seatNumber
                                Surface(
                                    color = if (isChosen) ZaraMagenta.copy(alpha = 0.3f) else ZaraSurfaceElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isChosen) ZaraMagenta else ZaraSurfaceBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedSeat = seat.seatNumber }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#${seat.seatNumber}",
                                            color = if (isChosen) Color.White else ZaraCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Camera & Mic Toggles
                Text(
                    text = "DEVICE PERMISSIONS ON JOIN:",
                    color = ZaraCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = if (videoEnabled) ZaraCyan else ZaraTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Camera Video", color = Color.White, fontSize = 12.sp)
                    }
                    Switch(
                        checked = videoEnabled,
                        onCheckedChange = { videoEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ZaraCyan)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = if (audioEnabled) ZaraGreenLive else ZaraTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Microphone Audio", color = Color.White, fontSize = 12.sp)
                    }
                    Switch(
                        checked = audioEnabled,
                        onCheckedChange = { audioEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ZaraGreenLive)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Short Note Field
                OutlinedTextField(
                    value = requestNote,
                    onValueChange = { requestNote = it },
                    label = { Text("Note for Host (Optional)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ZaraPurple,
                        unfocusedBorderColor = ZaraSurfaceBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmitRequest(selectedSeat, videoEnabled, audioEnabled, requestNote)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ZaraMagenta),
                modifier = Modifier.testTag("submit_join_request_button")
            ) {
                Text(text = "Send Request 🚀", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = ZaraTextSecondary)
            }
        }
    )
}

// =============================================================================
// BOTTOM SHEET: HOST JOIN REQUESTS MANAGEMENT
// =============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinRequestsManagementSheet(
    requests: List<JoinStreamRequest>,
    availableSeats: List<GuestSeat>,
    onApprove: (requestId: String, targetSeatNumber: Int?) -> Unit,
    onDecline: (requestId: String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ZaraDarkBg,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("join_requests_management_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = ZaraGoldHost,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Join Stream Requests (${requests.size})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Text(
                    text = "${availableSeats.size} Vacant Slots",
                    color = ZaraCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Review audience requests and assign them to guest slots #1 through #8.",
                color = ZaraTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            HorizontalDivider(
                color = ZaraSurfaceBorder,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (requests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No pending join requests right now ✨",
                        color = ZaraTextMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(requests, key = { it.id }) { req ->
                        RequestQueueCard(
                            request = req,
                            availableSeats = availableSeats,
                            onApprove = { targetSeat -> onApprove(req.id, targetSeat) },
                            onDecline = { onDecline(req.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RequestQueueCard(
    request: JoinStreamRequest,
    availableSeats: List<GuestSeat>,
    onApprove: (targetSeat: Int?) -> Unit,
    onDecline: () -> Unit
) {
    var expandedSeatPicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ZaraSurfaceElevated),
        border = BorderStroke(1.dp, ZaraSurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ZaraPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = request.userName.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = request.userName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (request.requestedSeatNumber != null) "Prefers Slot #${request.requestedSeatNumber}" else "Any Available Slot",
                                color = ZaraCyan,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (request.videoRequested) "📹 Video On" else "🎙️ Audio Only",
                                color = ZaraTextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Decline button
                IconButton(
                    onClick = onDecline,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Decline",
                        tint = ZaraRedLive,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (request.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = ZaraDarkBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"${request.note}\"",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action: Quick Assign or Pick Specific Seat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quick Assign Button
                Button(
                    onClick = { onApprove(request.requestedSeatNumber) },
                    colors = ButtonDefaults.buttonColors(containerColor = ZaraGreenLive),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (request.requestedSeatNumber != null) "Assign #${request.requestedSeatNumber}" else "Quick Assign",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // Assign to Specific Seat Dropdown Button
                OutlinedButton(
                    onClick = { expandedSeatPicker = !expandedSeatPicker },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ZaraCyan),
                    border = BorderStroke(1.dp, ZaraCyan),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "Choose Seat ▾", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Expanded Seat Selection Row
            if (expandedSeatPicker) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Assign to vacant slot:", fontSize = 10.sp, color = ZaraTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableSeats.forEach { vacant ->
                        FilledTonalButton(
                            onClick = {
                                onApprove(vacant.seatNumber)
                                expandedSeatPicker = false
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = ZaraPurple,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(text = "#${vacant.seatNumber}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// SHEET: SEAT DETAIL & ACTIONS (MUTE, REASSIGN, KICK, GIFT)
// =============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatDetailSheet(
    seat: GuestSeat,
    availableSeats: List<GuestSeat>,
    onDismiss: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleVideo: () -> Unit,
    onKick: () -> Unit,
    onReassign: (toSeat: Int) -> Unit,
    onSendGift: () -> Unit
) {
    var showReassignOptions by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ZaraDarkBg,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("seat_detail_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (seat.isHost) ZaraGoldHost else ZaraPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (seat.isHost) "👑" else seat.userName.take(2).uppercase(),
                            color = if (seat.isHost) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = seat.userName.ifEmpty { "Empty Seat #${seat.seatNumber}" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = seat.roleTitle,
                            color = if (seat.isHost) ZaraGoldHost else ZaraCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                // Status pill
                Surface(
                    color = if (seat.isMuted) ZaraRedLive.copy(alpha = 0.2f) else ZaraGreenLive.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (seat.isMuted) "Muted 🔇" else "Live Mic 🎙️",
                        color = if (seat.isMuted) ZaraRedLive else ZaraGreenLive,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Send Gift
                Button(
                    onClick = onSendGift,
                    colors = ButtonDefaults.buttonColors(containerColor = ZaraMagenta),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Send Gift 🎁", color = Color.White, fontSize = 11.sp)
                }

                // Toggle Mic
                FilledTonalButton(
                    onClick = onToggleMic,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (seat.isMuted) ZaraGreenLive else ZaraSurfaceElevated,
                        contentColor = if (seat.isMuted) Color.Black else Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (seat.isMuted) "Unmute 🎙️" else "Mute 🔇",
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Toggle Video
                FilledTonalButton(
                    onClick = onToggleVideo,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = ZaraSurfaceElevated),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (seat.videoOn) "Stop Cam 📹" else "Start Cam 📹",
                        color = ZaraCyan,
                        fontSize = 11.sp
                    )
                }

                if (!seat.isHost) {
                    // Reassign / Move Seat Button
                    FilledTonalButton(
                        onClick = { showReassignOptions = !showReassignOptions },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = ZaraSurfaceElevated),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Move Seat 🔄", color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            // Reassign Seat Picker
            if (showReassignOptions && availableSeats.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = ZaraSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Transfer guest to another vacant slot:",
                            fontSize = 11.sp,
                            color = ZaraCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            availableSeats.forEach { target ->
                                Button(
                                    onClick = { onReassign(target.seatNumber) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ZaraPurple),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(text = "Seat #${target.seatNumber}", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            if (!seat.isHost) {
                Spacer(modifier = Modifier.height(8.dp))

                // Kick / Vacate seat button
                OutlinedButton(
                    onClick = onKick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ZaraRedLive),
                    border = BorderStroke(1.dp, ZaraRedLive),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PersonRemove, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Remove Guest from Stage", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// =============================================================================
// HELPERS & VISUALIZERS
// =============================================================================
@Composable
fun SeatAudioWaveVisualizer(
    isTalking: Boolean,
    level: Float,
    color: Color
) {
    Row(
        modifier = Modifier
            .width(26.dp)
            .height(20.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bar1Height = if (isTalking) (8 + (level * 12)).dp else 4.dp
        val bar2Height = if (isTalking) (5 + (level * 16)).dp else 5.dp
        val bar3Height = if (isTalking) (10 + (level * 8)).dp else 3.dp

        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(bar1Height)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(bar2Height)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(bar3Height)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
    }
}

@Composable
fun LiveVideoCanvasBackdrop(
    modifier: Modifier = Modifier,
    tintColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseAnimBackdrop")
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
                colors = listOf(tintColor, Color(0xFF120B24), Color(0xFF07040D)),
                center = Offset(width * 0.5f, height * 0.5f),
                radius = width * 0.7f
            )
        )

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
