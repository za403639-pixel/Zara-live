package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.auth.GoogleAuthManager
import com.example.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if user is already logged in
        if (Firebase.auth.currentUser != null) {
            navigateToMainActivity()
            return
        }

        setContent {
            MyApplicationTheme {
                LoginScreen(
                    onLoginSuccess = { user, name ->
                        saveUserProfileToRealtimeDatabase(user, name)
                        navigateToMainActivity()
                    }
                )
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveUserProfileToRealtimeDatabase(user: FirebaseUser, customName: String) {
        val uid = user.uid
        val email = user.email ?: ""
        val name = when {
            customName.isNotBlank() -> customName
            !user.displayName.isNullOrBlank() -> user.displayName!!
            else -> email.substringBefore("@")
        }

        val profileMap = hashMapOf<String, Any>(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "createdAt" to System.currentTimeMillis()
        )

        try {
            // Save to Firebase Realtime Database
            FirebaseDatabase.getInstance().getReference("users")
                .child(uid)
                .setValue(profileMap)
        } catch (_: Exception) {
            // Realtime database may be offline or unprovisioned in preview container
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: (FirebaseUser, String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var isSignUpMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val auth = FirebaseAuth.getInstance()

    Scaffold(
        containerColor = ZaraDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E113A),
                            Color(0xFF0D0B14),
                            Color(0xFF08060D)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App Logo
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(2.dp, Brush.linearGradient(listOf(ZaraPurple, ZaraMagenta)), CircleShape)
                        .background(ZaraPurpleDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Z", fontSize = 38.sp, fontWeight = FontWeight.Black, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ZaraRedLive)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ZARA LIVE",
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp,
                        letterSpacing = 1.5.sp,
                        color = Color.White
                    )
                }

                Text(
                    text = if (isSignUpMode) "Create your stream account" else "Sign in to start streaming",
                    color = ZaraTextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Card with Fields
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ZaraSurfaceDark),
                    border = CardDefaults.outlinedCardBorder().copy(width = 0.5.dp, brush = Brush.linearGradient(listOf(ZaraSurfaceBorder, ZaraPurple)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isSignUpMode) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Display Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ZaraPurple) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = ZaraPurple
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_name_field")
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ZaraPurple) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ZaraPurple
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_field")
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ZaraPurple) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = ZaraTextMuted
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ZaraPurple
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_field")
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                color = ZaraRedLive,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Submit Button (Email/Password)
                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Please enter email and password"
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null

                                if (isSignUpMode) {
                                    // Register with Email/Password
                                    auth.createUserWithEmailAndPassword(email.trim(), password.trim())
                                        .addOnSuccessListener { result ->
                                            isLoading = false
                                            result.user?.let { onLoginSuccess(it, name) }
                                        }
                                        .addOnFailureListener { ex ->
                                            isLoading = false
                                            errorMessage = ex.localizedMessage ?: "Registration failed"
                                        }
                                } else {
                                    // Sign In with Email/Password
                                    auth.signInWithEmailAndPassword(email.trim(), password.trim())
                                        .addOnSuccessListener { result ->
                                            isLoading = false
                                            result.user?.let { onLoginSuccess(it, name) }
                                        }
                                        .addOnFailureListener { ex ->
                                            isLoading = false
                                            errorMessage = ex.localizedMessage ?: "Sign in failed"
                                        }
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = ZaraPurple),
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("email_auth_submit_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isSignUpMode) "Create Account" else "Sign In with Email",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Toggle Mode (Sign In <-> Sign Up)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isSignUpMode) "Already have an account? " else "Don't have an account? ",
                                color = ZaraTextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (isSignUpMode) "Sign In" else "Sign Up",
                                color = ZaraCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable {
                                    isSignUpMode = !isSignUpMode
                                    errorMessage = null
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = ZaraSurfaceBorder)
                    Text(
                        text = "  OR  ",
                        color = ZaraTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = ZaraSurfaceBorder)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Sign-In Button
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        GoogleAuthManager.onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isLoading = false
                                auth.currentUser?.let { onLoginSuccess(it, it.displayName ?: "") }
                            },
                            onAuthError = { err ->
                                isLoading = false
                                errorMessage = err
                            },
                            scope = scope,
                            onAuthCancelled = {
                                isLoading = false
                            }
                        )
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_google_sign_in_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "G", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF4285F4))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continue with Google",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
