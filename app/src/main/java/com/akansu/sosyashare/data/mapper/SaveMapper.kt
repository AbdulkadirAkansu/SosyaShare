package com.akansu.sosyashare.data.mapper

import com.akansu.sosyashare.data.model.SaveEntity
import com.akansu.sosyashare.domain.model.Save

object SaveMapper {
    fun toEntity(document: Map<String, Any?>): SaveEntity {
        return SaveEntity(
            postId = document["postId"] as? String ?: "",
            userId = document["userId"] as? String ?: "",
            timestamp = document["timestamp"] as? Long ?: System.currentTimeMillis()
        )
    }

    fun fromEntity(entity: SaveEntity): Map<String, Any> {
        return mapOf(
            "postId" to entity.postId,
            "userId" to entity.userId,
            "timestamp" to entity.timestamp
        )
    }

    fun toDomainModel(entity: SaveEntity): Save {
        return Save(
            postId = entity.postId,
            userId = entity.userId,
            savedAt = entity.timestamp
        )
    }
}


