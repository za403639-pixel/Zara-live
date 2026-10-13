package com.example.model

enum class StreamCategory(val displayName: String, val icon: String) {
    ALL("All", "⚡"),
    GAMING("Gaming", "🎮"),
    JUST_CHATTING("Just Chatting", "💬"),
    GUEST_ROOM("8+1 Guest Rooms", "🎙️"),
    ESPORTS("Esports", "🏆"),
    MUSIC("Music", "🎵")
}

data class LiveStream(
    val id: String,
    val title: String,
    val streamerName: String,
    val streamerAvatar: String,
    val category: StreamCategory,
    val viewersCount: Int,
    val isLive: Boolean = true,
    val tags: List<String>,
    val resolution: String = "1080p60",
    val likesCount: Int = 1240,
    val platforms: List<String> = listOf("Twitch", "YouTube"),
    val streamDurationSeconds: Long = 3450,
    val isVerified: Boolean = true,
    val isGuestRoomStream: Boolean = false,
    val guestCount: Int = 0
)
