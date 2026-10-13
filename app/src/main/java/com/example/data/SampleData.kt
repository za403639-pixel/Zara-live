package com.example.data

import com.example.model.*

object SampleData {
    val streams = listOf(
        LiveStream(
            id = "stream_1",
            title = "🔴 GRAND FINALS: Apex Legends Pro League Tournament [Zara Simulcast]",
            streamerName = "Zara_ApexKing",
            streamerAvatar = "https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=150",
            category = StreamCategory.GAMING,
            viewersCount = 28410,
            isLive = true,
            tags = listOf("Apex", "Ranked #1", "Simulcast", "Drops Enabled"),
            resolution = "1080p60",
            likesCount = 14300,
            platforms = listOf("Twitch", "YouTube", "TikTok")
        ),
        LiveStream(
            id = "stream_2",
            title = "🎙️ Zara 8-Seat Guest Room: Late Night Hot Takes & Music Cypher",
            streamerName = "Zara_PodcastLive",
            streamerAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            category = StreamCategory.GUEST_ROOM,
            viewersCount = 12950,
            isLive = true,
            tags = listOf("8+1 Guests", "Open Mic", "Voice Stage", "Interactive"),
            resolution = "1080p60",
            likesCount = 8900,
            platforms = listOf("Twitch", "Facebook"),
            isGuestRoomStream = true,
            guestCount = 6
        ),
        LiveStream(
            id = "stream_3",
            title = "⚡ Cyberpunk 2077 Phantom Liberty Ultra 4K 120FPS Gameplay",
            streamerName = "Kira_Valkyrie",
            streamerAvatar = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150",
            category = StreamCategory.GAMING,
            viewersCount = 9820,
            isLive = true,
            tags = listOf("Cyberpunk", "RTX On", "English/ES", "Twitch Partner"),
            resolution = "4K60",
            likesCount = 5400,
            platforms = listOf("Twitch", "YouTube")
        ),
        LiveStream(
            id = "stream_4",
            title = "☕ Late Night Cozy Lo-Fi Vibes, Coding & Chill Q&A",
            streamerName = "NovaChill",
            streamerAvatar = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=150",
            category = StreamCategory.JUST_CHATTING,
            viewersCount = 4210,
            isLive = true,
            tags = listOf("Just Chatting", "Cozy", "Chill", "Lo-Fi"),
            resolution = "1080p30",
            likesCount = 2300,
            platforms = listOf("Twitch")
        ),
        LiveStream(
            id = "stream_5",
            title = "🎵 Live Electronic Synthwave Beats & Improvisation Studio",
            streamerName = "SynthMaster_Zara",
            streamerAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
            category = StreamCategory.MUSIC,
            viewersCount = 6730,
            isLive = true,
            tags = listOf("Music", "Synthwave", "Live Performance"),
            resolution = "1080p60",
            likesCount = 3800,
            platforms = listOf("Twitch", "TikTok", "YouTube")
        )
    )

    val sampleChatMessages = listOf(
        ChatMessage("m1", "NightOwl_99", UserRole.VIEWER, "Let's GOOO! INSANE CLUTCH 🔥", "1m ago"),
        ChatMessage("m2", "Elena_Mod", UserRole.MODERATOR, "Welcome everyone to Zara Live! Please follow chat rules ✨", "1m ago"),
        ChatMessage("m3", "CryptoGamer", UserRole.SUBSCRIBER, "Subscribed for 6 months! Love the multi-stream setup", "45s ago"),
        ChatMessage("m4", "VipKing", UserRole.VIP, "That 360 no-scope was unbelievable 🚀", "30s ago"),
        ChatMessage("m5", "Zara_Host", UserRole.HOST, "Opening up Guest Seat #3 and #4 in 5 minutes! Raise your hand 🎙️", "20s ago"),
        ChatMessage("m6", "PixelSamurai", UserRole.VIEWER, "Audio and 60FPS video quality is so smooth today 👏", "10s ago"),
        ChatMessage("m7", "Maya_Star", UserRole.SUBSCRIBER, "Sent a Cyber Heart! Keep going! 💖", "Just now", isGift = true, giftName = "Cyber Heart", giftIcon = "💖", giftCoins = 10)
    )

    fun createInitialGuestRoom(): GuestRoom {
        val hostSeat = GuestSeat(
            seatNumber = 0,
            isHost = true,
            isOccupied = true,
            userName = "Zara_MasterHost",
            userAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            roleTitle = "👑 Room Host",
            isMuted = false,
            isTalking = true,
            videoOn = true,
            isHandRaised = false,
            audioLevel = 0.85f
        )

        // Exactly 8 guest seats
        val guestSeats = listOf(
            GuestSeat(
                seatNumber = 1,
                isHost = false,
                isOccupied = true,
                userName = "Alex_Raptor",
                userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                roleTitle = "Guest #1",
                isMuted = false,
                isTalking = true,
                videoOn = true,
                audioLevel = 0.65f
            ),
            GuestSeat(
                seatNumber = 2,
                isHost = false,
                isOccupied = true,
                userName = "Sophia_Live",
                userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                roleTitle = "Guest #2",
                isMuted = false,
                isTalking = false,
                videoOn = true,
                audioLevel = 0.05f
            ),
            GuestSeat(
                seatNumber = 3,
                isHost = false,
                isOccupied = true,
                userName = "Leo_Beatbox",
                userAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
                roleTitle = "Guest #3",
                isMuted = true,
                isTalking = false,
                videoOn = false,
                audioLevel = 0.0f
            ),
            GuestSeat(
                seatNumber = 4,
                isHost = false,
                isOccupied = true,
                userName = "Chloe_Gaming",
                userAvatar = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150",
                roleTitle = "Guest #4",
                isMuted = false,
                isTalking = false,
                videoOn = true,
                audioLevel = 0.1f
            ),
            GuestSeat(
                seatNumber = 5,
                isHost = false,
                isOccupied = false, // Empty seat available to join
                userName = "",
                userAvatar = "",
                roleTitle = "Guest #5 (Available)",
                isMuted = true,
                isTalking = false,
                videoOn = false
            ),
            GuestSeat(
                seatNumber = 6,
                isHost = false,
                isOccupied = false, // Empty seat available to join
                userName = "",
                userAvatar = "",
                roleTitle = "Guest #6 (Available)",
                isMuted = true,
                isTalking = false,
                videoOn = false
            ),
            GuestSeat(
                seatNumber = 7,
                isHost = false,
                isOccupied = false, // Empty seat available to join
                userName = "",
                userAvatar = "",
                roleTitle = "Guest #7 (Available)",
                isMuted = true,
                isTalking = false,
                videoOn = false
            ),
            GuestSeat(
                seatNumber = 8,
                isHost = false,
                isOccupied = false, // Empty seat available to join
                userName = "",
                userAvatar = "",
                roleTitle = "Guest #8 (Available)",
                isMuted = true,
                isTalking = false,
                videoOn = false
            )
        )

        val sampleJoinRequests = listOf(
            JoinStreamRequest(
                id = "req_1",
                userId = "user_elena",
                userName = "Elena_Singer",
                userAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                requestedSeatNumber = 5,
                timestamp = System.currentTimeMillis() - 45000,
                videoRequested = true,
                audioRequested = true,
                note = "I have my acoustic guitar ready for the cypher! 🎸"
            ),
            JoinStreamRequest(
                id = "req_2",
                userId = "user_tarik",
                userName = "Tarik_ProGamer",
                userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                requestedSeatNumber = null,
                timestamp = System.currentTimeMillis() - 20000,
                videoRequested = true,
                audioRequested = true,
                note = "Ready to discuss the grand finals clutch! 🎮"
            )
        )

        return GuestRoom(
            roomId = "room_zara_prime",
            title = "🔥 Zara 8-Seat VIP Voice & Video Cypher Stage",
            hostSeat = hostSeat,
            guestSeats = guestSeats,
            topic = "Live Gaming & Music Debate - Open 8 Seats",
            listenersCount = 8412,
            isRecording = true,
            pendingJoinRequests = sampleJoinRequests
        )
    }

    fun initialMultiStreamDestinations(): List<MultiStreamDestination> = listOf(
        MultiStreamDestination(
            platform = StreamPlatform.TWITCH,
            isEnabled = true,
            rtmpUrl = "rtmp://live.twitch.tv/app/",
            streamKey = "live_9281734_zara_twitch_key",
            status = BroadcastStatus.LIVE_STREAMING,
            currentBitrateKbps = 6000,
            droppedFramesPercent = 0.0f
        ),
        MultiStreamDestination(
            platform = StreamPlatform.YOUTUBE,
            isEnabled = true,
            rtmpUrl = "rtmp://a.rtmp.youtube.com/live2/",
            streamKey = "yt_live_9921_zara_stream_key",
            status = BroadcastStatus.LIVE_STREAMING,
            currentBitrateKbps = 5850,
            droppedFramesPercent = 0.1f
        ),
        MultiStreamDestination(
            platform = StreamPlatform.TIKTOK,
            isEnabled = true,
            rtmpUrl = "rtmp://live-push.tiktok.com/live/",
            streamKey = "tiktok_live_8372_stream_key",
            status = BroadcastStatus.LIVE_STREAMING,
            currentBitrateKbps = 4500,
            droppedFramesPercent = 0.0f
        ),
        MultiStreamDestination(
            platform = StreamPlatform.FACEBOOK,
            isEnabled = false,
            rtmpUrl = "rtmps://live-api-s.facebook.com:443/rtmp/",
            streamKey = "",
            status = BroadcastStatus.IDLE,
            currentBitrateKbps = 0,
            droppedFramesPercent = 0.0f
        ),
        MultiStreamDestination(
            platform = StreamPlatform.CUSTOM_RTMP,
            isEnabled = false,
            rtmpUrl = "rtmp://broadcast.mycompany.org/stream/",
            streamKey = "",
            status = BroadcastStatus.IDLE,
            currentBitrateKbps = 0,
            droppedFramesPercent = 0.0f
        )
    )
}
