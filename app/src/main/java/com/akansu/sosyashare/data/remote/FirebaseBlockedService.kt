package com.akansu.sosyashare.data.remote

import com.akansu.sosyashare.data.model.BlockedUserEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseBlockedService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun blockUser(blockedUserEntity: BlockedUserEntity) {
        firestore.collection("blocked_users").add(blockedUserEntity).await()
    }

    suspend fun unblockUser(blockerUserId: String, blockedUserId: String) {
        val query = firestore.collection("blocked_users")
            .whereEqualTo("blockerUserId", blockerUserId)
            .whereEqualTo("blockedUserId", blockedUserId)
            .get()
            .await()

        query.documents.forEach { it.reference.delete().await() }
    }

    suspend fun getBlockedUsersByUserId(userId: String): List<BlockedUserEntity> {
        val query = firestore.collection("blocked_users")
            .whereEqualTo("blockerUserId", userId)
            .get()
            .await()

        return query.toObjects(BlockedUserEntity::class.java)
    }

    suspend fun isUserBlocked(blockerUserId: String, blockedUserId: String): Boolean {
        val query = firestore.collection("blocked_users")
            .whereEqualTo("blockerUserId", blockerUserId)
            .whereEqualTo("blockedUserId", blockedUserId)
            .get()
            .await()

        return !query.isEmpty
    }

    suspend fun getUsersWhoBlockedUserId(userId: String): List<BlockedUserEntity> {
        val query = firestore.collection("blocked_users")
            .whereEqualTo("blockedUserId", userId)
            .get()
            .await()

        return query.toObjects(BlockedUserEntity::class.java)
    }
}
