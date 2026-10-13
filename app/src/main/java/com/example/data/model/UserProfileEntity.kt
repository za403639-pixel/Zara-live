package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfileEntity(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val streamerHandle: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val coinBalance: Long = 1500L,
    val diamondsEarned: Long = 0L,
    val followerCount: Long = 0L,
    val isVerified: Boolean = false,
    val twitchStreamKey: String = "",
    val youtubeStreamKey: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
