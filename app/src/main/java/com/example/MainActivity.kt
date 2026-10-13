package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.GoogleAuthManager
import com.example.data.repository.UserRepository
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.ZaraLiveViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

enum class ZaraScreen {
    BROADCAST, // Default CameraX Live Preview after login
    HOME,
    GUEST_ROOM,
    PROFILE,
    WATCH_STREAM
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if user is logged in. If not, redirect to LoginActivity
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val userRepository = remember { UserRepository(context.applicationContext) }
                val viewModel: ZaraLiveViewModel = viewModel {
                    ZaraLiveViewModel(userRepository = userRepository)
                }

                LaunchedEffect(currentUser.uid) {
                    viewModel.bindUserSession(
                        userId = currentUser.uid,
                        displayName = currentUser.displayName,
                        photoUrl = currentUser.photoUrl?.toString()
                    )
                }

                MainActivityContent(
                    viewModel = viewModel,
                    onLogout = {
                        logoutUser()
                    }
                )
            }
        }
    }

    private fun logoutUser() {
        Firebase.auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityContent(
    viewModel: ZaraLiveViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Default starts on BROADCAST (CameraX preview) per requirement #3
    var currentScreen by remember { mutableStateOf(ZaraScreen.BROADCAST) }
    var previousScreen by remember { mutableStateOf(ZaraScreen.BROADCAST) }

    BackHandler(enabled = currentScreen != ZaraScreen.BROADCAST) {
        currentScreen = ZaraScreen.BROADCAST
    }

    Scaffold(
        containerColor = ZaraDarkBg,
        topBar = {
            if (currentScreen != ZaraScreen.WATCH_STREAM) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(ZaraRedLive, shape = androidx.compose.foundation.shape.CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ZARA LIVE",
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    },
                    actions = {
                        // User handle pill
                        Surface(
                            color = ZaraSurfaceElevated,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = uiState.currentUserProfile.username,
                                color = ZaraCyan,
                                fontSize = 11.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Logout Button (Requirement #5)
                        IconButton(
                            onClick = {
                                GoogleAuthManager.signOut(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onSignOutComplete = onLogout,
                                    scope = scope
                                )
                            },
                            modifier = Modifier.testTag("main_logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Logout",
                                tint = ZaraRedLive
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ZaraDarkBg)
                )
            }
        },
        bottomBar = {
            if (currentScreen != ZaraScreen.WATCH_STREAM) {
                NavigationBar(
                    containerColor = ZaraSurfaceDark,
                    contentColor = ZaraPurple,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == ZaraScreen.BROADCAST,
                        onClick = { currentScreen = ZaraScreen.BROADCAST },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Videocam,
                                contentDescription = "CameraX Studio"
                            )
                        },
                        label = { Text("Camera Live", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = ZaraMagenta,
                            indicatorColor = ZaraMagenta,
                            unselectedIconColor = ZaraTextMuted,
                            unselectedTextColor = ZaraTextMuted
                        ),
                        modifier = Modifier.testTag("nav_go_live")
                    )

                    NavigationBarItem(
                        selected = currentScreen == ZaraScreen.HOME,
                        onClick = { currentScreen = ZaraScreen.HOME },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == ZaraScreen.HOME) Icons.Filled.Tv else Icons.Outlined.Tv,
                                contentDescription = "Browse Streams"
                            )
                        },
                        label = { Text("Streams", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = ZaraPurple,
                            indicatorColor = ZaraPurple,
                            unselectedIconColor = ZaraTextMuted,
                            unselectedTextColor = ZaraTextMuted
                        ),
                        modifier = Modifier.testTag("nav_browse")
                    )

                    NavigationBarItem(
                        selected = currentScreen == ZaraScreen.GUEST_ROOM,
                        onClick = { currentScreen = ZaraScreen.GUEST_ROOM },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == ZaraScreen.GUEST_ROOM) Icons.Filled.Groups else Icons.Outlined.Groups,
                                contentDescription = "8+1 Guest Room"
                            )
                        },
                        label = { Text("Guest Room", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = ZaraGoldHost,
                            indicatorColor = ZaraPurple,
                            unselectedIconColor = ZaraTextMuted,
                            unselectedTextColor = ZaraTextMuted
                        ),
                        modifier = Modifier.testTag("nav_guest_room")
                    )

                    NavigationBarItem(
                        selected = currentScreen == ZaraScreen.PROFILE,
                        onClick = { currentScreen = ZaraScreen.PROFILE },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == ZaraScreen.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "Profile"
                            )
                        },
                        label = { Text("Profile", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = ZaraCyan,
                            indicatorColor = ZaraPurple,
                            unselectedIconColor = ZaraTextMuted,
                            unselectedTextColor = ZaraTextMuted
                        ),
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                ZaraScreen.BROADCAST -> {
                    // CameraX preview stream screen
                    BroadcastStudioScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onBack = { currentScreen = ZaraScreen.HOME }
                    )
                }

                ZaraScreen.HOME -> {
                    HomeScreen(
                        uiState = uiState,
                        onSelectCategory = { viewModel.selectCategory(it) },
                        onSelectStream = { stream ->
                            viewModel.selectStream(stream)
                            previousScreen = ZaraScreen.HOME
                            currentScreen = ZaraScreen.WATCH_STREAM
                        },
                        onOpenGuestRoom = {
                            currentScreen = ZaraScreen.GUEST_ROOM
                        },
                        onOpenBroadcast = {
                            currentScreen = ZaraScreen.BROADCAST
                        }
                    )
                }

                ZaraScreen.GUEST_ROOM -> {
                    GuestRoomScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onBack = { currentScreen = ZaraScreen.BROADCAST }
                    )
                }

                ZaraScreen.PROFILE -> {
                    ProfileScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onOpenBroadcast = { currentScreen = ZaraScreen.BROADCAST },
                        onOpenGuestRoom = { currentScreen = ZaraScreen.GUEST_ROOM },
                        onSignOut = onLogout
                    )
                }

                ZaraScreen.WATCH_STREAM -> {
                    LiveStreamPlayerScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onBack = { currentScreen = previousScreen },
                        onOpenGuestRoom = { currentScreen = ZaraScreen.GUEST_ROOM }
                    )
                }
            }
        }
    }
}
