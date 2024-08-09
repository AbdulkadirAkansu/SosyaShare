package com.akansu.sosyashare.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.akansu.sosyashare.data.local.Converters
import java.util.*

@Entity(tableName = "users")
@TypeConverters(Converters::class)
data class UserEntity(
    @PrimaryKey val id: String = "",
    val username: String = "",
    val email: String = "",
    val profilePictureUrl: String? = null,
    val comments: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val followers: List<String> = emptyList(),
    val bio: String = "",
    val lastUsernameChange: Date? = null
)
