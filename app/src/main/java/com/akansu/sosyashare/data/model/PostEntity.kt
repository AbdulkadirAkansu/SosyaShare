package com.akansu.sosyashare.data.model


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    val likeCount: Int = 0,
    val createdAt: Date = Date()
)
