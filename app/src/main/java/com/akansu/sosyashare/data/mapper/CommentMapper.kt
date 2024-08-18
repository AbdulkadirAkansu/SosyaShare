package com.akansu.sosyashare.data.mapper

import com.akansu.sosyashare.data.model.CommentEntity
import com.akansu.sosyashare.domain.model.Comment
import java.util.Date


fun CommentEntity.toDomainModel(): Comment {
    return Comment(
        id = id,
        postId = postId,
        userId = userId,
        username = username,
        userProfileUrl = userProfileUrl.toString(),
        content = content,
        timestamp = timestamp,
        likes = likes.toMutableList()
    )
}

fun Comment.toEntityModel(): CommentEntity {
    return CommentEntity(
        id = id,
        postId = postId,
        userId = userId,
        username = username,
        userProfileUrl = userProfileUrl,
        content = content,
        timestamp = timestamp,
        likes = likes.toList()
    )
}

fun CommentEntity(
    id: String,
    postId: String,
    userId: String,
    username: String,
    userProfileUrl: String?,
    content: String,
    timestamp: Date,
    likes: List<String>
): CommentEntity {
    return CommentEntity(
        id = id,
        postId = postId,
        userId = userId,
        username = username,
        userProfileUrl = userProfileUrl,
        content = content,
        timestamp = timestamp,
        likes = likes.toMutableList()
    )
}