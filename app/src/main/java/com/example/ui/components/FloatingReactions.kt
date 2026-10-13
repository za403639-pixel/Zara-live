package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.random.Random

data class FloatingItem(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String,
    val startOffsetX: Float = Random.nextFloat() * 80f - 40f
)

@Composable
fun FloatingReactionsContainer(
    modifier: Modifier = Modifier,
    reactionTrigger: Long,
    emoji: String = "❤️"
) {
    var activeReactions by remember { mutableStateOf(listOf<FloatingItem>()) }

    LaunchedEffect(reactionTrigger) {
        if (reactionTrigger > 0) {
            val newItem = FloatingItem(emoji = emoji)
            activeReactions = activeReactions + newItem
            delay(2200)
            activeReactions = activeReactions.filter { it.id != newItem.id }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        activeReactions.forEach { item ->
            SingleFloatingReaction(item = item)
        }
    }
}

@Composable
private fun SingleFloatingReaction(item: FloatingItem) {
    val transition = rememberInfiniteTransition(label = "flutter")
    val sway by transition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    val progress = remember { Animatable(0f) }

    LaunchedEffect(item.id) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing)
        )
    }

    val yOffset = progress.value * -380f
    val alpha = 1f - progress.value
    val scale = 0.6f + (progress.value * 0.8f).coerceAtMost(1.3f)

    Text(
        text = item.emoji,
        fontSize = 28.sp,
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (item.startOffsetX + sway - 30).dp.roundToPx(),
                    y = (yOffset - 60).dp.roundToPx()
                )
            }
            .scale(scale)
            .alpha(alpha)
    )
}
