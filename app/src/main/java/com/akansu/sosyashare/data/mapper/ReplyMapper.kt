package com.akansu.sosyashare.data.mapper

import com.akansu.sosyashare.data.model.ReplyEntity
import com.akansu.sosyashare.domain.model.Reply


fun ReplyEntity.toDomainModel(): Reply {
    return Reply(
        id = id,
        commentId = commentId,
        userId = userId,
        username = username,
        userProfileUrl = userProfileUrl,
        content = content,
        timestamp = timestamp,
        likes = likes.toMutableList()
    )
}

fun Reply.toEntityModel(): ReplyEntity {
    return ReplyEntity(
        id = id,
        commentId = commentId,
        userId = userId,
        username = username,
        userProfileUrl = userProfileUrl,
        content = content,
        timestamp = timestamp,
        likes = likes.toList()
    )
}