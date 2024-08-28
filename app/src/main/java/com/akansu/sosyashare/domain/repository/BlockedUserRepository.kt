package com.akansu.sosyashare.domain.repository

import com.akansu.sosyashare.domain.model.BlockedUser

interface BlockedUserRepository {
    suspend fun blockUser(blockedUser: BlockedUser)
    suspend fun unblockUser(blockerUserId: String, blockedUserId: String)
    suspend fun getBlockedUsersByUserId(userId: String): List<BlockedUser>
    suspend fun isUserBlocked(blockerUserId: String, blockedUserId: String): Boolean
    suspend fun getUsersWhoBlockedUserId(userId: String): List<BlockedUser>

}
