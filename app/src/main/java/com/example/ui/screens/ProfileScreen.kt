package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.credentials.CredentialManager
import com.example.auth.GoogleAuthManager
import com.example.ui.theme.*
import com.example.viewmodel.ZaraLiveUiState
import com.example.viewmodel.ZaraLiveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ZaraLiveUiState,
    viewModel: ZaraLiveViewModel,
    onOpenBroadcast: () -> Unit,
    onOpenGuestRoom: () -> Unit,
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val profile = uiState.currentUserProfile
    var lowLatencyEnabled by remember { mutableStateOf(true) }
    var soundEffectsEnabled by remember { mutableStateOf(true) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ZaraDarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Streamer Hub • Zara Live",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            GoogleAuthManager.signOut(
                                context = context,
                                credentialManager = credentialManager,
                                onSignOutComplete = onSignOut,
                                scope = scope
                            )
                        },
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            tint = ZaraRedLive
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ZaraDarkBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ZaraSurfaceDark),
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(ZaraPurple, ZaraMagenta))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(2.dp, ZaraPurple, CircleShape)
                                .background(ZaraPurpleDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "👑",
                                fontSize = 34.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile.username,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            if (profile.isVerified) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = ZaraCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = profile.streamerHandle,
                            color = ZaraPurple,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = profile.bio,
                            color = ZaraTextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Grid (Coins, Diamonds, Followers)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatBox(
                                label = "Coins",
                                value = "${profile.coinBalance}",
                                icon = Icons.Default.MonetizationOn,
                                color = ZaraGoldHost
                            )
                            StatBox(
                                label = "Diamonds",
                                value = "${profile.diamondsEarned}",
                                icon = Icons.Default.Diamond,
                                color = ZaraCyan
                            )
                            StatBox(
                                label = "Followers",
                                value = String.format("%,d", profile.followerCount),
                                icon = Icons.Default.People,
                                color = ZaraMagenta
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Edit Streaming Identity
                            Button(
                                onClick = { showEditProfileDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ZaraPurple),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("edit_identity_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Edit Identity",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            // Daily Coin Claim
                            Button(
                                onClick = { viewModel.claimDailyCoins() },
                                colors = ButtonDefaults.buttonColors(containerColor = ZaraSurfaceElevated),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("profile_claim_coins_button")
                            ) {
                                Text(
                                    text = "+500 Coins 🪙",
                                    color = ZaraGoldHost,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Quick Launch Shortcuts
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenBroadcast() },
                        colors = CardDefaults.cardColors(containerColor = ZaraSurfaceDark),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "📹 Go Live Studio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Multi-platform simulcast", color = ZaraTextSecondary, fontSize = 11.sp)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenGuestRoom() },
                        colors = CardDefaults.cardColors(containerColor = ZaraSurfaceDark),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "🎙️ Guest Stage", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "8 Guests + 1 Host room", color = ZaraTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Multi-Platform Connections Status
            item {
                Text(
                    text = "Connected Broadcasting Platforms",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ZaraSurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        uiState.multiStreamDestinations.forEach { dest ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = dest.platform.iconEmoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = dest.platform.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (dest.isEnabled) ZaraGreenLive.copy(alpha = 0.2f) else ZaraSurfaceElevated)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (dest.isEnabled) "Connected 🟢" else "Disabled ⚪",
                                        color = if (dest.isEnabled) ZaraGreenLive else ZaraTextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Streaming Preferences
            item {
                Text(
                    text = "Stream Settings",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ZaraSurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Ultra Low Latency Mode", color = Color.White, fontSize = 13.sp)
                            Switch(
                                checked = lowLatencyEnabled,
                                onCheckedChange = { lowLatencyEnabled = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = ZaraPurple)
                            )
                        }
                        Divider(color = ZaraSurfaceBorder, modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Stage Soundboard Effects", color = Color.White, fontSize = 13.sp)
                            Switch(
                                checked = soundEffectsEnabled,
                                onCheckedChange = { soundEffectsEnabled = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = ZaraMagenta)
                            )
                        }
                    }
                }
            }

            // Sign out action button
            item {
                Button(
                    onClick = {
                        GoogleAuthManager.signOut(
                            context = context,
                            credentialManager = credentialManager,
                            onSignOutComplete = onSignOut,
                            scope = scope
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZaraSurfaceElevated),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = ZaraRedLive)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Sign Out of Zara Live", color = ZaraRedLive, fontWeight = FontWeight.Bold)
                }
            }

            // App Version & Firebase Identity Sync Badge
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Zara Live v1.0 • Cloud Profile Synced with Firebase",
                        color = ZaraTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Edit Profile Identity Dialog
        if (showEditProfileDialog) {
            EditStreamingIdentityDialog(
                currentName = profile.username,
                currentHandle = profile.streamerHandle,
                currentBio = profile.bio,
                currentTwitchKey = profile.twitchStreamKey,
                currentYoutubeKey = profile.youtubeStreamKey,
                onDismiss = { showEditProfileDialog = false },
                onSave = { name, handle, bio, twitchKey, ytKey ->
                    viewModel.updateStreamingProfile(name, handle, bio, twitchKey, ytKey)
                    showEditProfileDialog = false
                }
            )
        }
    }
}

@Composable
fun EditStreamingIdentityDialog(
    currentName: String,
    currentHandle: String,
    currentBio: String,
    currentTwitchKey: String,
    currentYoutubeKey: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var handle by remember { mutableStateOf(currentHandle) }
    var bio by remember { mutableStateOf(currentBio) }
    var twitchKey by remember { mutableStateOf(currentTwitchKey) }
    var ytKey by remember { mutableStateOf(currentYoutubeKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ZaraSurfaceDark,
        title = {
            Text(
                text = "Edit Streaming Identity",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ZaraPurple
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = handle,
                    onValueChange = { handle = it },
                    label = { Text("Streamer Handle (@handle)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ZaraPurple
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ZaraPurple
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = twitchKey,
                    onValueChange = { twitchKey = it },
                    label = { Text("Twitch Stream Key") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ZaraPurple
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ytKey,
                    onValueChange = { ytKey = it },
                    label = { Text("YouTube Live Stream Key") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ZaraPurple
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, handle, bio, twitchKey, ytKey) },
                colors = ButtonDefaults.buttonColors(containerColor = ZaraPurple)
            ) {
                Text("Save to Cloud")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ZaraTextSecondary)
            }
        }
    )
}

@Composable
fun StatBox(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = ZaraTextSecondary,
            fontSize = 11.sp
        )
    }
}
