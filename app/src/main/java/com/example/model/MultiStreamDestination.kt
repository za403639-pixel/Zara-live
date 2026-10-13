package com.example.model

enum class StreamPlatform(
    val title: String,
    val iconEmoji: String,
    val defaultRtmp: String,
    val brandColorHex: Long
) {
    TWITCH("Twitch", "🟣", "rtmp://live.twitch.tv/app/", 0xFF9146FF),
    YOUTUBE("YouTube Live", "🔴", "rtmp://a.rtmp.youtube.com/live2/", 0xFFFF0000),
    FACEBOOK("Facebook Live", "🔵", "rtmps://live-api-s.facebook.com:443/rtmp/", 0xFF1877F2),
    TIKTOK("TikTok Live", "🎵", "rtmp://live-push.tiktok.com/live/", 0xFFFE2C55),
    CUSTOM_RTMP("Custom RTMP", "🌐", "rtmp://stream.yourserver.com/live/", 0xFF00E5FF)
}

enum class BroadcastStatus {
    IDLE,
    CONNECTING,
    LIVE_STREAMING,
    RECONNECTING,
    STOPPED
}

data class MultiStreamDestination(
    val platform: StreamPlatform,
    val isEnabled: Boolean = false,
    val rtmpUrl: String = platform.defaultRtmp,
    val streamKey: String = "",
    val status: BroadcastStatus = BroadcastStatus.IDLE,
    val currentBitrateKbps: Int = 0,
    val droppedFramesPercent: Float = 0f
)

data class StreamSettings(
    val resolution: String = "1080p60",
    val videoBitrateKbps: Int = 6000,
    val audioBitrateKbps: Int = 160,
    val fps: Int = 60,
    val isLowLatency: Boolean = true,
    val isFrontCamera: Boolean = true,
    val isTorchOn: Boolean = false,
    val isMicMuted: Boolean = false
)
