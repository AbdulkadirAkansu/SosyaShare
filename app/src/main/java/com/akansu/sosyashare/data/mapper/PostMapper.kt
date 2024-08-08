package com.akansu.sosyashare.data.mapper

import com.akansu.sosyashare.data.model.PostEntity
import com.akansu.sosyashare.domain.model.Post

fun PostEntity.toDomainModel(): Post {
    return Post(
        id = id,
        userId = userId,
        content = content,
        imageUrl = imageUrl,
        likeCount = likeCount,
        createdAt = createdAt
    )
}

fun Post.toEntityModel(): PostEntity {
    return PostEntity(
        id = id,
        userId = userId,
        content = content,
        imageUrl = imageUrl,
        likeCount = likeCount,
        createdAt = createdAt
    )
}
