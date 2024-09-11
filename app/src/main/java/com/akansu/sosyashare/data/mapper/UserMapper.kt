package com.akansu.sosyashare.data.mapper

import com.akansu.sosyashare.data.model.UserEntity
import com.akansu.sosyashare.domain.model.User


object UserMapper {
    fun toDomainModel(entity: UserEntity): User {
        return User(
            id = entity.id,
            username = entity.username,
            email = entity.email,
            profilePictureUrl = entity.profilePictureUrl,
            backgroundImageUrl = entity.backgroundImageUrl,
            comments = entity.comments,
            following = entity.following,
            followers = entity.followers,
            bio = entity.bio,
            lastUsernameChange = entity.lastUsernameChange,
        )
    }
}

fun UserEntity.toDomainModel(): User {
    return User(
        id = id,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl,
        backgroundImageUrl = backgroundImageUrl,
        comments = comments,
        following = following,
        followers = followers,
        bio = bio,
        lastUsernameChange = lastUsernameChange,
    )
}

fun User.toEntityModel(): UserEntity {
    return UserEntity(
        id = id,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl,
        backgroundImageUrl = backgroundImageUrl,
        comments = comments,
        following = following,
        followers = followers,
        bio = bio,
        lastUsernameChange = lastUsernameChange,
    )
}