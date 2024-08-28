package com.akansu.sosyashare.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "blocked_users")
data class BlockedUserEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val blockerUserId: String = "",
    val blockedUserId: String = ""
)

