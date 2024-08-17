package com.akansu.sosyashare.data.mapper

import com.akansu.sosyashare.data.model.CommentEntity
import com.akansu.sosyashare.domain.model.Comment

fun CommentEntity.toDomainModel(): Comment {
    return Comment(
        id = id,
        postId = postId,
        userId = userId,
        content = content,
        timestamp = timestamp,
        likes = likes.toMutableList(),
        parentCommentId = parentCommentId,
        replies = replies.map { it.toDomainModel() }.toMutableList() // CommentEntity'den Comment'e dönüşüm
    )
}

fun Comment.toEntityModel(): CommentEntity {
    return CommentEntity(
        id = id,
        postId = postId,
        userId = userId,
        content = content,
        timestamp = timestamp,
        likes = likes.toList(),
        replies = replies.map { it.toEntityModel() }.toList(), // Comment'ten CommentEntity'ye dönüşüm
        parentCommentId = parentCommentId
    )
}
