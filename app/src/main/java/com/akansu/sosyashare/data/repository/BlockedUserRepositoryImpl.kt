package com.akansu.sosyashare.data.repository

import com.akansu.sosyashare.data.mapper.BlockUserMapper
import com.akansu.sosyashare.data.remote.FirebaseBlockedService
import com.akansu.sosyashare.domain.model.BlockedUser
import com.akansu.sosyashare.domain.repository.BlockedUserRepository
import javax.inject.Inject

class BlockedUserRepositoryImpl @Inject constructor(
    private val firebaseBlockedService: FirebaseBlockedService
) : BlockedUserRepository {
    override suspend fun blockUser(blockedUser: BlockedUser) {
        val entity = BlockUserMapper.toEntityModel(blockedUser)
        firebaseBlockedService.blockUser(entity)
    }

    override suspend fun unblockUser(blockerUserId: String, blockedUserId: String) {
        firebaseBlockedService.unblockUser(blockerUserId, blockedUserId)
    }

    override suspend fun getBlockedUsersByUserId(userId: String): List<BlockedUser> {
        val entities = firebaseBlockedService.getBlockedUsersByUserId(userId)
        return entities.map { BlockUserMapper.toDomainModel(it) }
    }

    override suspend fun isUserBlocked(blockerUserId: String, blockedUserId: String): Boolean {
        return firebaseBlockedService.isUserBlocked(blockerUserId, blockedUserId)
    }

    override suspend fun getUsersWhoBlockedUserId(userId: String): List<BlockedUser> {
        val blockedUserEntities = firebaseBlockedService.getUsersWhoBlockedUserId(userId)
        return blockedUserEntities.map { BlockUserMapper.toDomainModel(it) }
    }
}
