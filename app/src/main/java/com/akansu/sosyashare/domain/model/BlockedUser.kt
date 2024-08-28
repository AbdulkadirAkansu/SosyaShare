package com.akansu.sosyashare.domain.model

data class BlockedUser(
    val id: String = "",
    val blockerUserId: String,
    val blockedUserId: String
)
