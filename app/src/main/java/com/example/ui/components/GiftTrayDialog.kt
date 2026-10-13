package com.example.ui.components

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AvailableGifts
import com.example.model.GiftItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftTrayBottomSheet(
    coinBalance: Int,
    onSendGift: (GiftItem) -> Unit,
    onClaimCoins: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedGift by remember { mutableStateOf<GiftItem?>(AvailableGifts.first()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ZaraSurfaceDark,
        dragHandle = { BottomSheetDefaults.DragHandle(color = ZaraTextMuted) },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // Header: Balance + Claim
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Coins",
                        tint = ZaraGoldHost,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$coinBalance",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = " Coins",
                        color = ZaraTextSecondary,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = onClaimCoins,
                    colors = ButtonDefaults.buttonColors(containerColor = ZaraSurfaceElevated),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("claim_coins_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Claim",
                        tint = ZaraCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+500 Free",
                        color = ZaraCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gifts Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AvailableGifts) { gift ->
                    val isSelected = selectedGift?.id == gift.id
                    val canAfford = coinBalance >= gift.coinPrice

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) ZaraPurple.copy(alpha = 0.25f) else ZaraSurfaceElevated)
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) ZaraPurple else ZaraSurfaceBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedGift = gift }
                            .padding(8.dp)
                            .testTag("gift_${gift.id}"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = gift.emoji,
                            fontSize = 30.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = gift.name,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = if (canAfford) ZaraGoldHost else ZaraTextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${gift.coinPrice}",
                                color = if (canAfford) ZaraGoldHost else ZaraTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                selectedGift?.let { gift ->
                    Text(
                        text = "${gift.name}: ${gift.description}",
                        color = ZaraTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        selectedGift?.let {
                            onSendGift(it)
                            onDismiss()
                        }
                    },
                    enabled = selectedGift != null && coinBalance >= (selectedGift?.coinPrice ?: 0),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZaraMagenta,
                        disabledContainerColor = ZaraSurfaceElevated
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("send_gift_button")
                ) {
                    Text(
                        text = "Send Gift 🎁",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
