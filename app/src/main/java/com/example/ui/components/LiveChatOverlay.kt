package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.UserRole
import com.example.ui.theme.*
import com.example.viewmodel.ActiveGiftAlert

@Composable
fun LiveChatOverlay(
    modifier: Modifier = Modifier,
    messages: List<ChatMessage>,
    activeGiftAlert: ActiveGiftAlert?,
    onSendMessage: (String) -> Unit,
    onOpenGiftTray: () -> Unit,
    onHeartTap: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickEmojis = listOf("🔥", "❤️", "👏", "💯", "🚀", "🎉", "👑", "😂")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xD90D0B14)
                    )
                )
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Active Gift Banner Alert
        AnimatedVisibility(
            visible = activeGiftAlert != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            activeGiftAlert?.let { alert ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    ZaraMagenta.copy(alpha = 0.85f),
                                    ZaraPurple.copy(alpha = 0.85f)
                                )
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = alert.gift.emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${alert.senderName} sent a ${alert.gift.name}!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${alert.gift.coinPrice} Coins • GG!",
                            color = ZaraGoldHost,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Chat Message Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(msg = msg)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Emoji Reaction Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickEmojis.forEach { emoji ->
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable { onSendMessage(emoji) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = "Say something in Zara Live...",
                        color = ZaraTextMuted,
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = ZaraSurfaceDark.copy(alpha = 0.85f),
                    unfocusedContainerColor = ZaraSurfaceDark.copy(alpha = 0.65f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("chat_input_field")
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Send Button
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(ZaraPurple)
                    .testTag("send_chat_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Gift Button
            IconButton(
                onClick = onOpenGiftTray,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(ZaraMagenta)
                    .testTag("open_gift_tray_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = "Gifts",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Floating Reaction Heart Button
            IconButton(
                onClick = onHeartTap,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .testTag("heart_reaction_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Heart",
                    tint = ZaraMagenta,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(msg: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (msg.senderRole != UserRole.VIEWER) {
            Text(
                text = msg.senderRole.badge,
                fontSize = 11.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        Text(
            text = msg.senderName,
            color = Color(msg.colorHex),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.width(6.dp))

        if (msg.isGift) {
            Text(
                text = "${msg.message} (${msg.giftCoins}c)",
                color = ZaraGoldHost,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        } else {
            Text(
                text = msg.message,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 12.sp
            )
        }
    }
}
