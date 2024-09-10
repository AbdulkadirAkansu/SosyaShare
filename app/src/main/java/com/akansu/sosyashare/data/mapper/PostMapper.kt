package com.akansu.sosyashare.data.mapper

import com.akansu.sosyashare.data.model.PostEntity
import com.akansu.sosyashare.domain.model.Post

fun PostEntity.toDomainModel(): Post {
    return Post(
        id = this.id,
        userId = this.userId,
        content = this.content,
        imageUrl = this.imageUrl,
        likeCount = this.likeCount,
        likedBy = this.likedBy,
        createdAt = this.createdAt,
        commentCount = this.commentCount
    )
}

fun Post.toEntityModel(): PostEntity {
    return PostEntity(
        id = id,
        userId = userId,
        content = content,
        imageUrl = imageUrl,
        likeCount = likeCount,
        likedBy = likedBy,
        createdAt = createdAt,
        commentCount = commentCount
    )
}

