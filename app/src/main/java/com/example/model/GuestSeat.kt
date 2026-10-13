package com.example.model

data class GuestSeat(
    val seatNumber: Int, // 0 = Host Seat, 1..8 = Guest Seats
    val isHost: Boolean = false,
    val isOccupied: Boolean = false,
    val userName: String = "",
    val userAvatar: String = "",
    val roleTitle: String = if (isHost) "Room Host" else "Guest #$seatNumber",
    val isMuted: Boolean = false,
    val isTalking: Boolean = false,
    val videoOn: Boolean = true,
    val isHandRaised: Boolean = false,
    val audioLevel: Float = 0f,
    val isCurrentUser: Boolean = false
)

data class JoinStreamRequest(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String = "",
    val requestedSeatNumber: Int? = null, // null = Any available seat
    val timestamp: Long = System.currentTimeMillis(),
    val videoRequested: Boolean = true,
    val audioRequested: Boolean = true,
    val note: String = "Ready to stream live! 🎙️"
)

data class GuestRoom(
    val roomId: String = "zara-room-888",
    val title: String = "Zara Live: VIP Talk Show & Music Cypher",
    val hostSeat: GuestSeat,
    val guestSeats: List<GuestSeat>, // Exactly 8 guest seats
    val topic: String = "Multi-Guest Voice & Video Stage (8 Seats)",
    val listenersCount: Int = 4120,
    val isRecording: Boolean = true,
    val activeSoundEffect: String? = null,
    val pendingJoinRequests: List<JoinStreamRequest> = emptyList()
)
