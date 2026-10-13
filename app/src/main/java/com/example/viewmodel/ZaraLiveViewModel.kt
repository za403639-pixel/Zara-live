package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.data.repository.UserRepository
import com.example.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class UserProfile(
    val userId: String = "",
    val username: String = "ZaraStreamer",
    val streamerHandle: String = "@zarastreamer",
    val bio: String = "Live streamer on Zara Live!",
    val avatar: String = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
    val coinBalance: Int = 1500,
    val diamondsEarned: Int = 0,
    val followerCount: Int = 120,
    val isVerified: Boolean = true,
    val twitchStreamKey: String = "",
    val youtubeStreamKey: String = ""
)

data class ActiveGiftAlert(
    val id: String = UUID.randomUUID().toString(),
    val senderName: String,
    val gift: GiftItem,
    val timestamp: Long = System.currentTimeMillis()
)

data class ZaraLiveUiState(
    val streams: List<LiveStream> = SampleData.streams,
    val selectedCategory: StreamCategory = StreamCategory.ALL,
    val selectedStream: LiveStream = SampleData.streams.first(),
    val chatMessages: List<ChatMessage> = SampleData.sampleChatMessages,
    val guestRoom: GuestRoom = SampleData.createInitialGuestRoom(),
    val multiStreamDestinations: List<MultiStreamDestination> = SampleData.initialMultiStreamDestinations(),
    val broadcastSettings: StreamSettings = StreamSettings(),
    val isBroadcasting: Boolean = false,
    val broadcastDurationSeconds: Long = 0,
    val currentUserProfile: UserProfile = UserProfile(),
    val activeGiftAlert: ActiveGiftAlert? = null,
    val isCurrentUserInGuestSeat: Boolean = false,
    val currentUserSeatIndex: Int? = null,
    val soundEffectAlert: String? = null,
    val isProfileSyncing: Boolean = false,
    val hasPendingJoinRequest: Boolean = false,
    val seatActionNotice: String? = null
)

class ZaraLiveViewModel(
    private val userRepository: UserRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ZaraLiveUiState())
    val uiState: StateFlow<ZaraLiveUiState> = _uiState.asStateFlow()

    private var broadcastTimerJob: Job? = null
    private var simulatedChatJob: Job? = null
    private var audioVisualizerJob: Job? = null
    private var profileListenerJob: Job? = null

    init {
        startSimulatedLiveActivities()
    }

    fun bindUserSession(userId: String, displayName: String?, photoUrl: String?) {
        val userRepo = userRepository ?: return
        profileListenerJob?.cancel()
        _uiState.update { it.copy(isProfileSyncing = true) }

        profileListenerJob = viewModelScope.launch {
            try {
                val entity = userRepo.getOrCreateProfile(
                    userId = userId,
                    defaultName = displayName ?: "Streamer",
                    defaultAvatar = photoUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150"
                )
                _uiState.update { current ->
                    current.copy(
                        currentUserProfile = UserProfile(
                            userId = entity.userId,
                            username = entity.displayName,
                            streamerHandle = entity.streamerHandle,
                            bio = entity.bio,
                            avatar = if (entity.avatarUrl.isNotBlank()) entity.avatarUrl else (photoUrl ?: current.currentUserProfile.avatar),
                            coinBalance = entity.coinBalance.toInt(),
                            diamondsEarned = entity.diamondsEarned.toInt(),
                            followerCount = entity.followerCount.toInt(),
                            isVerified = entity.isVerified,
                            twitchStreamKey = entity.twitchStreamKey,
                            youtubeStreamKey = entity.youtubeStreamKey
                        ),
                        isProfileSyncing = false
                    )
                }

                // Sync keys to destination list if present
                if (entity.twitchStreamKey.isNotBlank()) {
                    updateStreamKey(StreamPlatform.TWITCH, entity.twitchStreamKey)
                }
                if (entity.youtubeStreamKey.isNotBlank()) {
                    updateStreamKey(StreamPlatform.YOUTUBE, entity.youtubeStreamKey)
                }

                // Attach real-time Firestore observation
                userRepo.observeUserProfile(userId).collect { updated ->
                    if (updated != null) {
                        _uiState.update { current ->
                            current.copy(
                                currentUserProfile = current.currentUserProfile.copy(
                                    username = updated.displayName,
                                    streamerHandle = updated.streamerHandle,
                                    bio = updated.bio,
                                    coinBalance = updated.coinBalance.toInt(),
                                    diamondsEarned = updated.diamondsEarned.toInt(),
                                    followerCount = updated.followerCount.toInt(),
                                    twitchStreamKey = updated.twitchStreamKey,
                                    youtubeStreamKey = updated.youtubeStreamKey
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isProfileSyncing = false) }
            }
        }
    }

    fun updateStreamingProfile(
        displayName: String,
        handle: String,
        bio: String,
        twitchKey: String,
        youtubeKey: String
    ) {
        val userRepo = userRepository ?: return
        viewModelScope.launch {
            try {
                userRepo.updateProfile(
                    displayName = displayName,
                    streamerHandle = handle,
                    bio = bio,
                    twitchKey = twitchKey,
                    youtubeKey = youtubeKey
                )
                if (twitchKey.isNotBlank()) updateStreamKey(StreamPlatform.TWITCH, twitchKey)
                if (youtubeKey.isNotBlank()) updateStreamKey(StreamPlatform.YOUTUBE, youtubeKey)
            } catch (_: Exception) {
            }
        }
    }

    private fun startSimulatedLiveActivities() {
        simulatedChatJob = viewModelScope.launch {
            val randomViewers = listOf(
                "Kev_Gamer" to "OMEGALUL that was insane! 😂",
                "StreamQueen" to "PogChamp this stream is next level 🔥",
                "Apex_Predator" to "Multi-streaming to Twitch & YouTube is so clear",
                "ChillVibes" to "Can I join seat 6 on the guest room? 🎙️",
                "NeonSamurai" to "GGs in the chat everyone!! 🚀",
                "Luna_Star" to "Zara Live is the best live streaming app ❤️",
                "HypeTrain99" to "HYPERS!! Let's get to 20k viewers!",
                "TechMod" to "Reminder to follow and sub to support the streamer ✨"
            )
            var index = 0
            while (true) {
                delay(3500)
                val pair = randomViewers[index % randomViewers.size]
                index++
                val newMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderName = pair.first,
                    senderRole = if (pair.first.contains("Mod")) UserRole.MODERATOR else UserRole.VIEWER,
                    message = pair.second,
                    timestamp = "Just now"
                )
                _uiState.update { current ->
                    current.copy(
                        chatMessages = (current.chatMessages + newMsg).takeLast(60)
                    )
                }
            }
        }

        audioVisualizerJob = viewModelScope.launch {
            while (true) {
                delay(600)
                _uiState.update { current ->
                    val updatedGuestSeats = current.guestRoom.guestSeats.map { seat ->
                        if (seat.isOccupied && !seat.isMuted) {
                            val isSpeaking = Math.random() > 0.4
                            seat.copy(
                                isTalking = isSpeaking,
                                audioLevel = if (isSpeaking) (0.3f + (Math.random().toFloat() * 0.7f)) else 0.05f
                            )
                        } else {
                            seat.copy(isTalking = false, audioLevel = 0f)
                        }
                    }
                    val hostSpeaking = Math.random() > 0.3
                    val updatedHost = current.guestRoom.hostSeat.copy(
                        isTalking = hostSpeaking,
                        audioLevel = if (hostSpeaking) (0.4f + (Math.random().toFloat() * 0.6f)) else 0.1f
                    )
                    current.copy(
                        guestRoom = current.guestRoom.copy(
                            hostSeat = updatedHost,
                            guestSeats = updatedGuestSeats
                        )
                    )
                }
            }
        }
    }

    fun selectCategory(category: StreamCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun selectStream(stream: LiveStream) {
        _uiState.update { it.copy(selectedStream = stream) }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val currentProfile = _uiState.value.currentUserProfile
        val newMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = currentProfile.username,
            senderRole = if (_uiState.value.isCurrentUserInGuestSeat) UserRole.GUEST else UserRole.SUBSCRIBER,
            message = text.trim(),
            timestamp = "Just now",
            colorHex = 0xFF00E5FF
        )
        _uiState.update { it.copy(chatMessages = (it.chatMessages + newMsg).takeLast(60)) }
    }

    fun sendGift(gift: GiftItem) {
        val current = _uiState.value
        if (current.currentUserProfile.coinBalance < gift.coinPrice) {
            return
        }

        val updatedBalance = current.currentUserProfile.coinBalance - gift.coinPrice
        val alert = ActiveGiftAlert(
            senderName = current.currentUserProfile.username,
            gift = gift
        )

        val giftMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = current.currentUserProfile.username,
            senderRole = UserRole.VIP,
            message = "sent a ${gift.name}! ${gift.emoji}",
            timestamp = "Just now",
            isGift = true,
            giftName = gift.name,
            giftIcon = gift.emoji,
            giftCoins = gift.coinPrice,
            colorHex = gift.badgeColorHex
        )

        _uiState.update {
            it.copy(
                currentUserProfile = it.currentUserProfile.copy(coinBalance = updatedBalance),
                activeGiftAlert = alert,
                chatMessages = (it.chatMessages + giftMessage).takeLast(60),
                selectedStream = it.selectedStream.copy(
                    likesCount = it.selectedStream.likesCount + (gift.coinPrice * 2)
                )
            )
        }

        // Sync coin balance to Firestore
        userRepository?.let { repo ->
            viewModelScope.launch {
                try {
                    repo.updateCoins(updatedBalance.toLong())
                } catch (_: Exception) {}
            }
        }

        viewModelScope.launch {
            delay(4000)
            _uiState.update {
                if (it.activeGiftAlert?.id == alert.id) it.copy(activeGiftAlert = null) else it
            }
        }
    }

    fun claimDailyCoins() {
        val newBalance = _uiState.value.currentUserProfile.coinBalance + 500
        _uiState.update {
            it.copy(
                currentUserProfile = it.currentUserProfile.copy(coinBalance = newBalance)
            )
        }
        userRepository?.let { repo ->
            viewModelScope.launch {
                try {
                    repo.updateCoins(newBalance.toLong())
                } catch (_: Exception) {}
            }
        }
    }

    fun takeGuestSeat(seatNumber: Int) {
        val current = _uiState.value
        val profile = current.currentUserProfile

        if (seatNumber in 1..8) {
            val updatedSeats = current.guestRoom.guestSeats.map { seat ->
                if (seat.seatNumber == seatNumber && !seat.isOccupied) {
                    seat.copy(
                        isOccupied = true,
                        userName = profile.username,
                        userAvatar = profile.avatar,
                        roleTitle = "Guest #$seatNumber",
                        isMuted = false,
                        isTalking = false,
                        videoOn = true,
                        isCurrentUser = true
                    )
                } else if (seat.isCurrentUser) {
                    seat.copy(
                        isOccupied = false,
                        userName = "",
                        userAvatar = "",
                        roleTitle = "Guest #${seat.seatNumber} (Available)",
                        isCurrentUser = false
                    )
                } else {
                    seat
                }
            }

            _uiState.update {
                it.copy(
                    guestRoom = it.guestRoom.copy(guestSeats = updatedSeats),
                    isCurrentUserInGuestSeat = true,
                    currentUserSeatIndex = seatNumber
                )
            }
        }
    }

    fun leaveGuestSeat() {
        val current = _uiState.value
        val updatedSeats = current.guestRoom.guestSeats.map { seat ->
            if (seat.isCurrentUser) {
                seat.copy(
                    isOccupied = false,
                    userName = "",
                    userAvatar = "",
                    roleTitle = "Guest #${seat.seatNumber} (Available)",
                    isCurrentUser = false,
                    isMuted = true,
                    isTalking = false,
                    videoOn = false
                )
            } else {
                seat
            }
        }

        _uiState.update {
            it.copy(
                guestRoom = it.guestRoom.copy(guestSeats = updatedSeats),
                isCurrentUserInGuestSeat = false,
                currentUserSeatIndex = null
            )
        }
    }

    fun toggleSeatMic(seatNumber: Int) {
        _uiState.update { current ->
            val updatedSeats = current.guestRoom.guestSeats.map { seat ->
                if (seat.seatNumber == seatNumber) {
                    seat.copy(isMuted = !seat.isMuted)
                } else {
                    seat
                }
            }
            current.copy(guestRoom = current.guestRoom.copy(guestSeats = updatedSeats))
        }
    }

    fun toggleSeatVideo(seatNumber: Int) {
        _uiState.update { current ->
            val updatedSeats = current.guestRoom.guestSeats.map { seat ->
                if (seat.seatNumber == seatNumber) {
                    seat.copy(videoOn = !seat.videoOn)
                } else {
                    seat
                }
            }
            current.copy(guestRoom = current.guestRoom.copy(guestSeats = updatedSeats))
        }
    }

    fun requestToJoinStream(
        requestedSeat: Int? = null,
        videoOn: Boolean = true,
        audioOn: Boolean = true,
        note: String = "Ready to stream live! 🎙️"
    ) {
        val current = _uiState.value
        val profile = current.currentUserProfile

        val newRequest = JoinStreamRequest(
            userId = profile.userId.ifEmpty { "current_user_${System.currentTimeMillis()}" },
            userName = profile.username,
            userAvatar = profile.avatar,
            requestedSeatNumber = requestedSeat,
            timestamp = System.currentTimeMillis(),
            videoRequested = videoOn,
            audioRequested = audioOn,
            note = note
        )

        val updatedRequests = current.guestRoom.pendingJoinRequests + newRequest
        _uiState.update {
            it.copy(
                guestRoom = it.guestRoom.copy(pendingJoinRequests = updatedRequests),
                hasPendingJoinRequest = true,
                seatActionNotice = if (requestedSeat != null) "Join request submitted for Seat #$requestedSeat ⏳" else "Join request submitted! Waiting for host... ⏳"
            )
        }
    }

    fun cancelJoinRequest() {
        val current = _uiState.value
        val profile = current.currentUserProfile
        val updatedRequests = current.guestRoom.pendingJoinRequests.filterNot {
            it.userName == profile.username || it.userId == profile.userId
        }
        _uiState.update {
            it.copy(
                guestRoom = it.guestRoom.copy(pendingJoinRequests = updatedRequests),
                hasPendingJoinRequest = false,
                seatActionNotice = "Join request cancelled"
            )
        }
    }

    fun approveJoinRequest(requestId: String, targetSeatNumber: Int? = null) {
        val current = _uiState.value
        val req = current.guestRoom.pendingJoinRequests.find { it.id == requestId } ?: return

        // Determine destination seat:
        // Priority: explicitly specified targetSeatNumber -> requestedSeatNumber (if available) -> first unoccupied seat
        val unoccupied = current.guestRoom.guestSeats.filter { !it.isOccupied }
        val seatToAssign = when {
            targetSeatNumber != null && unoccupied.any { it.seatNumber == targetSeatNumber } -> targetSeatNumber
            req.requestedSeatNumber != null && unoccupied.any { it.seatNumber == req.requestedSeatNumber } -> req.requestedSeatNumber
            unoccupied.isNotEmpty() -> unoccupied.first().seatNumber
            else -> null
        }

        if (seatToAssign == null) {
            _uiState.update { it.copy(seatActionNotice = "Cannot approve: All 8 guest seats are currently occupied!") }
            return
        }

        val isSelf = req.userName == current.currentUserProfile.username || req.userId == current.currentUserProfile.userId

        val updatedSeats = current.guestRoom.guestSeats.map { seat ->
            if (seat.seatNumber == seatToAssign) {
                seat.copy(
                    isOccupied = true,
                    userName = req.userName,
                    userAvatar = req.userAvatar,
                    roleTitle = "Guest #$seatToAssign",
                    isMuted = !req.audioRequested,
                    isTalking = false,
                    videoOn = req.videoRequested,
                    isCurrentUser = isSelf
                )
            } else {
                seat
            }
        }

        val remainingRequests = current.guestRoom.pendingJoinRequests.filterNot { it.id == requestId }

        _uiState.update {
            it.copy(
                guestRoom = it.guestRoom.copy(
                    guestSeats = updatedSeats,
                    pendingJoinRequests = remainingRequests
                ),
                isCurrentUserInGuestSeat = if (isSelf) true else it.isCurrentUserInGuestSeat,
                currentUserSeatIndex = if (isSelf) seatToAssign else it.currentUserSeatIndex,
                hasPendingJoinRequest = if (isSelf) false else it.hasPendingJoinRequest,
                seatActionNotice = "Assigned ${req.userName} to Seat #$seatToAssign 🎉"
            )
        }
    }

    fun declineJoinRequest(requestId: String) {
        val current = _uiState.value
        val req = current.guestRoom.pendingJoinRequests.find { it.id == requestId }
        val remaining = current.guestRoom.pendingJoinRequests.filterNot { it.id == requestId }
        val isSelf = req?.userName == current.currentUserProfile.username || req?.userId == current.currentUserProfile.userId

        _uiState.update {
            it.copy(
                guestRoom = it.guestRoom.copy(pendingJoinRequests = remaining),
                hasPendingJoinRequest = if (isSelf) false else it.hasPendingJoinRequest,
                seatActionNotice = "Declined request from ${req?.userName ?: "user"}"
            )
        }
    }

    fun assignGuestToSeat(
        targetSeatNumber: Int,
        userName: String,
        userAvatar: String,
        videoOn: Boolean = true,
        isMuted: Boolean = false
    ) {
        val current = _uiState.value
        val isSelf = userName == current.currentUserProfile.username

        val updatedSeats = current.guestRoom.guestSeats.map { seat ->
            if (seat.seatNumber == targetSeatNumber) {
                seat.copy(
                    isOccupied = true,
                    userName = userName,
                    userAvatar = userAvatar,
                    roleTitle = "Guest #$targetSeatNumber",
                    isMuted = isMuted,
                    isTalking = false,
                    videoOn = videoOn,
                    isCurrentUser = isSelf
                )
            } else {
                seat
            }
        }

        _uiState.update {
            it.copy(
                guestRoom = it.guestRoom.copy(guestSeats = updatedSeats),
                isCurrentUserInGuestSeat = if (isSelf) true else it.isCurrentUserInGuestSeat,
                currentUserSeatIndex = if (isSelf) targetSeatNumber else it.currentUserSeatIndex,
                seatActionNotice = "Assigned $userName to Seat #$targetSeatNumber"
            )
        }
    }

    fun kickGuestFromSeat(seatNumber: Int) {
        val current = _uiState.value
        val seatToClear = current.guestRoom.guestSeats.find { it.seatNumber == seatNumber }
        val wasSelf = seatToClear?.isCurrentUser == true

        val updatedSeats = current.guestRoom.guestSeats.map { seat ->
            if (seat.seatNumber == seatNumber) {
                seat.copy(
                    isOccupied = false,
                    userName = "",
                    userAvatar = "",
                    roleTitle = "Guest #${seat.seatNumber} (Available)",
                    isCurrentUser = false,
                    isMuted = true,
                    isTalking = false,
                    videoOn = false,
                    isHandRaised = false
                )
            } else {
                seat
            }
        }

        _uiState.update {
            it.copy(
                guestRoom = it.guestRoom.copy(guestSeats = updatedSeats),
                isCurrentUserInGuestSeat = if (wasSelf) false else it.isCurrentUserInGuestSeat,
                currentUserSeatIndex = if (wasSelf) null else it.currentUserSeatIndex,
                seatActionNotice = "Seat #$seatNumber vacated"
            )
        }
    }

    fun reassignGuestSeat(fromSeatNumber: Int, toSeatNumber: Int) {
        val current = _uiState.value
        val sourceSeat = current.guestRoom.guestSeats.find { it.seatNumber == fromSeatNumber } ?: return
        val destSeat = current.guestRoom.guestSeats.find { it.seatNumber == toSeatNumber } ?: return

        if (!sourceSeat.isOccupied || destSeat.isOccupied) {
            _uiState.update { it.copy(seatActionNotice = "Cannot move to an occupied seat!") }
            return
        }

        val updatedSeats = current.guestRoom.guestSeats.map { seat ->
            when (seat.seatNumber) {
                fromSeatNumber -> seat.copy(
                    isOccupied = false,
                    userName = "",
                    userAvatar = "",
                    roleTitle = "Guest #$fromSeatNumber (Available)",
                    isCurrentUser = false,
                    isMuted = true,
                    isTalking = false,
                    videoOn = false
                )
                toSeatNumber -> sourceSeat.copy(
                    seatNumber = toSeatNumber,
                    roleTitle = "Guest #$toSeatNumber"
                )
                else -> seat
            }
        }

        val wasSelf = sourceSeat.isCurrentUser

        _uiState.update {
            it.copy(
                guestRoom = it.guestRoom.copy(guestSeats = updatedSeats),
                currentUserSeatIndex = if (wasSelf) toSeatNumber else it.currentUserSeatIndex,
                seatActionNotice = "Moved ${sourceSeat.userName} from Seat #$fromSeatNumber to Seat #$toSeatNumber"
            )
        }
    }

    fun toggleRaiseHand(seatNumber: Int) {
        _uiState.update { current ->
            val updatedSeats = current.guestRoom.guestSeats.map { seat ->
                if (seat.seatNumber == seatNumber) {
                    seat.copy(isHandRaised = !seat.isHandRaised)
                } else {
                    seat
                }
            }
            current.copy(guestRoom = current.guestRoom.copy(guestSeats = updatedSeats))
        }
    }

    fun clearSeatNotice() {
        _uiState.update { it.copy(seatActionNotice = null) }
    }

    fun triggerSoundEffect(effectName: String) {
        _uiState.update { it.copy(soundEffectAlert = effectName) }
        viewModelScope.launch {
            delay(2500)
            _uiState.update { if (it.soundEffectAlert == effectName) it.copy(soundEffectAlert = null) else it }
        }
    }

    fun togglePlatformDestination(platform: StreamPlatform) {
        _uiState.update { current ->
            val updated = current.multiStreamDestinations.map { dest ->
                if (dest.platform == platform) {
                    dest.copy(isEnabled = !dest.isEnabled)
                } else {
                    dest
                }
            }
            current.copy(multiStreamDestinations = updated)
        }
    }

    fun updateStreamKey(platform: StreamPlatform, newKey: String) {
        _uiState.update { current ->
            val updated = current.multiStreamDestinations.map { dest ->
                if (dest.platform == platform) {
                    dest.copy(streamKey = newKey)
                } else {
                    dest
                }
            }
            current.copy(multiStreamDestinations = updated)
        }
    }

    fun toggleBroadcasting() {
        val currentlyBroadcasting = _uiState.value.isBroadcasting
        if (!currentlyBroadcasting) {
            broadcastTimerJob?.cancel()
            _uiState.update { current ->
                val activeDests = current.multiStreamDestinations.map { dest ->
                    if (dest.isEnabled) {
                        dest.copy(
                            status = BroadcastStatus.LIVE_STREAMING,
                            currentBitrateKbps = (4500..6200).random(),
                            droppedFramesPercent = 0.0f
                        )
                    } else {
                        dest.copy(status = BroadcastStatus.IDLE)
                    }
                }
                current.copy(
                    isBroadcasting = true,
                    broadcastDurationSeconds = 0,
                    multiStreamDestinations = activeDests
                )
            }
            broadcastTimerJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    _uiState.update {
                        it.copy(broadcastDurationSeconds = it.broadcastDurationSeconds + 1)
                    }
                }
            }
        } else {
            broadcastTimerJob?.cancel()
            _uiState.update { current ->
                val stoppedDests = current.multiStreamDestinations.map { dest ->
                    dest.copy(status = BroadcastStatus.STOPPED, currentBitrateKbps = 0)
                }
                current.copy(
                    isBroadcasting = false,
                    multiStreamDestinations = stoppedDests
                )
            }
        }
    }

    fun flipCamera() {
        _uiState.update { current ->
            current.copy(
                broadcastSettings = current.broadcastSettings.copy(
                    isFrontCamera = !current.broadcastSettings.isFrontCamera
                )
            )
        }
    }

    fun toggleTorch() {
        _uiState.update { current ->
            current.copy(
                broadcastSettings = current.broadcastSettings.copy(
                    isTorchOn = !current.broadcastSettings.isTorchOn
                )
            )
        }
    }

    fun toggleBroadcastMic() {
        _uiState.update { current ->
            current.copy(
                broadcastSettings = current.broadcastSettings.copy(
                    isMicMuted = !current.broadcastSettings.isMicMuted
                )
            )
        }
    }

    fun setResolution(res: String) {
        _uiState.update { current ->
            current.copy(
                broadcastSettings = current.broadcastSettings.copy(resolution = res)
            )
        }
    }
}
