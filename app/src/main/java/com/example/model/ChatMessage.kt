package com.example.model

enum class UserRole(val badge: String, val badgeLabel: String) {
    HOST("👑", "Host"),
    MODERATOR("🛡️", "Mod"),
    VIP("💎", "VIP"),
    SUBSCRIBER("⭐", "Sub"),
    GUEST("🎙️", "Guest"),
    VIEWER("", "")
}

data class ChatMessage(
    val id: String,
    val senderName: String,
    val senderRole: UserRole = UserRole.VIEWER,
    val message: String,
    val timestamp: String = "Just now",
    val isGift: Boolean = false,
    val giftName: String? = null,
    val giftIcon: String? = null,
    val giftCoins: Int = 0,
    val colorHex: Long = 0xFF9146FF
)
