package com.example.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun HostControlPanelOverlay(
    modifier: Modifier = Modifier,
    isMuted: Boolean,
    isFrontCamera: Boolean,
    isBroadcasting: Boolean,
    onToggleMute: () -> Unit,
    onCameraSwap: () -> Unit,
    onStopStream: () -> Unit,
    onToggleTorch: () -> Unit = {},
    isTorchOn: Boolean = false
) {
    var showEndStreamConfirmation by remember { mutableStateOf(false) }

    // Sleek Host Studio Floating Control Bar
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(ZaraGoldHost.copy(alpha = 0.8f), ZaraPurple)),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("host_control_panel_overlay"),
        color = Color(0xF2120C1F),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Audio Mute Toggle Button
            HostActionButton(
                icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                label = if (isMuted) "Unmute" else "Mute Mic",
                isActive = !isMuted,
                activeColor = ZaraGreenLive,
                inactiveColor = ZaraRedLive,
                tag = "host_btn_toggle_mute",
                onClick = onToggleMute
            )

            // 2. Camera Swap Button
            HostActionButton(
                icon = Icons.Default.FlipCameraAndroid,
                label = if (isFrontCamera) "Back Cam" else "Front Cam",
                isActive = true,
                activeColor = ZaraCyan,
                inactiveColor = ZaraCyan,
                tag = "host_btn_camera_swap",
                onClick = onCameraSwap
            )

            // 3. Torch / Flashlight Button
            HostActionButton(
                icon = if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                label = if (isTorchOn) "Torch On" else "Torch",
                isActive = isTorchOn,
                activeColor = ZaraGoldHost,
                inactiveColor = Color.White.copy(alpha = 0.6f),
                tag = "host_btn_toggle_torch",
                onClick = onToggleTorch
            )

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(ZaraSurfaceBorder)
            )

            // 4. Stop the Stream Button
            Button(
                onClick = {
                    if (isBroadcasting) {
                        showEndStreamConfirmation = true
                    } else {
                        onStopStream()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBroadcasting) ZaraRedLive else ZaraPurple
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("host_btn_stop_stream")
            ) {
                Icon(
                    imageVector = if (isBroadcasting) Icons.Default.Stop else Icons.Default.Sensors,
                    contentDescription = if (isBroadcasting) "Stop Stream" else "Go Live",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBroadcasting) "End Stream" else "Go Live",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }

    // Confirmation Dialog before stopping the stream
    if (showEndStreamConfirmation) {
        AlertDialog(
            onDismissRequest = { showEndStreamConfirmation = false },
            containerColor = ZaraSurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ZaraRedLive,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "End Live Stream?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to stop the live stream? Multi-platform broadcasting to all connected destinations will end.",
                    color = ZaraTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEndStreamConfirmation = false
                        onStopStream()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZaraRedLive),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(text = "Yes, End Stream", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndStreamConfirmation = false }) {
                    Text(text = "Cancel", color = ZaraTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun HostActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor.copy(alpha = 0.2f) else inactiveColor.copy(alpha = 0.2f))
                .border(
                    width = 1.dp,
                    color = if (isActive) activeColor else inactiveColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else inactiveColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
