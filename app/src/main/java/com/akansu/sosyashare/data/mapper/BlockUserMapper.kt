package com.akansu.sosyashare.data.mapper

import com.akansu.sosyashare.data.model.BlockedUserEntity
import com.akansu.sosyashare.domain.model.BlockedUser

object BlockUserMapper {
    fun toDomainModel(entity: BlockedUserEntity): BlockedUser {
        return BlockedUser(
            id = entity.id,
            blockerUserId = entity.blockerUserId,
            blockedUserId = entity.blockedUserId
        )
    }

    fun toEntityModel(domainModel: BlockedUser): BlockedUserEntity {
        return BlockedUserEntity(
            id = domainModel.id,
            blockerUserId = domainModel.blockerUserId,
            blockedUserId = domainModel.blockedUserId
        )
    }
}
