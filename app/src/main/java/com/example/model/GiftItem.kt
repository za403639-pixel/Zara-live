package com.example.model

data class GiftItem(
    val id: String,
    val name: String,
    val emoji: String,
    val coinPrice: Int,
    val description: String,
    val badgeColorHex: Long
)

val AvailableGifts = listOf(
    GiftItem("gift_rose", "Neon Rose", "🌹", 1, "A charming rose", 0xFFFF2A6D),
    GiftItem("gift_coffee", "Gamer Fuel", "☕", 5, "Energize the stream", 0xFFFF9E00),
    GiftItem("gift_heart", "Cyber Heart", "💖", 10, "Spread the love", 0xFFFF007F),
    GiftItem("gift_controller", "Pro Pad", "🎮", 50, "GG WP!", 0xFF00E5FF),
    GiftItem("gift_crown", "Zara Crown", "👑", 200, "Royal VIP status", 0xFFFFD700),
    GiftItem("gift_rocket", "Mega Rocket", "🚀", 500, "To the moon!", 0xFF7928CA),
    GiftItem("gift_galaxy", "Galaxy Nova", "🌌", 1000, "Ultra legendary blessing", 0xFF00F5D4)
)
