package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.camera.CameraPreviewView
import com.example.model.BroadcastStatus
import com.example.model.MultiStreamDestination
import com.example.model.StreamPlatform
import com.example.ui.theme.*
import com.example.viewmodel.ZaraLiveUiState
import com.example.viewmodel.ZaraLiveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastStudioScreen(
    uiState: ZaraLiveUiState,
    viewModel: ZaraLiveViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var selectedPlatformForEdit by remember { mutableStateOf<MultiStreamDestination?>(null) }
    var activeTab by remember { mutableStateOf(0) } // 0 = Camera Studio, 1 = Multi-Platform Config

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
                                text = "Zara Broadcast Studio",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            if (uiState.isBroadcasting) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ZaraRedLive)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (uiState.isBroadcasting) {
                                val seconds = uiState.broadcastDurationSeconds
                                val hrs = seconds / 3600
                                val mins = (seconds % 3600) / 60
                                val secs = seconds % 60
                                String.format("On Air: %02d:%02d:%02d • Multi-Platform", hrs, mins, secs)
                            } else {
                                "Ready to Simulcast • Multi-Platform"
                            },
                            color = if (uiState.isBroadcasting) ZaraGreenLive else ZaraTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                },
                actions = {
                    // Quick stats tag
                    val activeCount = uiState.multiStreamDestinations.count { it.isEnabled }
                    Surface(
                        color = ZaraSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "$activeCount Platforms Active",
                            color = ZaraCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ZaraDarkBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = ZaraSurfaceDark,
                contentColor = ZaraPurple
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Camera & Live Preview 📹", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Multi-Stream Targets 🌐", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                )
            }

            if (activeTab == 0) {
                // Live Camera Studio View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black)
                ) {
                    if (hasCameraPermission) {
                        CameraPreviewView(
                            modifier = Modifier.fillMaxSize(),
                            isFrontCamera = uiState.broadcastSettings.isFrontCamera,
                            isTorchOn = uiState.broadcastSettings.isTorchOn
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ZaraSurfaceDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = ZaraTextMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Camera Permission Required",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ZaraPurple)
                                ) {
                                    Text("Grant Permission")
                                }
                            }
                        }
                    }

                    // Top Left Telemetry Overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Bitrate: ${if (uiState.isBroadcasting) "5,820 kbps" else "0 kbps"}",
                            color = ZaraGreenLive,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "FPS: ${if (uiState.isBroadcasting) "60 FPS" else "Ready"}",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Resolution: ${uiState.broadcastSettings.resolution}",
                            color = ZaraCyan,
                            fontSize = 11.sp
                        )
                    }

                    // Top Right Quick Controls
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Flip Camera
                        IconButton(
                            onClick = { viewModel.flipCamera() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlipCameraAndroid,
                                contentDescription = "Flip Camera",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Torch
                        IconButton(
                            onClick = { viewModel.toggleTorch() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.broadcastSettings.isTorchOn) ZaraGoldHost.copy(alpha = 0.8f)
                                    else Color.Black.copy(alpha = 0.65f)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashlightOn,
                                contentDescription = "Torch",
                                tint = if (uiState.broadcastSettings.isTorchOn) Color.Black else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Mic Toggle
                        IconButton(
                            onClick = { viewModel.toggleBroadcastMic() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.broadcastSettings.isMicMuted) ZaraRedLive.copy(alpha = 0.8f)
                                    else Color.Black.copy(alpha = 0.65f)
                                )
                        ) {
                            Icon(
                                imageVector = if (uiState.broadcastSettings.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mic",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Bottom: Active Destinations Ticker
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Simulcasting to:",
                                color = ZaraTextSecondary,
                                fontSize = 11.sp
                            )
                            uiState.multiStreamDestinations.filter { it.isEnabled }.forEach { dest ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(dest.platform.brandColorHex).copy(alpha = 0.35f))
                                        .border(0.5.dp, Color(dest.platform.brandColorHex), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${dest.platform.iconEmoji} ${dest.platform.title}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Multi-Stream Targets Configuration
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ZaraSurfaceDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚡ Real-Time Multi-Platform Simulcast",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Broadcast your stream simultaneously to Twitch, YouTube, TikTok, Facebook, and custom RTMP endpoints with zero additional device load.",
                                    color = ZaraTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    items(uiState.multiStreamDestinations, key = { it.platform.name }) { destination ->
                        MultiStreamDestinationCard(
                            destination = destination,
                            isBroadcasting = uiState.isBroadcasting,
                            onToggle = { viewModel.togglePlatformDestination(destination.platform) },
                            onEdit = { selectedPlatformForEdit = destination }
                        )
                    }
                }
            }

            // Bottom Master Action Bar: GO LIVE / END STREAM
            Surface(
                color = ZaraSurfaceDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (uiState.isBroadcasting) "Broadcast Status: LIVE" else "Simulcast Engine: Standby",
                            color = if (uiState.isBroadcasting) ZaraRedLive else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${uiState.multiStreamDestinations.count { it.isEnabled }} Connected Channels",
                            color = ZaraTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.toggleBroadcasting() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isBroadcasting) ZaraRedLive else ZaraPurple
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .height(46.dp)
                            .testTag("master_broadcast_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isBroadcasting) Icons.Default.Stop else Icons.Default.Sensors,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isBroadcasting) "End Broadcast" else "GO LIVE NOW",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Edit Stream Key Dialog
        selectedPlatformForEdit?.let { dest ->
            EditStreamKeyDialog(
                destination = dest,
                onDismiss = { selectedPlatformForEdit = null },
                onSave = { newKey ->
                    viewModel.updateStreamKey(dest.platform, newKey)
                    selectedPlatformForEdit = null
                }
            )
        }
    }
}

@Composable
fun MultiStreamDestinationCard(
    destination: MultiStreamDestination,
    isBroadcasting: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("destination_card_${destination.platform.name}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ZaraSurfaceDark),
        border = BorderStroke(
            width = if (destination.isEnabled) 1.dp else 0.5.dp,
            color = if (destination.isEnabled) Color(destination.platform.brandColorHex) else ZaraSurfaceBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Platform Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(destination.platform.brandColorHex).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = destination.platform.iconEmoji, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = destination.platform.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (destination.isEnabled && isBroadcasting) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ZaraGreenLive)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "STREAMING",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (destination.streamKey.isNotBlank()) "Key: ••••••••••••" else "Key: Not configured",
                    color = ZaraTextSecondary,
                    fontSize = 11.sp
                )

                Text(
                    text = destination.rtmpUrl,
                    color = ZaraTextMuted,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Key",
                    tint = ZaraCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            Switch(
                checked = destination.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(destination.platform.brandColorHex)
                )
            )
        }
    }
}

@Composable
fun EditStreamKeyDialog(
    destination: MultiStreamDestination,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var keyText by remember { mutableStateOf(destination.streamKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ZaraSurfaceDark,
        title = {
            Text(
                text = "Configure ${destination.platform.title}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "RTMP Ingest Server:",
                    color = ZaraTextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = destination.rtmpUrl,
                    color = ZaraCyan,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Stream Key / Token:",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    placeholder = { Text("Enter private stream key...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ZaraPurple
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(keyText) },
                colors = ButtonDefaults.buttonColors(containerColor = ZaraPurple)
            ) {
                Text("Save Key")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ZaraTextSecondary)
            }
        }
    )
}
